package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateInvoiceDto;
import com.ventura.api.models.request.RecordPaymentDto;
import com.ventura.api.models.request.SendInvoiceDto;
import com.ventura.api.models.request.UpdateInvoiceStatusDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/** Thin wrapper around every {@code /invoices/*} call - see {@link AuthClient} for the rationale. */
public final class InvoiceClient {

    private InvoiceClient() {
    }

    public static Response create(RequestSpecification spec, CreateInvoiceDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.INVOICES);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.INVOICE_BY_ID, id);
    }

    public static Response recordPayment(RequestSpecification spec, String id, RecordPaymentDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.INVOICE_PAYMENT, id);
    }

    public static Response send(RequestSpecification spec, String id, SendInvoiceDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.INVOICE_SEND, id);
    }

    public static Response updateStatus(RequestSpecification spec, String id, UpdateInvoiceStatusDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.INVOICE_STATUS, id);
    }

    /** GET /invoices with any combination of page/limit/status/customerId/q query params. */
    public static Response list(RequestSpecification spec, Map<String, Object> queryParams) {
        return RestAssured.given().spec(spec).queryParams(queryParams).when().get(Endpoints.INVOICES);
    }

    public static Response list(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.INVOICES);
    }
}
