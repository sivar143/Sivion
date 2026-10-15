# Supply Chain Traceability and Delivery Management

## Scope

This capability connects supplier inbound shipments, inventory lots, manufacturing consumption/output, and customer deliveries. It must extend existing Procurement, Inventory, Manufacturing, Sales Order, audit, and notification workflows rather than create duplicate purchase orders, stock balances, or sales orders.

## Business journeys

1. Supplier inbound: purchase order -> supplier shipment -> carrier milestones -> arrival -> goods receipt -> quantity reconciliation -> inspection -> accepted/rejected inventory lots.
2. Manufacturing genealogy: material lot -> manufacturing order -> consumed lot quantities -> processing operations -> quality checks -> finished-product lot(s). One input lot may feed many production batches; one production batch may consume multiple input lots.
3. Customer delivery: sales order -> allocation of finished goods/lot(s) -> packing -> dispatch -> carrier milestones -> delivery confirmation or exception -> proof of delivery/return.

## Core records

- Shipment: tenant, direction (INBOUND/OUTBOUND/TRANSFER), human-readable shipment number, status, origin/destination, supplier/customer reference, purchase/sales/manufacturing order references, carrier, tracking number, dispatched/estimated/actual dates, and notes.
- ShipmentEvent: shipment, event type/status, event timestamp, recorded timestamp, location, remarks, source (MANUAL/CARRIER_WEBHOOK/SYSTEM), external event identifier, actor, and idempotency key.
- MaterialLot: tenant, lot code, material/product, supplier, supplier lot reference, receipt, received/accepted/rejected quantity, expiry/manufacture dates where applicable, warehouse/location, and quality status.
- MaterialConsumption: manufacturing order, input lot, quantity consumed, unit, and recorded timestamp.
- ProductionLot: finished product, production order, output lot code, produced/rejected quantities, quality disposition, and stock location.
- ShipmentLine: shipment, product/material, lot reference, planned/actual quantity, unit, and receipt/delivery outcome.
- ProofOfDelivery: shipment, recipient, delivered timestamp, notes, and optional secured document reference.

## Status rules

Inbound shipments: CREATED, READY_FOR_DISPATCH, DISPATCHED, IN_TRANSIT, ARRIVED, PARTIALLY_RECEIVED, RECEIVED, INSPECTION_PENDING, ACCEPTED, REJECTED, DELAYED, CANCELLED.

Outbound shipments: CREATED, PACKED, READY_FOR_DISPATCH, DISPATCHED, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, PARTIALLY_DELIVERED, DELIVERY_FAILED, RETURN_IN_TRANSIT, RETURNED, DELAYED, CANCELLED.

Transitions must be validated in the backend. A status update appends an immutable event and updates the current status in one transaction. Do not allow editing/deleting historical events. Duplicate carrier webhook events must be idempotent; out-of-order events must not silently regress the current shipment state. Exceptions must retain their reason and actor.

## API outline

- `POST /api/v1/supply-chain/shipments` create a shipment.
- `GET /api/v1/supply-chain/shipments` tenant-scoped searchable list, with direction/status/date filters.
- `GET /api/v1/supply-chain/shipments/{id}` shipment, lines, and chronological events.
- `POST /api/v1/supply-chain/shipments/{id}/events` append a validated status/milestone.
- `GET /api/v1/supply-chain/track/{publicTrackingToken}` limited customer-safe tracking projection; token must be random, revocable, and must not expose internal IDs, costs, supplier details, or private notes.
- `POST /api/v1/supply-chain/shipments/{id}/proof-of-delivery` record confirmation and a secured document reference.
- `GET /api/v1/supply-chain/lots/{lotCode}/genealogy` show upstream source lots and downstream production/delivery batches.
- Manufacturing endpoints record consumption and output lots against existing manufacturing orders.
- Carrier adapters ingest verified webhooks and poll APIs where supported; manual milestone entry remains available.

## Authorization and isolation

Use the existing JWT/Keycloak roles, `X-Tenant-ID` contract, and tenant isolation model. Every repository query and unique business key must be tenant-scoped. Internal shipment management requires appropriate procurement, warehouse, manufacturing, or sales permissions. Public tracking must use an unguessable token and return only a deliberately limited view. Carrier webhooks must be signature-verified and idempotent.

## UI

Add a Supply Chain workspace section with separate views for Inbound Shipments, Production Traceability, Outbound Deliveries, and Lot Genealogy. Shipment detail should show a vertical timeline, current status, planned vs actual dates, carrier/tracking details, lines/lots, and permitted status-update actions. All forms must follow the project-wide dialog contract: close only after successful saves; otherwise retain the dialog and show the actionable backend error. Layouts must remain usable on narrow screens.

## Notifications and audit

Publish domain events after committed status changes. Notify relevant procurement/warehouse/manufacturing/sales users for dispatch, delays, discrepancies, failed deliveries, quality rejection, and delivery completion. Use the existing notification and audit infrastructure; add retry/idempotency protections and never send notifications before the database transaction commits.

## Implementation sequence and acceptance criteria

### Phase 1 — Shipment foundation
- Database migration(s), entities, tenant-scoped repositories, validated state transitions, shipment list/detail and event APIs.
- Tests for tenant isolation, role access, invalid transitions, event immutability, duplicate events, and transaction rollback.

### Phase 2 — Inbound materials
- Supplier shipment creation linked to existing purchase orders; carrier metadata; receipt reconciliation; partial receipt; lot creation; inspection/rejection; notifications.

### Phase 3 — Manufacturing genealogy
- Record input-lot consumption and output-lot creation against manufacturing orders; enforce quantity/unit validation; query forward/backward genealogy; handle rework and scrap.

### Phase 4 — Outbound delivery
- Link existing dispatches and sales orders to shipments; carrier adapters; proof of delivery; partial/failed delivery; returns; customer-safe tracking.

### Phase 5 — Frontend, integration, and verification
- Angular views and API service with separate HTML/TypeScript/spec files; responsive states and actionable errors; end-to-end tests; migration verification against a populated database; backend `mvn -B verify`; frontend build and tests.

## Important data-integrity rules

- A carrier's "delivered" milestone is not a goods receipt or customer acceptance by itself.
- Inventory changes must go through the existing inventory ledger; do not maintain a second stock balance in the tracking module.
- Lot genealogy must be many-to-many and quantity-aware.
- Shipment tracking history is append-only; corrections are new events with an explanation.
- Do not claim carrier live tracking until an actual carrier integration and credentials are configured.
- Preserve existing local Compose configuration and production-ready Compose configuration.
