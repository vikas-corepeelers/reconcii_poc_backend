SELECT
    t.name as payment_type,
    COALESCE(SUM(t.amount), 0) AS sales,
    (SELECT
            SUM(settled_amount)
        FROM
            mpr
        WHERE
            transaction_date BETWEEN :startDate AND :endDate
                AND store_id IN (:storeList)
                AND bs_matched=true
                AND payment_type LIKE t.name) AS receipts,
    COALESCE(SUM(m.commission), 0) AS charges,
    (COALESCE(SUM(CASE WHEN m.bs_matched = true THEN m.mpr_amount ELSE 0 END), 0)) AS reconciled_amount,
    (COALESCE(SUM(t.amount), 0) - COALESCE(SUM(trm.trm_amount), 0)) AS posvstrm,
    (COALESCE(SUM(trm.trm_amount), 0) - COALESCE(SUM(m.mpr_amount), 0)) AS trmvsmpr,
    (COALESCE(SUM(CASE WHEN m.bs_matched = false THEN m.settled_amount ELSE 0 END), 0)) AS mprvsbank
FROM
    orders o
        JOIN
    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))
        LEFT JOIN
    trm trm ON (o.invoice_number = trm.order_id AND trm.transaction_status = 'SUCCESS' AND (t.rrn = trm.rrn || trm.acquirer_bank = 'AMEX'))
        LEFT JOIN
    mpr m ON (m.uid = trm.uid)
WHERE
    o.order_date BETWEEN "2023-11-01 00:00:00" AND "2023-11-15 23:59:59"
        AND o.store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')
        AND o.order_status = 'Paid'
        AND o.tender_name IS NOT NULL
        AND o.invoice_number IS NOT NULL
        GROUP BY t.name


SELECT
    t.payment_type,
    t.acquirer_bank,
    COALESCE(SUM(t.trm_amount), 0) AS sales_amount,
    (SELECT
            COALESCE(SUM(settled_amount), 0)
        FROM
            mpr
        WHERE
            transaction_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-15 23:59:59'
				AND store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')
                AND bs_matched = TRUE
                AND payment_type = t.payment_type
                AND bank = t.acquirer_bank) AS receipts_amount,
    COALESCE(SUM(m.commission), 0) AS charges,
    CASE
        WHEN
            t.acquirer_bank = 'AMEX'
        THEN
            (SELECT
                    COALESCE(SUM(settled_amount), 0)
                FROM
                    mpr
                WHERE
                    transaction_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-15 23:59:59'
						AND store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')
                        AND bs_matched = TRUE
                        AND payment_type = t.payment_type
                        AND bank = t.acquirer_bank)
        ELSE
			COALESCE(SUM(CASE WHEN m.bs_matched = TRUE THEN m.mpr_amount ELSE 0 END), 0)
    END AS reconciled_amount,
    COALESCE(SUM(t.trm_amount), 0) - COALESCE(SUM(m.mpr_amount), 0) AS trmvsmpr,
    COALESCE(SUM(CASE WHEN m.bs_matched = FALSE THEN m.settled_amount ELSE 0 END),0) AS mprvsbank
FROM
    mcd.trm t
        LEFT JOIN
    mcd.mpr m ON (m.uid = t.uid)
WHERE
    t.transaction_status = 'SUCCESS'
        AND t.transaction_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-15 23:59:59'
        AND t.store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')
GROUP BY t.payment_type , t.acquirer_bank