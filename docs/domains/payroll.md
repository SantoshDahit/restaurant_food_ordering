# Payroll

Monthly salary record per employee.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/payroll` | Body: restaurantCode, employeeCode, payPeriodStart, payPeriodEnd, grossSalary, deductions?, netSalary, status. Currently the caller computes everything. |
| GET | `/v1/payroll/{code}` | |
| GET | `/v1/payroll/search` | Paged. Filters: restaurantCode, employeeCode, status, date range. |
| PATCH | `/v1/payroll/{code}/status` | Update status (PENDING / PAID / FAILED — see SalaryStatus enum). |

Entity: `code`, `restaurantCode`, `employeeCode`, `payPeriodStart`, `payPeriodEnd`, `grossSalary`, `deductions`, `netSalary`, `status`, `paidAt`.

## Missing / planned features

- [ ] **Auto-generate payroll from attendance** — `POST /v1/payroll/generate?restaurantCode=...&month=...` that:
  1. Lists active employees.
  2. Sums attendance hours via the Attendance summary endpoint.
  3. Computes `grossSalary = monthlySalary × (presentDays / workingDays)` (or hourly × hours).
  4. Creates PENDING payroll rows in bulk.
- [ ] **Structured deductions** — currently a single `deductions` decimal. Break into list: `{type, amount, note}` e.g. tax, advance, late penalty.
- [ ] **Bonuses / overtime** — separate fields.
- [ ] **Payslip PDF** — `GET /v1/payroll/{code}/payslip.pdf`.
- [ ] **Bulk mark-as-paid** — when the owner pays multiple employees at once.
- [ ] **Tax tables / regulatory withholding** — country-specific (NPR currency suggests Nepal). Configurable per-restaurant tax brackets.
- [ ] **Block deleting/editing PAID rows** — once paid, payroll is read-only for audit.
- [ ] **Currency normalization** — currently no field; pulls from Restaurant. If multi-currency staff is ever needed, denormalize.
