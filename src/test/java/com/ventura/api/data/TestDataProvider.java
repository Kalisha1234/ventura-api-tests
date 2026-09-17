package com.ventura.api.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ventura.api.models.request.CreatePasswordDto;
import com.ventura.api.models.request.SignInPasswordDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads {@code testdata/test-data.json} once and exposes it both as typed DTOs (for the
 * canonical documented scenarios) and as a raw {@link JsonNode} tree (for ad-hoc/negative
 * payloads and enum reference lists), which is the common pattern for data-driven REST
 * Assured suites: static/shared fixtures live in one JSON file, uniqueness-sensitive data
 * is generated at runtime via {@link com.ventura.api.utils.RandomDataUtils}.
 */
public final class TestDataProvider {

    private static final String RESOURCE = "testdata/test-data.json";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonNode ROOT = load();

    private TestDataProvider() {
    }

    private static JsonNode load() {
        try (InputStream in = TestDataProvider.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Missing test data resource on classpath: " + RESOURCE);
            }
            return MAPPER.readTree(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + RESOURCE, e);
        }
    }

    /** Raw access for anything not covered by a typed getter below. */
    public static JsonNode raw() {
        return ROOT;
    }

    /** The seeded dev-environment payload: sets a password for a known passwordless user. */
    public static CreatePasswordDto passwordSet() {
        return convert(ROOT.get("passwordSet"), CreatePasswordDto.class);
    }

    public static SignInPasswordDto invalidSignInWrongPassword() {
        return convert(ROOT.at("/invalidSignIn/wrongPassword"), SignInPasswordDto.class);
    }

    public static SignInPasswordDto invalidSignInUnknownUser() {
        return convert(ROOT.at("/invalidSignIn/unknownUser"), SignInPasswordDto.class);
    }

    public static JsonNode invalidSignUp(String key) {
        return ROOT.at("/invalidSignUp/" + key);
    }

    public static JsonNode invalidPassword(String key) {
        return ROOT.at("/invalidPassword/" + key);
    }

    public static CreatePasswordDto invalidPasswordTooShort() {
        return convert(ROOT.at("/invalidPassword/tooShort"), CreatePasswordDto.class);
    }

    public static List<String> enumValues(String enumName) {
        JsonNode node = ROOT.at("/enums/" + enumName);
        return MAPPER.convertValue(node, new TypeReference<List<String>>() { });
    }

    /** A syntactically well-formed Mongo ObjectId that will never exist -> deterministic 404s. */
    public static String notFoundId() {
        return ROOT.get("notFoundId").asText();
    }

    /** A malformed id -> deterministic 400/validation-error cases. */
    public static String malformedId() {
        return ROOT.get("malformedId").asText();
    }

    private static <T> T convert(JsonNode node, Class<T> type) {
        try {
            return MAPPER.treeToValue(node, type);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert test data node to " + type, e);
        }
    }
}
