package com.ventura.api.tests.admin;

import com.ventura.api.clients.AdminClient;
import com.ventura.api.clients.UserClient;
import com.ventura.api.core.BaseTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.models.request.CreateAdminDto;
import com.ventura.api.models.request.CreateUserWithEmailDto;
import com.ventura.api.models.request.UpdateAdminProfileDto;
import com.ventura.api.models.response.AdminResponse;
import com.ventura.api.models.response.UserResponse;
import com.ventura.api.utils.RandomDataUtils;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

/**
 * Admin module coverage.
 *
 * <p><b>Security finding (empirically confirmed, not assumed):</b> unlike Business and
 * Customers - where every endpoint declares {@code security: [{bearer: []}]} - the OpenAPI
 * spec declares no security requirement at all for any {@code /admin/*} endpoint, and a
 * live, unauthenticated {@code curl} against {@code https://dev.ventura.csniico.com}
 * confirmed the documented behaviour is real: {@code GET /admin/users} returned {@code 200}
 * with the full list of real user records (id, name, email, verification/active/deleted
 * flags, timestamps) with zero {@code Authorization} header. {@code POST /admin/profile},
 * {@code GET/PATCH /admin/profile/{id}}, {@code GET /admin/users/{id}}, and the
 * soft-delete/restore/hard-delete user-management endpoints all behaved the same way -
 * fully reachable and mutable with no authentication of any kind. This looks like a
 * significant, unintentional gap (contrast with Business/Customers, which are 100%
 * bearer-gated) and is worth escalating to the team rather than treating as by-design.
 * Tests below use {@link RequestSpecs#anonymous()} throughout to make that explicit, assert
 * on the behaviour as observed, and - per the destructive-action guardrail for this
 * module - only ever create, soft-delete, restore, or hard-delete records created by the
 * test itself (a throwaway user signed up via {@code POST /users/email}, or an admin
 * profile created with a fresh random email); no pre-existing record is ever touched.
 */
@DisplayName("Admin")
class AdminTests extends BaseTest {

    private UserResponse createThrowawayUser() {
        CreateUserWithEmailDto body = CreateUserWithEmailDto.builder()
                .firstName("QAThrowaway")
                .email(RandomDataUtils.uniqueEmail())
                .build();

        return UserClient.signUpWithEmail(RequestSpecs.anonymous(), body)
                .then().statusCode(201)
                .extract().as(UserResponse.class);
    }

    private AdminResponse createAdminProfile(String email) {
        CreateAdminDto body = CreateAdminDto.builder()
                .name("QA Admin " + RandomDataUtils.uuidSuffix())
                .email(email)
                .build();

        return AdminClient.createProfile(RequestSpecs.anonymous(), body)
                .then().statusCode(200)
                .body("email", equalTo(email))
                .extract().as(AdminResponse.class);
    }

    @Test
    @DisplayName("POST /admin/profile is idempotent: creating with the same email twice returns the same id")
    void shouldCreateAdminProfileIdempotently() {
        String email = RandomDataUtils.uniqueEmail();

        AdminResponse first = createAdminProfile(email);
        AdminResponse second = createAdminProfile(email);

        Assertions.assertEquals(first.getId(), second.getId(),
                "Creating an admin profile with an email that already exists should return the existing record");
    }

    @Test
    @DisplayName("GET /admin/profile/{id} for a just-created admin profile returns 200")
    void shouldGetAdminProfileById() {
        AdminResponse created = createAdminProfile(RandomDataUtils.uniqueEmail());

        AdminClient.getProfileById(RequestSpecs.anonymous(), created.getId())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/admin-response-schema.json"))
                .body("id", equalTo(created.getId()))
                .body("email", equalTo(created.getEmail()));
    }

    @Test
    @DisplayName("PATCH /admin/profile/{id} updates the admin's name")
    void shouldUpdateAdminProfileName() {
        AdminResponse created = createAdminProfile(RandomDataUtils.uniqueEmail());
        String newName = "QA Admin Updated " + RandomDataUtils.uuidSuffix();

        UpdateAdminProfileDto body = UpdateAdminProfileDto.builder().name(newName).build();

        AdminClient.updateProfile(RequestSpecs.anonymous(), created.getId(), body)
                .then().statusCode(200)
                .body("name", equalTo(newName))
                .body("email", equalTo(created.getEmail()));
    }

    @Test
    @DisplayName("GET /admin/profile/{id} for a well-formed but non-existent admin id (UUID) returns 404")
    void shouldReturn404ForNonExistentAdminProfileId() {
        String neverIssuedUuid = UUID.randomUUID().toString();

        AdminClient.getProfileById(RequestSpecs.anonymous(), neverIssuedUuid)
                .then().statusCode(404);
    }

    @Test
    @DisplayName("Known finding: GET /admin/profile/{id} with a Mongo-ObjectId-shaped id (admin ids are actually "
            + "UUIDs) returns 500 instead of a clean 400/404 - documenting the observed behaviour")
    void knownFinding_getAdminProfileWithWrongIdShape_returns500() {
        // Admin/user records in this API are keyed by UUID, not Mongo ObjectId. Passing
        // TestDataProvider.notFoundId() (a well-formed ObjectId, used elsewhere for
        // Business/Customer 404 tests) surfaces an unhandled type error as a raw 500
        // instead of a validation 400 or a clean 404. Asserting the actual observed
        // status here so this documents the bug rather than silently ignoring it.
        AdminClient.getProfileById(RequestSpecs.anonymous(), TestDataProvider.notFoundId())
                .then().statusCode(500);
    }

    @Test
    @DisplayName("GET /admin/users returns 200 with an array that includes a just-created throwaway user")
    void shouldListUsersAndIncludeFixtureUser() {
        UserResponse fixtureUser = createThrowawayUser();

        Response response = AdminClient.listUsers(RequestSpecs.anonymous())
                .then().statusCode(200)
                .extract().response();

        List<Map<String, Object>> users = response.jsonPath().getList("$");
        Assertions.assertFalse(users.isEmpty(), "Expected at least one user in the admin user list");

        boolean found = users.stream().anyMatch(u -> fixtureUser.getId().equals(u.get("id")));
        Assertions.assertTrue(found, "Expected the just-created throwaway user to appear in the admin user list");
    }

    @Test
    @DisplayName("GET /admin/users/{id} for a just-created throwaway user returns 200 with matching data")
    void shouldGetUserById() {
        UserResponse fixtureUser = createThrowawayUser();

        AdminClient.getUserById(RequestSpecs.anonymous(), fixtureUser.getId())
                .then().statusCode(200)
                .body("id", equalTo(fixtureUser.getId()))
                .body("email", equalTo(fixtureUser.getEmail()));
    }

    @Test
    @DisplayName("DELETE /admin/users/{id} soft-deletes a throwaway user, then POST .../restore reverses it")
    void shouldSoftDeleteThenRestoreUser() {
        UserResponse fixtureUser = createThrowawayUser();

        AdminClient.softDeleteUser(RequestSpecs.anonymous(), fixtureUser.getId())
                .then().statusCode(200)
                .body("id", equalTo(fixtureUser.getId()))
                .body("deleted", equalTo(true));

        AdminClient.restoreUser(RequestSpecs.anonymous(), fixtureUser.getId())
                .then().statusCode(200)
                .body("id", equalTo(fixtureUser.getId()))
                .body("deleted", equalTo(false));
    }

    @Test
    @DisplayName("DELETE /admin/users/{id}/permanent hard-deletes a throwaway user this test created itself, "
            + "and a subsequent GET 404s")
    void shouldHardDeleteOwnThrowawayUser() {
        // Guardrail: this must only ever run against a user this exact test created -
        // never a pre-existing or shared fixture id.
        UserResponse fixtureUser = createThrowawayUser();

        AdminClient.permanentlyDeleteUser(RequestSpecs.anonymous(), fixtureUser.getId())
                .then().statusCode(200)
                .body("id", equalTo(fixtureUser.getId()));

        AdminClient.getUserById(RequestSpecs.anonymous(), fixtureUser.getId())
                .then().statusCode(404);
    }
}
