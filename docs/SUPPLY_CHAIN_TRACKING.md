# Supply-chain tracking and traceability

All endpoints below are under `/api/v1/supply-chain`. Administrative endpoints require the roles specified in their controllers.

## Linked business records

- Shipment creation validates supplied purchase order, goods receipt, sales order and dispatch IDs against tenant-scoped repositories. A goods receipt can be supplied by receipt number or numeric ID. A goods receipt resolves its purchase order automatically; a dispatch resolves its sales order automatically.
- Raw-material lot receipt validates the purchase order line, product/material, supplier and warehouse against the linked goods receipt / purchase order. An inbound shipment can supply the purchase-order and goods-receipt links.
- Inventory dispatch events automatically create an outbound shipment. Duplicate dispatch events return the existing shipment instead of creating a duplicate.
- The current repository did not contain a manufacturing-order domain, so a small manufacturing-order record/API was added for traceability: `GET/POST /manufacturing-orders` and `PATCH /manufacturing-orders/{id}/status`. Production lots must match the order's product, an open order status, and the planned quantity.

## Quality and returns

- `POST /lots/{lotCode}/inspection` accepts incremental accepted/rejected quantities. The sum cannot exceed the uninspected remainder. Accepted quantities are posted through the inventory transaction service; rejected quantities are not made available as stock. Each inspection is stored as an immutable audit record.
- `POST /shipments/{shipmentId}/returns` records a unique return reference, product, receiving warehouse, quantity, reason and disposition. Only shipments in `RETURNED` status can be received. `RESTOCK` posts a positive inventory transaction; `QUARANTINE` and `SCRAP` do not add usable stock.
- Shipment status milestones, lot inspection records and return records provide the audit trail. Public tracking deliberately omits internal event remarks and business identifiers.

## Carrier webhook configuration

The callback is `POST /carrier-webhooks/{shipmentId}` and requires:
- `X-Carrier-Provider`: a provider slug, e.g. `shiprocket` or `delhivery`
- `X-Carrier-Signature`: `sha256=<lowercase-hex-HMAC>`

Compute HMAC-SHA256 over the exact raw request body using the provider's configured secret. The JSON body must contain `externalEventId` and `status`; optional fields are `eventTime`, `location`, and `remarks`. Provider + external event ID form the idempotency key. Webhook updates are tagged with their carrier in shipment event history.

Configure secrets outside source control:
- `SIVION_SUPPLY_CHAIN_CARRIERS_SHIPROCKET_WEBHOOK_SECRET`
- `SIVION_SUPPLY_CHAIN_CARRIERS_DELHIVERY_WEBHOOK_SECRET`

These secrets authenticate signed callbacks; they do not replace a carrier's API credentials or implement provider-specific label creation/rate quoting/polling. Those outbound API calls require the actual carrier account, API contract and credentials to be selected/configured.

## Notification delivery and failures

Supply-chain events are published after the owning database transaction commits. Rabbit template and listener retries are configured (up to five attempts with exponential backoff). Events rejected after retry are dead-lettered; the dispatch integration dead-letter queue is `sivion.supply-chain.dispatches.dlq`, and notifications use `sivion.notifications.domain-events.dlq`. Monitor and replay dead-lettered messages after correcting the underlying cause.

## Verification

CI runs `mvn -B verify`, frontend unit tests, `npm run build`, and Docker image builds. This feature uses fresh test fixtures; it does not depend on production data. Run the backend and frontend checks from the repository's existing CI workflow before deployment.
