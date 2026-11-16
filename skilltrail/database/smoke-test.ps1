# Quick check of the running API. Usage: powershell -File database\smoke-test.ps1 [-BaseUrl http://localhost:8080] [-AdminPassword 123456]
param([string]$BaseUrl = "http://localhost:8080", [string]$AdminPassword = "")
$ErrorActionPreference = "Stop"
function Check($name, [scriptblock]$test) {
  try { $r = & $test; if ($r) { Write-Host "OK    $name" -ForegroundColor Green } else { Write-Host "FAIL  $name" -ForegroundColor Red } }
  catch { Write-Host "FAIL  $name  ($($_.Exception.Message))" -ForegroundColor Red }
}
function Api($method, $path, $body, $token) {
  $h = @{}; if ($token) { $h["Authorization"] = "Bearer $token" }
  if ($body) { Invoke-RestMethod -Method $method -Uri "$BaseUrl/api$path" -Headers $h -ContentType "application/json" -Body ($body | ConvertTo-Json -Depth 8) }
  else { Invoke-RestMethod -Method $method -Uri "$BaseUrl/api$path" -Headers $h }
}
function Status($method, $path, $body) {
  try { Api $method $path $body $null | Out-Null; return 200 } catch { return [int]$_.Exception.Response.StatusCode }
}
Check "GET /categories returns 6" { (Api GET "/categories").Count -eq 6 }
Check "GET /courses returns the seeded courses" { (Api GET "/courses").Count -ge 15 }
Check "GET /courses/1 has sections" { (Api GET "/courses/1").sections.Count -gt 0 }
Check "GET /courses/9999 is 404" { (Status GET "/courses/9999") -eq 404 }
Check "login with wrong password is 401" { (Status POST "/auth/login" @{ email = "nobody@example.com"; password = "wrong" }) -eq 401 }
Check "GET /users/me without token is 401" { (Status GET "/users/me") -eq 401 }
Check "register with a bad email is 400" { (Status POST "/auth/register" @{ fullName = "Test User"; email = "bad"; password = "abc123" }) -eq 400 }
if ($AdminPassword) {
  $login = Api POST "/auth/login" @{ email = "admin@gmail.com"; password = $AdminPassword }
  Check "admin login returns a token" { [bool]$login.token }
  Check "GET /admin/stats" { (Api GET "/admin/stats" $null $login.token).courses -ge 15 }
  Check "GET /admin/users" { (Api GET "/admin/users" $null $login.token).Count -ge 1 }
  Check "GET /admin/logs/account" { $null -ne (Api GET "/admin/logs/account" $null $login.token) }
  Check "students cannot call /admin/stats" { (Status GET "/admin/stats") -eq 401 }
}
