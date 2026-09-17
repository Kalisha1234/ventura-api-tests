package com.ventura.api.core;

import com.ventura.api.clients.AuthClient;
import com.ventura.api.config.ConfigManager;
import com.ventura.api.models.request.SignInPasswordDto;
import com.ventura.api.models.response.AuthResponse;
import io.restassured.response.Response;

/**
 * Resolves and caches a bearer access token for the whole suite, so every authenticated
 * test does not need to know or care how that token was obtained.
 *
 * <p>Resolution strategy, in priority order:
 * <ol>
 *   <li>A pre-issued token via {@code -Dtest.bearer.token=...} / {@code TEST_BEARER_TOKEN}.</li>
 *   <li>Signing in with {@code -Dtest.user.email=...} + {@code -Dtest.user.password=...}
 *       (or {@code TEST_USER_EMAIL} / {@code TEST_USER_PASSWORD}) against
 *       {@code POST /auth/sign-in-password}.</li>
 * </ol>
 *
 * <p>If neither is configured, {@link #getAccessToken()} throws {@link AuthNotConfiguredException}.
 * Authenticated test classes should extend {@link BaseAuthenticatedTest}, which uses
 * {@link AuthAvailableCondition} to skip (not fail) when auth isn't wired up yet - see
 * README for how to supply credentials.
 */
public final class TokenProvider {

    private static volatile String cachedAccessToken;
    private static volatile String cachedRefreshToken;
    private static volatile AuthResponse lastSignIn;

    private TokenProvider() {
    }

    public static boolean isConfigured() {
        return ConfigManager.bearerToken() != null
                || (ConfigManager.testUserEmail() != null && ConfigManager.testUserPassword() != null);
    }

    public static synchronized String getAccessToken() {
        if (cachedAccessToken != null) {
            return cachedAccessToken;
        }

        String preIssued = ConfigManager.bearerToken();
        if (preIssued != null) {
            cachedAccessToken = preIssued;
            cachedRefreshToken = ConfigManager.refreshToken();
            return cachedAccessToken;
        }

        String email = ConfigManager.testUserEmail();
        String password = ConfigManager.testUserPassword();
        if (email == null || password == null) {
            throw new AuthNotConfiguredException(
                    "No auth strategy configured. Provide TEST_BEARER_TOKEN, or both "
                            + "TEST_USER_EMAIL and TEST_USER_PASSWORD, as env vars or -D system properties.");
        }

        Response response = AuthClient.signInWithPassword(RequestSpecs.anonymous(),
                SignInPasswordDto.builder().email(email).password(password).build());

        if (response.statusCode() != 200) {
            throw new AuthNotConfiguredException(
                    "Sign-in with TEST_USER_EMAIL/TEST_USER_PASSWORD failed: HTTP "
                            + response.statusCode() + " - " + response.asString());
        }

        lastSignIn = response.as(AuthResponse.class);
        cachedAccessToken = lastSignIn.getAccessToken();
        cachedRefreshToken = lastSignIn.getRefreshToken();
        return cachedAccessToken;
    }

    /** The authenticated user, when the token was obtained via password sign-in in this run. */
    public static AuthResponse lastSignIn() {
        getAccessToken();
        return lastSignIn;
    }

    /** Exchanges the cached refresh token for a fresh token pair via POST /auth/refresh. */
    public static synchronized String refresh() {
        if (cachedRefreshToken == null) {
            throw new AuthNotConfiguredException("No refresh token available to exchange.");
        }
        Response response = AuthClient.refresh(RequestSpecs.withBearerToken(cachedRefreshToken));

        if (response.statusCode() != 200) {
            throw new AuthNotConfiguredException(
                    "Token refresh failed: HTTP " + response.statusCode() + " - " + response.asString());
        }
        AuthResponse refreshed = response.as(AuthResponse.class);
        cachedAccessToken = refreshed.getAccessToken();
        cachedRefreshToken = refreshed.getRefreshToken();
        return cachedAccessToken;
    }

    /** Test-only escape hatch to force re-resolution (e.g. after deliberately corrupting the token). */
    static synchronized void reset() {
        cachedAccessToken = null;
        cachedRefreshToken = null;
        lastSignIn = null;
    }

    public static class AuthNotConfiguredException extends RuntimeException {
        public AuthNotConfiguredException(String message) {
            super(message);
        }
    }
}
