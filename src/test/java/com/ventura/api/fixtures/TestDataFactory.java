package com.ventura.api.fixtures;

import com.ventura.api.clients.AppointmentClient;
import com.ventura.api.clients.BusinessClient;
import com.ventura.api.clients.CustomerClient;
import com.ventura.api.clients.InvoiceClient;
import com.ventura.api.clients.OrderClient;
import com.ventura.api.clients.ResourceClient;
import com.ventura.api.models.request.CreateAppointmentDto;
import com.ventura.api.models.request.CreateBusinessDto;
import com.ventura.api.models.request.CreateCustomerDto;
import com.ventura.api.models.request.CreateInvoiceDto;
import com.ventura.api.models.request.CreateOrderDto;
import com.ventura.api.models.request.CreateOrderItemDto;
import com.ventura.api.models.request.CreateResourceDto;
import com.ventura.api.models.response.AppointmentResponse;
import com.ventura.api.models.response.BusinessResponse;
import com.ventura.api.models.response.CustomerResponse;
import com.ventura.api.models.response.InvoiceResponse;
import com.ventura.api.models.response.OrderResponse;
import com.ventura.api.models.response.ResourceResponse;
import com.ventura.api.utils.RandomDataUtils;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Creates prerequisite domain data through the API itself (never by talking to a
 * database directly), routed through the same {@code clients/} layer the test classes use,
 * so every test class can stand up exactly the fixtures it needs without depending on
 * another test class having run first.
 *
 * <p>All methods take the caller's authenticated {@link RequestSpecification} so fixtures
 * are created as that same user/business.
 */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    /** Returns the authenticated user's business, creating one first if they don't have one yet. */
    public static BusinessResponse ensureBusiness(RequestSpecification authSpec) {
        Response existing = BusinessClient.getMine(authSpec);

        if (existing.statusCode() == 200) {
            return existing.as(BusinessResponse.class);
        }

        return createBusiness(authSpec);
    }

    public static BusinessResponse createBusiness(RequestSpecification authSpec) {
        CreateBusinessDto body = CreateBusinessDto.builder()
                .name(RandomDataUtils.companyName() + " " + RandomDataUtils.uuidSuffix())
                .categories(List.of("Retail"))
                .build();

        return BusinessClient.create(authSpec, body)
                .then().statusCode(201)
                .extract().as(BusinessResponse.class);
    }

    public static CustomerResponse createCustomer(RequestSpecification authSpec) {
        CreateCustomerDto body = CreateCustomerDto.builder()
                .name(RandomDataUtils.fullName())
                .email(RandomDataUtils.uniqueEmail())
                .phone(RandomDataUtils.phoneNumber())
                .notes("Created by automation fixture")
                .build();

        return CustomerClient.create(authSpec, body)
                .then().statusCode(201)
                .extract().as(CustomerResponse.class);
    }

    public static ResourceResponse createResource(RequestSpecification authSpec, String type) {
        CreateResourceDto body = CreateResourceDto.builder()
                .type(type)
                .name("QA Resource " + RandomDataUtils.uuidSuffix())
                .price(9.99)
                .availableQuantity(100.0)
                .lowStockThreshold(5.0)
                .build();

        return ResourceClient.create(authSpec, body)
                .then().statusCode(201)
                .extract().as(ResourceResponse.class);
    }

    public static ResourceResponse createProduct(RequestSpecification authSpec) {
        return createResource(authSpec, "product");
    }

    public static AppointmentResponse createAppointment(RequestSpecification authSpec) {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("QA appointment " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .end(end.toString())
                .build();

        return AppointmentClient.create(authSpec, body)
                .then().statusCode(201)
                .extract().as(AppointmentResponse.class);
    }

    public static OrderResponse createOrder(RequestSpecification authSpec, String customerId, String resourceId) {
        CreateOrderDto body = CreateOrderDto.builder()
                .customerId(customerId)
                .items(List.of(CreateOrderItemDto.builder().resourceId(resourceId).quantity(1.0).build()))
                .build();

        return OrderClient.create(authSpec, body)
                .then().statusCode(201)
                .extract().as(OrderResponse.class);
    }

    /** Convenience: creates a customer + product + order in one call. */
    public static OrderResponse createOrderWithNewCustomerAndResource(RequestSpecification authSpec) {
        CustomerResponse customer = createCustomer(authSpec);
        ResourceResponse resource = createProduct(authSpec);
        return createOrder(authSpec, customer.getId(), resource.getId());
    }

    public static InvoiceResponse createInvoice(RequestSpecification authSpec, List<String> orderIds) {
        CreateInvoiceDto body = CreateInvoiceDto.builder()
                .orderIds(orderIds)
                .invoiceType("STANDARD")
                .build();

        return InvoiceClient.create(authSpec, body)
                .then().statusCode(201)
                .extract().as(InvoiceResponse.class);
    }

    /** Convenience: creates a fresh order and invoices it in one call. */
    public static InvoiceResponse createInvoiceFromNewOrder(RequestSpecification authSpec) {
        OrderResponse order = createOrderWithNewCustomerAndResource(authSpec);
        return createInvoice(authSpec, List.of(order.getId()));
    }
}
