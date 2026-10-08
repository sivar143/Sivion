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

## 5. Recommended local setup

For normal Windows/Linux development, use:

```bash
docker compose -f docker-compose.local.yml up -d
```

The local Compose file starts **infrastructure only** (MySQL, Valkey, RabbitMQ and Keycloak). The backend and frontend are intentionally run manually from the host for faster development and debugging.

Before starting it, validate the Compose file:

```bash
docker compose -f docker-compose.local.yml config
```

If the command completes without a Compose configuration error, start the stack.

Check service status:

```bash
docker compose -f docker-compose.local.yml ps
```

---

## 6. Local application endpoints

With the default ports, the stack exposes:

| Service | URL / Address | Default credentials |
|---|---|---|
| Sivion frontend | http://localhost:4200 | Manually run with `npm start` |
| Sivion backend | http://localhost:8080 | Manually run with Maven |
| Backend health | http://localhost:8080/actuator/health | Public health endpoint |
| API ping | http://localhost:8080/api/v1/ping | Public endpoint |
| Keycloak | http://localhost:8081 | admin / admin |
| Keycloak realm | http://localhost:8081/realms/sivion | — |
| RabbitMQ management | http://localhost:15672 | sivion / sivion |
| MySQL | localhost:3307 | admin / admin |
| Valkey | localhost:6379 | — |
| Valkey | localhost:6379 | — |

The default credentials are development defaults. Do not use these credentials in a production deployment.

---

## 7. Check that the stack is healthy

Show all containers:

```bash
docker compose -f docker-compose.local.yml ps
```

Check application logs:

```bash
docker compose -f docker-compose.local.yml logs
```

Follow all logs:

```bash
docker compose -f docker-compose.local.yml logs -f
```

The backend and frontend are host processes in local mode, so run their logs directly in the Maven/Angular terminals.

Follow Keycloak:

```bash
docker compose -f docker-compose.local.yml logs -f keycloak
```

Follow infrastructure services:

```bash
docker compose -f docker-compose.local.yml logs -f mysql valkey rabbitmq
```

Check the backend health endpoint:

### PowerShell

```powershell
Invoke-WebRequest http://localhost:8080/actuator/health
```

### Linux/macOS/Git Bash

```bash
curl http://localhost:8080/actuator/health
```

The expected result is a successful HTTP response with the application health reported as UP.

---

## 8. Start, stop and restart commands

### Start existing containers

```bash
docker compose -f docker-compose.local.yml start
```

### Stop containers without deleting them

```bash
docker compose -f docker-compose.local.yml stop
```

### Restart the complete stack

```bash
docker compose -f docker-compose.local.yml restart
```

### Stop and remove containers/networks

```bash
docker compose -f docker-compose.local.yml down
```

### Start local infrastructure

```bash
docker compose -f docker-compose.local.yml up -d
```

### Force recreation of infrastructure containers

```bash
docker compose -f docker-compose.local.yml up -d --force-recreate
```

### Run backend manually (Windows PowerShell)

The local MySQL container is exposed on host port **3307** with database/user/password **sivion / admin / admin**. Set these variables before starting the backend:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/sivion"
$env:SPRING_DATASOURCE_USERNAME="admin"
$env:SPRING_DATASOURCE_PASSWORD="admin"
$env:SPRING_DATA_REDIS_HOST="localhost"
$env:SPRING_RABBITMQ_HOST="localhost"
$env:SPRING_RABBITMQ_USERNAME="sivion"
$env:SPRING_RABBITMQ_PASSWORD="sivion"
$env:SIVION_KEYCLOAK_ADMIN_BASE_URL="http://localhost:8081"
$env:SIVION_KEYCLOAK_ADMIN_USERNAME="admin"
$env:SIVION_KEYCLOAK_ADMIN_PASSWORD="admin"
cd backend
mvnw.cmd spring-boot:run
```

### Run backend manually (Linux/macOS)

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3307/sivion"
export SPRING_DATASOURCE_USERNAME="admin"
export SPRING_DATASOURCE_PASSWORD="admin"
export SPRING_DATA_REDIS_HOST="localhost"
export SPRING_RABBITMQ_HOST="localhost"
export SPRING_RABBITMQ_USERNAME="sivion"
export SPRING_RABBITMQ_PASSWORD="sivion"
export SIVION_KEYCLOAK_ADMIN_BASE_URL="http://localhost:8081"
export SIVION_KEYCLOAK_ADMIN_USERNAME="admin"
export SIVION_KEYCLOAK_ADMIN_PASSWORD="admin"
cd backend
./mvnw spring-boot:run
```

### Run frontend manually

```bash
cd frontend
npm start
```

---

## 9. Reset the local database and infrastructure data

The MySQL data is stored in the Docker volume `mysql-data`.

To stop the stack and remove persistent Compose volumes:

```bash
docker compose -f docker-compose.local.yml down -v
```

Then start again:

```bash
docker compose -f docker-compose.local.yml up -d
```

**Warning:** `down -v` deletes the local MySQL Docker volume and therefore destroys the local database data stored in that volume.

Use this only when a clean local database is required.

---

## 10. Rebuild only one application service

Backend:

```bash
cd backend
mvnw.cmd spring-boot:run
```

Frontend:

```bash
cd frontend
npm start
```

Rebuild both application images:

```bash
Run the backend and frontend manually in their respective terminals as shown above.
```

---

## 11. Environment variables

Docker Compose supports environment-variable overrides.

The important variables include:

```text
MYSQL_DATABASE
MYSQL_USER
MYSQL_PASSWORD
MYSQL_ROOT_PASSWORD
MYSQL_PORT

VALKEY_PORT

RABBITMQ_DEFAULT_USER
RABBITMQ_DEFAULT_PASS
RABBITMQ_PORT
RABBITMQ_MANAGEMENT_PORT

KEYCLOAK_ADMIN_USERNAME
KEYCLOAK_ADMIN_PASSWORD
KEYCLOAK_PORT

BACKEND_PORT
FRONTEND_PORT
```

The Compose files provide development-safe defaults when these variables are not supplied.

For a deployment environment, provide secure values through the environment or a deployment-specific environment file rather than committing secrets to Git.

Example PowerShell session:

```powershell
$env:MYSQL_PASSWORD="change-me"
$env:MYSQL_ROOT_PASSWORD="change-root-password"
$env:KEYCLOAK_ADMIN_PASSWORD="change-admin-password"
docker compose -f docker-compose.yml up -d --build
```

Example Linux shell:

```bash
export MYSQL_PASSWORD="change-me"
export MYSQL_ROOT_PASSWORD="change-root-password"
export KEYCLOAK_ADMIN_PASSWORD="change-admin-password"
docker compose -f docker-compose.yml up -d --build
```

Do not commit passwords, access tokens, private keys, or other secrets to the repository.

---

## 12. Local versus deployment Compose files

### Local development

Use:

```bash
docker compose -f docker-compose.local.yml up -d --build
```

This is the preferred command for a developer workstation.

### Deployment/default Compose configuration

Use:

```bash
docker compose -f docker-compose.yml up -d --build
```

Before deployment, validate the configuration:

```bash
docker compose -f docker-compose.yml config
```

The local and default Compose definitions currently use the same core service topology. Keep them synchronized when deployment architecture changes.

---

## 13. Keycloak configuration

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

## 14. RabbitMQ

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

## 15. MySQL

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
docker compose -f docker-compose.local.yml up -d --build
```

---

## 16. Valkey

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

## 17. Run the backend directly without Docker

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

## 18. Run the frontend directly without Docker

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

## 19. Backend verification

From the repository root:

```bash
cd backend
mvn -B verify
cd ..
```

This should compile the backend and execute the automated tests.

---

## 20. Frontend verification

From the repository root:

```bash
cd frontend
npm install
npm run build
cd ..
```

The CI pipeline uses the same dependency installation/build approach.

---

## 21. Docker Compose validation

Always validate Compose syntax/configuration before deployment:

```bash
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.yml config
```

Build the application images:

```bash
docker compose -f docker-compose.local.yml build backend frontend
```

A successful build confirms that the Dockerfiles and application build stages can be processed by Docker.

---

## 22. Complete local verification sequence

For a clean developer validation, run:

```bash
git status
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.local.yml up -d --build
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

## 23. Production/deployment validation

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

## 24. Useful Docker troubleshooting commands

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

## 25. Full clean rebuild

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

## 26. Git workflow for development

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
docker compose -f docker-compose.local.yml build backend frontend
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

## 27. CI validation

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

## 28. Recommended everyday commands

### First setup

```bash
git clone https://github.com/sivar143/Sivion.git
cd Sivion
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.local.yml up -d --build
docker compose -f docker-compose.local.yml ps
```

### Start the project next time

```bash
cd Sivion
docker compose -f docker-compose.local.yml up -d
```

### Rebuild after code changes

```bash
docker compose -f docker-compose.local.yml up -d --build
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
docker compose -f docker-compose.local.yml up -d --build
```

---

## 29. Important notes

- Use `docker-compose.local.yml` for normal local development.
- Use `docker-compose.yml` for deployment/default Compose validation.
- Do not expose development credentials in a production environment.
- Do not commit secrets to Git.
- Do not use `docker compose down -v` unless deleting local database data is intentional.
- If authentication fails, check Keycloak before changing application code.
- If the backend cannot connect to infrastructure, check container health and use Docker service names rather than `localhost` for container-to-container connections.
- If the frontend cannot reach the backend, verify the frontend URL/configuration and confirm the backend is running on port 8080.
- Keep the local and deployment Compose definitions synchronized when service architecture changes.
