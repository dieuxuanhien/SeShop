package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.catalog.api.PublicCatalogController;
import com.seshop.catalog.api.StaffCatalogController;
import com.seshop.catalog.api.dto.CategoryDto;
import com.seshop.catalog.api.dto.ProductDto;
import com.seshop.catalog.application.CatalogService;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.PermissionValidator;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC5: Add product (staff), UC13: Browse and filter variants (public).
 */
@WebMvcTest(controllers = {PublicCatalogController.class, StaffCatalogController.class})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class,
        PermissionValidator.class
})
@TestPropertySource(properties = "seshop.cors.allowed-origins=http://localhost:5173")
class CatalogControllerContractTest {

    private static final String STAFF_TOKEN = "staff-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpJwt() {
        List<String> permissions = List.of("catalog.write");
        AuthenticatedUser principal = new AuthenticatedUser(10L, "staff.manager", "STAFF", permissions);
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new).toList();
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, STAFF_TOKEN, authorities);
        given(jwtTokenProvider.validate(STAFF_TOKEN)).willReturn(true);
        given(jwtTokenProvider.authentication(STAFF_TOKEN)).willReturn(auth);
    }

    @Test
    void browseProductsReturnsPagedItems() throws Exception {
        ProductDto product = new ProductDto();
        product.setId(1L);
        product.setName("Vintage Tee");
        product.setStatus("PUBLISHED");

        given(catalogService.getPublishedProducts(isNull(), isNull(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(product)));

        mockMvc.perform(get("/api/v1/products")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].name").value("Vintage Tee"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void getProductDetailReturnsProduct() throws Exception {
        ProductDto product = new ProductDto();
        product.setId(5L);
        product.setName("Linen Shirt");
        product.setStatus("PUBLISHED");

        given(catalogService.getProductById(5L)).willReturn(product);

        mockMvc.perform(get("/api/v1/products/5")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-detail"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5))
                .andExpect(jsonPath("$.data.name").value("Linen Shirt"));
    }

    @Test
    void getCategoriesReturnsList() throws Exception {
        CategoryDto cat = new CategoryDto();
        cat.setId(3L);
        cat.setName("Band Tees");

        given(catalogService.getCategories()).willReturn(List.of(cat));

        mockMvc.perform(get("/api/v1/categories")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-cats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Band Tees"));
    }

    @Test
    void createProductRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/staff/products")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-no-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New","status":"DRAFT"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createProductRejectsAuthenticatedUserWithoutCatalogWritePermission() throws Exception {
        String token = "read-only-token";
        List<String> permissions = List.of("order.read");
        AuthenticatedUser principal = new AuthenticatedUser(11L, "staff.viewer", "STAFF", permissions);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                token,
                permissions.stream().map(SimpleGrantedAuthority::new).toList()
        );
        given(jwtTokenProvider.validate(token)).willReturn(true);
        given(jwtTokenProvider.authentication(token)).willReturn(auth);

        mockMvc.perform(post("/api/v1/staff/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-catalog-forbidden")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New","status":"DRAFT"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_002"));
    }

    @Test
    void createProductWithValidAuthReturnsCreated() throws Exception {
        ProductDto created = new ProductDto();
        created.setId(20L);
        created.setName("New Shirt");
        created.setStatus("DRAFT");

        given(catalogService.createProduct(any())).willReturn(created);

        mockMvc.perform(post("/api/v1/staff/products")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + STAFF_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-create-product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "New Shirt",
                                  "brand": "SeShop",
                                  "description": "Test product",
                                  "status": "DRAFT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(20))
                .andExpect(jsonPath("$.data.name").value("New Shirt"));
    }
}
