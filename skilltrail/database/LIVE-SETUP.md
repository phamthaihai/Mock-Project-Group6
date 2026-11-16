# Running the site on the real backend (Live API mode)

## 1. Database (SQL Server, database `SkillTrail`) - run once, in this order
1. `schema.sql`
2. `seed-courses.sql` (15 courses, instructors, sections, lessons, quizzes)
3. `email-verification.sql`
4. `backend-api.sql`

## 2. Start the app (PowerShell)
```powershell
$env:JAVA_HOME = "D:\JDK21"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:SPRING_DATASOURCE_PASSWORD = "<your sa password>"
$env:SKILLTRAIL_MAIL_USERNAME = "yourname@gmail.com"
$env:SKILLTRAIL_MAIL_PASSWORD = "<16-letter Gmail App Password>"
$env:SKILLTRAIL_JWT_SECRET = "<any long random text, 32+ characters>"
.\mvnw.cmd clean spring-boot:run
```

## 3. Switch the website to Live
Open http://localhost:8080, press the mode button in the navbar until it says **Live API**.
Admin login: `admin@gmail.com` / `123456` (from schema.sql).

## 4. Quick check
`powershell -File database\smoke-test.ps1` (add `-AdminPassword 123456` to also test the admin calls).
