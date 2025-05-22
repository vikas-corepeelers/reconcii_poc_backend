SELECT 
    SUM(COALESCE(CALCULATE_THREE_PO_SALES(bill_subtotal,freebie,merchant_pack_charge,gst_customer_bill,'zomato',action),0)) as threePOSales,
    SUM(coalesce(final_amount,0)) as threePOReceivables,
    SUM(coalesce(commission_value,0)) as threePOCommission,
    SUM(coalesce(freebie,0)) as freebies,
    SUM(COALESCE(CALCULATE_TOTAL_CHARGES(tds_amount,taxes_zomato_fee,pg_charge),0)) AS threePOCharges,
    SUM(coalesce(CALCULATE_RECEIVABLES(bill_subtotal,freebie,actual_packaging_charge,0,'zomato',action,'',dot_pe_order_cancelled_stage),0)) as posReceivables,
    SUM(coalesce(CALCULATE_COMMISSION_AMOUNT(bill_subtotal,freebie,actual_packaging_charge,0,'zomato',action),0)) as posCommission,
    SUM(coalesce(CALCULATE_TDS(bill_subtotal, freebie, actual_packaging_charge,0, 'zomato',action),0)+coalesce(CALCULATE_CHARGES_GST(bill_subtotal,freebie,actual_packaging_charge,0,'zomato',action),0)) as posCharges,
	SUM(coalesce(CALCULATE_UNRECONCILED_AMOUNT(bill_subtotal,final_amount,freebie,gst_customer_bill,commission_value,merchant_pack_charge,tds_amount,taxes_zomato_fee,pg_charge,pos_total_amount-pos_total_tax,actual_packaging_charge,0,'zomato',action,'','',dot_pe_order_cancelled_stage),0)) as unreconciled,
    SUM(coalesce(CALCULATE_RECONCILED_AMOUNT(bill_subtotal,final_amount,freebie,gst_customer_bill,commission_value,merchant_pack_charge,tds_amount,taxes_zomato_fee,pg_charge,pos_total_amount-pos_total_tax,actual_packaging_charge,0,'zomato',action,'','',dot_pe_order_cancelled_stage),0)) as reconciled
FROM
    mcd.zomato
WHERE
    order_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-11 00:00:00'    AND ( mcd_store_code in ('0223')) ;