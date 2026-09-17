package com.ventura.api.tests.customers;

import com.ventura.api.clients.CustomerClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateCustomerDto;
import com.ventura.api.models.request.ImportCustomerDto;
import com.ventura.api.models.request.ImportCustomersDto;
import com.ventura.api.models.request.UpdateCustomerDto;
import com.ventura.api.models.response.BulkImportResultResponse;
import com.ventura.api.models.response.CustomerResponse;
import com.ventura.api.models.response.PaginatedCustomerResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;

/**
 * Customers module coverage. Every {@code /customers*} endpoint declares
 * {@code security: [{bearer: []}]} in the OpenAPI spec (confirmed via
 * {@code https://dev.ventura.csniico.com/api/docs-json}), so this whole class extends
 * {@link BaseAuthenticatedTest} and skips automatically when no test credentials are
 * configured - see {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Customers")
class CustomerTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("POST /customers with valid data returns 201 and echoes the customer back")
    void shouldCreateCustomer() {
        CreateCustomerDto body = CreateCustomerDto.builder()
                .name(RandomDataUtils.fullName())
                .email(RandomDataUtils.uniqueEmail())
                .phone(RandomDataUtils.phoneNumber())
                .notes("Created by BusinessTests/CustomerTests automation")
                .build();

        CustomerResponse created = CustomerClient.create(authSpec(), body)
                .then().statusCode(201)
                .body(matchesJsonSchemaInClasspath("schemas/customer-response-schema.json"))
                .body("name", equalTo(body.getName()))
                .body("email", equalTo(body.getEmail()))
                .body("id", not(blankOrNullString()))
                .extract().as(CustomerResponse.class);

        Assertions.assertNotNull(created.getShortId(), "shortId should be generated");
    }

    @Test
    @DisplayName("GET /customers with page/limit query params returns a paginated envelope honouring the limit")
    void shouldListCustomersWithPagination() {
        TestDataFactory.createCustomer(authSpec());
        TestDataFactory.createCustomer(authSpec());

        PaginatedCustomerResponse page = CustomerClient.list(authSpec(), 1, 2)
                .then().statusCode(200)
                .body("meta.page", equalTo(1))
                .body("meta.limit", equalTo(2))
                .body("data.size()", lessThanOrEqualTo(2))
                .extract().as(PaginatedCustomerResponse.class);

        Assertions.assertNotNull(page.getMeta(), "meta should be present");
        Assertions.assertTrue(page.getMeta().getTotal() >= 2, "total should include the customers just created");
    }

    @Test
    @DisplayName("GET /customers?q=<name> finds the matching customer by free-text search")
    void shouldFindCustomerByQuerySearch() {
        String uniqueName = "QASearch-" + RandomDataUtils.uuidSuffix();
        CreateCustomerDto body = CreateCustomerDto.builder()
                .name(uniqueName)
                .email(RandomDataUtils.uniqueEmail())
                .build();
        CustomerResponse created = CustomerClient.create(authSpec(), body)
                .then().statusCode(201)
                .extract().as(CustomerResponse.class);

        PaginatedCustomerResponse results = CustomerClient.search(authSpec(), uniqueName)
                .then().statusCode(200)
                .extract().as(PaginatedCustomerResponse.class);

        boolean found = results.getData().stream().anyMatch(c -> created.getId().equals(c.getId()));
        Assertions.assertTrue(found, "Expected search by unique name to return the matching customer");
    }

    @Test
    @DisplayName("GET /customers/{id} for a customer the caller just created returns 200")
    void shouldGetCustomerById() {
        CustomerResponse created = TestDataFactory.createCustomer(authSpec());

        CustomerClient.getById(authSpec(), created.getId())
                .then().statusCode(200)
                .body("id", equalTo(created.getId()))
                .body("name", equalTo(created.getName()));
    }

    @Test
    @DisplayName("GET /customers/{id} for a non-existent id returns 404")
    void shouldReturn404ForNonExistentCustomer() {
        CustomerClient.getById(authSpec(), TestDataProvider.notFoundId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("PATCH /customers/{id} updates the customer and returns the new value")
    void shouldUpdateCustomer() {
        CustomerResponse created = TestDataFactory.createCustomer(authSpec());
        String newPhone = RandomDataUtils.phoneNumber();

        UpdateCustomerDto body = UpdateCustomerDto.builder()
                .phone(newPhone)
                .build();

        CustomerClient.update(authSpec(), created.getId(), body)
                .then().statusCode(200)
                .body("phone", equalTo(newPhone))
                .body("name", equalTo(created.getName()));
    }

    @Test
    @DisplayName("DELETE /customers/{id} removes the customer, and a subsequent GET 404s")
    void shouldDeleteCustomerThenNotFindIt() {
        CustomerResponse created = TestDataFactory.createCustomer(authSpec());

        CustomerClient.delete(authSpec(), created.getId())
                .then().statusCode(200);

        CustomerClient.getById(authSpec(), created.getId())
                .then().statusCode(404);
    }

    @Test
    @DisplayName("POST /customers/import with a mixed valid/invalid batch reports the valid one as created")
    void shouldBulkImportMixedBatch() {
        ImportCustomerDto valid = ImportCustomerDto.builder()
                .name("QAImport-" + RandomDataUtils.uuidSuffix())
                .email(RandomDataUtils.uniqueEmail())
                .build();
        // Missing the required "name" field - should not silently succeed like `valid` does.
        ImportCustomerDto invalid = ImportCustomerDto.builder()
                .email(RandomDataUtils.uniqueEmail())
                .build();

        ImportCustomersDto body = ImportCustomersDto.builder()
                .customers(List.of(valid, invalid))
                .build();

        BulkImportResultResponse result = CustomerClient.importCustomers(authSpec(), body)
                .then().statusCode(201)
                .extract().as(BulkImportResultResponse.class);

        boolean validWasCreated = result.getCreated().stream()
                .anyMatch(c -> valid.getName().equals(c.getName()));
        Assertions.assertTrue(validWasCreated, "The valid customer in the batch should have been created");

        int failedOrSkipped = result.getFailed().size() + result.getSkipped().size();
        Assertions.assertTrue(failedOrSkipped >= 1,
                "The customer missing a required name should have been reported as failed/skipped, "
                        + "not silently created");
    }

    @Test
    @DisplayName("POST /customers without a bearer token is rejected with 401")
    void shouldRejectCreateWithoutAuth() {
        CreateCustomerDto body = CreateCustomerDto.builder().name(RandomDataUtils.fullName()).build();

        CustomerClient.create(RequestSpecs.anonymous(), body)
                .then().statusCode(401);
    }

    @Test
    @DisplayName("GET /customers without a bearer token is rejected with 401")
    void shouldRejectListWithoutAuth() {
        CustomerClient.list(RequestSpecs.anonymous())
                .then().statusCode(401);
    }

    @Test
    @DisplayName("POST /customers without a name is rejected with 400")
    void shouldRejectCreateMissingName() {
        CreateCustomerDto body = CreateCustomerDto.builder().email(RandomDataUtils.uniqueEmail()).build();

        CustomerClient.create(authSpec(), body)
                .then().statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("statusCode", equalTo(400));
    }
}
