# Payment

Records a payment attempt against an Order.

## Current state

| Method | Path | Notes |
|---|---|---|
| POST | `/v1/payments` | Body: restaurantCode, orderCode, paymentMethod, amount, transactionId?. Public endpoint (customer-facing). |
| GET | `/v1/payments/{code}` | |
| GET | `/v1/payments/search` | Paged. Filters: restaurantCode, orderCode, status, method. |
| PATCH | `/v1/payments/{code}/status` | Update status (PENDING/COMPLETED/FAILED/REFUNDED). |

Entity (`Payment`): `code`, `restaurantCode`, `orderCode`, `paymentMethod` (CASH/CARD/QR/UPI/etc), `amount`, `transactionId`, `status`.

## Missing / planned features

- [ ] **Auto-complete order on payment COMPLETED** — currently the payment status and order status are independent. A successful payment should also transition the order to COMPLETED.
- [ ] **Refund flow** — POST `/v1/payments/{code}/refund` with amount + reason. Sets status to REFUNDED, optionally reverses the order.
- [ ] **Partial / split payments** — multiple Payment rows per Order with their amounts summing to `Order.totalAmount`. Currently nothing enforces or supports this.
- [ ] **Daily revenue report** — `GET /v1/payments/report?restaurantCode=...&date=...` aggregating completed payments by method, by hour.
- [ ] **Payment gateway integration** — current entity holds `transactionId` for an external system, but there's no actual gateway wiring (Stripe / eSewa / Khalti / Razorpay). Adding requires webhook endpoints + signature verification.
- [ ] **Idempotency keys** — POST should accept an `Idempotency-Key` header so retries from flaky networks don't double-charge.
- [ ] **Tip/gratuity field** — `tipAmount` on Payment, added to `Order.totalAmount` at completion.
- [ ] **Invoice/receipt linkage** — emit a receipt URL after COMPLETED (could reuse the Orders receipt-PDF endpoint once it exists).
