SELECT
    count(*), sum(trm_amount)
FROM
    mcd.trm t
        LEFT JOIN
    mcd.mpr m ON (m.uid = t.uid)
WHERE
t.transaction_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-15 23:59:59'
AND t.transaction_status = 'SUCCESS'
AND t.store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')
AND t.payment_type = "CARD"
AND t.acquirer_bank = "SBI"