package com.ventura.api.clients;

import com.ventura.api.core.Endpoints;
import com.ventura.api.models.request.DeleteFileDto;
import com.ventura.api.models.request.PresignUploadDto;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper around every {@code /files/*} call - see {@link AuthClient} for the rationale. */
public final class FileClient {

    private FileClient() {
    }

    public static Response presignUpload(RequestSpecification spec, PresignUploadDto body) {
        return RestAssured.given().spec(spec).body(body).when().post(Endpoints.FILES_PRESIGN);
    }

    public static Response deleteFile(RequestSpecification spec, DeleteFileDto body) {
        return RestAssured.given().spec(spec).body(body).when().delete(Endpoints.FILES);
    }
}
