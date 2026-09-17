package com.ventura.api.tests.resources;

import com.ventura.api.clients.ResourceClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateResourceDto;
import com.ventura.api.models.request.UpdateResourceDto;
import com.ventura.api.models.response.PaginatedResourceResponse;
import com.ventura.api.models.response.ResourceResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Resources module: products and services. Every endpoint requires a bearer token per the
 * OpenAPI spec, so this extends {@link BaseAuthenticatedTest} and auto-skips (see
 * {@link com.ventura.api.core.AuthAvailableCondition}) when no credentials are configured.
 */
@DisplayName("Resources")
class ResourceTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("POST /resources creates a product and echoes name/price/type")
    void shouldCreateProduct() {
        String name = "QA Product " + RandomDataUtils.uuidSuffix();
        CreateResourceDto body = CreateResourceDto.builder()
                .type("product")
                .name(name)
                .price(19.99)
                .build();

        ResourceResponse created = ResourceClient.create(authSpec(), body)
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/resource-response-schema.json"))
                .body("name", equalTo(name))
                .body("type", equalTo("product"))
                .extract().as(ResourceResponse.class);

        Assertions.assertEquals(19.99, created.getPrice(), 0.0001);
        Assertions.assertNotNull(created.getId());
    }

    @Test
    @DisplayName("POST /resources creates a service and echoes name/price/type")
    void shouldCreateService() {
        String name = "QA Service " + RandomDataUtils.uuidSuffix();
        CreateResourceDto body = CreateResourceDto.builder()
                .type("service")
                .name(name)
                .price(49.5)
                .build();

        ResourceClient.create(authSpec(), body)
                .then().statusCode(201)
                .body("name", equalTo(name))
                .body("type", equalTo("service"))
                .body("price", equalTo(49.5f));
    }

    @Test
    @DisplayName("GET /resources/{id} returns 200 for a resource that exists")
    void shouldGetResourceById() {
        ResourceResponse fixture = TestDataFactory.createProduct(authSpec());

        ResourceClient.getById(authSpec(), fixture.getId())
                .then().statusCode(200)
                .body("id", equalTo(fixture.getId()))
                .body("name", equalTo(fixture.getName()));
    }

    @Test
    @DisplayName("GET /resources/{id} returns 404 for a well-formed id that doesn't exist")
    void shouldReturn404ForUnknownResourceId() {
        ResourceClient.getById(authSpec(), TestDataProvider.notFoundId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("PATCH /resources/{id} updates name and price and reflects the new values")
    void shouldUpdateNameAndPrice() {
        ResourceResponse fixture = TestDataFactory.createProduct(authSpec());
        String newName = "Updated Resource " + RandomDataUtils.uuidSuffix();

        UpdateResourceDto update = UpdateResourceDto.builder()
                .name(newName)
                .price(123.45)
                .build();

        ResourceClient.update(authSpec(), fixture.getId(), update)
                .then().statusCode(200)
                .body("id", equalTo(fixture.getId()))
                .body("name", equalTo(newName))
                .body("price", equalTo(123.45f));
    }

    @Test
    @DisplayName("DELETE /resources/{id} removes the resource; a subsequent GET then 404s")
    void shouldDeleteResourceThenGet404s() {
        ResourceResponse fixture = TestDataFactory.createProduct(authSpec());

        ResourceClient.delete(authSpec(), fixture.getId())
                .then().statusCode(200);

        ResourceClient.getById(authSpec(), fixture.getId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("GET /resources?q= finds a resource by its unique name")
    void shouldSearchResourcesByFreeText() {
        ResourceResponse fixture = TestDataFactory.createProduct(authSpec());

        PaginatedResourceResponse page = ResourceClient.list(authSpec(),
                        Map.of("q", fixture.getName(), "page", 1, "limit", 20))
                .then().statusCode(200)
                .extract().as(PaginatedResourceResponse.class);

        Assertions.assertTrue(page.getData().stream().anyMatch(r -> r.getId().equals(fixture.getId())),
                "Expected search by unique name to return the fixture resource");
    }

    @Test
    @DisplayName("GET /resources?type=service filters results to only services")
    void shouldFilterResourcesByType() {
        ResourceResponse service = TestDataFactory.createResource(authSpec(), "service");

        ResourceClient.list(authSpec(), Map.of("type", "service", "limit", 100))
                .then().statusCode(200)
                .body("data.type", everyItem(equalTo("service")))
                .body("data.id", hasItem(service.getId()))
                .body("meta", instanceOf(Object.class))
                .body("meta.limit", equalTo(100));
    }

    @Test
    @DisplayName("GET /resources returns pagination metadata with total >= 0")
    void shouldReturnPaginationMetadata() {
        TestDataFactory.createProduct(authSpec());

        ResourceClient.list(authSpec(), Map.of("page", 1, "limit", 5))
                .then().statusCode(200)
                .body("meta.page", equalTo(1))
                .body("meta.limit", equalTo(5))
                .body("meta.total", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("POST /resources without a price is rejected with 400")
    void shouldRejectCreateWithoutPrice() {
        CreateResourceDto body = CreateResourceDto.builder()
                .type("product")
                .name("Missing Price " + RandomDataUtils.uuidSuffix())
                .build();

        ResourceClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /resources without a name is rejected with 400")
    void shouldRejectCreateWithoutName() {
        CreateResourceDto body = CreateResourceDto.builder()
                .type("product")
                .price(9.99)
                .build();

        ResourceClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /resources with an invalid type value is rejected with 400")
    void shouldRejectCreateWithInvalidType() {
        CreateResourceDto body = CreateResourceDto.builder()
                .type("widget")
                .name("Invalid Type " + RandomDataUtils.uuidSuffix())
                .price(9.99)
                .build();

        ResourceClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /resources without a bearer token is rejected with 401")
    void shouldRejectCreateWithoutToken() {
        CreateResourceDto body = CreateResourceDto.builder()
                .type("product")
                .name("No Auth " + RandomDataUtils.uuidSuffix())
                .price(9.99)
                .build();

        ResourceClient.create(RequestSpecs.anonymous(), body)
                .then().statusCode(401)
                .body("message", equalTo("Unauthorized"));
    }

    @Test
    @DisplayName("GET /resources without a bearer token is rejected with 401")
    void shouldRejectListWithoutToken() {
        ResourceClient.list(RequestSpecs.anonymous(), Map.of())
                .then().statusCode(401)
                .body("message", equalTo("Unauthorized"));
    }
}
