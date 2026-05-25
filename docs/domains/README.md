# Domain Roadmaps

Per-domain feature roadmaps. Each file lists the **current state** (what's already implemented and exposed via REST) and the **missing / planned features** (checkboxes to drive future work).

| Domain | File | Owner module |
|---|---|---|
| Auth | [auth.md](auth.md) | `controller/AuthController`, `service/facade/AuthFacade` |
| User | [user.md](user.md) | `controller/UserController`, `service/UserService` |
| Restaurant | [restaurant.md](restaurant.md) | `controller/RestaurantController`, `service/RestaurantService` |
| Table | [table.md](table.md) | `controller/RestaurantTableController`, `service/RestaurantTableService` |
| Menu category | [menu-category.md](menu-category.md) | `controller/MenuCategoryController` |
| Menu item | [menu-item.md](menu-item.md) | `controller/MenuItemController` |
| Orders | [orders.md](orders.md) | `controller/OrdersController`, `service/facade/OrdersFacade` |
| Order item | [order-item.md](order-item.md) | `controller/OrderItemController`, `service/facade/OrderItemFacade` |
| Payment | [payment.md](payment.md) | `controller/PaymentController` |
| Employee | [employee.md](employee.md) | `controller/EmployeeController` |
| Attendance | [attendance.md](attendance.md) | `controller/AttendanceController` |
| Payroll | [payroll.md](payroll.md) | `controller/PayrollController` |
| File | [file.md](file.md) | `controller/FileController`, `service/FilePresignedUrlService` |

## Conventions used across all domain MDs

- **Current state** = endpoints/behavior actually present in `dev` HEAD.
- **Missing / planned features** = unchecked boxes. Tick them as we ship.
- API paths are prefixed by Spring's `context-path: /api`, so a doc that lists `GET /v1/orders/{code}` is really reachable at `https://<host>/api/v1/orders/{code}`.
- All entities use string PKs (UUIDs via `UuidUtil.generate()`), soft delete via `deleted_at`, and ModelMapper-based mappers.
