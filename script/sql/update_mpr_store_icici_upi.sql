SET SQL_SAFE_UPDATES=0;
UPDATE mpr
        JOIN
    trm ON mpr.uid = trm.uid
        AND trm.transaction_status = 'SUCCESS'
SET
    mpr.tid = trm.tid,
    mpr.store_id = trm.store_id
WHERE
    mpr.store_id IS NULL
        AND mpr.bank = 'ICICI'
        AND mpr.payment_type = 'UPI';