# Employee

Restaurant staff — waiters, kitchen, cleaners. Separate from `User` (which is for admin/manager logins). Employees are referenced by `Orders.waiterCode` and feed into `Attendance` and `Payroll`.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/employees` | Body: restaurantCode, fullName, phone, role, hireDate, monthlySalary?, fileCode?. |
| GET | `/v1/employees/{code}` | |
| GET | `/v1/employees/search` | Paged. Filters: restaurantCode, role, isActive. |
| PATCH | `/v1/employees/{code}` | Update editable fields. |
| DELETE | `/v1/employees/{code}` | Soft-delete. |

Entity: `code`, `restaurantCode`, `fullName`, `phone`, `role` (free string — WAITER, KITCHEN, CLEANER, etc), `hireDate`, `monthlySalary`, `fileCode`, `isActive`.

## Missing / planned features

- [ ] **Promote role to enum** — currently a free string. Add `EmployeeRole` enum (WAITER, KITCHEN, MANAGER, CLEANER, CASHIER…).
- [ ] **Terminate / rehire flow** — `terminationDate` field + `terminate(date)` method on entity. Today the only signal is `isActive=false`.
- [ ] **Schedule / shift assignment** — separate `EmployeeShift` entity (employee, day-of-week, start, end) so the dashboard can render a weekly grid.
- [ ] **PIN / passcode login for waiters** — Waiter mode in the frontend currently authenticates as the admin user. Each waiter should have a 4-6 digit PIN so orders are attributable to them. New endpoint POST `/v1/employees/{code}/pin` + a separate auth endpoint.
- [ ] **Tip allocation per employee** — when tips become a Payment field, split rules per role.
- [ ] **Photo (`fileCode`) actually shown in dashboard** — backend supports it, frontend doesn't render it on the Employee list yet.
- [ ] **Bank/payout details** for payroll — bank name, account number, optional KYC field — but consider encryption at rest before adding.
