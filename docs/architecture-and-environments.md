# Sivion architecture and environments

## Development strategy

Sivion is developed as independently deployable business modules. Local development runs the complete stack in Docker containers so the same container boundaries can later be mapped to cloud workloads.

The initial implementation may run several modules in one Spring Boot process to keep development practical. Module boundaries must remain explicit: CRM, catalog, inventory, orders, procurement, and shared platform capabilities communicate through APIs and domain events rather than directly depending on another module's implementation classes or database tables.

## Data ownership

MySQL is the system of record. Each business module owns its tables and access layer. Cross-module relationships should use stable identifiers and APIs/events rather than JPA relationships across module boundaries.

Redis is an infrastructure capability for caching, short-lived state, rate limiting, and distributed coordination. It is never the authoritative business data store.

RabbitMQ is used for asynchronous domain/integration events. Consumers must be idempotent and should not make the producer transaction depend on the consumer being available.

## Local environment

Use Docker Compose:

```bash
docker compose -f docker-compose.local.yml up --build
```

Local services:

- Angular/Nginx: `http://localhost:4200`
- API: `http://localhost:8080`
- Keycloak: `http://localhost:8081`
- RabbitMQ management: `http://localhost:15672`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`

Local credentials are intentionally simple and must never be reused in non-local environments.

## Test environment

The test environment will use the same container images as local development, but with environment-specific configuration injected at deployment time. No application source code should contain test credentials or infrastructure addresses.

## Production direction

Production will later move to CI/CD-driven deployments on AWS EC2 and supporting managed/cloud infrastructure. The application containers should remain portable. Environment configuration, secrets, database credentials, Keycloak settings, broker settings, domains, TLS, and scaling parameters must be supplied externally.

Production must not depend on developer laptops, local bind mounts, local credentials, or Docker Compose state.

## Independence rules

1. No module may directly call another module's repository/DAO.
2. No module may depend on another module's entity classes.
3. No module may require another module's database transaction to complete its own transaction.
4. Synchronous cross-module calls must use versioned APIs.
5. Asynchronous integration should use RabbitMQ events where immediate consistency is not required.
6. Failures in optional downstream consumers must not corrupt the source module's transaction.
7. Every module must be testable independently with its own database fixtures/mocks.
8. Shared libraries should contain only genuinely cross-cutting concerns; business logic belongs to the owning module.
9. Every container must receive configuration through environment variables or mounted deployment configuration.
10. MySQL remains the authoritative business datastore.
