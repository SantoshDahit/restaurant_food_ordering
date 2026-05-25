# Menu item

Individual dish/drink that customers order.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/menu-items` | Body: restaurantCode, categoryCode?, name (unique within restaurant), description?, price, discountPercent?, fileCode?, isVeg?, prepTimeMinutes?, sortOrder?. |
| GET | `/v1/menu-items/{code}` | |
| GET | `/v1/menu-items/search` | Paged. Filters: restaurantCode, categoryCode, availability, isFeatured, isVeg. Public. |
| PATCH | `/v1/menu-items/{code}` | Update any field. |
| DELETE | `/v1/menu-items/{code}` | Soft-delete. |

Entity: `code`, `restaurantCode`, `categoryCode`, `name`, `description`, `price`, `discountPercent` (default 0), `fileCode`, `availability` (AVAILABLE / OUT_OF_STOCK), `isFeatured`, `isVeg`, `prepTimeMinutes` (default 15), `sortOrder`.

## Missing / planned features

- [ ] **Bulk availability toggle** — PATCH `/v1/menu-items/bulk-availability` with a list of codes → AVAILABLE/OUT_OF_STOCK. Useful for "marked sold out for the night".
- [ ] **Cost-of-goods field** — `costPrice` for margin reporting in the dashboard.
- [ ] **Stock count** — currently `availability` is binary. Add optional `stockCount` that decrements per order and auto-flips to OUT_OF_STOCK at 0.
- [ ] **Variants / options** — size (small/medium/large), add-ons (extra cheese, no onion). Currently `OrderItem.notes` is the only freeform place; structured variants would let pricing adjust.
- [ ] **Spice level enum** — `OrderItem.spiceLevel` is a free string; lock to MILD/MEDIUM/HOT and surface as a menu-item-level "is spicy" flag.
- [ ] **Reorder API** — same drag-and-drop story as menu categories.
- [ ] **Per-language name/description** — i18n JSON.
- [ ] **Allergen tags** — multi-select (gluten, dairy, nuts) for customer filtering.
- [ ] **Image presigned-URL flow already exists** — frontend MenuItemView uses it. Mark this as done in `file.md`.
