# Sivion

Sivion is a modular B2B business management platform combining CRM, product catalog, inventory, sales orders, procurement, fulfillment, audit, and reporting capabilities.

## Architecture

- Angular web application
- Java 25 / Spring Boot backend
- MySQL 8.4
- Valkey 8
- RabbitMQ 4
- Keycloak 26.7.4 for identity and RBAC
- Nginx reverse proxy
- Docker Compose for local development
- Kubernetes-ready deployment manifests

## Initial modules

CRM, catalog, inventory, orders, procurement, notifications, audit, and reporting.

## Local development

See `docs/getting-started.md`.

## Project principles

- Tenant isolation from the beginning
- API-first design
- Immutable inventory ledger
- Explicit order state machine
- Transactional writes with domain events
- Auditable business operations
- Secure-by-default configuration

Do not use -v if the existing database contains data you need.

docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up --build -d

for local development use this :

docker compose -f docker-compose.local.yml up -d

then:

mysql
valkey
rabbitmq
keycloak

containers will run then:

go to backend:
cd backend

to run backend :
mvn spring-boot:run -Dspring-boot.run.profiles=local for local deployemnts

to verify the backend :
mvn -B verify

to deploy the frontend :
cd frontend

to install all the required frontend liberieries:
npm install

to run the frontend:
npm start