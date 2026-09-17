package com.ventura.api.core;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Disables (skips, does not fail) any test class/method that needs a bearer token when
 * {@link TokenProvider} has no credentials configured, so the suite stays green out of
 * the box and lights up authenticated coverage the moment credentials are supplied.
 */
public class AuthAvailableCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        if (TokenProvider.isConfigured()) {
            return ConditionEvaluationResult.enabled("Auth is configured.");
        }
        return ConditionEvaluationResult.disabled(
                "Skipped: no auth configured. Set TEST_BEARER_TOKEN, or TEST_USER_EMAIL + "
                        + "TEST_USER_PASSWORD, as env vars to enable authenticated tests.");
    }
}
