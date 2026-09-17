package com.ventura.api.tests.users;

import com.ventura.api.clients.UserClient;
import com.ventura.api.config.ConfigManager;
import com.ventura.api.core.BaseAuthenticatedTest;
import com.ventura.api.data.TestDataProvider;
import com.ventura.api.models.request.CreatePasswordDto;
import com.ventura.api.models.request.UpdatePasswordDto;
import com.ventura.api.models.response.UserResponse;
import com.ventura.api.utils.RandomDataUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Password endpoints, exercised carefully: the bootstrap account's real credential is the
 * one every other authenticated test depends on for the rest of the run, so these tests
 * deliberately avoid ever successfully changing it. Only cases that are guaranteed to be
 * REJECTED (wrong old password, too-short new password, another user's account) are run
 * against the shared bootstrap session.
 */
@DisplayName("Users - password management")
class UserPasswordTests extends BaseAuthenticatedTest {

    @Test
    @DisplayName("PUT /users/password rejects an incorrect oldPassword")
    void shouldRejectUpdatePasswordWithWrongOldPassword() {
        String email = ConfigManager.testUserEmail();
        Assumptions.assumeTrue(email != null, "Requires TEST_USER_EMAIL to be configured (bearer-only setups skip this).");

        UpdatePasswordDto body = UpdatePasswordDto.builder()
                .userId(com.ventura.api.core.TokenProvider.lastSignIn().getUser().getId())
                .email(email)
                .oldPassword("definitely-not-the-real-password!")
                .newPassword(RandomDataUtils.strongPassword())
                .build();

        UserClient.updatePassword(authSpec(), body)
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.equalTo(400),
                        org.hamcrest.Matchers.equalTo(401),
                        org.hamcrest.Matchers.equalTo(403)));
    }

    @Test
    @DisplayName("PUT /users/password rejects a new password shorter than the 12-char minimum")
    void shouldRejectUpdatePasswordWithTooShortNewPassword() {
        String email = ConfigManager.testUserEmail();
        String password = ConfigManager.testUserPassword();
        Assumptions.assumeTrue(email != null && password != null,
                "Requires TEST_USER_EMAIL + TEST_USER_PASSWORD to be configured (bearer-only setups skip this).");

        UpdatePasswordDto body = UpdatePasswordDto.builder()
                .userId(com.ventura.api.core.TokenProvider.lastSignIn().getUser().getId())
                .email(email)
                .oldPassword(password)
                .newPassword("short1!")
                .build();

        UserClient.updatePassword(authSpec(), body)
                .then().statusCode(400);
    }

    @Test
    @DisplayName("POST /users/password (create) rejects a new password shorter than the 12-char minimum")
    void shouldRejectCreatePasswordWithTooShortNewPassword() {
        UserClient.createPassword(authSpec(), TestDataProvider.invalidPasswordTooShort())
                .then().statusCode(400);
    }

    @Test
    @DisplayName("POST /users/password (create), using our token, must not be able to set " +
            "a password on a different account (IDOR guard)")
    void shouldNotAllowCreatingPasswordForAnotherUsersAccount() {
        CreatePasswordDto seeded = TestDataProvider.passwordSet();
        UserResponse self = com.ventura.api.core.TokenProvider.lastSignIn().getUser();

        Assumptions.assumeTrue(!seeded.getUserId().equals(self.getId()),
                "Seeded userId happens to equal the bootstrap account id in this environment - skipping IDOR check.");

        int status = UserClient.createPassword(authSpec(), seeded).statusCode();

        org.junit.jupiter.api.Assertions.assertTrue(status == 400 || status == 403 || status == 404 || status == 409,
                "Setting another user's password succeeded (HTTP " + status + ") - likely an IDOR vulnerability.");
    }
}
