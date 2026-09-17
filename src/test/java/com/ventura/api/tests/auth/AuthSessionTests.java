package com.ventura.api.tests.auth;

import com.ventura.api.clients.AuthClient;
import com.ventura.api.config.ConfigManager;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.models.request.SignInPasswordDto;
import com.ventura.api.models.response.AuthResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * Full sign-in / refresh / logout lifecycle. Each test signs in fresh rather than reusing
 * {@link com.ventura.api.core.TokenProvider}'s cached token, because logout/refresh here
 * deliberately invalidate the session under test - reusing the shared token would break
 * every other authenticated test class that runs afterwards.
 */
@DisplayName("Auth - sign-in / refresh / logout lifecycle")
class AuthSessionTests extends BaseAuthenticatedTest {

    @BeforeEach
    void requireUsernamePasswordCreds() {
        Assumptions.assumeTrue(ConfigManager.testUserEmail() != null && ConfigManager.testUserPassword() != null,
                "Requires TEST_USER_EMAIL + TEST_USER_PASSWORD (a raw TEST_BEARER_TOKEN alone can't sign in fresh).");
    }

    private AuthResponse signInFresh() {
        SignInPasswordDto body = SignInPasswordDto.builder()
                .email(ConfigManager.testUserEmail())
                .password(ConfigManager.testUserPassword())
                .build();

        return AuthClient.signInWithPassword(RequestSpecs.anonymous(), body)
                .then().statusCode(200)
                .extract().as(AuthResponse.class);
    }

    @Test
    @DisplayName("POST /auth/sign-in-password with valid credentials returns a token pair and the user")
    void shouldSignInWithValidCredentials() {
        SignInPasswordDto body = SignInPasswordDto.builder()
                .email(ConfigManager.testUserEmail())
                .password(ConfigManager.testUserPassword())
                .build();

        AuthResponse auth = AuthClient.signInWithPassword(RequestSpecs.anonymous(), body)
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/auth-response-schema.json"))
                .extract().as(AuthResponse.class);

        org.junit.jupiter.api.Assertions.assertNotNull(auth.getAccessToken());
        org.junit.jupiter.api.Assertions.assertNotNull(auth.getRefreshToken());
        org.junit.jupiter.api.Assertions.assertEquals(ConfigManager.testUserEmail(), auth.getUser().getEmail());
    }

    @Test
    @DisplayName("POST /auth/refresh exchanges a valid refresh token for a fresh, different access token")
    void shouldRefreshAccessToken() {
        AuthResponse initial = signInFresh();

        AuthResponse refreshed = AuthClient.refresh(RequestSpecs.withBearerToken(initial.getRefreshToken()))
                .then().statusCode(200)
                .extract().as(AuthResponse.class);

        org.junit.jupiter.api.Assertions.assertNotNull(refreshed.getAccessToken());
        org.junit.jupiter.api.Assertions.assertNotEquals(initial.getAccessToken(), refreshed.getAccessToken(),
                "Refreshing should issue a genuinely new access token.");
    }

    @Test
    @DisplayName("POST /auth/logout revokes the session's refresh token, so refreshing it again fails")
    void shouldLogoutAndRevokeRefreshToken() {
        AuthResponse session = signInFresh();

        AuthClient.logout(RequestSpecs.withBearerToken(session.getAccessToken()))
                .then().statusCode(200);

        Response afterLogout = AuthClient.refresh(RequestSpecs.withBearerToken(session.getRefreshToken()));

        afterLogout.then().statusCode(401);
    }
}
