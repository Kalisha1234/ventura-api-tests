package com.ventura.api.tests.search;

import com.ventura.api.clients.SearchClient;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.core.Endpoints;
import com.ventura.api.core.RequestSpecs;
import com.ventura.api.fixtures.TestDataFactory;
import com.ventura.api.models.request.CreateCustomerDto;
import com.ventura.api.models.response.CustomerResponse;
import com.ventura.api.models.response.SearchResultsResponse;
import com.ventura.api.utils.RandomDataUtils;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

/**
 * GET /search - authenticated cross-entity search (customers/resources/orders/invoices/
 * appointments). Skipped automatically when no auth is configured - see
 * {@link com.ventura.api.core.AuthAvailableCondition}.
 */
@DisplayName("Search - authenticated cross-entity search")
class SearchTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("GET /search?q=<name> finds a freshly-created customer by a substring of their name")
    void shouldFindSeededCustomerByNameSubstring() {
        TestDataFactory.ensureBusiness(authSpec());

        String distinctiveName = RandomDataUtils.fullName() + " " + RandomDataUtils.uuidSuffix();
        CreateCustomerDto body = CreateCustomerDto.builder()
                .name(distinctiveName)
                .email(RandomDataUtils.uniqueEmail())
                .phone(RandomDataUtils.phoneNumber())
                .notes("Created by Search test fixture")
                .build();

        CustomerResponse customer = RestAssured.given().spec(authSpec())
                .body(body)
                .when().post(Endpoints.CUSTOMERS)
                .then().statusCode(201)
                .extract().as(CustomerResponse.class);

        // Search on a substring, not the full name, to confirm this is a substring/partial
        // match rather than an exact-match lookup.
        String queryFragment = distinctiveName.substring(0, distinctiveName.length() - 4);

        SearchResultsResponse results = SearchClient.search(authSpec(), queryFragment)
                .then().statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/search-results-response-schema.json"))
                .extract().as(SearchResultsResponse.class);

        Assertions.assertNotNull(results.getCustomers(), "customers list should be present");
        boolean found = results.getCustomers().stream()
                .anyMatch(c -> customer.getId().equals(c.getId()));
        Assertions.assertTrue(found,
                "Expected seeded customer '" + distinctiveName + "' to appear in search results for '"
                        + queryFragment + "'");
    }

    @Test
    @DisplayName("GET /search?q=<random uuid> matching nothing returns every result list empty")
    void shouldReturnAllEmptyListsForUnmatchedQuery() {
        String unmatchable = UUID.randomUUID().toString();

        SearchResultsResponse results = SearchClient.search(authSpec(), unmatchable)
                .then().statusCode(200)
                .extract().as(SearchResultsResponse.class);

        Assertions.assertNotNull(results.getCustomers());
        Assertions.assertNotNull(results.getResources());
        Assertions.assertNotNull(results.getOrders());
        Assertions.assertNotNull(results.getInvoices());
        Assertions.assertNotNull(results.getAppointments());

        Assertions.assertTrue(results.getCustomers().isEmpty(), "customers should be empty for an unmatched query");
        Assertions.assertTrue(results.getResources().isEmpty(), "resources should be empty for an unmatched query");
        Assertions.assertTrue(results.getOrders().isEmpty(), "orders should be empty for an unmatched query");
        Assertions.assertTrue(results.getInvoices().isEmpty(), "invoices should be empty for an unmatched query");
        Assertions.assertTrue(results.getAppointments().isEmpty(),
                "appointments should be empty for an unmatched query");
    }

    @Test
    @DisplayName("GET /search without the required q param is rejected with 400")
    void shouldRejectMissingQueryParam() {
        // Not independently verifiable against the live API without a bearer token (every
        // unauthenticated call short-circuits with 401 before validation runs), but a required
        // query param missing from a Nest/class-validator DTO is expected to fail request
        // validation with 400 rather than be treated as an empty/wildcard search.
        SearchClient.search(authSpec())
                .then().statusCode(400);
    }

    @Test
    @DisplayName("GET /search without a bearer token is rejected with 401")
    void shouldRejectWithoutToken() {
        SearchClient.search(RequestSpecs.anonymous(), "anything")
                .then().statusCode(401);
    }
}
