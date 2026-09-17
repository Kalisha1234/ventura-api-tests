package com.ventura.api.tests.business;

import com.ventura.api.clients.BusinessClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateBusinessDto;
import com.ventura.api.models.request.UpdateBusinessDto;
import com.ventura.api.models.response.BusinessResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Business module coverage. Every {@code /businesses/*} endpoint declares
 * {@code security: [{bearer: []}]} in the OpenAPI spec (confirmed via
 * {@code https://dev.ventura.csniico.com/api/docs-json}), so this whole class extends
 * {@link BaseAuthenticatedTest} and skips automatically when no test credentials are
 * configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Business")
class BusinessTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("POST /businesses with a valid name/categories returns 201 and echoes them back")
    void shouldCreateBusiness() {
        CreateBusinessDto body = CreateBusinessDto.builder()
                .name(RandomDataUtils.companyName() + " " + RandomDataUtils.uuidSuffix())
                .categories(List.of("Retail", "Wholesale"))
                .build();

        BusinessResponse created = BusinessClient.create(authSpec(), body)
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/business-response-schema.json"))
                .body("name", equalTo(body.getName()))
                .body("categories", equalTo(body.getCategories()))
                .body("id", not(blankOrNullString()))
                .extract().as(BusinessResponse.class);

        Assertions.assertNotNull(created.getShortId(), "shortId should be generated");
    }

    @Test
    @DisplayName("GET /businesses/{id} for a business the caller just created returns 200")
    void shouldGetBusinessById() {
        BusinessResponse created = TestDataFactory.createBusiness(authSpec());

        BusinessClient.getById(authSpec(), created.getId())
                .then().statusCode(200)
                .body("id", equalTo(created.getId()))
                .body("name", equalTo(created.getName()));
    }

    @Test
    @DisplayName("GET /businesses/{id} for a non-existent id is not found")
    void shouldNotFindBusinessByNonExistentId() {
        int status = BusinessClient.getById(authSpec(), TestDataProvider.notFoundId()).statusCode();

        Assertions.assertTrue(status == 403 || status == 404,
                "Expected a non-existent/foreign business id to be forbidden/not-found, but got " + status);
    }

    @Test
    @DisplayName("PATCH /businesses/{id} partially updates just the description, leaving the name untouched")
    void shouldPartiallyUpdateBusiness() {
        BusinessResponse created = TestDataFactory.createBusiness(authSpec());
        String newDescription = "Updated by automation - " + RandomDataUtils.uuidSuffix();

        UpdateBusinessDto body = UpdateBusinessDto.builder()
                .description(newDescription)
                .build();

        BusinessClient.update(authSpec(), created.getId(), body)
                .then().statusCode(200)
                .body("description", equalTo(newDescription))
                .body("name", equalTo(created.getName()));
    }

    @Test
    @DisplayName("GET /businesses/categories returns 200 and a non-empty array of suggested category strings")
    void shouldListSuggestedCategories() {
        List<String> categories = BusinessClient.getCategories(authSpec())
                .then().statusCode(200)
                .extract().jsonPath().getList("", String.class);

        Assertions.assertFalse(categories.isEmpty(), "Expected at least one suggested category");
    }

    @Test
    @DisplayName("GET /businesses/mine returns the authenticated user's business")
    void shouldGetMyBusiness() {
        BusinessResponse mine = TestDataFactory.ensureBusiness(authSpec());

        BusinessClient.getMine(authSpec())
                .then().statusCode(200)
                .body("id", equalTo(mine.getId()))
                .body("ownerId", not(blankOrNullString()));
    }

    @Test
    @DisplayName("POST /businesses without a bearer token is rejected with 401")
    void shouldRejectCreateWithoutAuth() {
        CreateBusinessDto body = CreateBusinessDto.builder()
                .name(RandomDataUtils.companyName())
                .categories(List.of("Retail"))
                .build();

        BusinessClient.create(RequestSpecs.anonymous(), body)
                .then().statusCode(401);
    }

    @Test
    @DisplayName("GET /businesses/mine without a bearer token is rejected with 401")
    void shouldRejectGetMineWithoutAuth() {
        BusinessClient.getMine(RequestSpecs.anonymous())
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /businesses without a name is rejected with 400")
    void shouldRejectCreateMissingName() {
        CreateBusinessDto body = CreateBusinessDto.builder()
                .categories(List.of("Retail"))
                .build();

        BusinessClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("statusCode", equalTo(400));
    }
}
