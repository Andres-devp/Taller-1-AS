param(
  [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"
$curlCommand = if ($env:OS -eq "Windows_NT") { "curl.exe" } else { "curl" }

function Invoke-Login([string]$username, [string]$password) {
  $payload = @{ username = $username; password = $password } | ConvertTo-Json -Compress
  return ((& $curlCommand -s -w "`n%{http_code}" -H "Content-Type: application/json" `
    -d $payload "$BaseUrl/api/auth/login") -join "`n")
}

function Assert-Response([string]$response, [int]$status, [bool]$authenticated) {
  $parts = $response -split "`n"
  $actualStatus = [int]$parts[-1]
  $body = ($parts[0..($parts.Length - 2)] -join "`n")
  if ($actualStatus -ne $status -or $body -notmatch ('"authenticated"\s*:\s*' + $authenticated.ToString().ToLower())) {
    throw "Respuesta inesperada. Estado=$actualStatus Cuerpo=$body"
  }
}

Assert-Response (Invoke-Login "student" "laboratory-password") 200 $true
Assert-Response (Invoke-Login "unknown" "anything") 401 $false
Assert-Response (Invoke-Login "student" "wrong-password") 401 $false
Write-Host "Smoke tests correctos."
