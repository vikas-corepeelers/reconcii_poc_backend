package com.cpl.reconciliation.web.service;

public interface PromoQueries {
    String SWIGGY_PROMO = "SELECT \n" +
            "    *,\n" +
            "    order_no AS orderID,\n" +
            "    order_date AS orderDate,\n" +
            "    store_code AS storeCode,\n" +
            "    invoice_number AS invoiceNumber,\n" +
            "    business_date AS businessDate,\n" +
            "    receipt_number AS receiptNumber,\n" +
            "    pos_id AS posId,\n" +
            "    dot_pe_order_status_description as dotPeOrderStatusDescription,\n" +
            "    order_status AS orderStatus,\n" +
            "    freebie_item AS freebieItem,\n" +
            "    freebie_cost AS freebieCost,\n" +
            "    freebie_sale_price AS freebieSalePrice,\n" +
            "    pick_up_status AS pickupStatus,\n" +
            "    cancellation_attribution AS cancellationRemark,\n" +
            "    refund_for_disputed_order AS refundForDisputedOrder,\n" +
            "    images AS images,\n" +
            "    reason AS reason,\n" +
            "    cash_pre_payment_at_restaurant AS cashPrePaymentAtRestaurant,\n" +
            "    item_total AS billSubtotal,\n" +
            "    dot_pe_order_cancelled_stage AS dotPeOrderCancelledStage,\n" +
            "    merchant_discount AS salt,\n" +
            "    COALESCE(CALCULATE_THREE_PO_SALES(item_total,\n" +
            "                    merchant_discount,\n" +
            "                    packing_and_service_charges,\n" +
            "                    gst_on_order_including_cess,\n" +
            "                    'swiggy',\n" +
            "                    order_status),\n" +
            "            0) AS threePOSales,\n" +
            "    pos_total_amount - pos_total_tax AS posSales,\n" +
            "    COALESCE(net_payable_amount_after_tcs_and_tds, 0) AS threePOReceivables,\n" +
            "    COALESCE(CALCULATE_RECEIVABLES(item_total,\n" +
            "                    merchant_discount,\n" +
            "                    actual_packaging_charge,\n" +
            "                    refund_for_disputed_order,\n" +
            "                    'swiggy',\n" +
            "                    order_status,\n" +
            "                    pick_up_status,\n" +
            "                    dot_pe_order_cancelled_stage),\n" +
            "            0) AS posReceivables,\n" +
            "    packing_and_service_charges AS threePOPackagingCharge,\n" +
            "    actual_packaging_charge AS posPackagingCharge,\n" +
            "    COALESCE(CALCULATE_TDS(item_total,\n" +
            "                    merchant_discount,\n" +
            "                    actual_packaging_charge,\n" +
            "                    refund_for_disputed_order,\n" +
            "                    'swiggy',\n" +
            "                    order_status),\n" +
            "            0) AS posTDS,\n" +
            "    tds AS threePOTDS,\n" +
            "    COALESCE(total_swiggy_service_fee, 0) AS threePOCommission,\n" +
            "    COALESCE(CALCULATE_COMMISSION_AMOUNT(item_total,\n" +
            "                    merchant_discount,\n" +
            "                    actual_packaging_charge,\n" +
            "                    refund_for_disputed_order,\n" +
            "                    'swiggy',\n" +
            "                    order_status),\n" +
            "            0) AS posCommission,\n" +
            "    0 AS threePOPgCharge,\n" +
            "    CALCULATE_PG_CHARGE(item_total,\n" +
            "            merchant_discount,\n" +
            "            actual_packaging_charge,\n" +
            "            'swiggy',\n" +
            "            order_status) AS posPGCharge,\n" +
            "    COALESCE(CALCULATE_CHARGES_GST(item_total,\n" +
            "                    merchant_discount,\n" +
            "                    actual_packaging_charge,\n" +
            "                    refund_for_disputed_order,\n" +
            "                    'swiggy',\n" +
            "                    order_status),\n" +
            "            0) AS posChargesGST,\n" +
            "    total_gst AS threePOChargesGST,\n" +
            "    gst_on_order_including_cess AS threePOConsumerGST,\n" +
            "    pos_total_tax AS posConsumerGST,\n" +
            "    (COALESCE(CALCULATE_UNRECONCILED_AMOUNT(item_total,\n" +
            "                    net_payable_amount_after_tcs_and_tds,\n" +
            "                    merchant_discount,\n" +
            "                    gst_on_order_including_cess,\n" +
            "                    total_swiggy_service_fee,\n" +
            "                    packing_and_service_charges,\n" +
            "                    tds,\n" +
            "                    total_gst,\n" +
            "                    0,\n" +
            "                    pos_total_amount - pos_total_tax,\n" +
            "                    actual_packaging_charge,\n" +
            "                    refund_for_disputed_order,\n" +
            "                    'swiggy',\n" +
            "                    order_status,\n" +
            "                    pick_up_status,\n" +
            "                    cancellation_attribution,\n" +
            "                    dot_pe_order_cancelled_stage),\n" +
            "            0)) AS unreconciled\n" +
            "FROM\n" +
            "    subway.swiggy_promo p\n" +
            "        LEFT JOIN\n" +
            "    subway.swiggy s ON p.order_id = s.order_no\n" +
            "WHERE\n" +
            "    date BETWEEN :startDate AND :endDate AND  (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))\n";


    String DATEWISE_FREEBIE_BUDGET = "SELECT \n" +
            "    day, SUM(total_budget) as budget\n" +
            "FROM\n" +
            "    subway.budget_master\n" +
            "WHERE\n" +
            "    tender_name = :tender\n" +
            "        AND type = 'Freebies'\n" +
            "        AND day IS NOT NULL\n" +
            "        AND (start_date BETWEEN :startDate AND :endDate\n" +
            "        or end_date BETWEEN :startDate AND :endDate)\n" +
            "GROUP BY day";


    String ZOMATO_PROMO = "SELECT \n" +
            "    p.*,\n" +
            "    order_id AS orderID,\n" +
            "    order_date AS orderDate,\n" +
            "    store_code AS storeCode,\n" +
            "    invoice_number AS invoiceNumber,\n" +
            "    business_date AS businessDate,\n" +
            "    receipt_number AS receiptNumber,\n" +
            "    dot_pe_order_status_description AS dotPeOrderStatusDescription,\n" +
            "    pos_id AS posId,\n" +
            "    action AS orderStatus,\n" +
            "    '' AS pickupStatus,\n" +
            "    '' AS cancellationRemark,\n" +
            "    0 AS refundForDisputedOrder,\n" +
            "    bill_subtotal AS billSubtotal,\n" +
            "    dot_pe_order_cancelled_stage AS dotPeOrderCancelledStage,\n" +
            "    freebie AS salt,\n" +
            "    COALESCE(CALCULATE_THREE_PO_SALES(bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    merchant_pack_charge,\n" +
            "                    gst_customer_bill,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS threePOSales,\n" +
            "    pos_total_amount - pos_total_tax AS posSales,\n" +
            "    COALESCE(z.final_amount, 0) AS threePOReceivables,\n" +
            "    COALESCE(CALCULATE_RECEIVABLES(bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action,\n" +
            "                    '',\n" +
            "                    dot_pe_order_cancelled_stage),\n" +
            "            0) AS posReceivables,\n" +
            "    merchant_pack_charge AS threePOPackagingCharge,\n" +
            "    actual_packaging_charge AS posPackagingCharge,\n" +
            "    COALESCE(CALCULATE_TDS(bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS posTDS,\n" +
            "    tds_amount AS threePOTDS,\n" +
            "    COALESCE(commission_value, 0) AS threePOCommission,\n" +
            "    COALESCE(CALCULATE_COMMISSION_AMOUNT(bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS posCommission,\n" +
            "    pg_charge AS threePOPgCharge,\n" +
            "    CALCULATE_PG_CHARGE(bill_subtotal,\n" +
            "            freebie,\n" +
            "            actual_packaging_charge,\n" +
            "            'zomato',\n" +
            "            action) AS posPGCharge,\n" +
            "    COALESCE(CALCULATE_CHARGES_GST(bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS posChargesGST,\n" +
            "    taxes_zomato_fee AS threePOChargesGST,\n" +
            "    gst_customer_bill AS threePOConsumerGST,\n" +
            "    pos_total_tax AS posConsumerGST,\n" +
            "    COALESCE(CALCULATE_UNRECONCILED_AMOUNT(bill_subtotal,\n" +
            "                    z.final_amount,\n" +
            "                    freebie,\n" +
            "                    gst_customer_bill,\n" +
            "                    commission_value,\n" +
            "                    merchant_pack_charge,\n" +
            "                    tds_amount,\n" +
            "                    taxes_zomato_fee,\n" +
            "                    pg_charge,\n" +
            "                    pos_total_amount - pos_total_tax,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action,\n" +
            "                    '',\n" +
            "                    '',\n" +
            "                    dot_pe_order_cancelled_stage),\n" +
            "            0) AS unreconciled\n" +
            "FROM\n" +
            "    zomato_promo p\n" +
            "        LEFT JOIN\n" +
            "    zomato z ON z.id = (SELECT \n" +
            "            id\n" +
            "        FROM\n" +
            "            zomato\n" +
            "        WHERE\n" +
            "            order_id = p.tab_id\n" +
            "                AND service_id IN ('DELIVERY' , 'PAY_LATER')\n" +
            "        ORDER BY CASE\n" +
            "            WHEN action = 'sale' THEN 1\n" +
            "            WHEN action = 'addition' THEN 2\n" +
            "            WHEN action = 'cancel' THEN 3\n" +
            "            WHEN action = 'refund' THEN 4\n" +
            "            ELSE 5\n" +
            "        END\n" +
            "        LIMIT 1) where p.aggregation between :startDate and :endDate AND  (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))\n" +
            "        \n" +
            "\n";

    String ZOMATO_SALT = "SELECT \n" +
            "    p.*,\n" +
            "    order_id AS orderID,\n" +
            "    order_date AS orderDate,\n" +
            "    store_code AS storeCode,\n" +
            "    invoice_number AS invoiceNumber,\n" +
            "    business_date AS businessDate,\n" +
            "    receipt_number AS receiptNumber,\n" +
            "    freebie_item AS freebieItem,\n" +
            "    freebie_cost AS freebieCost,\n" +
            "    freebie_sale_price AS freebieSalePrice,\n" +
            "    dot_pe_order_status_description AS dotPeOrderStatusDescription,\n" +
            "    pos_id AS posId,\n" +
            "    action AS orderStatus,\n" +
            "    '' AS pickupStatus,\n" +
            "    '' AS cancellationRemark,\n" +
            "    0 AS refundForDisputedOrder,\n" +
            "    z.bill_subtotal AS billSubtotal,\n" +
            "    dot_pe_order_cancelled_stage AS dotPeOrderCancelledStage,\n" +
            "    freebie AS salt,\n" +
            "    COALESCE(CALCULATE_THREE_PO_SALES(z.bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    merchant_pack_charge,\n" +
            "                    gst_customer_bill,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS threePOSales,\n" +
            "    pos_total_amount - pos_total_tax AS posSales,\n" +
            "    COALESCE(z.final_amount, 0) AS threePOReceivables,\n" +
            "    COALESCE(CALCULATE_RECEIVABLES(z.bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action,\n" +
            "                    '',\n" +
            "                    dot_pe_order_cancelled_stage),\n" +
            "            0) AS posReceivables,\n" +
            "    merchant_pack_charge AS threePOPackagingCharge,\n" +
            "    actual_packaging_charge AS posPackagingCharge,\n" +
            "    COALESCE(CALCULATE_TDS(z.bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS posTDS,\n" +
            "    tds_amount AS threePOTDS,\n" +
            "    COALESCE(commission_value, 0) AS threePOCommission,\n" +
            "    COALESCE(CALCULATE_COMMISSION_AMOUNT(z.bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS posCommission,\n" +
            "    pg_charge AS threePOPgCharge,\n" +
            "    CALCULATE_PG_CHARGE(z.bill_subtotal,\n" +
            "            freebie,\n" +
            "            actual_packaging_charge,\n" +
            "            'zomato',\n" +
            "            action) AS posPGCharge,\n" +
            "    COALESCE(CALCULATE_CHARGES_GST(z.bill_subtotal,\n" +
            "                    freebie,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action),\n" +
            "            0) AS posChargesGST,\n" +
            "    taxes_zomato_fee AS threePOChargesGST,\n" +
            "    gst_customer_bill AS threePOConsumerGST,\n" +
            "    pos_total_tax AS posConsumerGST,\n" +
            "    COALESCE(CALCULATE_UNRECONCILED_AMOUNT(z.bill_subtotal,\n" +
            "                    z.final_amount,\n" +
            "                    freebie,\n" +
            "                    gst_customer_bill,\n" +
            "                    commission_value,\n" +
            "                    merchant_pack_charge,\n" +
            "                    tds_amount,\n" +
            "                    taxes_zomato_fee,\n" +
            "                    pg_charge,\n" +
            "                    pos_total_amount - pos_total_tax,\n" +
            "                    actual_packaging_charge,\n" +
            "                    0,\n" +
            "                    'zomato',\n" +
            "                    action,\n" +
            "                    '',\n" +
            "                    '',\n" +
            "                    dot_pe_order_cancelled_stage),\n" +
            "            0) AS unreconciled\n" +
            "FROM\n" +
            "    zomato_salt p\n" +
            "        LEFT JOIN\n" +
            "    zomato z ON z.id = (SELECT \n" +
            "            id\n" +
            "        FROM\n" +
            "            zomato\n" +
            "        WHERE\n" +
            "            order_id = p.tab_id\n" +
            "                AND service_id IN ('DELIVERY' , 'PAY_LATER')\n" +
            "        ORDER BY CASE\n" +
            "            WHEN action = 'sale' THEN 1\n" +
            "            WHEN action = 'addition' THEN 2\n" +
            "            WHEN action = 'cancel' THEN 3\n" +
            "            WHEN action = 'refund' THEN 4\n" +
            "            ELSE 5\n" +
            "        END\n" +
            "        LIMIT 1) where p.created_at between :startDate and :endDate and p.salt_discount<>0 AND (COALESCE(:storeCodes,NULL) is null or p.store_code in (:storeCodes))\n";



}
