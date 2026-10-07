# Sivion Frontend Technical Design & Development Specification

**Document:** Frontend Technical Design Document (TDD) / Technical Specification (Tech Spec)  
**Repository:** `sivar143/Sivion`  
**Applies to:** `frontend/`  
**Authoritative development branch:** `integration`

## 1. Purpose

This document is the authoritative development standard for the Sivion Angular frontend. Any future frontend change, refactor, feature, bug fix, or file move must follow these rules.

Before making frontend changes, developers/agents must read this document and verify that the proposed change does not violate the structure, dependency, testing, asset, configuration, or build rules below.

If a requirement is not covered here, preserve the existing architecture and document any intentional architectural exception.

## 2. Core Architecture Rules

### 2.1 Component separation

- Keep TypeScript and HTML separate.
- Components must use `templateUrl`; do not place large component HTML in inline `template:` blocks.
- Each component belongs to its own domain folder under `src/app/`.
- Each component must have its own `.spec.ts` test file.
- Do not create duplicate/stale component files at `src/app/` root.
- Component-specific implementation belongs with its component domain.

### 2.2 Services

All frontend services must be located under:

`src/app/services/`

Current service categories include authentication and domain API services.

Components must import services using the correct relative path from their domain folder.

Do not recreate a service in a component folder when an appropriate shared service already exists.

### 2.3 Application home/entry files

The application entry and global home files must be located under:

`src/app/home/`

Required files:

- `index.html`
- `main.ts`
- `styles.css`

Angular build configuration must reference these exact locations.

### 2.4 Application configuration

Application/runtime configuration must be located under:

`src/app/config/`

Examples include:

- runtime configuration
- frontend application configuration
- environment-independent application configuration
- workspace/role configuration when it is application configuration

Do not leave application configuration files directly under `src/app/` when they belong in `config/`.

**Important:** Angular CLI/project configuration is different and must remain at the frontend project root. Do not move `angular.json`, `package.json`, `tsconfig*.json`, etc. into `src/app/config/`.

### 2.5 Static assets

All frontend images, icons, logos, SVGs, and other static visual assets must be kept under:

`src/assets/`

Recommended organization:

```
src/assets/
├── images/
├── icons/
└── fonts/        # only when required
```

Rules:

- Do not store images/icons in component folders.
- Do not store images/icons under `src/app/`.
- Do not create duplicate asset locations.
- Reference assets through the Angular assets path, e.g. `assets/images/example.png`.
- `src/favicon.ico` may remain at the source root because Angular explicitly supports it as a build asset.

## 3. Required Frontend Structure

The intended structure is:

```
frontend/
├── src/
│   ├── app/
│   │   ├── config/
│   │   ├── home/
│   │   │   ├── index.html
│   │   │   ├── main.ts
│   │   │   └── styles.css
│   │   ├── services/
│   │   ├── finance/
│   │   ├── hr/
│   │   ├── inventory/
│   │   ├── procurement/
│   │   ├── sales-order/
│   │   ├── app.component.ts
│   │   ├── app.component.html
│   │   └── app.component.spec.ts
│   ├── assets/
│   │   ├── images/
│   │   ├── icons/
│   │   └── fonts/
│   └── favicon.ico
├── angular.json
├── package.json
├── package-lock.json
├── tsconfig.json
├── tsconfig.app.json
└── tsconfig.spec.json
```

This is the target architecture. New frontend files must follow it.

## 4. Angular Build Configuration

`frontend/angular.json` must reference:

```json
"index": "src/app/home/index.html",
"browser": "src/app/home/main.ts",
"assets": [
  "src/favicon.ico",
  "src/assets"
],
"styles": [
  "src/app/home/styles.css"
]
```

Do not point Angular back to `src/index.html`, `src/main.ts`, or `src/styles.css`.

The build configuration must continue to include `src/assets`.

## 5. Import and Path Rules

When files are moved, all imports and configuration references must be updated in the same change.

Examples:

From a component under `src/app/hr/`:

```ts
import { HrApi } from '../services/hr-api.service';
```

From `src/app/home/main.ts`:

```ts
import { AppComponent } from '../app.component';
import { AuthService } from '../services/auth.service';
```

Configuration imports must point to `config/` after configuration files are moved.

Never leave imports pointing to deleted or legacy paths.

## 6. Testing Rules

Every Angular component must have an individual test file:

```
<domain>/<component>.component.spec.ts
```

Tests should cover, as applicable:

- component creation
- important calculated values
- user interactions
- state changes
- API/service interaction using mocks
- important business rules
- regressions for previously fixed bugs

When a feature is changed, its component test must be reviewed or extended.

Do not remove tests merely to make a build pass.


## 6.1 Service Testing

Every frontend service under `src/app/services/` must have a corresponding individual test file:

```
<service>.service.ts
<service>.service.spec.ts
```

Service tests must cover, as applicable:

- service creation and dependency injection
- HTTP endpoint URL construction
- HTTP method and request payload
- request headers and authentication behavior
- successful responses
- HTTP/error responses
- CRUD operations
- important business rules and transformations
- edge cases and regression scenarios
- dependent services mocked where appropriate

For HTTP services, use Angular HTTP testing utilities rather than making real backend requests.

Service specifications must not be removed or weakened merely to make a build or test run pass.

## 6.2 Coding, Naming and Formatting Standards

These rules are mandatory for all new and modified frontend code.

### Naming

Use `camelCase` consistently for:

- frontend folder names
- variables
- properties
- function and method names
- service instances
- local constants

Examples:

```
finance/
salesOrder/
customerCallCenter/

customerName
selectedDepartment
organizationId

loadCustomers()
openNewDesignation()
saveOrganization()
```

TypeScript language constructs that conventionally require `PascalCase` remain `PascalCase`, including classes, interfaces, enums, and type aliases:

```
interface Customer {}
class SalesOrder {}
type WorkspaceId = string;
```

File names must follow the established Angular naming convention, for example `sales-order.component.ts` and `sales-order-api.service.ts`. The naming rule applies to the identifiers and directory names inside the established file naming convention; do not invent inconsistent filename styles.

### TypeScript

- Use strict typing; avoid `any` unless there is a documented technical reason.
- Prefer interfaces/types for API contracts.
- Use explicit, meaningful names.
- Avoid duplicated business logic.
- Keep methods focused and reasonably small.
- Keep API communication in services rather than directly in components.
- Follow the existing Angular dependency-injection pattern.
- Keep imports clean and consistently ordered/formatted.
- Do not leave dead code, commented-out implementation, or temporary debugging statements in completed changes.

### HTML / Angular Templates

- Keep templates in their dedicated `.html` files.
- Use consistent indentation throughout the complete template.
- Keep Angular bindings and attributes consistently formatted.
- Avoid unnecessarily complex template expressions.
- Do not place TypeScript in HTML.
- Preserve semantic and accessible markup where applicable.

### CSS / SCSS

- Use consistent indentation and formatting.
- Keep selectors and declarations consistently structured.
- Avoid unnecessary duplication.
- Avoid `!important` unless technically justified.
- Keep component-specific styles with the appropriate component and global styles in the designated global stylesheet.

### Mandatory Formatting Gate

Before any frontend change is considered complete, formatting must be applied to every modified applicable file, including:

- `.ts`
- `.html`
- `.scss`
- `.css`
- `.json`

Formatting must produce:

- consistent indentation
- no mixed tabs/spaces
- consistent spacing
- consistent brace and delimiter placement
- consistently formatted imports
- consistently formatted Angular templates
- no trailing whitespace
- no unnecessary formatting inconsistencies

The formatter used must be the project's configured/approved formatter. Do not manually override formatter output without a documented reason.

### Required Completion Sequence

Every frontend implementation must follow this sequence before commit:

```
Implement change
    ↓
Apply project formatter to modified files
    ↓
Check TypeScript
    ↓
Check Angular templates
    ↓
Check CSS/SCSS
    ↓
Run affected unit tests
    ↓
Run frontend production build
    ↓
Review git diff
    ↓
Search for stale imports/references
    ↓
Commit
```

If a validation step cannot be executed because of an environment limitation, report that limitation explicitly. Do not claim the step passed when it was not executed.

## 7. Template Rules

- Keep HTML in `.component.html` files.
- Do not put TypeScript code after the closing HTML.
- Do not mix component implementation code into HTML.
- Avoid accidental duplicated templates.
- After automated refactoring, inspect the complete template for leaked TypeScript, invalid Angular blocks, malformed ICU expressions, or unmatched HTML.
- Preserve existing Angular Material/layout behavior unless the requested change requires UI changes.

## 8. Domain Organization

The current domain folders are:

- `finance/`
- `hr/`
- `inventory/`
- `procurement/`
- `sales-order/`

Future domains must receive their own folder rather than adding unrelated components to the root `app/` directory.

The root `app/` directory is reserved for application-level components/files such as `app.component.*` and architectural directories such as `config/`, `home/`, and `services/`.

## 9. Configuration and Environment Rules

Do not confuse application runtime configuration with Angular build configuration.

### Application configuration

Place under:

`src/app/config/`

### Angular/toolchain configuration

Keep at:

`frontend/`

Examples:

- `angular.json`
- `package.json`
- `package-lock.json`
- `tsconfig.json`
- `tsconfig.app.json`
- `tsconfig.spec.json`

### Backend/environment configuration

Backend Spring configuration belongs under the backend project and must not be copied into the frontend configuration directory.

## 10. Local vs Production Rules

Frontend changes must not accidentally change production deployment behavior when a change is intended to be local-only.

The repository currently distinguishes local and production deployment configuration. Any Docker Compose change must explicitly identify which compose file it affects.

Rule:

- Local-only deployment behavior → `docker-compose.local.yml`
- Production-ready deployment behavior → `docker-compose.yml`
- Never modify the production compose file merely to solve a local development problem.

Frontend build changes must be checked against both local manual deployment and production container deployment.

## 11. Docker/Nginx Compatibility

The Angular build output must remain compatible with the existing frontend Docker/Nginx deployment.

When changing:

- Angular output paths
- build configuration
- index location
- static assets
- routing
- Nginx configuration

verify that the generated `dist/sivion-web` output remains deployable by the existing frontend container.

Do not change Docker/Nginx behavior unless required by the actual change.

## 12. Refactoring Rules

Before a refactor:

1. Inspect the current repository state.
2. Search for every reference to files being moved.
3. Check imports, Angular configuration, tests, Docker configuration, and documentation.
4. Make the smallest safe set of changes.
5. Remove obsolete files only after references are updated.
6. Re-check the repository for stale paths.
7. Review the resulting structure against this document.
8. Build/test when the environment permits it.
9. Never claim tests passed unless they were actually executed successfully.

Automated mass replacements must not be used blindly. After any automated change, inspect affected files for syntax corruption.

## 13. Change Safety Rules

Every frontend change must preserve existing functionality unless the requested change explicitly changes that behavior.

In particular:

- Organization functionality must remain available.
- Department and Designation functionality must remain available.
- Existing workspace/role behavior must remain intact.
- API service paths must remain correct.
- Authentication/Keycloak initialization must remain functional.
- Existing component tests must remain valid.
- Production compose configuration must not be changed for local-only work.

## 14. Git Branch and Commit Rules

For Sivion work, use the `integration` branch unless the user explicitly requests another branch.

Do not create feature/fix branches by default.

Before committing:

- verify the current branch
- inspect the files being changed
- check for stale imports/references
- review the resulting repository structure
- run available validation where possible

Commit messages should clearly describe the architectural or functional change.

## 15. Required Review Checklist

Before declaring a frontend change complete:

### Structure
- [ ] Component is in the correct domain folder.
- [ ] HTML is separate from TypeScript.
- [ ] Component has a `.spec.ts`.
- [ ] Shared services are under `src/app/services/`.
- [ ] Application configuration is under `src/app/config/`.
- [ ] Entry/global home files are under `src/app/home/`.
- [ ] Images/icons/logos/SVGs are under `src/assets/`.
- [ ] No stale duplicate files remain.

### References
- [ ] All imports point to current paths.
- [ ] `angular.json` points to the correct home files.
- [ ] Asset paths are correct.
- [ ] No references remain to old `src/home/`, `src/main.ts`, `src/index.html`, or `src/styles.css` paths after migration.

### Code quality
- [ ] No inline large HTML templates.
- [ ] No TypeScript leaked into HTML.
- [ ] No accidental duplicate code.
- [ ] Existing behavior is preserved.
- [ ] Tests cover important changed behavior.

### Deployment
- [ ] Frontend production build configuration remains valid.
- [ ] Local development remains valid.
- [ ] Existing Docker/Nginx deployment remains compatible.
- [ ] Production compose is unchanged for local-only requirements.

### Git
- [ ] Work is on `integration` unless explicitly instructed otherwise.
- [ ] Changes are reviewed before commit.
- [ ] Commit message accurately describes the change.

## 16. Architectural Exceptions

If a future requirement makes one of these rules impractical, do not silently violate the rule.

Instead:

1. Explain why the exception is required.
2. Update this TDD/Tech Spec if the new architecture is intended to become standard.
3. Keep the repository structure internally consistent.
4. Update the review checklist if necessary.

## 17. Source of Truth

This document is the **frontend architectural source of truth** for Sivion development.

When a future request says to "follow the frontend rules", "follow the frontend TDD", or "check the frontend documentation", this document must be consulted before making changes.

If existing code conflicts with this document, do not automatically preserve the conflicting structure. Determine whether it is legacy code, an intentional exception, or a defect, and correct/document it accordingly.
