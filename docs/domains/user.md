# User

Authenticated user accounts (admin/manager). One owner per Restaurant via `Restaurant.user`.

## Current state

| Method | Path | Notes |
|---|---|---|
| GET | `/v1/users/{code}` | Returns full user profile. |
| GET | `/v1/users/search` | Paged search by `role`, `fullName`. |

Entity (`User`): `code`, `fullName`, `email` (unique), `phone` (unique), `passwordHash`, `role` (ADMIN/MANAGER), `fileCode` (avatar), `isActive`. Soft delete via `BaseFullTimeEntity.deletedAt`.

## Missing / planned features

- [ ] **PATCH `/v1/users/{code}`** — update fullName, phone, fileCode. Entity has `update(...)`; only the controller method is missing.
- [ ] **POST `/v1/users/{code}/password`** — change password (current + new). Entity has `updatePassword(...)`.
- [ ] **DELETE `/v1/users/{code}`** — soft-delete (`deactivate()`). Currently no controller.
- [ ] **GET `/v1/users/me`** — return the authenticated user from JWT (avoids the client tracking its own code).
- [ ] **Avatar upload integration** — POST to file API → set `fileCode` via PATCH. Document as flow, not a new endpoint.
- [ ] **Last-login tracking** — entity field + update on login. Useful for "inactive user" cleanup.
- [ ] **Role check on writes** — currently the SecurityConfig is permissive (`permitAll`). Add `@PreAuthorize` so only ADMIN can list/modify users.
