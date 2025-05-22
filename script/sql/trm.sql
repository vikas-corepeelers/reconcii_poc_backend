SELECT
    trm.*
FROM
    mcd.trm
WHERE
    transaction_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-15 23:59:59'
AND transaction_status = 'SUCCESS'
AND trm.store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')