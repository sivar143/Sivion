# Sivion Development Standards

This document defines the coding and review standards that every developer must follow when modifying the Sivion repository.

The goal is not only to make the application work, but to make the source code easy for another human developer to read, review, debug, maintain, and extend.

## 1. General principles

- Write code for humans first and machines second.
- Prefer clear, explicit code over compressed or clever code.
- Keep one logical statement or operation per line.
- Use consistent indentation and spacing throughout the file.
- Do not place multiple declarations, statements, or method bodies on a single line.
- Use descriptive names for classes, methods, variables, constants, and parameters.
- Keep methods focused on one responsibility.
- Avoid duplicated business logic.
- Preserve existing application behavior unless the change explicitly requires a behavior change.
- Do not introduce generated files, build output, IDE metadata, credentials, or local environment files into source control.
- Do not make unrelated formatting or architectural changes in a functional fix.

## 2. Human-readable class structure

Classes must follow a predictable structure so that a developer can quickly understand what the class contains.

Recommended order:

1. Package/module declaration.
2. Imports.
3. Class/interface/decorator declaration.
4. Constants.
5. Injected dependencies and fields.
6. Constructors.
7. Lifecycle/public entry-point methods.
8. Public business methods.
9. Protected methods when required.
10. Private helper methods.
11. Nested types/records/enums when required.

Keep related fields together and separate major sections with a blank line.

Do not compress an entire class into a small number of lines simply because the compiler accepts it.

## 3. Formatting rule

The following style is mandatory:

~~~typescript
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly keycloak = new Keycloak({
    url: SIVION_CONFIG.keycloakUrl,
    realm: SIVION_CONFIG.keycloakRealm,
    clientId: SIVION_CONFIG.keycloakClientId
  });

  private ready = false;

  async init(): Promise<void> {
    if (this.ready) {
      return;
    }

    await this.keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      checkLoginIframe: false
    });

    this.ready = true;
  }
}
~~~

Do not write the equivalent logic as compressed statements such as:

~~~typescript
private ready=false;
async init():Promise<void>{if(this.ready)return;await this.keycloak.init(...);this.ready=true;}
~~~

The second form is harder to review and is not acceptable for Sivion production source code.

## 4. TypeScript / Angular standards

### Imports

Use one import per line and keep imports organized.

### Components

Angular components should clearly separate:

- imports
- component metadata
- class fields
- constructor/injected dependencies
- lifecycle methods
- event handlers
- public component methods
- private helper methods

Use external templates and styles for substantial components instead of embedding large templates or styles directly inside the TypeScript class.

### Services

Services should:

- have a clear single responsibility
- use dependency injection consistently
- expose readable public methods
- keep HTTP/API details separated where an API service exists
- handle authentication and authorization consistently
- avoid duplicating API calls across components

### Templates

Angular HTML must remain HTML.

Do not place TypeScript, Java, debugging code, or large blocks of component implementation inside an HTML template.

Use readable indentation and keep complex expressions out of templates when they belong in component/service logic.

### Styling

Do not compress CSS/SCSS into unreadable one-line rules.

Do not change existing application layout or styling merely for formatting unless the change is required by the feature or bug fix.

## 5. Java / Spring standards

### Class layout

Spring classes should normally follow this structure:

~~~java
@Service
public class ExampleService {

    private static final String STATUS_ACTIVE = "ACTIVE";

    private final ExampleRepository repository;
    private final ExampleMapper mapper;

    public ExampleService(
            ExampleRepository repository,
            ExampleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ExampleView find(Long id) {
        // Validate the requested record before mapping it for the API.
        Example entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Record not found"));

        return mapper.toView(entity);
    }

    private void validate(ExampleRequest request) {
        // Keep business validation close to the operation that depends on it.
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
    }
}
~~~

### Backend rules

- Use constructor injection.
- Keep fields private unless a stronger visibility is explicitly required.
- Prefer final for dependencies and values that do not change.
- Keep controller methods focused on HTTP/API concerns.
- Keep business rules in services/domain logic rather than controllers.
- Keep persistence logic in repositories.
- Validate input at the appropriate application boundary.
- Preserve tenant isolation on every tenant-scoped operation.
- Use transactions deliberately for multi-step writes.
- Do not silently swallow exceptions.
- Do not duplicate global exception handling in individual controllers unless there is a documented reason.
- Avoid hard-coded tenant/user/security values in production business logic.
- Use clear DTO/request/response types instead of exposing persistence entities unnecessarily.

## 6. Inline comments

Comments are required for important or non-obvious logic, not for every line.

Good comments explain:

- why a business rule exists
- why an unusual implementation is necessary
- why ordering or transaction boundaries matter
- why a security check is required
- why a workaround exists
- why a state transition is restricted
- why data must be retained instead of deleted

Example:

~~~java
// Existing mappings are intentionally inactivated instead of deleted
// so historical customer/call-center relationships remain auditable.
mapping.setActive(false);
~~~

Avoid comments that merely repeat the code:

~~~java
// Set active to false.
mapping.setActive(false);
~~~

When code can be made self-explanatory, prefer improving the code instead of adding a comment.

## 7. Security and authentication

- Never commit real credentials, tokens, private keys, or secrets.
- Keep environment-specific configuration outside source code.
- Use Keycloak consistently for authentication and role-based access control.
- Keep authorization checks explicit and readable.
- Do not rely only on frontend role checks for security.
- Backend authorization remains authoritative.
- Authentication/token failures must produce clear errors without leaking sensitive information.

## 8. Database and tenant safety

- Tenant-scoped data must always be queried and modified within the correct tenant context.
- Do not remove tenant predicates from existing repository queries without reviewing the security impact.
- Prefer database constraints for invariants that must always hold.
- Preserve historical/audit information where business requirements require it.
- Use migrations or controlled initialization scripts rather than ad-hoc production database modifications.

## 9. Local versus production configuration

- Keep local development configuration separate from production-ready configuration.
- docker-compose.local.yml is intended for local infrastructure development.
- Do not accidentally move development-only settings into docker-compose.yml.
- If a configuration change genuinely affects both environments, verify both configurations explicitly.
- Never commit local secrets or machine-specific paths.

## 10. Git and branch rules

- Work only on the branch assigned for the current task.
- For the current Sivion development workflow, the assigned integration branch is integration.
- Do not modify main unless explicitly requested.
- Check the working tree before starting a change.
- Review the complete diff before committing.
- Check for merge conflicts and unintended files.
- Keep commits focused and descriptive.
- Do not commit generated build output such as target, dist, .angular, or node_modules.

## 11. Review requirement

Before considering a task complete, the developer must review:

1. Changed source files.
2. Related frontend/backend callers.
3. Authentication and authorization implications.
4. Tenant isolation.
5. Database effects.
6. Local configuration.
7. Production/default configuration.
8. Docker/Compose impact.
9. Tests and build output.
10. Git diff for unintended changes.

The review must consider the entire affected feature, not only the file that was edited.

## 12. Build and validation requirements

At minimum, validate the affected application layer.

Backend:

~~~bash
cd backend
mvn -B verify
~~~

Frontend:

~~~bash
cd frontend
npm install
npm run build
~~~

Compose configuration:

~~~bash
docker compose -f docker-compose.local.yml config
docker compose -f docker-compose.yml config
~~~

When Docker images or deployment behavior are affected, validate the relevant image/build path as well.

## 13. Readability acceptance criteria

A developer should be able to open a source file and understand its structure without first running the application.

A file should be considered too compressed if:

- multiple logical statements are placed on one line
- method bodies are difficult to visually identify
- declarations are packed together without spacing
- conditionals are written without readable indentation
- constructor arguments are unnecessarily compressed
- important business logic cannot be identified quickly
- comments are missing for genuinely non-obvious logic
- formatting makes code review unnecessarily difficult

If there is a conflict between extremely compact code and readable code, choose the readable implementation.

## 14. Final developer checklist

Before committing:

- [ ] Code is readable and consistently formatted.
- [ ] No unnecessary one-line/compressed classes or methods remain in the changed area.
- [ ] Important business/security logic has concise inline comments where necessary.
- [ ] No generated artifacts are included.
- [ ] No secrets are included.
- [ ] Authorization is enforced on the backend.
- [ ] Tenant isolation is preserved.
- [ ] Local and production configuration remain correctly separated.
- [ ] Existing functionality has not been unintentionally changed.
- [ ] Relevant backend/frontend builds pass.
- [ ] Relevant Compose configuration validation passes.
- [ ] Git diff has been reviewed.
- [ ] No merge conflicts or unrelated changes are present.
