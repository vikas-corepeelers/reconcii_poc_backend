SELECT
   *
FROM
    mcd.orders o
          JOIN
    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))
WHERE
    order_date BETWEEN "2023-11-01 00:00:00" AND "2023-11-15 23:59:59"
AND o.store_id IN ('0023','0091','0094','0103','0115','0133','0144','0166','0181','0223','0244')
AND o.tender_name IS NOT NULL
AND o.invoice_number IS NOT NULL
AND o.order_status = 'Paid'