package com.ventura.api.tests.setup;

import com.ventura.api.clients.SetupClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.models.response.SetupStatusResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * GET /setup/status - authenticated onboarding-progress flags. Skipped automatically when
 * no auth is configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Setup - authenticated onboarding status")
class SetupTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("GET /setup/status returns all boolean flags, with complete implying every has* flag is true")
    void shouldReturnInternallyConsistentStatus() {
        SetupStatusResponse status = SetupClient.getStatus(authSpec())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/setup-status-response-schema.json"))
                .extract().as(SetupStatusResponse.class);

        // The bootstrap account's existing state (how much of setup it has already done) is
        // unknown to this suite, so we don't assert exact has* values - only that the API's
        // own "complete" summary flag is logically consistent with the individual flags it
        // is presumably derived from: complete=true must imply every has* flag is true.
        if (status.isComplete()) {
            Assertions.assertTrue(status.isHasBusiness(), "complete=true but hasBusiness=false");
            Assertions.assertTrue(status.isHasCustomers(), "complete=true but hasCustomers=false");
            Assertions.assertTrue(status.isHasResources(), "complete=true but hasResources=false");
            Assertions.assertTrue(status.isHasOrders(), "complete=true but hasOrders=false");
            Assertions.assertTrue(status.isHasInvoices(), "complete=true but hasInvoices=false");
            Assertions.assertTrue(status.isHasAppointments(), "complete=true but hasAppointments=false");
        }
    }

    @Test
    @DisplayName("GET /setup/status without a bearer token is rejected with 401")
    void shouldRejectWithoutToken() {
        SetupClient.getStatus(RequestSpecs.anonymous())
                .then().statusCode(401);
    }
}
