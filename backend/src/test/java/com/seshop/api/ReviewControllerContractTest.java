package com.seshop.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.seshop.review.api.ReviewController;
import com.seshop.review.api.dto.ReviewDto;
import com.seshop.review.application.ReviewService;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.exception.GlobalExceptionHandler;
import com.seshop.shared.security.AuthenticatedUser;
import com.seshop.shared.security.JwtAuthenticationFilter;
import com.seshop.shared.security.JwtTokenProvider;
import com.seshop.shared.security.RestAccessDeniedHandler;
import com.seshop.shared.security.RestAuthenticationEntryPoint;
import com.seshop.shared.security.SecurityConfig;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * UC18: Leave review with image – WebMvc contract for /api/v1/reviews.
 */
@WebMvcTest(controllers = ReviewController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = "seshop.cors.allowed-origins=http://localhost:5173")
class ReviewControllerContractTest {

    private static final String CUSTOMER_TOKEN = "customer-token";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUpJwt() {
        List<String> permissions = List.of();
        AuthenticatedUser principal = new AuthenticatedUser(42L, "demo.customer", "CUSTOMER", permissions);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, CUSTOMER_TOKEN, List.of());
        given(jwtTokenProvider.validate(CUSTOMER_TOKEN)).willReturn(true);
        given(jwtTokenProvider.authentication(CUSTOMER_TOKEN)).willReturn(auth);
    }

    @Test
    void createReviewReturnsCreatedDto() throws Exception {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(401L);
        dto.setProductId(1L);
        dto.setOrderItemId(5001L);
        dto.setRating(5);
        dto.setComment("Authentic vintage feel!");
        dto.setCreatedAt(OffsetDateTime.now());
        given(reviewService.createReview(anyLong(), any())).willReturn(dto);

        mockMvc.perform(post("/api/v1/reviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + CUSTOMER_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderItemId": 5001,
                                  "rating": 5,
                                  "comment": "Authentic vintage feel!",
                                  "imageUrl": "https://cdn.example.com/review.jpg"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reviewId").value(401))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    void createReviewReturnsBadRequestForInvalidRating() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + CUSTOMER_TOKEN)
                        .header(TraceIdFilter.TRACE_HEADER, "trace-bad-review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderItemId": 5001,
                                  "rating": 6,
                                  "comment": "Good"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("GEN_001"));
    }

    @Test
    void listReviewsByProductReturnsArray() throws Exception {
        ReviewDto dto = new ReviewDto();
        dto.setReviewId(401L);
        dto.setRating(5);
        given(reviewService.getReviewsByProduct(1L)).willReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/reviews/product/1")
                        .header(TraceIdFilter.TRACE_HEADER, "trace-list-reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].reviewId").value(401));
    }

    @Test
    void createReviewRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderItemId":1,"rating":5,"comment":"Great"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
