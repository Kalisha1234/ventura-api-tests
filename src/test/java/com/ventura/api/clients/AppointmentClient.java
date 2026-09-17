package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateAppointmentDto;
import com.ventura.api.models.request.UpdateAppointmentDto;
import com.ventura.api.models.request.UpdateAppointmentStatusDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /appointments/*} call - see {@link AuthClient} for the rationale. */
public final class AppointmentClient {

    private AppointmentClient() {
    }

    public static Response create(RequestSpecification spec, CreateAppointmentDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.APPOINTMENTS);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.APPOINTMENT_BY_ID, id);
    }

    /** GET /appointments?from&to - both query params are required per the OpenAPI spec. */
    public static Response list(RequestSpecification spec, String from, String to) {
        return RestAssured.given().spec(spec)
                .queryParam("from", from)
                .queryParam("to", to)
                .when().get(Endpoints.APPOINTMENTS);
    }

    public static Response update(RequestSpecification spec, String id, UpdateAppointmentDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.APPOINTMENT_BY_ID, id);
    }

    public static Response updateStatus(RequestSpecification spec, String id, UpdateAppointmentStatusDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.APPOINTMENT_STATUS, id);
    }

    public static Response delete(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().delete(Endpoints.APPOINTMENT_BY_ID, id);
    }
}
