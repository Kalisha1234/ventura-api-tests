package com.ventura.api.core;

/**
 * Path constants mirrored from the Ventura API OpenAPI spec
 * (https://dev.ventura.csniico.com/api/docs). Keeping them here means a path
 * only ever needs to change in one place.
 */
public final class Endpoints {

    private Endpoints() {
    }

    // Users
    public static final String USERS_EMAIL = "/users/email";
    public static final String USERS_GOOGLE = "/users/google";
    public static final String USERS_LINK_GOOGLE = "/users/link-google";
    public static final String USERS_PASSWORD = "/users/password";
    public static final String USER_HAS_PASSWORD = "/users/{id}/has-password";
    public static final String USER_BY_ID = "/users/{id}";
    public static final String USER_PROFILE = "/users/{id}/profile";
    public static final String USER_FIRST_NAME = "/users/{id}/first-name";
    public static final String USER_LAST_NAME = "/users/{id}/last-name";
    public static final String USER_EMAIL_CHANGE = "/users/{id}/email";
    public static final String USER_EMAIL_CHANGE_CONFIRM = "/users/{id}/email/confirm";
    public static final String USER_AVATAR = "/users/{id}/avatar";
    public static final String USER_BUSINESS = "/users/{id}/business";

    // Files
    public static final String FILES_PRESIGN = "/files/presign";
    public static final String FILES = "/files";

    // Admin
    public static final String ADMIN_PROFILE = "/admin/profile";
    public static final String ADMIN_PROFILE_BY_ID = "/admin/profile/{id}";
    public static final String ADMIN_USERS = "/admin/users";
    public static final String ADMIN_USER_BY_ID = "/admin/users/{id}";
    public static final String ADMIN_USER_RESTORE = "/admin/users/{id}/restore";
    public static final String ADMIN_USER_PERMANENT = "/admin/users/{id}/permanent";

    // Auth
    public static final String AUTH_SIGN_IN_PASSWORD = "/auth/sign-in-password";
    public static final String AUTH_SIGN_IN_EMAIL = "/auth/sign-in-email";
    public static final String AUTH_VERIFY_CODE = "/auth/verify-code";
    public static final String AUTH_SIGN_IN_GOOGLE = "/auth/sign-in-google";
    public static final String AUTH_SIGN_IN_APPLE = "/auth/sign-in-apple";
    public static final String AUTH_APPLE_CALLBACK = "/auth/apple/callback";
    public static final String AUTH_REFRESH = "/auth/refresh";
    public static final String AUTH_LOGOUT = "/auth/logout";

    // Business
    public static final String BUSINESS_CATEGORIES = "/businesses/categories";
    public static final String BUSINESS_MINE = "/businesses/mine";
    public static final String BUSINESSES = "/businesses";
    public static final String BUSINESS_BY_ID = "/businesses/{id}";

    // Customers
    public static final String CUSTOMERS = "/customers";
    public static final String CUSTOMERS_IMPORT = "/customers/import";
    public static final String CUSTOMER_BY_ID = "/customers/{id}";

    // Appointments
    public static final String APPOINTMENTS = "/appointments";
    public static final String APPOINTMENT_BY_ID = "/appointments/{id}";
    public static final String APPOINTMENT_STATUS = "/appointments/{id}/status";

    // Resources
    public static final String RESOURCES = "/resources";
    public static final String RESOURCE_BY_ID = "/resources/{id}";

    // Orders
    public static final String ORDERS = "/orders";
    public static final String ORDER_BY_ID = "/orders/{id}";
    public static final String ORDER_STATUS = "/orders/{id}/status";

    // Invoices
    public static final String INVOICES = "/invoices";
    public static final String INVOICE_BY_ID = "/invoices/{id}";
    public static final String INVOICE_PAYMENT = "/invoices/{id}/payment";
    public static final String INVOICE_SEND = "/invoices/{id}/send";
    public static final String INVOICE_STATUS = "/invoices/{id}/status";

    // Dashboard / Search / Setup
    public static final String DASHBOARD_SUMMARY = "/dashboard/summary";
    public static final String SEARCH = "/search";
    public static final String SETUP_STATUS = "/setup/status";
}
