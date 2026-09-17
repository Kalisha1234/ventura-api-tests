package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateOrderDto;
import com.ventura.api.models.request.UpdateOrderDto;
import com.ventura.api.models.request.UpdateOrderStatusDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/** Thin wrapper around every {@code /orders/*} call - see {@link AuthClient} for the rationale. */
public final class OrderClient {

    private OrderClient() {
    }

    public static Response create(RequestSpecification spec, CreateOrderDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.ORDERS);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.ORDER_BY_ID, id);
    }

    public static Response update(RequestSpecification spec, String id, UpdateOrderDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.ORDER_BY_ID, id);
    }

    public static Response updateStatus(RequestSpecification spec, String id, UpdateOrderStatusDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.ORDER_STATUS, id);
    }

    /** GET /orders with any combination of page/limit/status/customerId/q query params. */
    public static Response list(RequestSpecification spec, Map<String, Object> queryParams) {
        return RestAssured.given().spec(spec).queryParams(queryParams).when().get(Endpoints.ORDERS);
    }

    public static Response list(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.ORDERS);
    }
}
