package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateBusinessDto;
import com.ventura.api.models.request.UpdateBusinessDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /businesses/*} call - see {@link AuthClient} for the rationale. */
public final class BusinessClient {

    private BusinessClient() {
    }

    public static Response create(RequestSpecification spec, CreateBusinessDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.BUSINESSES);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.BUSINESS_BY_ID, id);
    }

    public static Response update(RequestSpecification spec, String id, UpdateBusinessDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.BUSINESS_BY_ID, id);
    }

    public static Response getCategories(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.BUSINESS_CATEGORIES);
    }

    public static Response getMine(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.BUSINESS_MINE);
    }
}
