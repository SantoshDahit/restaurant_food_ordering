# Auth

JWT-based authentication. Issues an access token (1h) and a refresh token (7d) on successful login/register.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/auth/register` | Body: `fullName, email, phone?, password, role`. Creates User, returns `LoginResponse` (accessToken + refreshToken + user). |
| POST | `/v1/auth/login` | Body: `email, password`. Returns `LoginResponse`. |

JWT secret read from `${JWT_SECRET}` env. Tokens signed HS256. Filter at `security/jwt/JwtAuthenticationFilter` validates `Authorization: Bearer <token>` on every request; populates SecurityContext.

## Missing / planned features

- [ ] **POST `/v1/auth/refresh`** — exchange a valid refresh token for a new access token without re-entering password.
- [ ] **POST `/v1/auth/logout`** — invalidate refresh token (requires a refresh-token store/blocklist; currently stateless).
- [ ] **POST `/v1/auth/forgot-password`** — email a one-time reset link.
- [ ] **POST `/v1/auth/reset-password`** — consume reset token + new password.
- [ ] **POST `/v1/auth/change-password`** — authenticated; takes current + new password.
- [ ] **Rate limiting on `/login`** to slow brute-force attempts (e.g. 5/min per IP).
- [ ] **Email verification on register** — send code, gate active=true on confirmation.
- [ ] **OAuth providers** (Google/Kakao) if customer-facing logins are ever needed beyond admin.
