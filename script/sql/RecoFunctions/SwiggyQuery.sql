SELECT 
    SUM(COALESCE(CALCULATE_THREE_PO_SALES(item_total,merchant_discount,packing_and_service_charges,gst_on_order_including_cess,'swiggy',order_status),0)) as threePOSales,
    SUM(coalesce(net_payable_amount_after_tcs_and_tds,0)) as threePOReceivables,
    SUM(coalesce(total_swiggy_service_fee,0)) as threePOCommission,
    SUM(coalesce(merchant_discount,0)) as freebies,
    SUM(COALESCE(CALCULATE_TOTAL_CHARGES(tds,total_gst,0),0)) AS threePOCharges,
    SUM(coalesce(CALCULATE_RECEIVABLES(item_total,merchant_discount,actual_packaging_charge,refund_for_disputed_order,'swiggy',order_status,pick_up_status,dot_pe_order_cancelled_stage),0)) as posReceivables,
    SUM(coalesce(CALCULATE_COMMISSION_AMOUNT(item_total,merchant_discount,actual_packaging_charge,refund_for_disputed_order,'swiggy',order_status),0)) as posCommission,
    SUM((coalesce(CALCULATE_TDS(item_total,merchant_discount, actual_packaging_charge, refund_for_disputed_order, 'swiggy',order_status),0)+coalesce(CALCULATE_CHARGES_GST(item_total,merchant_discount,actual_packaging_charge,refund_for_disputed_order,'swiggy',order_status),0))) as posCharges,
	SUM(coalesce(CALCULATE_UNRECONCILED_AMOUNT(item_total,net_payable_amount_after_tcs_and_tds,merchant_discount,gst_on_order_including_cess,total_swiggy_service_fee,packing_and_service_charges,tds,total_gst,0,pos_total_amount-pos_total_tax,actual_packaging_charge,refund_for_disputed_order,'swiggy',order_status,pick_up_status,cancellation_attribution,dot_pe_order_cancelled_stage),0)) as unreconciled,
    SUM(coalesce(CALCULATE_RECONCILED_AMOUNT(item_total,net_payable_amount_after_tcs_and_tds,merchant_discount,gst_on_order_including_cess,total_swiggy_service_fee,packing_and_service_charges,tds,total_gst,0,pos_total_amount-pos_total_tax,actual_packaging_charge,refund_for_disputed_order,'swiggy',order_status,pick_up_status,cancellation_attribution,dot_pe_order_cancelled_stage),0)) as reconciled
FROM
    mcd.swiggy
WHERE
    order_date BETWEEN '2023-11-01 00:00:00' AND '2023-11-11 00:00:00'    AND ( mcd_store_code in ('0223')) ;