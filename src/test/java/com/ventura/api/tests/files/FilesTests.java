package com.ventura.api.tests.files;

import com.ventura.api.clients.FileClient;
import com.ventura.api.core.BaseTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.models.request.DeleteFileDto;
import com.ventura.api.models.request.PresignUploadDto;
import com.ventura.api.models.response.DeleteFileResponse;
import com.ventura.api.models.response.PresignedUploadResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;

/**
 * Files module coverage.
 *
 * <p><b>Security finding (empirically confirmed, not assumed):</b> unlike Business and
 * Customers - where every endpoint declares {@code security: [{bearer: []}]} - the OpenAPI
 * spec for both {@code POST /files/presign} and {@code DELETE /files} declares no security
 * requirement at all, and a live, unauthenticated {@code curl} against
 * {@code https://dev.ventura.csniico.com} confirmed the documented behaviour is real: both
 * endpoints returned {@code 200} with no {@code Authorization} header whatsoever. In
 * particular, {@code POST /files/presign} happily minted a real, working S3 presigned PUT
 * URL (bucket {@code csniico-ventura-bucket}) for an anonymous caller. This means anyone who
 * can reach the API can obtain upload credentials into the bucket and can attempt to delete
 * arbitrary file keys, with no authentication of any kind. This is worth flagging back to
 * the team as a likely gap rather than an intentional design (contrast with every other
 * module tested, which is 100% bearer-gated). Tests below assert on the behaviour as
 * observed; they do not assume it will change.
 */
@DisplayName("Files")
class FilesTests extends BaseTest {

    @Test
    @DisplayName("POST /files/presign for a plausible image upload returns 200 with non-blank fileKey/fileUrl/uploadUrl")
    void shouldPresignUpload() {
        PresignUploadDto body = PresignUploadDto.builder()
                .contentType("image/png")
                .filename("qa-" + RandomDataUtils.uuidSuffix() + ".png")
                .folder("avatars")
                .build();

        PresignedUploadResponse presigned = FileClient.presignUpload(RequestSpecs.anonymous(), body)
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/presigned-upload-response-schema.json"))
                .extract().as(PresignedUploadResponse.class);

        Assertions.assertNotNull(presigned.getFileKey(), "fileKey should be present");
        Assertions.assertFalse(presigned.getFileKey().isBlank(), "fileKey should not be blank");
        Assertions.assertNotNull(presigned.getFileUrl(), "fileUrl should be present");
        Assertions.assertFalse(presigned.getFileUrl().isBlank(), "fileUrl should not be blank");
        Assertions.assertNotNull(presigned.getUploadUrl(), "uploadUrl should be present");
        Assertions.assertFalse(presigned.getUploadUrl().isBlank(), "uploadUrl should not be blank");
    }

    @Test
    @DisplayName("DELETE /files for a key that was never uploaded is observed to return 200, echoing the key back "
            + "(not a 404) - documenting actual behaviour rather than inventing state")
    void deletingNonExistentFileKey_returnsObservedBehaviour() {
        DeleteFileDto body = DeleteFileDto.builder()
                .fileKey("uploads/qa-never-existed-" + RandomDataUtils.uuidSuffix() + ".png")
                .build();

        DeleteFileResponse response = FileClient.deleteFile(RequestSpecs.anonymous(), body)
                .then().statusCode(200)
                .body("fileKey", equalTo(body.getFileKey()))
                .extract().as(DeleteFileResponse.class);

        Assertions.assertEquals(body.getFileKey(), response.getFileKey());
    }

    @Test
    @DisplayName("POST /files/presign without a contentType is rejected with 400")
    void shouldRejectPresignMissingContentType() {
        PresignUploadDto body = PresignUploadDto.builder()
                .filename("qa-" + RandomDataUtils.uuidSuffix() + ".png")
                .build();

        FileClient.presignUpload(RequestSpecs.anonymous(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("statusCode", equalTo(400));
    }

    @Test
    @DisplayName("POST /files/presign without a filename is rejected with 400")
    void shouldRejectPresignMissingFilename() {
        PresignUploadDto body = PresignUploadDto.builder()
                .contentType("image/png")
                .build();

        FileClient.presignUpload(RequestSpecs.anonymous(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("statusCode", equalTo(400));
    }
}
