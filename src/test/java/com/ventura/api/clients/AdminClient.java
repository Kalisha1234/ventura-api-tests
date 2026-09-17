package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.CreateAdminDto;
import com.ventura.api.models.request.UpdateAdminProfileDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /admin/*} call - see {@link AuthClient} for the rationale. */
public final class AdminClient {

    private AdminClient() {
    }

    public static Response createProfile(RequestSpecification spec, CreateAdminDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.ADMIN_PROFILE);
    }

    public static Response getProfileById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.ADMIN_PROFILE_BY_ID, id);
    }

    public static Response updateProfile(RequestSpecification spec, String id, UpdateAdminProfileDto body) {
        return RestAssured.given().spec(spec).body(body).when().patch(Endpoints.ADMIN_PROFILE_BY_ID, id);
    }

    public static Response listUsers(RequestSpecification spec) {
        return RestAssured.given().spec(spec).when().get(Endpoints.ADMIN_USERS);
    }

    public static Response getUserById(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().get(Endpoints.ADMIN_USER_BY_ID, id);
    }

    public static Response softDeleteUser(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().delete(Endpoints.ADMIN_USER_BY_ID, id);
    }

    public static Response restoreUser(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().post(Endpoints.ADMIN_USER_RESTORE, id);
    }

    public static Response permanentlyDeleteUser(RequestSpecification spec, String id) {
        return RestAssured.given().spec(spec).when().delete(Endpoints.ADMIN_USER_PERMANENT, id);
    }
}
