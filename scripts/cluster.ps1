param(
  [string]$ClusterName = "auth-lab"
)

$ErrorActionPreference = "Stop"

foreach ($tool in @("kind", "kubectl", "helm")) {
  if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) {
    throw "Se requiere '$tool' en el PATH. Revise README.md."
  }
}

$root = Split-Path -Parent $PSScriptRoot
kind create cluster --name $ClusterName --config (Join-Path $root "infrastructure/kind-config.yaml")

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm repo update
helm upgrade --install ingress-nginx ingress-nginx/ingress-nginx `
  --namespace ingress-nginx --create-namespace --version 4.11.3 `
  --set controller.service.type=NodePort `
  --set controller.service.nodePorts.http=30080 `
  --wait --timeout 3m

Write-Host "Clúster listo. Ejecute: make deploy"
