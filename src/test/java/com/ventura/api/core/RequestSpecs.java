package com.ventura.api.core;

import com.ventura.api.config.ConfigManager;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * Factory for REST Assured {@link RequestSpecification}s. Centralizing spec construction
 * means every test hits the same base URI / content type / logging behaviour, and only
 * needs to opt in to auth via {@link #authenticated()}.
 */
public final class RequestSpecs {

    private RequestSpecs() {
    }

    /**
     * Base spec shared by every request: base URI, JSON content type, log-on-failure only.
     * Full request/response logging on assertion failure is enabled globally in
     * {@link BaseTest#setUpRestAssuredDefaults()} via
     * {@code RestAssured.enableLoggingOfRequestAndResponseIfValidationFails}.
     */
    public static RequestSpecification base() {
        return new RequestSpecBuilder()
                .setBaseUri(ConfigManager.baseUri())
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .log(LogDetail.URI)
                .build();
    }

    /** Unauthenticated spec - for public endpoints (sign-up, sign-in, categories lookup, etc). */
    public static RequestSpecification anonymous() {
        return base();
    }

    /** Spec with a bearer token attached, resolved via {@link TokenProvider}. */
    public static RequestSpecification authenticated() {
        return withBearerToken(TokenProvider.getAccessToken());
    }

    /**
     * Spec with an explicit bearer token, for tests that manage their own session (sign-in,
     * refresh, logout) rather than the one {@link TokenProvider} caches for the whole suite.
     */
    public static RequestSpecification withBearerToken(String token) {
        return base().header("Authorization", "Bearer " + token);
    }
}
