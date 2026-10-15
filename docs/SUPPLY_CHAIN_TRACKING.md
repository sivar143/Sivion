# Supply-chain tracking and traceability

All endpoints below are under `/api/v1/supply-chain`. Administrative endpoints require the roles specified in their controllers.

## Linked business records

- Shipment creation validates supplied purchase order, goods receipt, sales order and dispatch IDs against tenant-scoped repositories. A goods receipt can be supplied by receipt number or numeric ID. A goods receipt resolves its purchase order automatically; a dispatch resolves its sales order automatically.
- Raw-material lot receipt validates the purchase order line, product/material, supplier and warehouse against the linked goods receipt / purchase order. An inbound shipment can supply the purchase-order and goods-receipt links.
- `POST /shipments/{shipmentId}/receipts` receives a partial or final shipment quantity against the linked purchase order. It creates an existing procurement `GoodsReceipt`, a raw-material lot in `PENDING_INSPECTION` state, and an immutable shipment-to-receipt allocation. Accepted quantity remains zero until inspection, so uninspected stock is not made available. The same `receiptReference` and payload are idempotent; a conflicting retry returns `409`. `GET /shipments/{shipmentId}/receipts` returns the receipt history.
- Inventory dispatch events automatically create an outbound shipment. Duplicate dispatch events return the existing shipment instead of creating a duplicate.
- The current repository did not contain a manufacturing-order domain, so a small manufacturing-order record/API was added for traceability: `GET/POST /manufacturing-orders` and `PATCH /manufacturing-orders/{id}/status`. Production lots must match the order's product, an open order status, and the planned quantity.

## Quality and returns

- `POST /lots/{lotCode}/inspection` accepts incremental accepted/rejected quantities. The sum cannot exceed the uninspected remainder. Accepted quantities are posted through the inventory transaction service; rejected quantities are not made available as stock. Each inspection is stored as an immutable audit record.
- `POST /lot-consumptions` records raw-material genealogy and decrements the same warehouse's available inventory in the same transaction.
- `POST /production-lots/{lotCode}/shipments` allocates an accepted finished-lot quantity to an outbound shipment. Allocations are bounded by both the accepted lot quantity and the linked inventory dispatch quantity; `GET` on the same path lists allocations.
- `POST /shipments/{shipmentId}/returns` records a unique return reference, product, receiving warehouse, quantity, reason and disposition. Only shipments in `RETURNED` status can be received. `RESTOCK` posts a positive inventory transaction; `QUARANTINE` and `SCRAP` do not add usable stock.
- Shipment status milestones, lot inspection records and return records provide the audit trail. Public tracking deliberately omits internal event remarks and business identifiers.

## Carrier webhook configuration

The Shiprocket callback URL is `POST /api/v1/supply-chain/carrier-webhooks/shiprocket/{shipmentId}` (so Shiprocket does not need to send a custom provider header). Other configured carriers use `POST /api/v1/supply-chain/carrier-webhooks/{shipmentId}` with `X-Carrier-Provider`.

- **Shiprocket webhook:** uses the configured `X-API-Key` callback token. Payload fields such as `awb`, `current_status`, `current_status_id`, and `current_timestamp` are normalized into Sivion shipment milestones.
- **Other providers:** use `X-Carrier-Signature: sha256=<lowercase-hex-HMAC>`, computed as HMAC-SHA256 over the exact raw request body with that provider's configured secret. The JSON body must contain `externalEventId` and `status`; optional fields are `eventTime`, `location`, and `remarks`.

Provider + external event ID form the idempotency key. Webhook updates are tagged with their carrier in shipment event history. Shiprocket can also be refreshed on demand using `POST /api/v1/supply-chain/shipments/{id}/carrier-refresh`; the backend obtains and caches a bearer token and calls the Shiprocket AWB tracking API.

Configure credentials outside source control:
- `SIVION_SUPPLY_CHAIN_CARRIERS_SHIPROCKET_WEBHOOK_SECRET`
- `SIVION_SHIPROCKET_API_EMAIL`
- `SIVION_SHIPROCKET_API_PASSWORD`
- `SIVION_SUPPLY_CHAIN_CARRIERS_DELHIVERY_WEBHOOK_SECRET`

Shiprocket live refresh requires an API user configured in Shiprocket and a valid AWB. API credentials are only read by the backend. Other carriers still need their own documented adapter/API credentials before live polling or label creation can be enabled.

## Notification delivery and failures

Supply-chain events are published after the owning database transaction commits. Rabbit template and listener retries are configured (up to five attempts with exponential backoff). Events rejected after retry are dead-lettered; the dispatch integration dead-letter queue is `sivion.supply-chain.dispatches.dlq`, and notifications use `sivion.notifications.domain-events.dlq`. Monitor and replay dead-lettered messages after correcting the underlying cause.

## Verification

CI runs `mvn -B verify`, frontend unit tests, `npm run build`, and Docker image builds. This feature uses fresh test fixtures; it does not depend on production data. Run the backend and frontend checks from the repository's existing CI workflow before deployment.
