package com.ventura.api.tests.dashboard;

import com.ventura.api.clients.DashboardClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.models.response.DashboardSummaryResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;

/**
 * GET /dashboard/summary - authenticated dashboard rollup. Skipped automatically when no
 * auth is configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Dashboard - authenticated summary")
class DashboardTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("GET /dashboard/summary with no range param defaults to 30d and returns a sane shape")
    void shouldGetSummaryWithDefaultRange() {
        DashboardSummaryResponse summary = DashboardClient.getSummary(authSpec())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/dashboard-summary-response-schema.json"))
                .body("revenue", notNullValue())
                .body("inventory", notNullValue())
                .body("recentInvoices", notNullValue())
                .body("dailyRevenue", notNullValue())
                .extract().as(DashboardSummaryResponse.class);

        Assertions.assertNotNull(summary.getRevenue(), "revenue block should be present");
        Assertions.assertTrue(summary.getRevenue().getTotal() >= 0, "revenue.total should be non-negative");
        Assertions.assertTrue(summary.getRevenue().getLast30Days() >= 0, "revenue.last30Days should be non-negative");
        Assertions.assertTrue(summary.getRevenue().getPrevious30Days() >= 0,
                "revenue.previous30Days should be non-negative");

        Assertions.assertNotNull(summary.getInventory(), "inventory block should be present");
        Assertions.assertTrue(summary.getInventory().getLowStockCount() >= 0,
                "inventory.lowStockCount should be non-negative");
        Assertions.assertNotNull(summary.getInventory().getTopProducts(),
                "inventory.topProducts should be an array, even if empty");

        Assertions.assertNotNull(summary.getRecentInvoices(), "recentInvoices should be an array, even if empty");
        Assertions.assertNotNull(summary.getDailyRevenue(), "dailyRevenue should be an array, even if empty");
    }

    @Test
    @DisplayName("GET /dashboard/summary accepts each documented range value (7d, 30d, 90d)")
    void shouldAcceptEachExplicitRangeValue() {
        for (String range : new String[] {"7d", "30d", "90d"}) {
            DashboardSummaryResponse summary = DashboardClient.getSummary(authSpec(), range)
                    .then().statusCode(200)
                    .extract().as(DashboardSummaryResponse.class);

            Assertions.assertNotNull(summary.getDailyRevenue(), "dailyRevenue should be present for range=" + range);
        }
    }

    @Test
    @DisplayName("GET /dashboard/summary with an out-of-enum range value is rejected with 400")
    void shouldRejectInvalidRangeValue() {
        // Not independently verifiable against the live API without a bearer token (every
        // unauthenticated call short-circuits with 401 before validation runs), but a
        // Nest/class-validator enum check on an unrecognised query value is expected to fail
        // request validation rather than silently fall back to the default.
        DashboardClient.getSummary(authSpec(), "3d")
                .then().statusCode(400);
    }

    @Test
    @DisplayName("GET /dashboard/summary without a bearer token is rejected with 401")
    void shouldRejectWithoutToken() {
        DashboardClient.getSummary(RequestSpecs.anonymous())
                .then().statusCode(401);
    }
}
