param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$Release = "auth-lab"
)

$ErrorActionPreference = "Stop"
$curlCommand = if ($env:OS -eq "Windows_NT") { "curl.exe" } else { "curl" }

function Assert-Login([int]$expectedStatus) {
  $response = ((& $curlCommand -s -w "`n%{http_code}" -H "Content-Type: application/json" `
    -d '{"username":"student","password":"laboratory-password"}' "$BaseUrl/api/auth/login") -join "`n")
  $actualStatus = [int](($response -split "`n")[-1])
  if ($actualStatus -ne $expectedStatus) { throw "Se esperaba HTTP $expectedStatus y se obtuvo $actualStatus" }
}

function Wait-BackendReady {
  kubectl rollout status "deployment/$Release-backend" --timeout=2m
}

Assert-Login 200

# Caso 2: queda una API lista mientras Kubernetes recrea la eliminada.
$apiPod = kubectl get pods -l "app.kubernetes.io/instance=$Release,app.kubernetes.io/component=backend" -o jsonpath='{.items[0].metadata.name}'
kubectl delete pod $apiPod
Assert-Login 200
Wait-BackendReady

# Caso 3: Secondary asume consultas cuando Primary no tiene pods.
kubectl scale "deployment/$Release-h2-primary" --replicas=0
Start-Sleep -Seconds 8
Assert-Login 200
kubectl scale "deployment/$Release-h2-primary" --replicas=1
kubectl rollout status "deployment/$Release-h2-primary" --timeout=2m

# Caso 4: Primary sigue atendiendo con Secondary caída.
kubectl scale "deployment/$Release-h2-secondary" --replicas=0
Start-Sleep -Seconds 8
Assert-Login 200
kubectl scale "deployment/$Release-h2-secondary" --replicas=1
kubectl rollout status "deployment/$Release-h2-secondary" --timeout=2m

# Casos 5 y 6: sin bases la API devuelve 503; al restaurar una base se recupera.
kubectl scale "deployment/$Release-h2-primary" --replicas=0
kubectl scale "deployment/$Release-h2-secondary" --replicas=0
Start-Sleep -Seconds 8
Assert-Login 503
kubectl scale "deployment/$Release-h2-primary" --replicas=1
kubectl rollout status "deployment/$Release-h2-primary" --timeout=2m
Assert-Login 200

# Se deja el laboratorio en su estado nominal.
kubectl scale "deployment/$Release-h2-secondary" --replicas=1
kubectl rollout status "deployment/$Release-h2-secondary" --timeout=2m
Write-Host "Pruebas de disponibilidad correctas."
