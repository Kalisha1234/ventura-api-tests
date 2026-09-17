package com.ventura.api.core;

import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Parent for test classes whose endpoints all require a bearer token. Automatically
 * skips (via {@link AuthAvailableCondition}) instead of failing when no credentials are
 * configured, so {@code mvn test} stays green until auth is wired up.
 */
@ExtendWith(AuthAvailableCondition.class)
public abstract class BaseAuthenticatedTest extends BaseTest {

    protected RequestSpecification authSpec() {
        return RequestSpecs.authenticated();
    }
}
