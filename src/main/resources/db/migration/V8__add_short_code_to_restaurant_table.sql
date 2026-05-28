-- =====================================================================
-- V8__add_short_code_to_restaurant_table.sql
-- Add a short, human-friendly short_code to each table (e.g. BG-7K2N).
-- Backfill any existing rows with a derived initials-prefix + random suffix
-- so the NOT NULL UNIQUE constraint can be applied.
-- =====================================================================

ALTER TABLE restaurant_table
    ADD COLUMN short_code VARCHAR(20) NULL UNIQUE AFTER code;

-- Backfill: <up-to-3-letter restaurant initials>-<6 hex chars uppercase>.
-- The suffix is the first 6 chars of MD5(rt.code) — this gives a uniformly
-- distributed hash per row so collisions inside one restaurant are vanishingly
-- rare. Plain UUID prefixes can repeat when rows are inserted in the same ms.
UPDATE restaurant_table rt
JOIN restaurant r ON r.code = rt.restaurant_code
SET rt.short_code = CONCAT(
    UPPER(LEFT(REGEXP_REPLACE(r.name, '[^A-Za-z]', ''), 3)),
    '-',
    UPPER(SUBSTRING(MD5(rt.code), 1, 6))
)
WHERE rt.short_code IS NULL;

ALTER TABLE restaurant_table
    MODIFY COLUMN short_code VARCHAR(20) NOT NULL;
