# Attendance

Daily clock-in/clock-out per employee.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/attendance` | Body: restaurantCode, employeeCode, checkInTime, status. Records a clock-in. |
| GET | `/v1/attendance/search` | Paged. Filters: restaurantCode, employeeCode, status, date range. |
| PATCH | `/v1/attendance/{code}` | Update — typically used to set `checkOutTime` and recompute `hoursWorked`. |

Entity: `code`, `restaurantCode`, `employeeCode`, `checkInTime`, `checkOutTime` (nullable), `hoursWorked` (computed at checkout), `status` (PRESENT, ABSENT, HALF_DAY, LATE…), `notes`.

## Missing / planned features

- [ ] **Dedicated check-out endpoint** — POST `/v1/attendance/{code}/check-out` that captures current timestamp and computes `hoursWorked` instead of expecting the client to set it via PATCH.
- [ ] **One-click "Clock in" for the logged-in employee** — POST `/v1/attendance/clock-in` taking just the employee code from the auth context.
- [ ] **Prevent duplicate clock-in same day** — entity has no uniqueness check; today an employee can have N rows for the same date.
- [ ] **Auto-mark ABSENT at end of day** — scheduled job at e.g. 23:59 that creates ABSENT rows for any active employee with no PRESENT record. Currently no Spring `@Scheduled` tasks exist.
- [ ] **Late-arrival detection** — compare `checkInTime` to scheduled shift start (depends on Employee schedule feature).
- [ ] **Monthly summary endpoint** — `GET /v1/attendance/summary?restaurantCode=...&employeeCode=...&month=...` returning days present/absent/late + total hours. Used by Payroll.
- [ ] **Geolocation / device fingerprint** on clock-in for fraud prevention.
- [ ] **Manager override / approval** — for "edit attendance" requests by employees.
