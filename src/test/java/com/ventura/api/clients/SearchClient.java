package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /search} call - see {@link AuthClient} for the rationale. */
public final class SearchClient {

    private SearchClient() {
    }

    /** GET /search?q={q}. */
    public static Response search(RequestSpecification spec, String q) {
        return RestAssured.given().spec(spec).queryParam("q", q).when().get(Endpoints.SEARCH);
    }

    /** Overload for negative tests that intentionally omit the required {@code q} param. */
    public static Response search(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.SEARCH);
    }
}
