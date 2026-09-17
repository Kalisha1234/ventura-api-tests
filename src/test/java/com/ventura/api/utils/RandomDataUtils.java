package com.ventura.api.utils;

import net.datafaker.Faker;

import java.util.UUID;

/**
 * Generates realistic, unique-per-run test data so parallel/repeated test runs never
 * collide on things the API treats as unique (email, etc).
 */
public final class RandomDataUtils {

    private static final Faker FAKER = new Faker();

    private RandomDataUtils() {
    }

    /** Unique email so repeated suite runs never collide on an existing account. */
    public static String uniqueEmail() {
        return "ventura.qa+" + UUID.randomUUID().toString().substring(0, 12) + "@example.com";
    }

    public static String firstName() {
        return FAKER.name().firstName();
    }

    public static String lastName() {
        return FAKER.name().lastName();
    }

    public static String fullName() {
        return FAKER.name().fullName();
    }

    public static String phoneNumber() {
        return FAKER.phoneNumber().phoneNumber();
    }

    public static String companyName() {
        return FAKER.company().name();
    }

    public static String strongPassword() {
        // Must satisfy CreatePasswordDto/UpdatePasswordDto's minLength(12) constraint.
        return "Qa!" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String uuidSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
