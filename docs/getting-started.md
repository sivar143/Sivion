# Getting started

## Prerequisites

Docker Desktop and Git.

## Start infrastructure

```bash
docker compose up --build
```

Services:
- Web/API: http://localhost:8080
- Keycloak: http://localhost:8081
- RabbitMQ: http://localhost:15672 (sivion / sivion)
- MySQL: localhost:3306 (sivion / sivion)
- Redis: localhost:6379

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
