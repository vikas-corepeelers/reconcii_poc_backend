SELECT 
SUM(COALESCE(CALCULATE_THREE_PO_SALES(item_amount,0,packaging_charge,gst,'magicpin',order_status),0)) as threePOSales,
    SUM(coalesce(net_payable,0)) as threePOReceivables,
    SUM(coalesce(commission,0)) as threePOCommission,
    SUM(coalesce(0,0)) as freebies,
    SUM(COALESCE(CALCULATE_TOTAL_CHARGES(tds,gst_on_commission,0),0)) AS threePOCharges,
    SUM(coalesce(CALCULATE_RECEIVABLES(item_amount,0,actual_packaging_charge,debited_amount,'magicpin',order_status,'',dot_pe_order_cancelled_stage),0)) as posReceivables,
    SUM(coalesce(CALCULATE_COMMISSION_AMOUNT(item_amount,0,actual_packaging_charge,debited_amount,'magicpin',order_status),0)) as posCommission,
    SUM(coalesce(CALCULATE_TDS(item_amount, 0, actual_packaging_charge,debited_amount, 'magicpin',order_status),0)+coalesce(CALCULATE_CHARGES_GST(item_amount,0,actual_packaging_charge,0,'magicpin',order_status),0)) as posCharges,
    SUM(coalesce(CALCULATE_UNRECONCILED_AMOUNT(item_amount,net_payable,0,gst,commission,packaging_charge,tds,gst_on_commission,0,pos_total_amount-pos_total_tax,actual_packaging_charge,debited_amount,'magicpin',order_status,'','',dot_pe_order_cancelled_stage),0)) as unreconciled,
    SUM(coalesce(CALCULATE_RECONCILED_AMOUNT(item_amount,net_payable,0,gst,commission,packaging_charge,tds,gst_on_commission,0,pos_total_amount-pos_total_tax,actual_packaging_charge,debited_amount,'magicpin',order_status,'','',dot_pe_order_cancelled_stage),0)) as reconciled
FROM
    mcd.magicpin
WHERE
    date BETWEEN '2023-11-01' AND '2023-11-02'   ;