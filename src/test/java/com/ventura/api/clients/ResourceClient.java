package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateResourceDto;
import com.ventura.api.models.request.UpdateResourceDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/** Thin wrapper around every {@code /resources/*} call - see {@link AuthClient} for the rationale. */
public final class ResourceClient {

    private ResourceClient() {
    }

    public static Response create(RequestSpecification spec, CreateResourceDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.RESOURCES);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.RESOURCE_BY_ID, id);
    }

    public static Response update(RequestSpecification spec, String id, UpdateResourceDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.RESOURCE_BY_ID, id);
    }

    public static Response delete(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().delete(Endpoints.RESOURCE_BY_ID, id);
    }

    /**
     * GET /resources - which query params apply (page/limit/q/type) varies per test, so callers
     * pass exactly the ones they need rather than every method having its own combination.
     */
    public static Response list(RequestSpecification spec, Map<String, Object> queryParams) {
        return RestAssured.given().spec(spec).queryParams(queryParams).when().get(Endpoints.RESOURCES);
    }
}
