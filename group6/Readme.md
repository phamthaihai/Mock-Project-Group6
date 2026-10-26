# LearnHub — Front-end handoff

LearnHub is a course-learning website prototype. This project keeps the existing pages, sample course content, and mock workflows so the front-end can be developed and tested while the backend/database team completes the REST API.

## Front-end scope

- Pages: home/course catalog, course details, learning player, quizzes, checkout, completion/certificate, profile, admin, and login/signup.
- Styling: `src/main/resources/static/css/`
- Page markup: `src/main/resources/static/*.html`
- Front-end logic and API adapter: `src/main/resources/static/js/common.js`
- Runtime API configuration: `src/main/resources/static/js/config.js`
- Mock/sample course data: `src/main/resources/static/js/seed-content.js`
- API contract for backend integration: `database/API_CONTRACT.md`
- SQL Server schema draft: `database/schema.sql`

## Run the current front-end prototype

The static pages are served by Spring Boot from `src/main/resources/static`. With Java and Maven installed, run from this folder:

```bash
./mvnw spring-boot:run
```

On Windows PowerShell/CMD:

```bat
mvnw.cmd spring-boot:run
```

Then open `http://localhost:8080/`.

The front-end currently runs in **mock mode**. Most flows use browser `localStorage` and seeded data; this lets the UI be exercised before the API is implemented. Browser storage is demo persistence only, not authentication or production storage.

## Connect the real backend later

1. Agree on request/response payloads with the backend team using `database/API_CONTRACT.md` as the starting contract.
2. Implement and test the documented endpoints and authentication behavior on the server.
3. Update `src/main/resources/static/js/config.js`:

   ```js
   window.LEARNHUB_CONFIG = Object.freeze({
     USE_MOCK: false,
     API_BASE: '/api',
     REQUEST_TIMEOUT_MS: 15000
   });
   ```

4. Keep API paths and response fields aligned with the contract. If the backend uses a different base path or response envelope, coordinate the change rather than silently adapting only one side.
5. Test success, validation errors, unauthenticated requests, forbidden requests, empty responses (204), and backend/network failures.

`common.js` owns the API adapter. Page code should call `api(path, method, body)` rather than calling `fetch()` directly, so mock and real API modes remain interchangeable.

## Important integration notes

- The backend must enforce authorization and validate all data; front-end route guards are for navigation/UX only.
- Quiz answers must be graded on the server. Do not send `isCorrect` data to the browser before submission.
- Do not treat localStorage values as trusted user identity or role information.
- The checkout flow is a prototype unless a real payment provider is integrated and verified.
- Do not use demo credentials or mock data in production.
- `database/schema.sql` is a development schema script and currently drops/recreates tables. Do not run it against a database containing data you need to keep.
- Database credentials should be supplied through environment variables or local ignored configuration, never committed to source control.

## Quick front-end smoke-test checklist

- [ ] Home page loads and course search/filtering works.
- [ ] Course cards open the correct course details.
- [ ] Signup/login/logout and page guards behave as expected in mock mode.
- [ ] Course enrollment/checkout flow completes and the resulting state is reflected in the profile.
- [ ] Lessons can be opened and marked complete.
- [ ] Quiz submission shows score/review and pass/fail states.
- [ ] Course completion/certificate is shown only when the configured completion rules are satisfied.
- [ ] Profile and admin pages display expected demo data and deny inappropriate navigation.
- [ ] Refreshing a page does not break the expected mock state.
- [ ] When real API mode is enabled, check the browser Network/Console tabs and verify requests against `database/API_CONTRACT.md`.

## Build and tests

Run the existing Java test suite with `./mvnw test` (Windows: `mvnw.cmd test`). The current test is a Spring application-context smoke test; it does not replace the front-end workflow checklist or API integration tests.
