.DEFAULT_GOAL := help

.PHONY: help cluster deploy test smoke availability clean

help:
	@echo "make cluster       Crear Kind de un nodo e instalar ingress-nginx"
	@echo "make deploy        Construir imagenes y desplegar con Helm"
	@echo "make test          Ejecutar pruebas backend y frontend"
	@echo "make smoke         Ejecutar pruebas HTTP del despliegue"
	@echo "make availability  Simular fallos de API y H2"
	@echo "make clean         Eliminar el cluster Kind"

cluster:
	powershell -ExecutionPolicy Bypass -File scripts/cluster.ps1

deploy:
	powershell -ExecutionPolicy Bypass -File scripts/deploy.ps1

test:
	mvn -B -f backend/pom.xml test
	npm --prefix frontend ci
	npm --prefix frontend test

smoke:
	powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1

availability:
	powershell -ExecutionPolicy Bypass -File scripts/availability-tests.ps1

clean:
	kind delete cluster --name auth-lab
