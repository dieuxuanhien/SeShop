package com.seshop.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class SeedPermissionCatalogTest {

    private static final List<String> EXPECTED_PERMISSION_CODES = List.of(
            "role.create",
            "role.permission.assign",
            "staff.role.assign",
            "audit.read",
            "catalog.write",
            "inventory.adjust",
            "inventory.transfer",
            "order.read",
            "order.ship",
            "refund.process",
            "promo.manage",
            "social.compose",
            "social.connect",
            "customer.read",
            "customer.write",
            "report.read"
    );

    @Test
    void permissionCatalogSeedMatchesTheDocumentedAuthorizationModel() throws IOException {
        String seed = readResource("db/migration/V2__seed_permission_catalog.sql");

        assertThat(EXPECTED_PERMISSION_CODES)
                .allSatisfy(code -> assertThat(seed).contains("'" + code + "'"));
        assertThat(extractPermissionCodes(seed)).containsExactlyElementsOf(EXPECTED_PERMISSION_CODES);
    }

    @Test
    void demoSeedUsesOnlyCatalogPermissionsAndTheExpectedRoleAssignments() throws IOException {
        String seed = readResource("db/migration/V3__seed_demo_database.sql");

        assertThat(seed).contains("('super.admin', 'SUPER_ADMIN', 'super.admin')");
        assertThat(seed).contains("('staff.manager', 'STORE_MANAGER', 'super.admin')");
        assertThat(seed).contains("('demo.customer', 'CUSTOMER', 'super.admin')");

        Matcher matcher = Pattern.compile("JOIN permissions p ON p.code IN \\((.*?)\\)", Pattern.DOTALL)
                .matcher(seed);
        assertThat(matcher.find()).isTrue();
        assertThat(splitSqlStringList(matcher.group(1))).containsExactly(
                "catalog.write",
                "inventory.adjust",
                "inventory.transfer",
                "order.read",
                "order.ship",
                "refund.process",
                "promo.manage",
                "social.compose",
                "social.connect",
                "customer.read",
                "report.read"
        );
        assertThat(matcher.find()).isTrue();
        assertThat(splitSqlStringList(matcher.group(1))).containsExactly(
                "inventory.adjust",
                "inventory.transfer",
                "order.read"
        );
    }

    private String readResource(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            assertThat(inputStream).as("Resource %s should exist", resourcePath).isNotNull();
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private List<String> extractPermissionCodes(String seed) {
        return Pattern.compile("\\('([^']+)'\\s*,\\s*'[^']*'\\)")
                .matcher(seed)
                .results()
                .map(matchResult -> matchResult.group(1))
                .toList();
    }

    private List<String> splitSqlStringList(String rawList) {
        return Pattern.compile("'([^']+)'")
                .matcher(rawList)
                .results()
                .map(matchResult -> matchResult.group(1))
                .toList();
    }
}
