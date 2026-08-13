# Laboratorio 3-tier de autenticación

Aplicación académica que sólo valida credenciales existentes. No incluye registro, recuperación, roles, sesiones, JWT ni ninguna funcionalidad posterior al login.

## Arquitectura

`Ingress → React/Nginx → Service → Auth API x2 → UserRepository → H2 Primary / H2 Secondary`

El API devuelve `200 {"authenticated":true}` para credenciales válidas, el mismo `401 {"authenticated":false}` para usuario inexistente o contraseña incorrecta, y `503` si las dos H2 están indisponibles.

La cuenta de demostración es `student` / `laboratory-password`. La contraseña se guarda exclusivamente como hash BCrypt en `database/seed.sql`.

## Prerrequisitos

- Java 17 y Maven 3.9+
- Node.js 20+
- Docker Desktop en ejecución
- Kind, kubectl y Helm 3
- GNU Make y PowerShell

## Desarrollo y pruebas

```powershell
mvn -B -f backend/pom.xml test
npm ci --prefix frontend
npm test --prefix frontend
npm run build --prefix frontend
```

`make test` ejecuta las pruebas backend y frontend una vez instaladas las dependencias npm.

## Despliegue local con Kind y Helm

```powershell
make cluster
make deploy
make smoke
```

`make cluster` crea exactamente un nodo Kind e instala ingress-nginx. `make deploy` construye las tres imágenes locales, las carga en Kind y ejecuta Helm con `--atomic --wait`. Abra [http://localhost:8080](http://localhost:8080).

Los valores configurables están en `helm/auth-lab/values.yaml`: imágenes, réplicas, H2 Primary/Secondary, probes, recursos e ingress. La contraseña predeterminada es sólo una credencial sintética de laboratorio; para otro entorno use un Secret existente mediante `database.credentials.existingSecret`.

## Simular disponibilidad y recuperación

Ejecute todos los casos con:

```powershell
make availability
```

El script demuestra login normal, eliminación de una API, caída de Primary con Secondary operativa, caída de Secondary con Primary operativa, respuesta `503` sin ambas bases y recuperación al restaurar Primary. Para observar recursos:

```powershell
kubectl get pods
kubectl get ingress
```

También se pueden ejecutar manualmente, por ejemplo:

```powershell
kubectl scale deployment/auth-lab-h2-primary --replicas=0
make smoke
kubectl scale deployment/auth-lab-h2-primary --replicas=1
```

No se debe usar `make smoke` cuando ambas H2 están detenidas: ese caso se verifica con `make availability`, que espera HTTP 503.

## Rollback y limpieza

Helm revierte automáticamente una actualización que no alcanza estado Ready gracias a `--atomic`. Para volver a una revisión confirmada:

```powershell
helm history auth-lab
helm rollback auth-lab <revision> --wait
```

Elimine el laboratorio con `make clean`.

## Limitaciones del laboratorio

El único nodo Kind permite demostrar redundancia de APIs y failover de H2, no una caída de nodo ni un SLA verificable de 99.99%. H2 Primary y Secondary tienen la misma semilla inmutable, no replicación de producción. HTTPS y rate limiting se dejan fuera deliberadamente.

Consulte [docs/architecture.md](docs/architecture.md) para las tácticas de calidad y el diagrama.
