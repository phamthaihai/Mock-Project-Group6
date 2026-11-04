## Mock mode/API Backend mode:

- 'window.CONFIG={USE_MOCK:true,API_BASE:'/api',REQUEST_TIMEOUT:10000};' -> USE_MOCK:false
-  URL: `?mock=true` or `?mock=false`.
  

## Run:
- 1st time or after opening vscode:
  '$env:JAVA_HOME = "D:\JDK21"\
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"\
  $env:DB_USERNAME = "username"\
  $env:SPRING_DATASOURCE_PASSWORD = "password"\
  .\mvnw.cmd spring-boot:run'

- From 2nd time:
  '.\mvnw.cmd spring-boot:run'


## New features
- **Shopping cart** (`cart.html`): "Add to cart" on paid course pages, cart icon with count in the navbar, one checkout for several courses (card / e-wallet / bank / demo). Not shown for ADMIN accounts, who enroll without payment.
- **Roles**: STUDENT and ADMIN only. The instructor role and instructor studio were removed; a course's instructor is just the teacher name shown on the course. Admins manage courses, curriculum and quizzes in the admin console.
- Mock-mode endpoints live in `js/common.js`; the contract for the real backend is at the end of `database/API.md` (plus a `CartItems` table in `database/schema.sql`).
- Try it: `student@learnpath.com` / `123456` (cart), `admin@learnpath.com` / `admin123` (admin console).
