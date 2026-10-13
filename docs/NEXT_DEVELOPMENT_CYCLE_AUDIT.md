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
