# Sivion User Guide

## 1. About Sivion

Sivion is a modular B2B business management platform designed to bring customer management, sales, inventory, procurement, finance, and employee operations into one secured workspace.

This guide is written for business users, clients, administrators, and functional stakeholders. It focuses on what the application does and how the currently implemented features are intended to be used. Developer architecture and implementation details are documented separately in `TECHNICAL_INFO.md`.

> **Current implementation note:** Sivion is under active development. This guide documents the functionality currently implemented in the project and does not describe future modules as if they were already available.

## 2. Accessing the application

For local development, start the application using Docker Compose:

```bash
docker compose up --build
```

The web application is available at:

- `http://localhost:4200`

Keycloak is available locally at:

- `http://localhost:8081`

The default development administrator configured by the local environment is:

- Username: `admin`
- Password: `admin`

These credentials are for local development only and must be replaced or externally managed in non-development environments.

## 3. Signing in and workspaces

Sivion uses Keycloak-based authentication and role-based access control. After authentication, the application presents a workspace appropriate to the user's assigned role.

A user's available functions are determined by role and workspace configuration. Typical currently configured roles include:

- Administrator
- Sales Manager
- Sales User
- HR Administrator / HR User
- Manager
- Employee
- Inventory Manager / Inventory User
- Warehouse Manager / Warehouse User
- Procurement Manager
- Finance Manager / Finance User
- Marketing User

The exact menus displayed depend on the roles assigned to the user.

## 4. CRM and customer management

The CRM area currently supports the core customer relationship lifecycle.

### Customers

Users with the appropriate CRM permissions can:

- View customers.
- Create customers.
- Update customer information.
- Search customer records.
- Work with tenant-scoped customer data.

### Contacts

Users can maintain contacts associated with customers, including creating and viewing contact records.

### Leads

The lead lifecycle currently supports controlled status progression including:

- `NEW`
- `CONTACTED`
- `QUALIFIED`
- `CONVERTED`

Lead status should be changed through the available application actions rather than by directly modifying database data.

### Opportunities

Opportunities support sales pipeline stages including:

- Qualification
- Needs Analysis
- Proposal
- Negotiation
- Closed Won
- Closed Lost

Opportunity records can contain an amount and probability and can be associated with customer activity.

### Activities

Activities can be associated with CRM records such as customers, leads, and opportunities. They provide a place to record follow-up work and business interactions.

## 5. Sales Orders

Sales Orders connect the CRM/sales process with inventory availability and fulfillment.

A sales order contains:

- Customer
- Optional opportunity reference
- Order items
- Quantity per item
- Unit price
- Line description
- Delivery address
- Notes
- Total amount

### Sales order lifecycle

The implemented lifecycle includes:

`DRAFT → CONFIRMED → RESERVED → PARTIALLY_DISPATCHED → DISPATCHED`

A reservation failure can place an order into `RESERVATION_FAILED`, from which it can be confirmed again or cancelled.

Orders can also be cancelled while cancellation is permitted by the current lifecycle rules. Cancellation releases active inventory reservations.

### Inventory and dispatch behavior

When a confirmed order is processed, inventory is reserved for its items. If the complete quantity is not dispatched in one operation, the order can remain `PARTIALLY_DISPATCHED`. Once all active reservations have been consumed and the order is fully dispatched, it becomes `DISPATCHED`.

This allows partial fulfillment without incorrectly treating an order as completely shipped.

## 6. Inventory management

The inventory area currently supports:

- Materials/products
- Warehouses
- Stock-in operations
- Stock adjustments
- Inventory ledger/history
- Inventory reservations
- Sales-order dispatch
- Dispatch history

Inventory quantities are maintained through transactions rather than by treating a manually edited balance as the authoritative source.

### Stock-in

Stock can be received into a warehouse. Procurement goods receipts can also generate inventory stock transactions through the integration flow.

### Stock adjustments

Authorized users can record stock adjustments. Adjustments should represent a real business reason and should not be used to bypass normal procurement or dispatch processes.

### Reservations

Sales-order confirmation can reserve inventory. Reserved quantities reduce the amount available for other orders while the stock remains physically in the warehouse.

### Dispatch

A dispatch consumes the corresponding reservation when a sales order is being fulfilled. The system records the dispatch and inventory transaction and publishes the appropriate business event for downstream processing.

## 7. Procurement

The procurement area currently supports the basic purchasing lifecycle:

- Suppliers
- Purchase requests
- Purchase orders
- Purchase order items
- Goods receipts
- Receipt-driven inventory integration

### Suppliers

Supplier records include a business code and identifying information. Supplier codes are normalized and must be unique within the tenant.

### Purchase requests

Purchase requests can be created, listed, and moved through their supported status lifecycle.

### Purchase orders

Purchase orders contain one or more item lines. Each line identifies the product/material, warehouse, quantity, and pricing information required by the implemented purchasing flow.

Duplicate product/warehouse lines within the same purchase order are prevented by validation.

### Goods receipt

A goods receipt records the quantity actually received against a purchase order line. The system prevents the cumulative received quantity from exceeding the ordered quantity.

A purchase order can therefore move through partial receipt before becoming fully received.

When a valid goods receipt is processed, an integration event can update inventory through the inventory service.

## 8. Finance

The currently implemented finance functionality covers:

- Invoices
- Payments
- Expenses

### Invoices

Invoices can be created with a customer and positive invoice amount.

For completed sales-order fulfillment, the finance integration creates the invoice from the completed dispatch event. Partial dispatches do not represent final fulfillment and therefore should not create the final sales-order invoice.

### Payments

Payments are associated with invoices. The system validates that:

- The invoice exists.
- The payment amount is positive.
- A closed/cancelled invoice cannot receive another payment.
- The cumulative payment amount cannot exceed the invoice amount.

Invoices can therefore be partially paid and become `PAID` when the full amount has been received.

### Expenses

Expenses require a category, description, and positive amount and are stored with a submitted status for the implemented workflow.

## 9. HR and employee workspace

The current application includes HR and employee-oriented workspace configuration covering areas such as:

- Employees
- Organization
- Attendance
- Leave management
- Holidays
- Meetings
- Performance
- Goals
- Knowledge development
- Employee documents
- Reports

The availability of these functions depends on the current implementation and assigned role. The HR area should be treated as an actively evolving part of the platform while the current stabilization work continues.

## 10. Administrator workspace

The administrator workspace provides centralized access to areas currently configured for administration and business operations, including sales, employees, inventory, procurement, finance, and other platform areas.

Administrator access should be restricted to trusted users because it can expose multiple business functions.

## 11. Business data isolation

Sivion is designed as a tenant-aware platform. Business records are associated with a tenant so that data belonging to one organization can be separated from another organization's data.

The current development implementation uses a default development tenant for local functionality. Production deployments must configure real tenant identity and authorization rather than relying on the development default.

## 12. Recommended user practices

- Use the application UI/API workflow rather than modifying business tables directly.
- Confirm inventory availability before committing customer delivery promises.
- Use cancellation instead of deleting business transactions when a record must be stopped after processing has started.
- Record partial receipts and partial dispatches accurately.
- Do not reuse business identifiers for unrelated transactions.
- Protect administrator credentials.
- Do not use local development passwords in production.

## 13. Local support information

For a developer or deployment administrator, see `TECHNICAL_INFO.md` for:

- Architecture
- Technology stack
- Docker services
- Database and integration design
- Keycloak/RBAC
- API conventions
- Event-driven processing
- Development and deployment information
- Testing and troubleshooting guidance

## 14. Current project status

Sivion is currently in a stabilization and hardening phase. The focus is on improving the functionality already implemented across CRM, sales orders, inventory, procurement, finance, HR, authentication, integration, and the application shell before additional business modules are introduced.
