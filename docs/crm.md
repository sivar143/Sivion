# CRM module

The first functional Sivion module covers the core B2B customer lifecycle:

- Customer accounts with tenant-scoped search and CRUD
- Multiple contacts per customer
- Lead capture with source, rating, assignment and lifecycle status
- Opportunity pipeline with stage, amount and probability
- Activities linked to customers, leads or opportunities
- Keycloak authentication and a seeded ADMIN user for local development
- Tenant boundary enforced by `X-Tenant-ID` at the API/service layer

## API

All CRM endpoints live under `/api/v1/crm` and require a valid Keycloak bearer token.

| Resource | Endpoints |
|---|---|
| Customers | `GET/POST /customers`, `GET/PUT /customers/{id}` |
| Contacts | `GET/POST /contacts` |
| Leads | `GET/POST /leads`, `PATCH /leads/{id}/status` |
| Opportunities | `GET/POST /opportunities`, `PATCH /opportunities/{id}/stage` |
| Activities | `GET/POST /activities` |

For local development the demo tenant is tenant `1`. The seeded Keycloak user is `admin` / `admin`.

## CRM lifecycle

Lead: `NEW -> CONTACTED -> QUALIFIED -> CONVERTED`

Opportunity: `QUALIFICATION -> NEEDS_ANALYSIS -> PROPOSAL -> NEGOTIATION -> CLOSED_WON/CLOSED_LOST`

The API prevents cross-tenant customer access and automatically generates business numbers when they are omitted.
