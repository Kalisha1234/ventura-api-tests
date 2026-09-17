package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /dashboard/*} call - see {@link AuthClient} for the rationale. */
public final class DashboardClient {

    private DashboardClient() {
    }

    /** GET /dashboard/summary with no {@code range} param - the API defaults it to 30d. */
    public static Response getSummary(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.DASHBOARD_SUMMARY);
    }

    /** GET /dashboard/summary?range={range}. */
    public static Response getSummary(RequestSpecification spec, String range) {
        return RestAssured.given().spec(spec).queryParam("range", range).when().get(Endpoints.DASHBOARD_SUMMARY);
    }
}
