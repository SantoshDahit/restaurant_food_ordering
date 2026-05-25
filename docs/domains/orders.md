# Orders

A customer's order. Has many `OrderItem`s, may have a `tableCode`, may have a `waiterCode`, ends with a `Payment`.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/orders` | Body: restaurantCode, tableCode?, waiterCode?, orderType (DINE_IN/TAKEAWAY/QR_ORDER/KIOSK), specialNotes?, deviceType?. Auto-generates `orderNumber = ORD-<timestamp>`. Auto-flips referenced table to OCCUPIED. Public endpoint (customer-facing). |
| GET | `/v1/orders/{code}` | Order metadata only (no items). |
| GET | `/v1/orders/{code}/detail` | **Aggregate** response: order + items + resolved restaurant/table/waiter/menu-item names in a single round-trip. Used by OrderDetailView. |
| GET | `/v1/orders/search` | Paged. Filters: restaurantCode, status, orderType, tableCode. Public. |
| PATCH | `/v1/orders/{code}/status` | Sets order status. **Side effects**: cascades the new status to every OrderItem, and (if terminal: COMPLETED or CANCELLED) frees the referenced table back to AVAILABLE. |
| DELETE | `/v1/orders/{code}` | Soft-deletes + sets status to CANCELLED + frees the table. |

Statuses: PENDING → CONFIRMED → PREPARING → READY → COMPLETED, or CANCELLED at any point.

Entity (`Orders`): `code`, `restaurantCode`, `tableCode`, `waiterCode`, `orderNumber` (unique), `orderType`, `status`, `subtotal`, `discountAmount`, `taxAmount`, `totalAmount`, `specialNotes`, `deviceType`, soft-delete timestamps.

Totals are recalculated by `OrdersFacade` after every item add/update/remove via `OrdersService.recalculateTotals`.

## Missing / planned features

- [ ] **Date-range filter on search** — `from`/`to` query params. Required for any "today's orders" or "weekly revenue" view.
- [ ] **Kitchen view endpoint** — `GET /v1/orders/kitchen?restaurantCode=...` returning PENDING/CONFIRMED/PREPARING orders with full items, sorted by `createdAt` asc. Currently the frontend filters client-side.
- [ ] **Bulk item add at create time** — POST `/v1/orders` currently creates an empty order and forces a second loop of `POST /items`. A `items[]` field in the create body would atomize the operation.
- [ ] **Tax & service charge** — fields are on the entity but `taxAmount` is always 0. Apply restaurant-level percent during `recalculateTotals`.
- [ ] **Discount codes / promotions** — currently only per-item `discountAmount`. Restaurant-wide promo logic absent.
- [ ] **Customer-side order tracking** — `GET /v1/orders/by-number/{orderNumber}` public endpoint so a QR/kiosk customer can check status from their phone after ordering.
- [ ] **Receipt PDF** — `GET /v1/orders/{code}/receipt.pdf`.
- [ ] **Order printing webhook / SSE for kitchen display system** — push status changes to subscribed clients.
- [ ] **Refund flow** — currently only via Payment.status. Modeling a refund-against-order with reason/amount.
- [ ] **Order number generator should be per-restaurant + daily-resetting** (e.g. ORD-A1B2-001 for the day) instead of millisecond-since-epoch.
