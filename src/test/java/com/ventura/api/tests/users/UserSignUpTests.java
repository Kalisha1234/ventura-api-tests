package com.ventura.api.tests.users;

import com.ventura.api.clients.UserClient;
import com.ventura.api.core.BaseTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.models.request.CreatePasswordDto;
import com.ventura.api.models.request.CreateUserWithEmailDto;
import com.ventura.api.models.request.UpdatePasswordDto;
import com.ventura.api.models.response.UserResponse;
import com.ventura.api.utils.RandomDataUtils;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;

/**
 * POST /users/email is the only fully public, mutation-safe endpoint in the API (every
 * other mutating endpoint requires a bearer token), so it - and the validation around it -
 * is covered here without needing any auth bootstrap.
 */
@DisplayName("Users - sign up with email (public)")
class UserSignUpTests extends BaseTest {

    @Test
    @DisplayName("POST /users/email with a valid, unique email returns 201 and the created user")
    void shouldSignUpWithValidEmail() {
        CreateUserWithEmailDto body = CreateUserWithEmailDto.builder()
                .firstName(RandomDataUtils.firstName())
                .email(RandomDataUtils.uniqueEmail())
                .build();

        UserResponse user = UserClient.signUpWithEmail(RequestSpecs.anonymous(), body)
                .then().statusCode(201)
                .contentType(ContentType.JSON)
                .body(matchesJsonSchemaInClasspath("schemas/user-response-schema.json"))
                .body("email", equalTo(body.getEmail()))
                .body("firstName", equalTo(body.getFirstName()))
                .body("isEmailVerified", equalTo(false))
                .body("deleted", equalTo(false))
                .body("id", not(blankOrNullString()))
                .extract().as(UserResponse.class);

        org.junit.jupiter.api.Assertions.assertNotNull(user.getShortId(), "shortId should be generated");
    }

    @Test
    @DisplayName("POST /users/email without an email is rejected with 400")
    void shouldRejectSignUpMissingEmail() {
        UserClient.signUpWithEmail(RequestSpecs.anonymous(), TestDataProvider.invalidSignUp("missingEmail"))
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /users/email without a firstName is rejected with 400")
    void shouldRejectSignUpMissingFirstName() {
        UserClient.signUpWithEmail(RequestSpecs.anonymous(), TestDataProvider.invalidSignUp("missingFirstName"))
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /users/email with a malformed email is rejected with 400")
    void shouldRejectSignUpMalformedEmail() {
        UserClient.signUpWithEmail(RequestSpecs.anonymous(), TestDataProvider.invalidSignUp("malformedEmail"))
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("GET /users/{id} without a bearer token is rejected with 401")
    void shouldRejectGetUserByIdWithoutAuth() {
        UserClient.getById(RequestSpecs.anonymous(), TestDataProvider.notFoundId())
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /users/password (create) without a bearer token is rejected with 401, " +
            "even for the exact seeded payload")
    void shouldRejectCreatePasswordWithoutAuth() {
        CreatePasswordDto body = TestDataProvider.passwordSet();

        UserClient.createPassword(RequestSpecs.anonymous(), body)
                .then().statusCode(401);
    }

    @Test
    @DisplayName("PUT /users/password (change) without a bearer token is rejected with 401")
    void shouldRejectUpdatePasswordWithoutAuth() {
        UpdatePasswordDto body = UpdatePasswordDto.builder()
                .userId(TestDataProvider.notFoundId())
                .email(RandomDataUtils.uniqueEmail())
                .oldPassword("whatever-old-1!")
                .newPassword(RandomDataUtils.strongPassword())
                .build();

        UserClient.updatePassword(RequestSpecs.anonymous(), body)
                .then().statusCode(401);
    }

    @Test
    @DisplayName("GET /users/{id}/has-password with a syntactically invalid id should not 500")
    void hasPasswordWithMalformedId_shouldNotReturnServerError() {
        // Known finding: this currently returns 500 ("Internal server error") for a malformed
        // Mongo ObjectId instead of a 400/404. Asserting the correct contract here so this test
        // documents the bug until the backend validates/handles the id properly.
        UserClient.hasPassword(RequestSpecs.anonymous(), TestDataProvider.malformedId())
                .then().statusCode(not(greaterThan(499)));
    }
}
