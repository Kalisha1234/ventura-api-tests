# Ventura API test automation

REST Assured + JUnit 5 API automation suite for the Ventura backend
(`https://dev.ventura.csniico.com`, OpenAPI spec at `/api/docs-json`).

## Running

```
mvn test
```

By default this runs against the `dev` environment (`src/test/resources/config-dev.properties`).
Override with `-Denv=staging` once `config-staging.properties` has a real `base.uri` filled in
(only the staging *frontend* URL is currently known, not the API host, so it's left blank on
purpose rather than guessed).

## Auth

Nearly every endpoint besides `POST /users/email` requires a bearer token, and there is no
way for the suite to obtain one on its own (verification codes go to a real inbox this suite
can't read). `TokenProvider` (`core/TokenProvider.java`) resolves a token in this order, and
`BaseAuthenticatedTest`/`AuthAvailableCondition` automatically **skip** (not fail) any
authenticated test class when none of these are configured, so `mvn test` stays green out of
the box:

1. **`TEST_BEARER_TOKEN`** (+ optionally `TEST_REFRESH_TOKEN`) — a pre-issued JWT access token,
   used as-is.
2. **`TEST_USER_EMAIL` + `TEST_USER_PASSWORD`** — credentials for an existing dev-environment
   user that already has a password set; the suite signs in via `POST /auth/sign-in-password`
   once and caches the token for the whole run.

Set these as environment variables (recommended) or `-D` system properties, e.g.:

```
export TEST_USER_EMAIL=someone@ventura.com
export TEST_USER_PASSWORD='the-real-password'
mvn test
```

Once set, every authenticated module (Business, Customers, Appointments, Resources, Orders,
Invoices, Dashboard, Search, Setup) lights up automatically.

Some tests (`AuthSessionTests`, parts of `UserPasswordTests`) specifically need
`TEST_USER_EMAIL`/`TEST_USER_PASSWORD` rather than a raw bearer token, because they sign in
fresh themselves (so that logging out or refreshing doesn't disturb the shared cached token
every other test class relies on) — those individually `Assumptions.assumeTrue(...)`-skip if
only a raw bearer token was supplied.

## Structure

```
src/test/java/com/ventura/api/
  config/    - environment + credential resolution (ConfigManager)
  core/      - Endpoints, RequestSpecs, TokenProvider, Base(Authenticated)Test
  clients/   - one class per API resource (UserClient, OrderClient, ...): each method builds
               and fires a single request and returns the raw Response, no assertions inside
  models/    - request/response POJOs, one per DTO in the OpenAPI spec
  data/      - TestDataProvider, backed by testdata/test-data.json
  utils/     - RandomDataUtils (datafaker-backed, for unique/realistic values)
  fixtures/  - TestDataFactory: creates prerequisite domain entities (business, customer,
               resource, order, invoice, appointment) through the clients above
  tests/     - one package per API module (users, auth, business, customers, resources,
               appointments, orders, invoices, admin, files, dashboard, search, setup)

src/test/resources/
  config-*.properties - per-environment base URI
  testdata/           - static/shared fixture JSON (see below)
  schemas/            - JSON Schema (draft-07) for the main response shape per module,
                        checked via RestAssured's json-schema-validator module
```

**Why a client layer.** Test methods call `UserClient.getById(spec, id)` rather than building
`RestAssured.given().spec(spec)...` inline. The endpoint's path, HTTP verb, and request-body
wiring live in exactly one place per operation; if a path or payload shape ever changes,
only the client method needs to change; every test that calls it keeps working. Client methods
return the raw REST Assured `Response` (not something pre-asserted), so the test still owns
every assertion via the normal `.then().statusCode(...)...` chain — the client only owns "how
to call it", never "what the correct answer looks like".

**Why JSON Schema validation.** Field-by-field `.body("x", equalTo(y))` checks confirm the
*values* you expect are there, but say nothing about the overall shape (extra/missing fields,
wrong types elsewhere in the payload). Each module's main happy-path test additionally asserts
`.body(matchesJsonSchemaInClasspath("schemas/<module>-response-schema.json"))` against a
schema hand-derived from that response POJO and cross-checked against the live OpenAPI spec —
a coarser, complementary check that the *whole* response still matches the documented contract.
Schemas are self-contained (no cross-file `$ref`s, to avoid classpath-resolution complexity) and
only cover the one or two main response shapes per module, not every nested type.

Test data philosophy: static/shared fixtures (the seeded password-set payload, invalid
payloads, enum reference lists, deterministic not-found/malformed ids) live in
`testdata/test-data.json` via `TestDataProvider`; anything uniqueness-sensitive (emails, names)
is generated at runtime via `RandomDataUtils` so repeated runs never collide.

## Known findings surfaced while building this

- **⚠️ `/admin/*` and `/files/*` endpoints require no authentication at all**, confirmed live
  against `https://dev.ventura.csniico.com` with zero `Authorization` header — not just an
  OpenAPI documentation gap. `GET /admin/users` returns the full real user list (ids, names,
  emails, verification/active/deleted flags, timestamps) to anyone. `POST/GET/PATCH
  /admin/profile[/id]`, `GET/DELETE /admin/users/{id}`, `.../restore`, and
  `.../permanent` (hard delete) are all reachable and mutable anonymously, as is
  `POST /files/presign` (mints a real, working S3 presigned upload URL into
  `csniico-ventura-bucket` for anyone) and `DELETE /files`. Every other module (Business,
  Customers, Appointments, Resources, Orders, Invoices, Users self-service, Dashboard,
  Search, Setup) is 100% bearer-gated, which makes this look like an unintentional gap rather
  than a deliberate design choice — worth escalating and confirming with the team rather than
  treating as expected. See `AdminTests`/`FilesTests` class-level Javadoc.
- **`GET /users/{id}/has-password` returns `500 Internal server error`** for a syntactically
  invalid id instead of a `400`/`404`. Covered by
  `UserSignUpTests.hasPasswordWithMalformedId_shouldNotReturnServerError`, which currently
  fails against the real API — that's intentional, it's documenting the bug, not a broken test.
- **`GET /admin/profile/{id}` returns `500`** (instead of `400`/`404`) when given a
  Mongo-ObjectId-shaped id, because admin/user records are actually keyed by UUID. Covered
  (as a passing, documented-behavior assertion) by
  `AdminTests.knownFinding_getAdminProfileWithWrongIdShape_returns500`.
- `POST /users/password` (and `PUT /users/password`) genuinely require a bearer token as
  documented — confirmed live (`401` when called anonymously with the seeded
  `userId`/`email`/`newPassword` test data), which is why that data alone can't bootstrap auth.

## Module coverage

| Module | Test classes | Notes |
|---|---|---|
| Users | `UserSignUpTests`, `UserAuthenticatedTests`, `UserPasswordTests` | sign-up is the only public mutation in the whole API |
| Auth | `AuthPublicTests`, `AuthSessionTests` | session tests sign in fresh, never reuse the shared cached token |
| Business | `BusinessTests` | fully bearer-gated |
| Customers | `CustomerTests` | fully bearer-gated, incl. bulk import |
| Resources | `ResourceTests` | fully bearer-gated |
| Appointments | `AppointmentTests` | fully bearer-gated |
| Orders | `OrderTests` | fully bearer-gated |
| Invoices | `InvoiceTests` | fully bearer-gated |
| Dashboard | `DashboardTests` | fully bearer-gated |
| Search | `SearchTests` | fully bearer-gated |
| Setup | `SetupTests` | fully bearer-gated |
| Admin | `AdminTests` | **no auth required (see finding above)** |
| Files | `FilesTests` | **no auth required (see finding above)** |

123 tests total. With no auth configured, 97 skip cleanly (everything gated behind a bearer
token) and the rest run against the live dev API, with exactly one expected failure (the
`has-password` 500 finding above). Configure `TEST_USER_EMAIL`/`TEST_USER_PASSWORD` to light
up the remaining 97.
