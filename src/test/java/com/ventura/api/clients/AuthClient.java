package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.SignInAppleDto;
import com.ventura.api.models.request.SignInEmailDto;
import com.ventura.api.models.request.SignInGoogleDto;
import com.ventura.api.models.request.SignInPasswordDto;
import com.ventura.api.models.request.VerifyCodeDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Thin wrapper around every {@code /auth/*} call. Client classes own "how to call the
 * endpoint" (path, verb, body) and deliberately return the raw {@link Response} rather than
 * asserting anything themselves - assertions stay in the test class, which keeps a single
 * place to fix things if a path or payload shape ever changes without disturbing every test
 * that exercises it.
 */
public final class AuthClient {

    private AuthClient() {
    }

    public static Response signInWithPassword(RequestSpecification spec, SignInPasswordDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.AUTH_SIGN_IN_PASSWORD);
    }

    public static Response signInWithEmail(RequestSpecification spec, SignInEmailDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.AUTH_SIGN_IN_EMAIL);
    }

    public static Response verifyCode(RequestSpecification spec, VerifyCodeDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.AUTH_VERIFY_CODE);
    }

    public static Response signInWithGoogle(RequestSpecification spec, SignInGoogleDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.AUTH_SIGN_IN_GOOGLE);
    }

    public static Response signInWithApple(RequestSpecification spec, SignInAppleDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.AUTH_SIGN_IN_APPLE);
    }

    /** Expects the refresh token to be supplied as the bearer header on {@code spec}. */
    public static Response refresh(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().post(Endpoints.AUTH_REFRESH);
    }

    /** Expects the access token to be supplied as the bearer header on {@code spec}. */
    public static Response logout(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().post(Endpoints.AUTH_LOGOUT);
    }
}
