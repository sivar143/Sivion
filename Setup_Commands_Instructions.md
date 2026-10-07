# Sivion Setup, Run and Deployment Instructions

This document is the single reference for setting up, validating, running, stopping, rebuilding, and troubleshooting the Sivion project.

## 1. Project overview

Sivion is a modular B2B business management platform with:

- Angular 22 frontend
- Java 25 / Spring Boot 4 backend
- MySQL 8.4
- Valkey 8
- RabbitMQ 4
- Keycloak 26.7.4
- Nginx serving the frontend container
- Docker Compose for local and deployment-style environments
- Kubernetes-ready application architecture

The repository contains the frontend, backend, database initialization, infrastructure configuration, and Docker deployment configuration.

---

## 2. Required software

### Windows 11

Recommended/current project toolchain:

| Component | Version |
|---|---:|
| Git | Current stable |
| Docker Desktop | Current stable |
| Docker Compose | v2 / Docker Compose plugin |
| Node.js | 24.x |
| npm | 11.x |
| Java / JDK | 25 |
| Maven | 3.9.x |

The project is containerized, so Docker Desktop is the primary requirement for running the complete application stack.

### Linux

Install:

- Git
- Docker Engine
- Docker Compose plugin
- Java 25 JDK and Maven 3.9.x if running backend tests/builds outside Docker
- Node.js 24.x and npm 11.x if running the Angular application/build outside Docker

---

## 3. Get the repository

Clone the repository:

```bash
git clone https://github.com/sivar143/Sivion.git
cd Sivion
```

Check the current branch and working tree:

```bash
git branch --show-current
git status
```

For the repository-hardening/stabilization work, use:

```bash
git checkout stabilization/repository-hardening-2026-09-30
git pull origin stabilization/repository-hardening-2026-09-30
```

For the normal released/default project state, use the repository's `main` branch:

```bash
git checkout main
git pull origin main
```

---

## 4. Verify prerequisites

### Git

```bash
git --version
```

### Docker

```bash
docker --version
docker compose version
docker info
```

Docker Desktop must be running before using Docker Compose.

### Java

```bash
java -version
```

The backend targets Java 25.

### Maven

```bash
mvn -version
```

Maven should report Java 25 when the backend is built locally.

### Node.js and npm

```bash
node --version
npm --version
```

The frontend uses Node.js 24.x and npm 11.x.

---

## 5. Recommended fast local development setup

The local Compose file intentionally starts **only infrastructure**. It does not start or build the backend/frontend containers.

### Docker-managed services

- MySQL 8.4
- Valkey 8
- RabbitMQ 4
- Keycloak 26.7.4

### Manually managed services

- Spring Boot backend on port 8080
- Angular development server on port 4200

This means you can change Java or Angular code without rebuilding the entire Docker stack.

### Step 1 — Start infrastructure once

From the repository root:

~~~bash
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps
~~~

Expected local Compose services:

~~~text
mysql
valkey
rabbitmq
keycloak
~~~

You should not see backend or frontend.

Leave these infrastructure containers running during development.

### Step 2 — Start the backend manually

Open a second terminal:

~~~bash
cd backend
mvn -B verify
mvn spring-boot:run
~~~

The backend is available at:

~~~text
http://localhost:8080
~~~

Health check:

~~~text
http://localhost:8080/actuator/health
~~~

On Windows PowerShell:

~~~powershell
Invoke-WebRequest http://localhost:8080/actuator/health
~~~

The backend connects to host-exposed infrastructure:

~~~text
MySQL:    localhost:3307
Valkey:   localhost:6379
RabbitMQ: localhost:5672
Keycloak: localhost:8081
~~~

### Step 3 — Start the frontend manually

Open a third terminal:

~~~bash
cd frontend
npm install
npm start
~~~

Open:

~~~text
http://localhost:4200
~~~

Angular is already configured to proxy /api to:

~~~text
http://localhost:8080
~~~

### Step 4 — Develop normally

For frontend-only changes, Angular automatically rebuilds/reloads.

For backend-only changes, stop the Spring Boot process with Ctrl+C and run:

~~~bash
mvn spring-boot:run
~~~

There is no Docker image rebuild and no need to restart MySQL, Valkey, RabbitMQ, or Keycloak.

### Step 5 — Stop development

Stop the backend and frontend with Ctrl+C.

Stop infrastructure only when you are finished:

~~~bash
docker compose -f docker-compose.local.yml down
~~~

---

## 6. Local application and infrastructure endpoints

| Service | Address | Default credentials |
|---|---|---|
| Angular frontend | http://localhost:4200 | Keycloak login |
| Spring Boot backend | http://localhost:8080 | Keycloak protected |
| Backend health | http://localhost:8080/actuator/health | Public health endpoint |
| API ping | http://localhost:8080/api/v1/ping | Public endpoint |
| Keycloak | http://localhost:8081 | admin / admin |
| Keycloak realm | http://localhost:8081/realms/sivion | — |
| RabbitMQ management | http://localhost:15672 | sivion / sivion |
| MySQL | localhost:3307 | admin / admin |
| Valkey | localhost:6379 | — |

The local MySQL port is 3307, not 3306.

---

## 7. Daily fast-development commands

### Start infrastructure

~~~bash
docker compose -f docker-compose.local.yml up -d
~~~

### Check infrastructure

~~~bash
docker compose -f docker-compose.local.yml ps
~~~

### Backend

~~~bash
cd backend
mvn spring-boot:run
~~~

### Backend tests

~~~bash
cd backend
mvn -B verify
~~~

### Frontend

~~~bash
cd frontend
npm start
~~~

### Frontend production build check

~~~bash
cd frontend
npm run build
~~~

### Infrastructure logs

~~~bash
docker compose -f docker-compose.local.yml logs -f
~~~

Backend and frontend logs are printed directly by Maven/Spring Boot and Angular CLI.

---

## 8. What must be restarted after a change

| Change | Restart required |
|---|---|
| Angular .ts, .html, .css | Normally no; Angular reloads automatically |
| Angular dependency/package change | Stop/start npm start; run npm install |
| Java/Spring source change | Restart only backend |
| Backend Maven configuration change | Restart backend; run Maven verification if needed |
| Database data change | Usually restart neither application nor infrastructure |
| Database initialization from scratch | Reset MySQL volume |
| Keycloak realm/config change | Restart only Keycloak |
| RabbitMQ configuration change | Restart only RabbitMQ |
| Valkey configuration change | Restart only Valkey |

---

## 9. Manual backend configuration

When running the backend directly on the host, use host addresses rather than Docker service names.

### Windows PowerShell

~~~powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/sivion"
$env:SPRING_DATASOURCE_USERNAME="admin"
$env:SPRING_DATASOURCE_PASSWORD="admin"
$env:SPRING_DATA_REDIS_HOST="localhost"
$env:SPRING_RABBITMQ_HOST="localhost"
$env:SPRING_RABBITMQ_USERNAME="sivion"
$env:SPRING_RABBITMQ_PASSWORD="sivion"
$env:SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://localhost:8081/realms/sivion"
$env:SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI="http://localhost:8081/realms/sivion/protocol/openid-connect/certs"
mvn spring-boot:run
~~~

### Linux / macOS / Git Bash

~~~bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/sivion"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="admin"
export SPRING_DATA_REDIS_HOST="localhost"
export SPRING_RABBITMQ_HOST="localhost"
export SPRING_RABBITMQ_USERNAME="sivion"
export SPRING_RABBITMQ_PASSWORD="sivion"
export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://localhost:8081/realms/sivion"
export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI="http://localhost:8081/realms/sivion/protocol/openid-connect/certs"
mvn spring-boot:run
~~~

These variables apply only to the current terminal session.

---

## 10. Manual frontend configuration

The Angular development server is configured to use:

~~~text
frontend/proxy.conf.json
~~~

The proxy sends:

~~~text
/api/* -> http://localhost:8080
~~~

Normal local flow:

~~~text
Browser :4200
    |
    | /api
    v
Angular proxy
    |
    v
Spring Boot :8080
    |
    +--> MySQL :3307
    +--> Valkey :6379
    +--> RabbitMQ :5672
    +--> Keycloak :8081
~~~

Do not change the local proxy target to backend:8080. backend is a Docker service name and the backend now runs directly on the host.

---

## 11. Build/package the backend manually

For tests and validation:

~~~bash
cd backend
mvn -B verify
~~~

For a packaged JAR:

~~~bash
mvn -B clean package
~~~

The output is under backend/target/.

To run the packaged JAR:

~~~bash
java -jar target/<generated-sivion-api-jar>.jar
~~~

For normal development, prefer mvn spring-boot:run because it avoids building a JAR for every iteration.

---

## 12. Reset local database data

The MySQL data is stored in the Docker volume mysql-data.

To completely reset local MySQL:

~~~bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up -d
~~~

Warning: down -v deletes the local MySQL data.

After MySQL becomes healthy, restart the manually managed backend:

~~~bash
cd backend
mvn spring-boot:run
~~~

The initialization script is database/init.sql.

---

## 13. Local versus deployment Compose files

### Local development

~~~bash
docker compose -f docker-compose.local.yml up -d
~~~

This starts only:

- MySQL
- Valkey
- RabbitMQ
- Keycloak

Backend and frontend are started manually.

### Deployment/default stack

~~~bash
docker compose -f docker-compose.yml config
docker compose -f docker-compose.yml up -d --build
~~~

The deployment/default Compose file still contains:

- MySQL
- Valkey
- RabbitMQ
- Keycloak
- Backend
- Frontend

Do not remove backend/frontend from docker-compose.yml for this local optimization.

---

## 14. Keycloak configuration

Keycloak is started with the Sivion realm import:

```text
infrastructure/keycloak
```

The Compose configuration mounts this directory into Keycloak's import directory and starts Keycloak with:

```text
start-dev --import-realm
```

The backend uses:

- Browser-visible issuer: `http://localhost:8081/realms/sivion`
- Internal Docker-network JWK endpoint: `http://keycloak:8080/realms/sivion/protocol/openid-connect/certs`

If Keycloak authentication fails, first check:

```bash
docker compose -f docker-compose.local.yml logs keycloak
```

Then verify:

```text
http://localhost:8081/realms/sivion
```

---

## 15. RabbitMQ

RabbitMQ is available at:

```text
AMQP:       localhost:5672
Management: http://localhost:15672
Username:   sivion
Password:   sivion
```

Check the container:

```bash
docker compose -f docker-compose.local.yml ps rabbitmq
```

Check logs:

```bash
docker compose -f docker-compose.local.yml logs -f rabbitmq
```

The backend connects to RabbitMQ using the Docker service name `rabbitmq`, not `localhost`.

---

## 16. MySQL

Default development connection:

```text
Host:     localhost
Port:     3306
Database: sivion
Username: sivion
Password: sivion
```

Inside the Docker network, the backend connects using:

```text
mysql:3306
```

The database initialization script is:

```text
database/init.sql
```

It is mounted into MySQL during first initialization.

If the database must be initialized from scratch, remove the local Compose volume:

```bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up -d
```

---

## 17. Valkey

Valkey provides the Redis-compatible data/cache service.

Default local address:

```text
localhost:6379
```

Inside Docker, the backend uses the service name:

```text
valkey
```

Check it:

```bash
docker compose -f docker-compose.local.yml ps valkey
```

The container health check uses:

```text
valkey-cli ping
```

---

## 18. Run the backend directly without Docker

Docker Compose is the recommended way to run the complete application.

For backend-only development:

```bash
cd backend
mvn -B verify
```

Run the Spring Boot application:

```bash
mvn spring-boot:run
```

The backend requires MySQL, Valkey, RabbitMQ, and Keycloak to be configured and reachable when running outside Docker.

Return to the repository root:

```bash
cd ..
```

---

## 19. Run the frontend directly without Docker

Install frontend dependencies:

```bash
cd frontend
npm install
```

Start Angular development server:

```bash
npm start
```

Build the frontend:

```bash
npm run build
```

Return to the repository root:

```bash
cd ..
```

When using the standalone Angular development server, ensure the backend and Keycloak URLs used by the frontend match the local development configuration.

---

## 20. Backend verification

From the repository root:

```bash
cd backend
mvn -B verify
cd ..
```

This should compile the backend and execute the automated tests.

---

## 21. Frontend verification

From the repository root:

```bash
cd frontend
npm install
npm run build
cd ..
```

The CI pipeline uses the same dependency installation/build approach.

---

## 22. Docker Compose validation

Always validate Compose syntax/configuration before deployment:

```bash
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.yml config
```

Build the application images:

```bash
```

A successful build confirms that the Dockerfiles and application build stages can be processed by Docker.

---

## 23. Complete local verification sequence

For a clean developer validation, run:

```bash
git status
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps
```

Then verify:

1. Frontend opens at `http://localhost:4200`.
2. Keycloak opens at `http://localhost:8081`.
3. RabbitMQ management opens at `http://localhost:15672`.
4. Backend health responds at `http://localhost:8080/actuator/health`.
5. Backend ping responds at `http://localhost:8080/api/v1/ping`.
6. All required containers remain running.
7. No repeated startup failures appear in the logs.

Check all logs if required:

```bash
docker compose -f docker-compose.local.yml logs --tail=200
```

---

## 24. Production/deployment validation

Do not use development passwords or `start-dev` Keycloak configuration as a production security configuration.

Before a real deployment:

1. Supply strong secrets.
2. Review exposed ports.
3. Put the application behind the intended reverse proxy/load balancer.
4. Configure TLS/HTTPS.
5. Configure production Keycloak settings.
6. Configure production database persistence and backups.
7. Configure production RabbitMQ credentials.
8. Configure production Valkey settings.
9. Review CORS and allowed origins.
10. Review OAuth2 issuer and JWK configuration.
11. Confirm persistent volumes/storage.
12. Confirm health checks and restart policies.
13. Confirm application logging and monitoring.
14. Do not expose MySQL, Valkey, or RabbitMQ management ports publicly unless explicitly required and secured.
15. Use the deployment-specific configuration rather than relying on development defaults.

Validate the deployment Compose file before starting it:

```bash
docker compose -f docker-compose.yml config
```

Then start:

```bash
docker compose -f docker-compose.yml up -d --build
```

Check:

```bash
docker compose -f docker-compose.yml ps
docker compose -f docker-compose.yml logs --tail=200
```

---

## 25. Useful Docker troubleshooting commands

List Sivion containers:

```bash
docker compose -f docker-compose.local.yml ps -a
```

Inspect a service:

```bash
docker compose -f docker-compose.local.yml logs backend
```

Restart one service:

```bash
docker compose -f docker-compose.local.yml restart backend
```

Inspect Docker images:

```bash
docker images
```

Inspect Docker volumes:

```bash
docker volume ls
```

Inspect the project network:

```bash
docker network ls
```

Show resource usage:

```bash
docker stats
```

If a port is already in use on Windows:

```powershell
Get-NetTCPConnection -LocalPort 4200 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 3306 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 5672 -ErrorAction SilentlyContinue
Get-NetTCPConnection -LocalPort 6379 -ErrorAction SilentlyContinue
```

On Linux:

```bash
ss -ltnp
```

---

## 26. Full clean rebuild

When application containers behave unexpectedly:

```bash
docker compose -f docker-compose.local.yml down
docker compose -f docker-compose.local.yml build --no-cache
docker compose -f docker-compose.local.yml up -d
```

If a completely fresh local database is also required:

```bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml build --no-cache
docker compose -f docker-compose.local.yml up -d
```

Use `--no-cache` only when necessary because it makes builds substantially slower.

---

## 27. Git workflow for development

Before starting work:

```bash
git status
git pull
```

After making changes:

```bash
git status
git diff
```

Run the relevant validation:

```bash
docker compose -f docker-compose.local.yml config
```

For backend changes:

```bash
cd backend
mvn -B verify
cd ..
```

For frontend changes:

```bash
cd frontend
npm install
npm run build
cd ..
```

Commit only after the relevant checks pass.

---

## 28. CI validation

GitHub Actions validates:

- Backend Maven tests
- Frontend npm installation and Angular build
- Local Compose configuration
- Default Compose configuration
- Backend Docker image build
- Frontend Docker image build

The CI workflow is:

```text
.github/workflows/ci.yml
```

The local Compose configuration can therefore be checked with the same basic validation used by CI:

```bash
docker compose -f docker-compose.local.yml config
```

---

## 29. Recommended everyday commands

### First setup

```bash
git clone https://github.com/sivar143/Sivion.git
cd Sivion
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.local.yml up -d
docker compose -f docker-compose.local.yml ps
```

### Start the project next time

```bash
cd Sivion
docker compose -f docker-compose.local.yml up -d
```

### Rebuild after code changes

```bash
docker compose -f docker-compose.local.yml up -d
```

### View logs

```bash
docker compose -f docker-compose.local.yml logs -f
```

### Stop the project

```bash
docker compose -f docker-compose.local.yml down
```

### Completely reset local data

```bash
docker compose -f docker-compose.local.yml down -v
docker compose -f docker-compose.local.yml up -d
```

---

## 30. Important notes

- Use `docker-compose.local.yml` for normal local development.
- Use `docker-compose.yml` for deployment/default Compose validation.
- Do not expose development credentials in a production environment.
- Do not commit secrets to Git.
- Do not use `docker compose down -v` unless deleting local database data is intentional.
- If authentication fails, check Keycloak before changing application code.
- If the backend cannot connect to infrastructure, check container health and use Docker service names rather than `localhost` for container-to-container connections.
- If the frontend cannot reach the backend, verify the frontend URL/configuration and confirm the backend is running on port 8080.
- Keep the local and deployment Compose definitions synchronized when service architecture changes.
