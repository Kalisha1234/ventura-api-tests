package com.ventura.api.tests.orders;

import com.ventura.api.clients.OrderClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateOrderDto;
import com.ventura.api.models.request.CreateOrderItemDto;
import com.ventura.api.models.request.UpdateOrderDto;
import com.ventura.api.models.request.UpdateOrderStatusDto;
import com.ventura.api.models.response.CustomerResponse;
import com.ventura.api.models.response.OrderResponse;
import com.ventura.api.models.response.ResourceResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Orders module: creation, retrieval, item/status updates, listing/filtering and the
 * validation/negative paths around them. Skipped automatically when no auth is
 * configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Orders")
class OrderTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("POST /orders creates an order with a computed totalAmount, pending status, and null invoiceId")
    void shouldCreateOrderWithComputedTotal() {
        CustomerResponse customer = TestDataFactory.createCustomer(authSpec());
        ResourceResponse resource = TestDataFactory.createProduct(authSpec());

        CreateOrderDto body = CreateOrderDto.builder()
                .customerId(customer.getId())
                .items(List.of(CreateOrderItemDto.builder().resourceId(resource.getId()).quantity(2.0).build()))
                .build();

        OrderResponse created = OrderClient.create(authSpec(), body)
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/order-response-schema.json"))
                .body("customerId", equalTo(customer.getId()))
                .body("status", equalTo("pending"))
                .body("invoiceId", nullValue())
                .extract().as(OrderResponse.class);

        Assertions.assertEquals(19.98, created.getTotalAmount(), 0.001,
                "2 x a 9.99-priced product should total 19.98");
    }

    @Test
    @DisplayName("GET /orders/{id} returns the created order")
    void shouldGetOrderById() {
        OrderResponse order = TestDataFactory.createOrderWithNewCustomerAndResource(authSpec());

        OrderClient.getById(authSpec(), order.getId())
                .then().statusCode(200)
                .body("id", equalTo(order.getId()))
                .body("customerId", equalTo(order.getCustomerId()));
    }

    @Test
    @DisplayName("GET /orders/{id} for a well-formed but non-existent id returns 404")
    void shouldReturn404ForUnknownOrderId() {
        OrderClient.getById(authSpec(), TestDataProvider.notFoundId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("PATCH /orders/{id} on a pending order updates its items and recalculates totalAmount")
    void shouldUpdateItemsOnPendingOrder() {
        CustomerResponse customer = TestDataFactory.createCustomer(authSpec());
        ResourceResponse resource = TestDataFactory.createProduct(authSpec());
        OrderResponse order = TestDataFactory.createOrder(authSpec(), customer.getId(), resource.getId());

        UpdateOrderDto body = UpdateOrderDto.builder()
                .items(List.of(CreateOrderItemDto.builder().resourceId(resource.getId()).quantity(3.0).build()))
                .build();

        OrderResponse updated = OrderClient.update(authSpec(), order.getId(), body)
                .then().statusCode(200)
                .extract().as(OrderResponse.class);

        Assertions.assertEquals(29.97, updated.getTotalAmount(), 0.001,
                "3 x a 9.99-priced product should total 29.97 after the items update");
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status transitions pending -> completed, and items can no longer be edited")
    void shouldTransitionStatusAndThenRejectFurtherItemEdits() {
        OrderResponse order = TestDataFactory.createOrderWithNewCustomerAndResource(authSpec());

        OrderClient.updateStatus(authSpec(), order.getId(), UpdateOrderStatusDto.builder().status("completed").build())
                .then().statusCode(200)
                .body("status", equalTo("completed"));

        UpdateOrderDto editAttempt = UpdateOrderDto.builder()
                .items(List.of(CreateOrderItemDto.builder()
                        .resourceId(order.getItems().get(0).getResourceId())
                        .quantity(5.0)
                        .build()))
                .build();

        int status = OrderClient.update(authSpec(), order.getId(), editAttempt)
                .statusCode();

        Assertions.assertTrue(status == 400 || status == 409,
                "Editing items of a completed order should be rejected (400/409), but got " + status);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/status with an invalid status value is rejected with 400")
    void shouldRejectInvalidStatusValue() {
        OrderResponse order = TestDataFactory.createOrderWithNewCustomerAndResource(authSpec());

        OrderClient.updateStatus(authSpec(), order.getId(),
                        UpdateOrderStatusDto.builder().status("not-a-real-status").build())
                .then().statusCode(400);
    }

    @Test
    @DisplayName("GET /orders supports pagination plus status and customerId filters")
    void shouldListOrdersWithPaginationAndFilters() {
        OrderResponse order = TestDataFactory.createOrderWithNewCustomerAndResource(authSpec());

        OrderClient.list(authSpec(), Map.of("page", 1, "limit", 5))
                .then().statusCode(200)
                .body("meta.page", equalTo(1))
                .body("meta.limit", equalTo(5))
                .body("meta.total", greaterThanOrEqualTo(0));

        OrderClient.list(authSpec(), Map.of("status", "pending"))
                .then().statusCode(200)
                .body("data.status", everyItem(equalTo("pending")));

        OrderClient.list(authSpec(), Map.of("customerId", order.getCustomerId()))
                .then().statusCode(200)
                .body("data.customerId", everyItem(equalTo(order.getCustomerId())))
                .body("data.id", org.hamcrest.Matchers.hasItem(order.getId()));
    }

    @Test
    @DisplayName("POST /orders with an empty items array is rejected with a validation error")
    void shouldRejectCreateWithEmptyItems() {
        CustomerResponse customer = TestDataFactory.createCustomer(authSpec());

        CreateOrderDto body = CreateOrderDto.builder()
                .customerId(customer.getId())
                .items(List.of())
                .build();

        OrderClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /orders without a customerId is rejected with a validation error")
    void shouldRejectCreateWithoutCustomerId() {
        ResourceResponse resource = TestDataFactory.createProduct(authSpec());

        CreateOrderDto body = CreateOrderDto.builder()
                .items(List.of(CreateOrderItemDto.builder().resourceId(resource.getId()).quantity(1.0).build()))
                .build();

        OrderClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /orders without a bearer token is rejected with 401")
    void shouldRejectCreateWithoutAuth() {
        CreateOrderDto body = CreateOrderDto.builder()
                .customerId(TestDataProvider.notFoundId())
                .items(List.of(CreateOrderItemDto.builder().resourceId(TestDataProvider.notFoundId()).quantity(1.0).build()))
                .build();

        OrderClient.create(RequestSpecs.anonymous(), body)
                .then().statusCode(401)
                .body("message", equalTo("Unauthorized"));
    }

    @Test
    @DisplayName("GET /orders without a bearer token is rejected with 401")
    void shouldRejectListWithoutAuth() {
        OrderClient.list(RequestSpecs.anonymous())
                .then().statusCode(401)
                .body("statusCode", equalTo(401));
    }
}
