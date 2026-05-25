# Order item

A single line item on an Order. Holds a snapshot of `unitPrice` at order time so menu price changes don't retroactively alter past orders.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/orders/{orderCode}/items` | Body: menuItemCode, quantity, discountAmount?, spiceLevel?, notes?. Looks up `MenuItem.price` and snapshots it. Triggers `OrdersFacade.recalculate(orderCode)`. Public. |
| GET | `/v1/orders/{orderCode}/items` | Lists items for an order. Detail aggregate (`/orders/{code}/detail`) already includes them with names — prefer that. |
| PATCH | `/v1/orders/{orderCode}/items/{code}` | Update quantity, spiceLevel, notes. Recalculates totals. |
| DELETE | `/v1/orders/{orderCode}/items/{code}` | Hard-deletes. Recalculates totals. |

Entity (`OrderItem`): `code`, `orderCode`, `menuItemCode`, `quantity`, `unitPrice` (snapshot), `discountAmount`, `totalPrice` (computed), `spiceLevel`, `notes`, `status` (mirrors Orders.status), `createdAt`. No soft delete — items are hard-removed when cancelled before order completion.

## Missing / planned features

- [ ] **Per-item status divergence** — currently `OrdersFacade.updateStatus` cascades the order status to every item. A real kitchen needs per-item independent progression (one dish ready, another still preparing). Decouple item status from order status; compute order status from item statuses.
- [ ] **Soft-delete instead of hard-delete** — for audit/reporting. Keep `deletedAt` on the entity, exclude from `findByOrderCode`.
- [ ] **Bulk add** — POST `/v1/orders/{orderCode}/items/bulk` taking `items[]`. Avoids the per-item loop the frontend currently runs.
- [ ] **Modifiers / options** — once MenuItem has variants, OrderItem needs to capture which variant + add-ons + price delta.
- [ ] **Course / serving group** — assign an item to "Starter"/"Main" so kitchen can sequence releases.
- [ ] **"Sent to kitchen" timestamp** vs. created timestamp — track when item was actually fired vs. when it was added.
- [ ] **Quantity = 0 protection** — `PatchRequest` accepts any Integer. Guard against ≤0.
