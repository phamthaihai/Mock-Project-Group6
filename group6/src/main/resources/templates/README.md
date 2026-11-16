## Mock mode/API Backend mode:

- 'window.CONFIG={USE_MOCK:true,API_BASE:'/api',REQUEST_TIMEOUT:10000};' -> USE_MOCK:false
-  URL: `?mock=true` or `?mock=false`.
  

## Configuration
Settings are read from environment variables (names listed in `.env.example`; Spring does **not** load a `.env` file by itself, so set them in the terminal or in your IDE run configuration).

| Variable | Needed | Meaning |
|---|---|---|
| `SKILLTRAIL_DB_URL` | no | SQL Server URL (default: `localhost:1433`, database `SkillTrail`) |
| `SKILLTRAIL_DB_USERNAME` | no | DB user (default `sa`) |
| `SKILLTRAIL_DB_PASSWORD` | **yes** | DB password (no default) |
| `SKILLTRAIL_MAIL_USERNAME` / `SKILLTRAIL_MAIL_PASSWORD` | for email codes | Gmail address and app password |
| `SKILLTRAIL_JWT_SECRET` | recommended | Token signing secret, at least 32 characters. If missing, a random one is used for that run (everyone is logged out after a restart); if too short, the app refuses to start |
| `SKILLTRAIL_GOOGLE_CLIENT_ID` | no | Google sign-in client id |

Passwords are stored as BCrypt hashes. Signup, change password, forgot password and unbinding Google all use the same rule (6-64 characters, no spaces, at least one letter and one number). Changing a password signs out every other device.

## Run (PowerShell):
- 1st time or after reopening VS Code:
  ```
  $env:JAVA_HOME = "D:\JDK21"
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
  $env:SKILLTRAIL_DB_USERNAME = "sa"
  $env:SKILLTRAIL_DB_PASSWORD = "your-db-password"
  $env:SKILLTRAIL_JWT_SECRET = "at-least-32-random-characters-here"
  .\mvnw.cmd spring-boot:run
  ```
- From the 2nd time in the same terminal: `.\mvnw.cmd spring-boot:run`

## New features
- **Shopping cart** (`cart.html`): "Add to cart" on paid course pages, cart icon with count in the navbar, one checkout for several courses (card / e-wallet / bank / demo). Not shown for ADMIN accounts, who enroll without payment.
- **Roles**: STUDENT and ADMIN are stored accounts; GUEST is any visitor who is not logged in (see `database/ROLES.md`, `GET /api/roles`). The instructor role and instructor studio were removed; a course's instructor is just the teacher name shown on the course. Admins manage courses, curriculum and quizzes in the admin console.
- Mock-mode endpoints live in `js/common.js`; the contract for the real backend is at the end of `database/API.md` (plus a `CartItems` table in `database/schema.sql`).
- Try it: `student@skilltrail.com` / `123456` (cart), `admin@skilltrail.com` / `admin123` (admin console).


## For backend development
- Same layout as `group6` on main: package `com.mockProject.group6` with `controller`, `entity`, `repository`, `service`, `service/impl` (the Auth classes are still empty).
- Pages are HTML files in `src/main/resources/templates`, served by `PageController` (add a new page's name to its `PAGES` list). `js/` and `css/` stay in `src/main/resources/static`.
- API contract: `database/API.md`, tables: `database/schema.sql`. Set `USE_MOCK:false` in `static/js/config.js` once the API is ready.
