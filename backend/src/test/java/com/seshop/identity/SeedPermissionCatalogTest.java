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
            "role.update",
            "role.delete",
            "role.permission.assign",
            "staff.role.assign",
            "staff.user.read",
            "staff.user.create",
            "staff.user.update",
            "staff.user.delete",
            "staff.location.assign",
            "location.scope.all",
            "audit.read",
            "catalog.write",
            "inventory.adjust",
            "inventory.adjust.override",
            "inventory.transfer",
            "order.read",
            "order.ship",
            "refund.process",
            "promo.manage",
            "pos.sell",
            "pos.shift.manage",
            "invoice.manage",
            "social.compose",
            "social.connect",
            "customer.read",
            "customer.write",
            "report.read",
            "review.moderate",
            "inventory.cycle_count"
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
                "role.create",
                "role.update",
                "role.delete",
                "role.permission.assign",
                "staff.role.assign",
                "staff.user.read",
                "staff.user.create",
                "staff.user.update",
                "staff.user.delete",
                "catalog.write",
                "inventory.adjust",
                "inventory.adjust.override",
                "inventory.transfer",
                "order.read",
                "order.ship",
                "refund.process",
                "promo.manage",
                "pos.sell",
                "pos.shift.manage",
                "invoice.manage",
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

    @Test
    void enhancedDemoSeedRefreshesManagedRolePermissionAssignments() throws IOException {
        String seed = readResource("db/migration/V12__enhance_demo_seed_catalog.sql");

        assertThat(seed).contains("DELETE FROM role_permissions");
        assertThat(seed).contains("SELECT 'SUPER_ADMIN', p.code");
        assertThat(seed).contains(
                "('INVENTORY_MANAGER', 'Owns multi-location stock, transfers, receiving, adjustments, and cycle counts.', 'ACTIVE')",
                "('CASHIER', 'Runs point-of-sale receipts and shift operations at assigned stores.', 'ACTIVE')",
                "('FULFILLMENT_STAFF', 'Picks, packs, ships, and transfers customer orders across assigned locations.', 'ACTIVE')",
                "('MARKETING_MANAGER', 'Manages promotions, catalog presentation, reviews, and social commerce drafts.', 'ACTIVE')",
                "('CUSTOMER_SUPPORT', 'Handles customer profiles, order questions, reviews, returns, and refunds.', 'ACTIVE')",
                "('FINANCE_MANAGER', 'Manages tax invoices, refund review, audit checks, and reporting.', 'ACTIVE')",
                "('INVENTORY_AUDITOR', 'Runs stock counts and reads inventory reports without override privileges.', 'ACTIVE')",
                "('LOCATION_SUPERVISOR', 'Assigns staff to locations and monitors inventory coverage across sites.', 'ACTIVE')");

        assertThat(extractRolePermissionCodes(seed, "STORE_MANAGER")).containsExactly(
                "role.create",
                "role.update",
                "role.delete",
                "role.permission.assign",
                "staff.role.assign",
                "staff.user.read",
                "staff.user.create",
                "staff.user.update",
                "staff.user.delete",
                "staff.location.assign",
                "location.scope.all",
                "audit.read",
                "catalog.write",
                "inventory.adjust",
                "inventory.adjust.override",
                "inventory.transfer",
                "inventory.cycle_count",
                "order.read",
                "order.ship",
                "refund.process",
                "promo.manage",
                "pos.sell",
                "pos.shift.manage",
                "invoice.manage",
                "social.compose",
                "social.connect",
                "customer.read",
                "customer.write",
                "report.read",
                "review.moderate"
        );
        assertThat(extractRolePermissionCodes(seed, "STAFF")).containsExactly(
                "catalog.write",
                "inventory.adjust",
                "inventory.transfer",
                "inventory.cycle_count",
                "order.read",
                "order.ship",
                "refund.process",
                "pos.sell",
                "pos.shift.manage"
        );
        assertThat(extractRolePermissionCodes(seed, "CUSTOMER")).isEmpty();
    }

    @Test
    void enhancedDemoSeedAddsAccessoryCatalogAndLocations() throws IOException {
        String seed = readResource("db/migration/V12__enhance_demo_seed_catalog.sql");

        assertThat(seed).contains(
                "ACC-BELT-001",
                "ACC-BAG-001",
                "ACC-SCARF-001",
                "ACC-CAP-001",
                "ACC-WATCH-001",
                "ACC-WALLET-001",
                "ACC-TOTE-001",
                "ACC-NECKLACE-001");
        assertThat(seed).contains(
                "https://commons.wikimedia.org/wiki/Special:FilePath/Leather_belt.jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/Kate_Spade_handbag.jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/Scarf,_late_19th_century_(CH_18466163).jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/Baseball_cap_(15930546668).jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/Fossil_wristwatch_with_white_background.jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/A_men%27s_wallet.jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/QWSTION-FLAP-TOTE-MEDIUM-ALL-BLACK-FRONT.jpg?width=1200",
                "https://commons.wikimedia.org/wiki/Special:FilePath/Necklace_-_Meta_Overbeck_(27770427469).jpg?width=1200");
        assertThat(seed).contains(
                "('ONLINE-HN', 'Online Fulfillment - Hanoi', 'STORAGE', 'ACTIVE')",
                "('STORE-D3', 'District 3 Curated Accessories Studio', 'STORE', 'ACTIVE')",
                "('STORE-THAO-DIEN', 'Thao Dien Designer Vintage Store', 'STORE', 'ACTIVE')",
                "('STORE-HN-OLD', 'Hanoi Old Quarter Vintage Store', 'STORE', 'ACTIVE')",
                "('STORAGE-BT', 'Binh Thanh Returns and Repair Hub', 'STORAGE', 'ACTIVE')",
                "('STORAGE-DN', 'Da Nang Regional Stockroom', 'STORAGE', 'ACTIVE')");
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

    private List<String> extractRolePermissionCodes(String seed, String roleName) {
        return Pattern.compile("\\('" + Pattern.quote(roleName) + "'\\s*,\\s*'([a-z]+(?:\\.[a-z_]+)+)'\\)")
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
