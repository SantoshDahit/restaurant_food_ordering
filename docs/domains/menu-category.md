# Menu category

Top-level grouping for menu items (e.g. "Appetizers", "Main Course", "Drinks").

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/menu-categories` | Body: restaurantCode, name, type, sortOrder?. |
| GET | `/v1/menu-categories/{code}` | |
| GET | `/v1/menu-categories/search` | Paged. Filters: restaurantCode, type. Public (per security memo). |
| PATCH | `/v1/menu-categories/{code}` | Update name, type, sortOrder. |
| DELETE | `/v1/menu-categories/{code}` | Soft-delete. Currently does **not** block when category still has menu items. |

Entity: `code`, `restaurantCode`, `name`, `type` (MenuCategoryType enum), `sortOrder`.

## Missing / planned features

- [ ] **Block delete when items reference the category** — MenuItemJpaRepository already has `existsByCategoryCodeAndDeletedAtIsNull(...)`. Service should call it and throw `ApiException` if present, or cascade-soft-delete items.
- [ ] **Reorder API** — PATCH `/v1/menu-categories/reorder` taking an ordered list of codes and bulk-updating `sortOrder`. Frontend currently has no drag-and-drop because of this gap.
- [ ] **Availability window** — `availableFrom`/`availableTo` time-of-day fields so e.g. "Breakfast" only shows 6-11am on the customer kiosk.
- [ ] **Localized names** — `nameI18n` JSON map for multi-language menus (English/Nepali at minimum given the NPR default currency).
- [ ] **Image / icon support** — `fileCode` field so categories can have a header image on the kiosk.
