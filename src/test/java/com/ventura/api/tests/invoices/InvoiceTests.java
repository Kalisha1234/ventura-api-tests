package com.ventura.api.tests.invoices;

import com.ventura.api.clients.InvoiceClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateInvoiceDto;
import com.ventura.api.models.request.RecordPaymentDto;
import com.ventura.api.models.request.SendInvoiceDto;
import com.ventura.api.models.request.UpdateInvoiceStatusDto;
import com.ventura.api.models.response.InvoiceResponse;
import com.ventura.api.models.response.OrderResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;

/**
 * Invoices module: creation from orders, retrieval, payments, sending, status updates,
 * listing/filtering and the validation/negative paths around them. Skipped automatically
 * when no auth is configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Invoices")
class InvoiceTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("POST /invoices creates a DRAFT invoice from an order with internally consistent tax math")
    void shouldCreateInvoiceFromOrderWithConsistentTaxMath() {
        OrderResponse order = TestDataFactory.createOrderWithNewCustomerAndResource(authSpec());

        InvoiceResponse invoice = InvoiceClient.create(authSpec(),
                        CreateInvoiceDto.builder().orderIds(List.of(order.getId())).invoiceType("STANDARD").build())
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/invoice-response-schema.json"))
                .body("status", equalTo("DRAFT"))
                .body("orderIds", hasItem(order.getId()))
                .extract().as(InvoiceResponse.class);

        double expectedTotalTax = invoice.getVatAmount() + invoice.getNhilAmount() + invoice.getGetfundAmount();
        Assertions.assertEquals(expectedTotalTax, invoice.getTotalTax(), 0.01,
                "totalTax should equal the sum of vat + nhil + getfund amounts");

        double expectedTotalAmount = invoice.getSubtotal() + invoice.getTotalTax();
        Assertions.assertEquals(expectedTotalAmount, invoice.getTotalAmount(), 0.01,
                "totalAmount should equal subtotal + totalTax");
    }

    @Test
    @DisplayName("GET /invoices/{id} returns the created invoice")
    void shouldGetInvoiceById() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());

        InvoiceClient.getById(authSpec(), invoice.getId())
                .then().statusCode(200)
                .body("id", equalTo(invoice.getId()));
    }

    @Test
    @DisplayName("GET /invoices/{id} for a well-formed but non-existent id returns 404")
    void shouldReturn404ForUnknownInvoiceId() {
        InvoiceClient.getById(authSpec(), TestDataProvider.notFoundId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("PATCH /invoices/{id}/payment records a partial payment and updates amountPaid")
    void shouldRecordPartialPayment() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());
        double partialAmount = invoice.getTotalAmount() / 2;

        InvoiceResponse updated = InvoiceClient.recordPayment(authSpec(), invoice.getId(),
                        RecordPaymentDto.builder().amount(partialAmount).paymentMethod("CASH").build())
                .then().statusCode(200)
                .extract().as(InvoiceResponse.class);

        Assertions.assertEquals(partialAmount, updated.getAmountPaid(), 0.01,
                "amountPaid should reflect the partial payment just recorded");
        Assertions.assertEquals("PARTIALLY_PAID", updated.getStatus(),
                "a payment less than totalAmount should move the invoice to PARTIALLY_PAID");
    }

    @Test
    @DisplayName("PATCH /invoices/{id}/payment records a full payment and moves the invoice to PAID")
    void shouldRecordFullPayment() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());

        InvoiceResponse updated = InvoiceClient.recordPayment(authSpec(), invoice.getId(),
                        RecordPaymentDto.builder().amount(invoice.getTotalAmount()).paymentMethod("MOBILE_MONEY").build())
                .then().statusCode(200)
                .extract().as(InvoiceResponse.class);

        Assertions.assertEquals(invoice.getTotalAmount(), updated.getAmountPaid(), 0.01,
                "amountPaid should equal totalAmount after paying in full");
        Assertions.assertEquals("PAID", updated.getStatus(),
                "paying the full totalAmount should move the invoice to PAID");
    }

    @Test
    @DisplayName("PATCH /invoices/{id}/payment rejects an overpayment beyond totalAmount")
    void shouldRejectOverpayment() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());
        double overpayAmount = invoice.getTotalAmount() + 1000.0;

        int status = InvoiceClient.recordPayment(authSpec(), invoice.getId(),
                        RecordPaymentDto.builder().amount(overpayAmount).paymentMethod("CARD").build())
                .statusCode();

        Assertions.assertEquals(400, status,
                "Recording a payment greater than totalAmount should be rejected with 400, but got " + status);
    }

    @Test
    @DisplayName("POST /invoices/{id}/send sends a draft invoice")
    void shouldSendInvoice() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());

        InvoiceClient.send(authSpec(), invoice.getId(), SendInvoiceDto.builder().build())
                .then().statusCode(200)
                .body("status", equalTo("SENT"));
    }

    @Test
    @DisplayName("PATCH /invoices/{id}/status updates the status directly")
    void shouldUpdateInvoiceStatusDirectly() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());

        InvoiceClient.updateStatus(authSpec(), invoice.getId(), UpdateInvoiceStatusDto.builder().status("CANCELLED").build())
                .then().statusCode(200)
                .body("status", equalTo("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /invoices/{id}/status with an invalid status value is rejected with 400")
    void shouldRejectInvalidInvoiceStatusValue() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());

        InvoiceClient.updateStatus(authSpec(), invoice.getId(),
                        UpdateInvoiceStatusDto.builder().status("NOT_A_REAL_STATUS").build())
                .then().statusCode(400);
    }

    @Test
    @DisplayName("GET /invoices supports pagination plus status and customerId filters")
    void shouldListInvoicesWithPaginationAndFilters() {
        InvoiceResponse invoice = TestDataFactory.createInvoiceFromNewOrder(authSpec());

        InvoiceClient.list(authSpec(), Map.of("page", 1, "limit", 5))
                .then().statusCode(200)
                .body("meta.page", equalTo(1))
                .body("meta.limit", equalTo(5))
                .body("meta.total", greaterThanOrEqualTo(0));

        InvoiceClient.list(authSpec(), Map.of("status", "DRAFT"))
                .then().statusCode(200)
                .body("data.status", everyItem(equalTo("DRAFT")));

        InvoiceClient.list(authSpec(), Map.of("customerId", invoice.getCustomerId()))
                .then().statusCode(200)
                .body("data.customerId", everyItem(equalTo(invoice.getCustomerId())))
                .body("data.id", hasItem(invoice.getId()));
    }

    @Test
    @DisplayName("POST /invoices with an empty orderIds array is rejected with a validation error")
    void shouldRejectCreateWithEmptyOrderIds() {
        InvoiceClient.create(authSpec(), CreateInvoiceDto.builder().orderIds(List.of()).build())
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /invoices without a bearer token is rejected with 401")
    void shouldRejectCreateWithoutAuth() {
        InvoiceClient.create(RequestSpecs.anonymous(),
                        CreateInvoiceDto.builder().orderIds(List.of(TestDataProvider.notFoundId())).build())
                .then().statusCode(401)
                .body("message", equalTo("Unauthorized"));
    }

    @Test
    @DisplayName("GET /invoices without a bearer token is rejected with 401")
    void shouldRejectListWithoutAuth() {
        InvoiceClient.list(RequestSpecs.anonymous())
                .then().statusCode(401)
                .body("statusCode", equalTo(401));
    }
}
