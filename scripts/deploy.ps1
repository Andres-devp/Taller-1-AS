param(
  [string]$ClusterName = "auth-lab",
  [string]$ImageTag = "local"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

foreach ($tool in @("docker", "kind", "kubectl", "helm")) {
  if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
    throw "Se requiere '$tool' en el PATH. Revise README.md."
  }
}

Push-Location $root
try {
  docker build -t "auth-lab-backend:$ImageTag" backend
  docker build -t "auth-lab-frontend:$ImageTag" frontend
  docker build -t "auth-lab-h2:$ImageTag" database
  kind load docker-image --name $ClusterName "auth-lab-backend:$ImageTag" "auth-lab-frontend:$ImageTag" "auth-lab-h2:$ImageTag"

  helm upgrade --install auth-lab helm/auth-lab `
    --set "backend.image.tag=$ImageTag" `
    --set "frontend.image.tag=$ImageTag" `
    --set "database.image.tag=$ImageTag" `
    --atomic --wait --timeout 3m
} finally {
  Pop-Location
}

Write-Host "Despliegue listo en http://localhost:8080"
