# Sivion

Sivion is a modular B2B business management platform combining CRM, product catalog, inventory, sales orders, procurement, fulfillment, audit, and reporting capabilities.

## Architecture

- Angular web application
- Java 25 / Spring Boot backend
- MySQL 8.4
- Redis 8
- RabbitMQ 4
- Keycloak for identity and RBAC
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
