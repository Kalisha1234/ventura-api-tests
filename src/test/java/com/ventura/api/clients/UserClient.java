package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.ConfirmEmailChangeDto;
import com.ventura.api.models.request.CreatePasswordDto;
import com.ventura.api.models.request.CreateUserWithEmailDto;
import com.ventura.api.models.request.CreateUserWithGoogleDto;
import com.ventura.api.models.request.LinkGoogleAccountDto;
import com.ventura.api.models.request.RequestEmailChangeDto;
import com.ventura.api.models.request.SetBusinessIdDto;
import com.ventura.api.models.request.UpdateAvatarDto;
import com.ventura.api.models.request.UpdateFirstNameDto;
import com.ventura.api.models.request.UpdateLastNameDto;
import com.ventura.api.models.request.UpdatePasswordDto;
import com.ventura.api.models.request.UpdateProfileDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /users/*} call - see {@link AuthClient} for the rationale. */
public final class UserClient {

    private UserClient() {
    }

    public static Response signUpWithEmail(RequestSpecification spec, CreateUserWithEmailDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.USERS_EMAIL);
    }

    /** Overload for negative-payload tests that intentionally don't match the DTO shape. */
    public static Response signUpWithEmail(RequestSpecification spec, Object rawBody) {
        return RestAssured.given().spec(spec).body(rawBody).when().post(Endpoints.USERS_EMAIL);
    }

    public static Response signUpWithGoogle(RequestSpecification spec, CreateUserWithGoogleDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.USERS_GOOGLE);
    }

    public static Response linkGoogleAccount(RequestSpecification spec, LinkGoogleAccountDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.USERS_LINK_GOOGLE);
    }

    /** POST /users/password - set a password for a user that has none. */
    public static Response createPassword(RequestSpecification spec, CreatePasswordDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.USERS_PASSWORD);
    }

    /** PUT /users/password - change an existing password. */
    public static Response updatePassword(RequestSpecification spec, UpdatePasswordDto body) {
        return RestAssured.given().spec(spec).body(body).when().put(Endpoints.USERS_PASSWORD);
    }

    public static Response hasPassword(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.USER_HAS_PASSWORD, id);
    }

    public static Response getById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.USER_BY_ID, id);
    }

    public static Response deleteAccount(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().delete(Endpoints.USER_BY_ID, id);
    }

    public static Response updateProfile(RequestSpecification spec, String id, UpdateProfileDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.USER_PROFILE, id);
    }

    public static Response updateFirstName(RequestSpecification spec, String id, UpdateFirstNameDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.USER_FIRST_NAME, id);
    }

    public static Response updateLastName(RequestSpecification spec, String id, UpdateLastNameDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.USER_LAST_NAME, id);
    }

    public static Response requestEmailChange(RequestSpecification spec, String id, RequestEmailChangeDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.USER_EMAIL_CHANGE, id);
    }

    public static Response confirmEmailChange(RequestSpecification spec, String id, ConfirmEmailChangeDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.USER_EMAIL_CHANGE_CONFIRM, id);
    }

    public static Response updateAvatar(RequestSpecification spec, String id, UpdateAvatarDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.USER_AVATAR, id);
    }

    public static Response setBusiness(RequestSpecification spec, String id, SetBusinessIdDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.USER_BUSINESS, id);
    }
}
