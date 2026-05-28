package com.restaurant.api.config;

import com.restaurant.api.service.OrdersService;
import com.restaurant.api.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Idempotent backfills that run on every startup. No-op when there's nothing
 * to do. Two passes:
 *   1) Assign daily tickets to orders that were created before V12.
 *   2) Issue receipts for any payments still missing one (V10-era orphans).
 */
@Component
@RequiredArgsConstructor
public class ReceiptBackfillRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(ReceiptBackfillRunner.class);

    private final OrdersService ordersService;
    private final ReceiptService receiptService;

    @Override
    public void run(String... args) {
        try {
            int tickets = ordersService.backfillTickets();
            if (tickets > 0) log.info("Backfilled {} order ticket(s).", tickets);
        } catch (Exception e) {
            log.warn("Order ticket backfill failed; will retry on next startup.", e);
        }
        try {
            int issued = receiptService.backfillMissing();
            if (issued > 0) log.info("Backfilled {} receipt(s) for payments missing them.", issued);
        } catch (Exception e) {
            log.warn("Receipt backfill failed; will retry on next startup.", e);
        }
    }
}
