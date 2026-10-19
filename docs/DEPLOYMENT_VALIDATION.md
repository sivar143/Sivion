# Sivion deployment validation

This checklist is the release gate for local development, production Compose, and future cloud or dedicated-hardware deployment. A build or `docker compose config` success is not evidence that the running system is healthy.

## Required checks

| Check | Evidence required | Current status at time of writing |
| --- | --- | --- |
| Backend tests | `cd backend && mvn -B verify` | Last observed CI run passed |
| Frontend unit tests | `cd frontend && npm test` | Failing: Supply Chain dialog tests; investigate the next CI run after the test adjustment |
| Frontend production build | `cd frontend && npm run build` | Previously skipped when unit tests failed; CI now attempts the build independently |
| Local Compose configuration and infrastructure health | `docker compose -f docker-compose.local.yml config`, start MySQL/Valkey/RabbitMQ/Keycloak, wait for health/readiness, and verify each dependency | Configuration validated; live health check not yet automated |
| Independently launched frontend/backend | Start infrastructure with local Compose; run backend using `mvn spring-boot:run -Dspring-boot.run.profiles=local`; run frontend using `npm start`; check readiness and API requests | Manual steps documented in README; automated smoke test not yet implemented |
| Production Compose security | Validate configuration and reject Keycloak `start-dev` or development HTTP settings | **Blocked**: current `docker-compose.yml` still uses Keycloak `start-dev`; configure production hostname, TLS/HTTPS, external secrets and secure database settings before this gate can pass |
| Production image startup and health | Start clean images on Linux; wait for backend/frontend and dependency health checks; probe HTTP endpoints; collect logs on failure | Image build is covered; full-stack startup/health smoke test not yet implemented |
| Authentication, migrations, core APIs, critical UI flows | Integration tests using disposable dependencies and a test identity provider; cover login/roles, schema migration, core business APIs and critical user journeys | Backend unit/integration tests run, but end-to-end acceptance coverage is not yet demonstrated |
| Restart and persistence | Write representative test data, restart services without deleting volumes, and verify data survives | Not yet automated |
| Backup restoration | Create a database backup, restore to a fresh disposable database/volume, and verify schema and representative records | Not yet automated |

## Local validation commands

Run from the repository root:

```bash
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps
```

Wait until MySQL, Valkey, and RabbitMQ report healthy. Keycloak has no Compose health check in the current local file, so verify its readiness endpoint separately before launching the applications.

Then, in separate terminals:

```bash
cd backend
mvn -B verify
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
cd frontend
npm install
npm test
npm run build
npm start
```

The backend defaults to port 8080 and Angular development server defaults to port 4200. Confirm the backend's actuator health endpoint and the frontend's HTTP response; an open port alone is not sufficient.

## Production-readiness rules

- Do not deploy with Keycloak `start-dev`, default credentials, or development HTTP settings.
- Use deployment-managed secrets; never commit production passwords, signing secrets, or private keys.
- Use explicit schema migrations for production and disable Hibernate schema auto-update for production deployments.
- Define health checks for backend, frontend, database, cache, message broker, and identity provider; use readiness dependencies instead of startup ordering alone.
- Keep durable application documents and database files on persistent volumes, and test backup restoration rather than only backup creation.
- Test the production image on a clean Linux runner with no developer-local state. Record image digest, test output, logs on failure, and the tested commit.
- Do not mark a release as deployment-ready while any required check is unverified or blocked.

## Known limitations

The current CI pipeline does not yet prove all checks above. In particular, production Compose currently configures Keycloak in development mode, and live-stack smoke tests, authentication/UI end-to-end tests, restart/persistence verification, and backup-restore tests remain outstanding. These are release blockers, not assumed successes.
