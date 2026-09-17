package com.ventura.api.core;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.BeforeAll;

/**
 * Parent for every test class. Sets REST Assured's global defaults exactly once per JVM:
 * only log request/response bodies when a validation actually fails, which keeps normal
 * test output readable while still giving full diagnostics on failure.
 */
public abstract class BaseTest {

    @BeforeAll
    static void setUpRestAssuredDefaults() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.BODY);
    }
}
