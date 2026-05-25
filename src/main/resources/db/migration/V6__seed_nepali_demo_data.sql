-- =====================================================================
-- V6__seed_nepali_demo_data.sql
-- Demo seed: 10 Nepali-themed restaurants, each with a manager owner,
-- 6 menu categories, 28 menu items, and 4 tables. Plus 5 STAFF users.
--
-- All passwords for seeded users = "password123"
--   BCrypt hash: $2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO
--
-- All `code` columns use MySQL's UUID() function so every row gets a
-- fresh, real UUID. Parent-child relationships are resolved via
-- INSERT...SELECT joins on natural keys (email, kiosk_code, name).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Managers (10) and Staff (5)
-- ---------------------------------------------------------------------
INSERT INTO user (code, full_name, email, phone, password_hash, role, is_active, created_at, updated_at) VALUES
  (UUID(), 'Ram Bahadur Thapa',     'ram.thapa@himalayan.np',       '9851000001', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Sita Sharma',           'sita.sharma@annapurna.np',     '9851000002', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Hari Prasad Acharya',   'hari.acharya@newarighar.np',   '9851000003', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Maya Gurung',           'maya.gurung@thakali.np',       '9851000004', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Krishna Tamang',        'krishna.tamang@yalacafe.np',   '9851000005', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Sunita Magar',          'sunita.magar@sherpakitchen.np','9851000006', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Ramesh Limbu',          'ramesh.limbu@gorkhagrill.np',  '9851000007', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Anjali Rai',            'anjali.rai@pokharaplates.np',  '9851000008', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Bishnu Karki',          'bishnu.karki@bhojangriha.np',  '9851000009', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),
  (UUID(), 'Pratima Shrestha',      'pratima.shrestha@mustang.np',  '9851000010', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'MANAGER', 1, NOW(), NOW()),

  (UUID(), 'Bikash Lama',           'bikash.lama@staff.np',         '9841000001', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'STAFF',   1, NOW(), NOW()),
  (UUID(), 'Prakash Bhattarai',     'prakash.bhattarai@staff.np',   '9841000002', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'STAFF',   1, NOW(), NOW()),
  (UUID(), 'Nirmala Adhikari',      'nirmala.adhikari@staff.np',    '9841000003', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'STAFF',   1, NOW(), NOW()),
  (UUID(), 'Deepak Khadka',         'deepak.khadka@staff.np',       '9841000004', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'STAFF',   1, NOW(), NOW()),
  (UUID(), 'Sapana Chhetri',        'sapana.chhetri@staff.np',      '9841000005', '$2a$10$wEDsyK40EAAp7z8Au6DXouR2LQp4rPK1Qg9uIxlk9krb8YfnnADdO', 'STAFF',   1, NOW(), NOW());

-- ---------------------------------------------------------------------
-- 2. Restaurants (one per manager) — link via user.email
-- ---------------------------------------------------------------------
INSERT INTO restaurant (code, kiosk_code, user_code, name, address, business_number, phone, email, currency, is_active, created_at, updated_at)
SELECT UUID(), data.kiosk_code, u.code, data.name, data.address, data.business_number, data.phone, data.email, 'NPR', 1, NOW(), NOW()
FROM user u
JOIN (
  SELECT 'HIMAKL' AS kiosk_code, 'Himalayan Kitchen' AS name, 'Thamel, Kathmandu'             AS address, 'BN-001' AS business_number, '01-4441001' AS phone, 'hello@himalayan.np'     AS email, 'ram.thapa@himalayan.np'        AS owner_email UNION ALL
  SELECT 'ANNAPL',                 'Annapurna Cafe',           'Lakeside, Pokhara',                       'BN-002',                    '061-460002',           'hello@annapurna.np',                'sita.sharma@annapurna.np' UNION ALL
  SELECT 'NEWARG',                 'Newari Ghar',              'Asan, Kathmandu',                         'BN-003',                    '01-4441003',           'hello@newarighar.np',               'hari.acharya@newarighar.np' UNION ALL
  SELECT 'THAKAL',                 'Thakali Bhansa',           'Jhamsikhel, Lalitpur',                    'BN-004',                    '01-5541004',           'hello@thakali.np',                  'maya.gurung@thakali.np' UNION ALL
  SELECT 'YALACF',                 'Yala Cafe',                'Patan Durbar Square, Lalitpur',           'BN-005',                    '01-5541005',           'hello@yalacafe.np',                 'krishna.tamang@yalacafe.np' UNION ALL
  SELECT 'SHERPK',                 'Sherpa Kitchen',           'Boudha, Kathmandu',                       'BN-006',                    '01-4441006',           'hello@sherpakitchen.np',            'sunita.magar@sherpakitchen.np' UNION ALL
  SELECT 'GORKAG',                 'Gorkha Grill',             'Durbarmarg, Kathmandu',                   'BN-007',                    '01-4441007',           'hello@gorkhagrill.np',              'ramesh.limbu@gorkhagrill.np' UNION ALL
  SELECT 'POKHAR',                 'Pokhara Plates',           'Phewa Lake, Pokhara',                     'BN-008',                    '061-460008',           'hello@pokharaplates.np',            'anjali.rai@pokharaplates.np' UNION ALL
  SELECT 'BHOJGR',                 'Bhojan Griha',             'Dilli Bazaar, Kathmandu',                 'BN-009',                    '01-4441009',           'hello@bhojangriha.np',              'bishnu.karki@bhojangriha.np' UNION ALL
  SELECT 'MUSTNG',                 'Mustang Cafe',             'Jomsom, Mustang',                         'BN-010',                    '069-440010',           'hello@mustang.np',                  'pratima.shrestha@mustang.np'
) data ON u.email = data.owner_email;

-- ---------------------------------------------------------------------
-- 3. Tables — 4 per restaurant, capacities 2/4/4/6
-- ---------------------------------------------------------------------
INSERT INTO restaurant_table (code, restaurant_code, table_number, capacity, status, is_active, created_at, updated_at)
SELECT UUID(), r.code, CONCAT('T', t.n), t.cap, 'AVAILABLE', 1, NOW(), NOW()
FROM restaurant r
CROSS JOIN (
  SELECT 1 AS n, 2 AS cap UNION ALL
  SELECT 2,        4      UNION ALL
  SELECT 3,        4      UNION ALL
  SELECT 4,        6
) t
WHERE r.kiosk_code IN ('HIMAKL','ANNAPL','NEWARG','THAKAL','YALACF','SHERPK','GORKAG','POKHAR','BHOJGR','MUSTNG');

-- ---------------------------------------------------------------------
-- 4. Menu categories — 6 per restaurant
-- ---------------------------------------------------------------------
INSERT INTO menu_category (code, restaurant_code, name, category_type, sort_order, is_active, created_at, updated_at)
SELECT UUID(), r.code, c.name, c.category_type, c.sort_order, 1, NOW(), NOW()
FROM restaurant r
CROSS JOIN (
  SELECT 1 AS sort_order, 'Momos'             AS name, 'NON_VEG'    AS category_type UNION ALL
  SELECT 2,                'Khana Sets',                'SPECIALS' UNION ALL
  SELECT 3,                'Chowmein & Thukpa',         'NON_VEG' UNION ALL
  SELECT 4,                'Snacks',                    'APPETIZERS' UNION ALL
  SELECT 5,                'Beverages',                 'DRINKS' UNION ALL
  SELECT 6,                'Sweets',                    'DESSERTS'
) c
WHERE r.kiosk_code IN ('HIMAKL','ANNAPL','NEWARG','THAKAL','YALACF','SHERPK','GORKAG','POKHAR','BHOJGR','MUSTNG');

-- ---------------------------------------------------------------------
-- 5. Menu items — joined to category by (restaurant_code, category name)
-- ---------------------------------------------------------------------
INSERT INTO menu_item (code, restaurant_code, category_code, name, description, price, discount_percent, availability, is_featured, is_veg, prep_time_minutes, sort_order, created_at, updated_at)
SELECT
  UUID(),
  c.restaurant_code,
  c.code AS category_code,
  i.name,
  i.descr,
  i.price,
  0.00 AS discount_percent,
  'AVAILABLE',
  CASE WHEN i.sort_order = 1 THEN 1 ELSE 0 END AS is_featured,
  i.is_veg,
  i.prep,
  i.sort_order,
  NOW(), NOW()
FROM menu_category c
JOIN (
  -- Momos
  SELECT 'Momos' AS cat_name, 1 AS sort_order, 'Chicken Momo (Steamed)' AS name, 'Hand-folded dumplings filled with marinated minced chicken, served with achar.' AS descr, 180.00 AS price, 0 AS is_veg, 15 AS prep UNION ALL
  SELECT 'Momos',             2,                'Buff Momo (Steamed)',           'Classic Kathmandu street favourite — minced water buffalo with onion & spices.',                            170.00, 0, 15 UNION ALL
  SELECT 'Momos',             3,                'Veg Momo (Steamed)',            'Cabbage, carrot and paneer parcels with a tomato dipping sauce.',                                            150.00, 1, 15 UNION ALL
  SELECT 'Momos',             4,                'Chicken C-Momo',                'Chilli momos tossed in a hot Newari-style sauce. Spicy!',                                                    220.00, 0, 18 UNION ALL
  SELECT 'Momos',             5,                'Jhol Momo',                     'Momos in a tangy sesame-tomato broth — soup + dumpling in one bowl.',                                        240.00, 0, 18 UNION ALL
  -- Khana Sets
  SELECT 'Khana Sets',        1,                'Dal Bhat Tarkari',              'Steamed rice, lentil soup, seasonal vegetable curry, achar, papad.',                                         320.00, 1, 20 UNION ALL
  SELECT 'Khana Sets',        2,                'Chicken Khana Set',             'Dal-bhat with masala chicken curry, mixed pickle, and salad.',                                               420.00, 0, 22 UNION ALL
  SELECT 'Khana Sets',        3,                'Mutton Khana Set',              'Slow-cooked Nepali mutton curry, rice, gundruk soup, achar.',                                                520.00, 0, 25 UNION ALL
  SELECT 'Khana Sets',        4,                'Thakali Khana Set',             'Premium Thakali platter: black-eyed beans, gundruk, ghee, timur achar.',                                     480.00, 1, 22 UNION ALL
  SELECT 'Khana Sets',        5,                'Newari Samay Baji',             'Traditional Newari festive plate: beaten rice, choila, boiled egg, soya.',                                   450.00, 0, 18 UNION ALL
  -- Chowmein & Thukpa
  SELECT 'Chowmein & Thukpa', 1,                'Chicken Chowmein',              'Stir-fried Nepali-style noodles with chicken, cabbage and spring onion.',                                    220.00, 0, 14 UNION ALL
  SELECT 'Chowmein & Thukpa', 2,                'Veg Chowmein',                  'Tossed noodles with carrot, cabbage, capsicum and house sauce.',                                             180.00, 1, 12 UNION ALL
  SELECT 'Chowmein & Thukpa', 3,                'Chicken Thukpa',                'Hot noodle soup with shredded chicken, ginger and a hint of timur pepper.',                                  240.00, 0, 16 UNION ALL
  SELECT 'Chowmein & Thukpa', 4,                'Veg Thukpa',                    'Tibetan-style noodle soup with seasonal vegetables.',                                                        200.00, 1, 14 UNION ALL
  -- Snacks
  SELECT 'Snacks',            1,                'Chicken Choila',                'Spicy grilled chicken tossed with mustard oil, garlic and timur pepper.',                                    280.00, 0, 18 UNION ALL
  SELECT 'Snacks',            2,                'Buff Choila',                   'Smoky water-buffalo choila — a Newari classic, plenty of heat.',                                             320.00, 0, 18 UNION ALL
  SELECT 'Snacks',            3,                'Chicken Sekuwa',                'Marinated chicken skewers grilled over open flame, served with bhuteko bhat.',                               350.00, 0, 22 UNION ALL
  SELECT 'Snacks',            4,                'Bara',                          'Crisp Newari lentil pancake topped with minced meat or vegetables.',                                         140.00, 1, 12 UNION ALL
  SELECT 'Snacks',            5,                'Aloo Sandeko',                  'Boiled potato salad with onion, chilli and lemon — quick Newari snack.',                                     120.00, 1, 8 UNION ALL
  -- Beverages
  SELECT 'Beverages',         1,                'Masala Chiya',                  'Strong milk tea brewed with cardamom, cinnamon and clove.',                                                   40.00, 1, 5 UNION ALL
  SELECT 'Beverages',         2,                'Lemon Honey Ginger',            'Warm honey-lemon-ginger tonic. Hits the spot after a trek.',                                                  80.00, 1, 6 UNION ALL
  SELECT 'Beverages',         3,                'Sweet Lassi',                   'Whipped yoghurt with a touch of cardamom and sugar.',                                                        120.00, 1, 6 UNION ALL
  SELECT 'Beverages',         4,                'Mint Mojito',                   'Fresh mint, lime and soda — non-alcoholic.',                                                                 180.00, 1, 6 UNION ALL
  SELECT 'Beverages',         5,                'Tongba',                        'Traditional fermented millet beer, served warm. 21+.',                                                       350.00, 1, 8 UNION ALL
  -- Sweets
  SELECT 'Sweets',            1,                'Yomari',                        'Steamed rice-flour dumpling stuffed with molasses and sesame. Newari classic.',                              120.00, 1, 18 UNION ALL
  SELECT 'Sweets',            2,                'Sel Roti',                      'Crisp sweet rice-flour ring fried in ghee. Festival favourite.',                                              50.00, 1, 10 UNION ALL
  SELECT 'Sweets',            3,                'Sikarni',                       'Hung-yoghurt dessert spiced with cardamom, pistachio and saffron.',                                          180.00, 1, 8 UNION ALL
  SELECT 'Sweets',            4,                'Lalmohan',                      'Soft fried milk-solid balls drenched in cardamom syrup.',                                                    140.00, 1, 6
) i ON i.cat_name = c.name
JOIN restaurant r ON r.code = c.restaurant_code
WHERE r.kiosk_code IN ('HIMAKL','ANNAPL','NEWARG','THAKAL','YALACF','SHERPK','GORKAG','POKHAR','BHOJGR','MUSTNG');
