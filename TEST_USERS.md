# Sivion Test Users

> **Local-development test accounts only.** These accounts are intended for functional and role/permission testing in the local Docker environment. Do not reuse these credentials outside local development.
>
> The repository already contains the Keycloak realm definition and the administrator test account. For security, this document intentionally does **not** store plaintext passwords. Use the local Keycloak realm configuration as the source of truth for imported credentials, or reset a test user's password from the Keycloak Admin Console.

## Local URLs

| Service | URL |
|---|---|
| Sivion application | http://localhost:4200 |
| Keycloak | http://localhost:8081 |
| Keycloak Admin Console | http://localhost:8081/admin/ |
| Backend API | http://localhost:8080 |
| RabbitMQ Management | http://localhost:15672 |

## Role test accounts

| Username | Role | Primary workspace / functionality to verify |
|---|---|---|
| `admin` | `ADMIN` | Administrator — verify all available workspaces and administrative access |
| `sales.manager` | `SALES_MANAGER` | Sales Manager — customers, contacts, leads, opportunities, sales orders, team, reports |
| `sales.user` | `SALES_USER` | Sales User — customer/lead/opportunity and sales-order user workflows |
| `hr.admin` | `HR_ADMIN` | HR Administrator — employee and HR management workflows |
| `hr.user` | `HR_USER` | HR User — HR user-level workflows |
| `manager` | `MANAGER` | Manager — team, attendance, leaves, meetings, goals and performance |
| `employee` | `EMPLOYEE` | Employee — profile, attendance, leaves, meetings, goals and performance |
| `inventory.manager` | `INVENTORY_MANAGER` | Inventory Manager — materials, inventory, stock in/adjustment, dispatch and reports |
| `inventory.user` | `INVENTORY_USER` | Inventory User — inventory, materials and dispatch workflows |
| `warehouse.manager` | `WAREHOUSE_MANAGER` | Warehouse Manager — warehouse-oriented inventory workflows |
| `warehouse.user` | `WAREHOUSE_USER` | Warehouse User — warehouse user-level workflows |
| `procurement.manager` | `PROCUREMENT_MANAGER` | Procurement Manager — suppliers, requests, purchase orders and goods receipts |
| `finance.manager` | `FINANCE_MANAGER` | Finance Manager — invoices, payments, expenses and finance workflows |
| `finance.user` | `FINANCE_USER` | Finance User — finance user-level workflows |
| `marketing.user` | `MARKETING_USER` | Marketing User — currently verify role/workspace visibility only; marketing module remains outside the current implementation scope |

## Administrator test account

The local realm currently defines:

- Username: `admin`
- Role: `ADMIN`
- Email: `admin@sivion.local`
- Password: the value defined in `infrastructure/keycloak/sivion-realm.json`

## Recommended role-testing sequence

1. Start the local environment with `docker compose up`.
2. Open `http://localhost:4200`.
3. Sign in with one test account at a time.
4. Verify that the expected workspace and navigation items are visible.
5. Verify that actions allowed for the role work correctly.
6. Verify that actions belonging to another role are not exposed or accepted.
7. Sign out before switching to the next role.
8. Record functional defects separately from intentional permission restrictions.

## Important testing note

These accounts are for local functional testing. Before any shared/test/staging/production deployment, replace them with managed identities and secrets and remove development credentials from exported/imported realm data.
