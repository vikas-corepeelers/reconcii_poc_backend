set sql_safe_updates=0;
UPDATE zomato_salt s
JOIN zomato_mappings z ON s.res_id = z.zomato_store_code
SET
    s.store_code = z.store_code
WHERE
    s.store_code IS NULL;
