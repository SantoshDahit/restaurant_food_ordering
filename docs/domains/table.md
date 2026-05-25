# Table (RestaurantTable)

Physical tables in a restaurant. Each can have a QR token printed for customer-side QR ordering.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/tables` | Body: restaurantCode, tableNumber, capacity?. Rejects duplicate `tableNumber` within a restaurant. |
| GET | `/v1/tables/{code}` | |
| GET | `/v1/tables/search` | Paged. Filters: restaurantCode, status. |
| PATCH | `/v1/tables/{code}` | Update tableNumber, capacity, status. |
| DELETE | `/v1/tables/{code}` | Soft-delete (`deactivate()`). |
| POST | `/v1/tables/{code}/qr` | Generate a new `qrCodeToken` and `qrCodeUrl` for the table. Regenerating invalidates the previous QR. |
| GET | `/v1/tables/by-token/{token}` | Public lookup — customer scans QR, frontend resolves token → table. |

Entity (`RestaurantTable`): `code`, `restaurantCode`, `tableNumber`, `capacity` (default 4), `status` (AVAILABLE/OCCUPIED/RESERVED/CLEANING), `qrCodeUrl`, `qrCodeToken` (unique), `isActive`.

Status sync: `OrdersFacade` auto-flips status to OCCUPIED when a DINE_IN/QR_ORDER with a `tableCode` is created, and back to AVAILABLE on COMPLETED/CANCELLED. RESERVED/CLEANING states are preserved.

## Missing / planned features

- [ ] **Bulk QR regeneration** — POST `/v1/tables/bulk-qr?restaurantCode=...` for printing a fresh batch.
- [ ] **Printable QR sheet** — backend endpoint that returns a PDF with all tables' QR codes labeled (currently the frontend renders one PNG at a time).
- [ ] **Reserved-window support** — RESERVED status carries no time data. Add `reservedFrom`/`reservedTo` + auto-flip when window passes.
- [ ] **Table layout / seating plan** — `x`, `y`, `width`, `height` for a drag-and-drop floor plan UI.
- [ ] **Active-order link on the table object** — currently `TableListView` fetches all orders client-side and groups by tableCode. Backend `GET /v1/tables/{code}/active-order` would be cleaner.
- [ ] **Auto-cleaning timer** — when status goes COMPLETED → AVAILABLE, optionally route through CLEANING for N minutes first.
