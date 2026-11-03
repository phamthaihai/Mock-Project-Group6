## Mock mode/API Backend mode:

- 'window.CONFIG={USE_MOCK:true,API_BASE:'/api',REQUEST_TIMEOUT:10000};' -> USE_MOCK:false
-  URL: `?mock=true` or `?mock=false`.
  

## Run:
- 1st time or after opening vscode:
  '$env:JAVA_HOME = "D:\JDK21"
  $env:Path = "$env:JAVA_HOME\bin;$env:Path"
  $env:DB_USERNAME = "username"
  $env:SPRING_DATASOURCE_PASSWORD = "password"
  .\mvnw.cmd spring-boot:run'

- From 2nd time:
  '.\mvnw.cmd spring-boot:run'
