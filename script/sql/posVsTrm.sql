SELECT 
    SUM(t.amount) AS sales_amount,
    COUNT(*) AS sales_count,
    SUM(trm.trm_amount) AS match_amount,
    COUNT(trm.transaction_id) AS match_count,
	SUM(t.amount) - SUM(trm.trm_amount) AS amount_difference,
    COUNT(*) - COUNT(DISTINCT trm.transaction_id) AS count_difference,
    (SELECT SUM(trm.trm_amount) FROM trm WHERE transaction_status = 'SUCCESS') AS trm_amount,
    (SELECT count(*) FROM trm WHERE transaction_status = 'SUCCESS') AS trm_count
FROM
    orders o
        JOIN
    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))
        LEFT JOIN
    trm ON (o.invoice_number = trm.order_id AND trm.transaction_status = 'SUCCESS' AND (t.rrn = trm.rrn || trm.acquirer_bank = 'AMEX'))
WHERE
    o.order_status = 'Paid'
        AND o.tender_name IS NOT NULL
        AND o.invoice_number IS NOT NULL;