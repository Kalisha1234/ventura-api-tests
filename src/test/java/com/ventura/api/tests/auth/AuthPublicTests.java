package com.ventura.api.tests.auth;

import com.ventura.api.clients.AuthClient;
import com.ventura.api.core.BaseTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.models.request.SignInEmailDto;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Negative-path Auth scenarios that need no bootstrap credentials at all. */
@DisplayName("Auth - public negative paths")
class AuthPublicTests extends BaseTest {

    @Test
    @DisplayName("POST /auth/sign-in-password with the wrong password is rejected with 401")
    void shouldRejectSignInWithWrongPassword() {
        AuthClient.signInWithPassword(RequestSpecs.anonymous(), TestDataProvider.invalidSignInWrongPassword())
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /auth/sign-in-password for an unknown email is rejected with 401")
    void shouldRejectSignInForUnknownUser() {
        AuthClient.signInWithPassword(RequestSpecs.anonymous(), TestDataProvider.invalidSignInUnknownUser())
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /auth/refresh with a garbage token is rejected with 401")
    void shouldRejectRefreshWithInvalidToken() {
        AuthClient.refresh(RequestSpecs.withBearerToken("not-a-real-token"))
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /auth/logout without a bearer token is rejected with 401")
    void shouldRejectLogoutWithoutAuth() {
        AuthClient.logout(RequestSpecs.anonymous())
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /auth/sign-in-email for an unknown address does not reveal whether the account exists")
    void signInWithEmail_shouldNotLeakAccountExistence() {
        // Requesting a passwordless code for a bogus address should behave the same
        // (200 + generic message) as for a real one, so this endpoint can't be used to
        // enumerate registered emails. If it 4xx's instead, that's an enumeration leak.
        AuthClient.signInWithEmail(RequestSpecs.anonymous(),
                        SignInEmailDto.builder().email(RandomDataUtils.uniqueEmail()).build())
                .then().statusCode(200);
    }
}
