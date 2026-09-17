package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateCustomerDto;
import com.ventura.api.models.request.ImportCustomersDto;
import com.ventura.api.models.request.UpdateCustomerDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /customers*} call - see {@link AuthClient} for the rationale. */
public final class CustomerClient {

    private CustomerClient() {
    }

    public static Response create(RequestSpecification spec, CreateCustomerDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.CUSTOMERS);
    }

    /** GET /customers with no query params. */
    public static Response list(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.CUSTOMERS);
    }

    /** GET /customers?page=&limit= */
    public static Response list(RequestSpecification spec, int page, int limit) {
        return RestAssured.given().spec(spec)
                .queryParam("page", page)
                .queryParam("limit", limit)
                .when().get(Endpoints.CUSTOMERS);
    }

    /** GET /customers?q= */
    public static Response search(RequestSpecification spec, String query) {
        return RestAssured.given().spec(spec)
                .queryParam("q", query)
                .when().get(Endpoints.CUSTOMERS);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.CUSTOMER_BY_ID, id);
    }

    public static Response update(RequestSpecification spec, String id, UpdateCustomerDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.CUSTOMER_BY_ID, id);
    }

    public static Response delete(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().delete(Endpoints.CUSTOMER_BY_ID, id);
    }

    public static Response importCustomers(RequestSpecification spec, ImportCustomersDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.CUSTOMERS_IMPORT);
    }
}
