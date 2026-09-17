package com.ventura.api.tests.appointments;

import com.ventura.api.clients.AppointmentClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateAppointmentDto;
import com.ventura.api.models.request.InviteeDto;
import com.ventura.api.models.request.RecurrenceDto;
import com.ventura.api.models.request.UpdateAppointmentDto;
import com.ventura.api.models.request.UpdateAppointmentStatusDto;
import com.ventura.api.models.response.AppointmentResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

/**
 * Appointments module. Every endpoint requires a bearer token per the OpenAPI spec, so this
 * extends {@link BaseAuthenticatedTest} and auto-skips (see
 * {@link com.ventura.api.core.AuthAvailableCondition}) when no credentials are configured.
 */
@DisplayName("Appointments")
class AppointmentTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("POST /appointments with only the required fields creates an appointment")
    void shouldCreateAppointmentWithRequiredFieldsOnly() {
        Instant start = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        String title = "QA Appointment " + RandomDataUtils.uuidSuffix();

        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title(title)
                .start(start.toString())
                .end(end.toString())
                .build();

        AppointmentResponse created = AppointmentClient.create(authSpec(), body)
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/appointment-response-schema.json"))
                .body("title", equalTo(title))
                .body("status", equalTo("scheduled"))
                .extract().as(AppointmentResponse.class);

        Assertions.assertNotNull(created.getId());
    }

    @Test
    @DisplayName("POST /appointments with invitees and a recurrence round-trips both in the response")
    void shouldCreateAppointmentWithInviteesAndRecurrence() {
        Instant start = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);
        String inviteeName = RandomDataUtils.fullName();
        String inviteeEmail = RandomDataUtils.uniqueEmail();

        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("QA Appointment With Invitees " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .end(end.toString())
                .invitees(List.of(InviteeDto.builder().name(inviteeName).email(inviteeEmail).build()))
                .recurrence(RecurrenceDto.builder().frequency("weekly").interval(1).build())
                .build();

        AppointmentResponse created = AppointmentClient.create(authSpec(), body)
                .then().statusCode(201)
                .extract().as(AppointmentResponse.class);

        Assertions.assertNotNull(created.getInvitees());
        Assertions.assertEquals(1, created.getInvitees().size());
        Assertions.assertEquals(inviteeName, created.getInvitees().get(0).getName());
        Assertions.assertEquals(inviteeEmail, created.getInvitees().get(0).getEmail());

        Assertions.assertNotNull(created.getRecurrence());
        Assertions.assertEquals("weekly", created.getRecurrence().getFrequency());
        Assertions.assertEquals(1, created.getRecurrence().getInterval());
    }

    @Test
    @DisplayName("GET /appointments/{id} returns 200 for an appointment that exists")
    void shouldGetAppointmentById() {
        AppointmentResponse fixture = TestDataFactory.createAppointment(authSpec());

        AppointmentClient.getById(authSpec(), fixture.getId())
                .then().statusCode(200)
                .body("id", equalTo(fixture.getId()))
                .body("title", equalTo(fixture.getTitle()));
    }

    @Test
    @DisplayName("GET /appointments/{id} returns 404 for a well-formed id that doesn't exist")
    void shouldReturn404ForUnknownAppointmentId() {
        AppointmentClient.getById(authSpec(), TestDataProvider.notFoundId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("GET /appointments?from&to includes a fixture appointment whose start falls in the window")
    void shouldListAppointmentsWithinWindow() {
        Instant start = Instant.now().plus(4, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("QA Window Appointment " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .end(end.toString())
                .build();

        AppointmentResponse created = AppointmentClient.create(authSpec(), body)
                .then().statusCode(201)
                .extract().as(AppointmentResponse.class);

        String from = Instant.now().toString();
        String to = start.plus(1, ChronoUnit.DAYS).toString();

        AppointmentResponse[] page = AppointmentClient.list(authSpec(), from, to)
                .then().statusCode(200)
                .extract().as(AppointmentResponse[].class);

        boolean found = Arrays.stream(page).anyMatch(a -> a.getId().equals(created.getId()));
        Assertions.assertTrue(found, "Expected the fixture appointment to appear inside its own start window");
    }

    @Test
    @DisplayName("GET /appointments?from&to excludes an appointment whose start falls outside the window")
    void shouldExcludeAppointmentOutsideWindow() {
        Instant start = Instant.now().plus(5, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("QA Outside Window Appointment " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .end(end.toString())
                .build();

        AppointmentResponse created = AppointmentClient.create(authSpec(), body)
                .then().statusCode(201)
                .extract().as(AppointmentResponse.class);

        // Window entirely before the fixture's start, far in the past relative to "now".
        String from = Instant.now().minus(30, ChronoUnit.DAYS).toString();
        String to = Instant.now().minus(20, ChronoUnit.DAYS).toString();

        AppointmentResponse[] page = AppointmentClient.list(authSpec(), from, to)
                .then().statusCode(200)
                .extract().as(AppointmentResponse[].class);

        boolean found = Arrays.stream(page).anyMatch(a -> a.getId().equals(created.getId()));
        Assertions.assertFalse(found, "Expected the fixture appointment NOT to appear outside its start window");
    }

    @Test
    @DisplayName("PATCH /appointments/{id} updates title and time and reflects the new values")
    void shouldUpdateTitleAndTime() {
        AppointmentResponse fixture = TestDataFactory.createAppointment(authSpec());
        String newTitle = "Updated Appointment " + RandomDataUtils.uuidSuffix();
        Instant newStart = Instant.now().plus(10, ChronoUnit.DAYS);
        Instant newEnd = newStart.plus(2, ChronoUnit.HOURS);

        UpdateAppointmentDto update = UpdateAppointmentDto.builder()
                .title(newTitle)
                .start(newStart.toString())
                .end(newEnd.toString())
                .build();

        AppointmentClient.update(authSpec(), fixture.getId(), update)
                .then().statusCode(200)
                .body("id", equalTo(fixture.getId()))
                .body("title", equalTo(newTitle));
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/status transitions scheduled -> completed")
    void shouldUpdateStatusToCompleted() {
        AppointmentResponse fixture = TestDataFactory.createAppointment(authSpec());
        Assertions.assertEquals("scheduled", fixture.getStatus());

        UpdateAppointmentStatusDto update = UpdateAppointmentStatusDto.builder()
                .status("completed")
                .build();

        AppointmentClient.updateStatus(authSpec(), fixture.getId(), update)
                .then().statusCode(200)
                .body("id", equalTo(fixture.getId()))
                .body("status", equalTo("completed"));
    }

    @Test
    @DisplayName("PATCH /appointments/{id}/status with an invalid status string is rejected with 400")
    void shouldRejectInvalidStatusValue() {
        AppointmentResponse fixture = TestDataFactory.createAppointment(authSpec());

        UpdateAppointmentStatusDto update = UpdateAppointmentStatusDto.builder()
                .status("not-a-real-status")
                .build();

        AppointmentClient.updateStatus(authSpec(), fixture.getId(), update)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("PATCH /appointments/{id} with clearRecurrence:true removes a previously-set recurrence")
    void shouldClearRecurrence() {
        Instant start = Instant.now().plus(6, ChronoUnit.DAYS);
        Instant end = start.plus(1, ChronoUnit.HOURS);

        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("QA Recurring Appointment " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .end(end.toString())
                .recurrence(RecurrenceDto.builder().frequency("daily").interval(1).build())
                .build();

        AppointmentResponse created = AppointmentClient.create(authSpec(), body)
                .then().statusCode(201)
                .extract().as(AppointmentResponse.class);
        Assertions.assertNotNull(created.getRecurrence(), "Fixture must have a recurrence set before it can be cleared");

        UpdateAppointmentDto clear = UpdateAppointmentDto.builder()
                .clearRecurrence(true)
                .build();

        AppointmentClient.update(authSpec(), created.getId(), clear)
                .then().statusCode(200)
                .body("id", equalTo(created.getId()))
                .body("recurrence", org.hamcrest.Matchers.nullValue());
    }

    @Test
    @DisplayName("DELETE /appointments/{id} removes the appointment; a subsequent GET then 404s")
    void shouldDeleteAppointmentThenGet404s() {
        AppointmentResponse fixture = TestDataFactory.createAppointment(authSpec());

        AppointmentClient.delete(authSpec(), fixture.getId())
                .then().statusCode(200);

        AppointmentClient.getById(authSpec(), fixture.getId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("POST /appointments without a title is rejected with 400")
    void shouldRejectCreateWithoutTitle() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .start(start.toString())
                .end(start.plus(1, ChronoUnit.HOURS).toString())
                .build();

        AppointmentClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /appointments without a start is rejected with 400")
    void shouldRejectCreateWithoutStart() {
        Instant end = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("Missing Start " + RandomDataUtils.uuidSuffix())
                .end(end.toString())
                .build();

        AppointmentClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /appointments without an end is rejected with 400")
    void shouldRejectCreateWithoutEnd() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("Missing End " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .build();

        AppointmentClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /appointments without a bearer token is rejected with 401")
    void shouldRejectCreateWithoutToken() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        CreateAppointmentDto body = CreateAppointmentDto.builder()
                .title("No Auth " + RandomDataUtils.uuidSuffix())
                .start(start.toString())
                .end(start.plus(1, ChronoUnit.HOURS).toString())
                .build();

        AppointmentClient.create(RequestSpecs.anonymous(), body)
                .then().statusCode(401)
                .body("message", equalTo("Unauthorized"));
    }

    @Test
    @DisplayName("GET /appointments without a bearer token is rejected with 401")
    void shouldRejectListWithoutToken() {
        AppointmentClient.list(RequestSpecs.anonymous(),
                        Instant.now().toString(), Instant.now().plus(1, ChronoUnit.DAYS).toString())
                .then().statusCode(401)
                .body("message", equalTo("Unauthorized"));
    }
}
