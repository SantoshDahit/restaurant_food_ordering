# Restaurant

The top-level tenant entity. One Restaurant per owner User. Every other domain (tables, menu, orders, etc.) carries a `restaurantCode` FK pointing here.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/restaurants` | Body: name, address, businessNumber, userCode, phone?, email?, currency?, fileCode?. Auto-generates UUID `code` AND a 6-char `kioskCode` (collision-checked). |
| GET | `/v1/restaurants/{code}` | By UUID code. |
| GET | `/v1/restaurants/by-owner/{userCode}` | Lookup by the owner user's code. Used after login to discover the user's restaurant. |
| GET | `/v1/restaurants/by-kiosk-code/{kioskCode}` | Public lookup by the short 6-char code. Used by KioskView to resolve the friendly code → UUID before fetching menus. |
| GET | `/v1/restaurants/search` | Paged search by name. |
| PATCH | `/v1/restaurants/{code}` | Update name/address/phone/email/currency/fileCode. Does NOT change kioskCode. |
| DELETE | `/v1/restaurants/{code}` | Soft-delete (`deactivate()`). |

Entity (`Restaurant`): `code` (UUID), `kioskCode` (unique 6-char), `user` (ManyToOne), `name`, `address`, `businessNumber`, `phone`, `email`, `currency` (default NPR), `fileCode` (logo), `isActive`. Flyway migration V3 added `kiosk_code`.

## Missing / planned features

- [ ] **Regenerate kiosk code** — POST `/v1/restaurants/{code}/kiosk-code` if the owner wants a new one (e.g. printed material went out of date). Entity needs a setter method.
- [ ] **Working hours / open-closed status** — fields + endpoint. Affects whether kiosk/QR can place orders.
- [ ] **Tax & service-charge config** — currently `Orders.taxAmount` is always 0. Restaurant-level percent → applied per order.
- [ ] **Multiple locations per owner** — currently 1:1 User↔Restaurant. If chain support is desired, drop the unique constraint and add a "current restaurant" picker in the dashboard.
- [ ] **Logo upload UX** — file API exists; the create/patch form should let the owner upload during creation, not after.
- [ ] **Public profile page** — `GET /v1/restaurants/by-kiosk-code/{kioskCode}/public` returning only safe fields (name, logo, address) for the customer landing screen.
