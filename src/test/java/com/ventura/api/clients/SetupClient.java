package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /setup/*} call - see {@link AuthClient} for the rationale. */
public final class SetupClient {

    private SetupClient() {
    }

    /** GET /setup/status. */
    public static Response getStatus(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.SETUP_STATUS);
    }
}
