# Getting started

## Prerequisites

Docker Desktop and Git.

## Start infrastructure

```bash
docker compose -f docker-compose.local.yml up --build -d
```

Services:
- Frontend: http://localhost:4200
- API: http://localhost:8080
- Keycloak: http://localhost:8081 (admin / admin)
- RabbitMQ: http://localhost:15672 (sivion / sivion)
- MySQL: localhost:3307 (database sivion, admin / admin)
- Valkey: localhost:6379

## MySQL host-port mapping

MySQL listens on port `3306` inside Docker and is exposed as `3307` on the host to avoid conflicts with another local MySQL instance. Container-to-container connections must continue to use `mysql:3306`.

You can override the host port if required:

```powershell
$env:MYSQL_HOST_PORT="3308"
docker compose -f docker-compose.local.yml up --build -d
```

## Existing local MySQL volume

The MySQL image creates `MYSQL_USER` and `MYSQL_PASSWORD` only when the data directory is initialized for the first time. If your existing `mysql-data` volume was initialized with the previous `sivion/sivion` credentials, changing Compose variables will not change that existing user.

For a disposable development database, recreate the volume:

```bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up --build -d
```

Do not use `down -v` if the existing database contains data you need to preserve; create the `admin` MySQL account manually first.

## API

Health: `GET /actuator/health`

Ping: `GET /api/v1/ping`

The API is protected by Keycloak except health, OpenAPI, and Swagger endpoints.

## Roadmap

1. Tenant-aware CRM APIs
2. Product/catalog APIs
3. Inventory ledger and reservations
4. Sales order state machine
5. Procurement and goods receipt
6. Notifications and domain events
7. Angular application
8. CI/CD and Kubernetes deployment
