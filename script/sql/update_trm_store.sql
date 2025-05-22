#For update missing store_id in trm
use mcd;
SET SQL_SAFE_UPDATES = 0;
UPDATE trm
JOIN store_tid_mapping ON trm.tid = store_tid_mapping.tid
SET trm.store_id = store_tid_mapping.store_code where trm.store_id is null;


#For check missing TID store code mapping in trm
SELECT distinct(trm.tid)
FROM trm
LEFT JOIN store_tid_mapping ON trm.tid = store_tid_mapping.tid
WHERE store_tid_mapping.tid IS NULL;
