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

## Run (SQL)
- 1st time run token-version.sql on existing database (SkillTrail) 


## Admin Account:
- Email: Admin@gmail.com
- Password: 123456