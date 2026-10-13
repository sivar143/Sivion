# Sivion next development cycle: implementation audit

Audit baseline: `integration` at `6949ba12baa65b7b2118e634d1990d00d29b1ba0` (2026-10-13).

This is a source-tree audit, not a claim that every route has been exercised against a live database.

## Confirmed findings

| Capability | Source-tree finding | Next action |
| --- | --- | --- |
| Audit trail | No dedicated audit entity, repository, endpoint, or UI existed in the audited tree. | Add request-metadata audit recording and an admin-only view. |
| Notifications | No dedicated notification domain/API/UI was found. RabbitMQ infrastructure exists, but it is not a user notification inbox. | Design persistence, read/unread state, recipient scoping, and event-to-notification mapping before implementing. |
| Reporting | A dedicated HR report component exists. No shared reporting module was found in the frontend tree. | Validate report totals against domain data and add module-specific reports incrementally. |
| HR holidays | Navigation exists, but the HR shell routes it to a generic placeholder. | Implement a persisted holiday calendar and tenant/role checks. |
| HR meetings | Navigation exists, but the HR shell routes it to a generic placeholder. Sales activities are not a substitute for organization-wide meeting management. | Implement meeting records, attendees, and access rules. |
| HR performance | Navigation exists, but the HR shell routes it to a generic placeholder. | Define review cycles, review ownership, and employee visibility. |
| HR knowledge development | Navigation exists, but the HR shell routes it to a generic placeholder. | Implement learning records and completion tracking. |
| HR employee documents | Navigation exists, but the HR shell routes it to a generic placeholder. | Implement secure metadata and file storage with employee-scoped access. |
| Administration / Marketing | Workspace entries exist, but dedicated components were not present in the audited frontend file tree. | Audit their current routing and APIs before defining feature scope. |

## Delivery order

1. Audit trail: record write-request metadata centrally, expose a bounded tenant-scoped read endpoint to administrators, and show recent records in the UI.
2. Notifications: add durable recipient-specific notifications and explicit read/unread operations; connect domain events only after event semantics and recipients are defined.
3. Reporting: test each existing report against backend queries, then add Sales, Inventory, Procurement, and Finance reports based on persisted data.
4. Replace HR placeholders with persisted, permission-checked workflows in small independently tested components.
5. Complete Administration and Marketing after their intended capabilities and authorization rules are reviewed.

## Audit trail scope and limitations

The first audit-trail implementation records method, path, actor, tenant header, HTTP status, timestamp, and remote address for API write requests. It intentionally does not store request bodies, query strings, passwords, or tokens. It is request-metadata auditing, not yet a field-level before/after change history. A later iteration should add business-level change events for critical records and formal retention controls.

Tenant isolation must be reviewed alongside the existing tenant-context implementation; the audit endpoint filters by the tenant header currently used by the frontend. The header is not itself proof of tenant membership, so this implementation should not be considered a replacement for authoritative tenant authorization.

## Verification checklist

- Run backend `mvn -B verify`.
- Run frontend `npm test` and `npm run build`.
- Validate local and production Compose files.
- Verify that non-admin roles receive 403 for audit history.
- Verify that read-only requests do not create audit records.
- Verify that failed writes are recorded with their HTTP status.
- Confirm the generated `audit_log` table in the target database and review retention needs.


## Current implementation status — 2026-10-13

The following increments have now been added on `integration` after the original audit:

- **Audit trail:** request-metadata auditing and an admin-only UI are implemented. It is not field-level before/after history.
- **Notifications:** persistent recipient-specific inbox, unread/read state, and administrator-sent notifications are implemented. HR leave requests/decisions and the existing sales/inventory/procurement domain events now create inbox notifications through a dedicated RabbitMQ queue.
- **Reporting:** operational summary screens now read from the existing Sales/CRM, Inventory, Procurement, and Finance APIs. These are operational indicators, not audited financial statements or a reconciliation ledger.
- **HR calendar:** holidays and organization meetings are persisted and role-checked.
- **HR performance and learning:** performance reviews and learning-resource records are persisted; employee-visible records are scoped through the HR service's existing employee visibility rules.
- **HR employee documents:** file upload/download and metadata are implemented with a 10 MB limit, an allowlist of document MIME types, and employee-scope checks. Configure a persistent, access-controlled storage path for deployment; the default local filesystem path is not a substitute for a managed backup/retention policy.
- **Administration:** a staff-account and role-responsibility overview is implemented and links to the existing staff-login workflow. This is not yet a full permissions editor or general Keycloak user-management console.
- **Marketing:** campaign create/list/edit is implemented with persistent records, date validation, budgets, channels, and status. Campaign-to-lead attribution and campaign conversion reporting are still not modeled as first-class relationships.

## Remaining gaps after these increments

1. **Tenant architecture:** the current services still use tenant 1 in several places. A global guard now rejects client-selected tenant IDs other than 1 and rejects JWT tenant claims that identify another tenant. This is a safe single-tenant guard, not multi-tenant support. Before enabling more tenants, all services and repositories must resolve an authoritative tenant membership from trusted identity claims and apply it consistently.
2. **Administration:** implement auditable role assignment/permission editing, system configuration, and broader Keycloak user management if those capabilities are required beyond the existing HR staff-login flow.
3. **Marketing:** connect campaigns to lead attribution, campaign spend, conversion, and ROI using explicit relationships and validated backend aggregation.
4. **Notification reliability:** add durable event idempotency/outbox handling and operational visibility for retries/dead-letter events. The current event listener is recipient-aware but does not yet provide end-to-end exactly-once delivery.
5. **Audit history:** add business-level before/after records for critical fields, retention policy, and tenant-aware administrative access when the application becomes multi-tenant.
6. **Deployment hardening:** mount the employee-document storage path on persistent protected storage, back it up, and define retention/access policies.
7. **Regression verification:** rerun backend tests, frontend tests/build, and both Compose validations after the latest commits; inspect the final CI run before declaring the batch complete.

The implementation intentionally does not claim that the remaining items above are complete.
