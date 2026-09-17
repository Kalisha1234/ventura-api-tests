package com.ventura.api.tests.users;

import com.ventura.api.clients.UserClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.TokenProvider;
import com.ventura.api.models.request.RequestEmailChangeDto;
import com.ventura.api.models.request.UpdateFirstNameDto;
import com.ventura.api.models.request.UpdateLastNameDto;
import com.ventura.api.models.request.UpdateProfileDto;
import com.ventura.api.models.response.UserResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Self-service profile endpoints for the authenticated user. Skipped automatically when
 * no auth is configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Users - authenticated self-service")
class UserAuthenticatedTests extends BaseAuthenticatedTest {

    private String selfId() {
        return TokenProvider.lastSignIn().getUser().getId();
    }

    @Test
    @DisplayName("GET /users/{id} for the authenticated user's own id returns 200")
    void shouldGetOwnUserById() {
        UserClient.getById(authSpec(), selfId())
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/user-response-schema.json"))
                .body("id", equalTo(selfId()));
    }

    @Test
    @DisplayName("PATCH /users/{id}/first-name updates the first name")
    void shouldUpdateFirstName() {
        String newFirstName = RandomDataUtils.firstName();

        UserResponse updated = UserClient.updateFirstName(authSpec(), selfId(),
                        UpdateFirstNameDto.builder().firstName(newFirstName).build())
                .then().statusCode(200)
                .extract().as(UserResponse.class);

        org.junit.jupiter.api.Assertions.assertEquals(newFirstName, updated.getFirstName());
    }

    @Test
    @DisplayName("PATCH /users/{id}/last-name accepts null to clear the last name")
    void shouldClearLastName() {
        UserClient.updateLastName(authSpec(), selfId(), UpdateLastNameDto.builder().lastName(null).build())
                .then().statusCode(200)
                .body("lastName", nullValue());
    }

    @Test
    @DisplayName("PATCH /users/{id}/profile bulk-updates first and last name together")
    void shouldBulkUpdateProfile() {
        String firstName = RandomDataUtils.firstName();
        String lastName = RandomDataUtils.lastName();

        UserClient.updateProfile(authSpec(), selfId(),
                        UpdateProfileDto.builder().firstName(firstName).lastName(lastName).build())
                .then().statusCode(200)
                .body("firstName", equalTo(firstName))
                .body("lastName", equalTo(lastName));
    }

    @Test
    @DisplayName("POST /users/{id}/email requests an email change and returns a confirmation message")
    void shouldRequestEmailChange() {
        UserClient.requestEmailChange(authSpec(), selfId(),
                        RequestEmailChangeDto.builder().newEmail(RandomDataUtils.uniqueEmail()).build())
                .then().statusCode(200)
                .body("message", org.hamcrest.Matchers.not(org.hamcrest.Matchers.blankOrNullString()));
        // Confirming the change requires the code emailed to the new address, which this
        // suite has no mailbox access to retrieve - see README for how to wire that up.
    }

    @Test
    @DisplayName("GET /users/{id} for another user's id, using our own token, is not allowed")
    void shouldNotAllowReadingAnotherUsersRecord() {
        // IDOR guard: the seeded test-data user id is a different account than whoever is
        // configured as the bootstrap credential, so this must not succeed as if it were "us".
        String foreignUserId = com.ventura.api.data.TestDataProvider.passwordSet().getUserId();
        org.junit.jupiter.api.Assumptions.assumeTrue(!foreignUserId.equals(selfId()),
                "Seeded userId happens to equal the bootstrap account id in this environment - skipping IDOR check.");

        int status = UserClient.getById(authSpec(), foreignUserId).statusCode();

        org.junit.jupiter.api.Assertions.assertTrue(status == 403 || status == 404,
                "Expected reading another user's record to be forbidden/not-found, but got " + status);
    }
}
