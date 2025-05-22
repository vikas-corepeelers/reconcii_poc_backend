#For update missing store_id in mpr
use mcd;
SET SQL_SAFE_UPDATES = 0;
UPDATE mpr
JOIN store_tid_mapping ON mpr.tid = store_tid_mapping.tid
SET mpr.store_id = store_tid_mapping.store_code where mpr.store_id is null;



#For update missing store_id in mpr for AMEX
use mcd;
SET SQL_SAFE_UPDATES = 0;
UPDATE mpr
JOIN amex_store_mapping ON mpr.tid = amex_store_mapping.amex_store_name
SET mpr.store_id = amex_store_mapping.store_code where store_id is null and bank="AMEX";



#For check missing TID store code mapping in mpr
SELECT distinct(mpr.tid)
FROM mpr
LEFT JOIN store_tid_mapping ON mpr.tid = store_tid_mapping.tid
WHERE store_tid_mapping.tid IS NULL;