package com.cpl.reconciliation.domain.util;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
/*
 * @author Abhishek N
 */
@Component
public class QueryConfig {

    public static final Map<String, Map<Long, Map<String, Map<String, String>>>> TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP = new LinkedHashMap();
    public static final Map<String, Map<String, String>> TENDER_WISE_DYNAMIC_QUERY_MAP = new LinkedHashMap();
    public static final Map<String, Map<String, String>> TENDER_WISE_UNRECONCILED_REASONS_MAP = new LinkedHashMap();
    /* Dashboard Summary Queries for ZOMATO, SWIGGY*/
    private String ZomatoThreePOSummaryDynamicQuery = "SELECT \n"
            + "z.store_code as store_id,<DATE_EFFECTIVETYPE_SUMMARY_SELECT>,"
            + "SUM(COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,0)) as threePOSales,"
            + "SUM(COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0)) as PosSales,\n"
            + "SUM(COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0)) as threePOReceivables,\n"
            + "SUM(COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0)) as threePOCommission,\n"
            + "0 as freebies,"
            + "SUM(COALESCE(o.discount,0)) as posDiscounts,"
            + "SUM(COALESCE(z.merchant_voucher_discount,0)) as threePODiscounts,\n"
            + "SUM(COALESCE(z.tds_amount + z.taxes_zomato_fee + z.pg_charge,0)) AS threePOCharges,\n"
            + "SUM(COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0)) as posReceivables,\n"
            + "SUM(COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0)) as posCommission,\n"
            + "SUM(ABS(COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0)-COALESCE(z.payout_amount,0))) as receivablesVsReceipts,\n"
            + "SUM(COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0)+COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0)) as posCharges,\n"
            + "SUM(COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0)) as unreconciled,"
            + "SUM(COALESCE(<RECONCILED_AMOUNT_QUERY_PART>,0)) as reconciled\n"
            + "FROM\n"
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO' \n"
            + "WHERE\n"
            + "<DATE_RANGE_SUMMARY_CLAUSE_CONDITION> AND z.store_code is not null group by z.store_code,<EFFECTIVETYPE_SUMMARY_CLAUSE_CONDITION>";

    private String SwiggyThreePOSymmaryDynamicQuery = "SELECT \n"
            + "s.store_code as store_id,<DATE_EFFECTIVETYPE_SUMMARY_SELECT>,"
            + "SUM(COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,0)) as threePOSales,"
            + "SUM(COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0)) as PosSales,\n"
            + "SUM(COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0)) as threePOReceivables,\n"
            + "SUM(COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0)) as threePOCommission,\n"
            + "0 as freebies,"
            + "SUM(coalesce(o.discount,0)) as posDiscounts,"
            + "SUM(COALESCE(s.merchant_discount,0)) as threePODiscounts,\n"
            + "SUM(ABS(COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0)-COALESCE(s.payout_amount,0))) as receivablesVsReceipts,\n"
            + "SUM(COALESCE(s.tds+s.total_gst,0)) AS threePOCharges,\n"
            + "SUM(COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0)) as posReceivables,\n"
            + "SUM(COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0)) as posCommission,\n"
            + "SUM((COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0)+COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0))) as posCharges,\n"
            + "SUM(COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0)) as unreconciled,\n"
            + "SUM(COALESCE(<RECONCILED_AMOUNT_QUERY_PART>,0)) as reconciled\n"
            + "FROM\n"
            + " swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + "<DATE_RANGE_SUMMARY_CLAUSE_CONDITION> AND s.store_code is not null group by s.store_code,<EFFECTIVETYPE_SUMMARY_CLAUSE_CONDITION>";

    String Zomato_threePOSaleQuery = "SELECT * FROM zomato z WHERE (<DATE_RANGE_REPORT_CLAUSE_CONDITION> "
            + "AND (COALESCE(:storeCodes,NULL) is null OR z.store_code IN (:storeCodes)))";

    private String Zomato_threePOReceivablesQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.final_amount,\n"
            + " COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0) as threePOReceivables\n"
            + "FROM\n"
            + " zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_threePOCommissionQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.commission_value,\n"
            + " coalesce(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0) as threePOCommission\n"
            + " FROM\n"
            + " zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_threePOChargesQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.tds_amount,z.taxes_zomato_fee,z.pg_charge,\n"
            + " COALESCE(z.tds_amount + z.taxes_zomato_fee + z.pg_charge,0) AS threePOCharges\n"
            + "FROM\n"
            + " zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_allThreePOChargesQuery = "SELECT \n"
            + "z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.commission_value,z.tds_amount,z.taxes_zomato_fee,z.pg_charge,\n"
            + "COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0) as threePOCommission,\n"
            + "COALESCE(z.freebie,0) as freebies,\n"
            + "COALESCE(z.merchant_voucher_discount,0) as threePODiscounts,\n"
            + "COALESCE(z.tds_amount + z.taxes_zomato_fee + z.pg_charge,0) AS threePOCharges\n"
            + " FROM\n"
            + " zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_threePOFreebieQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.freebie,\n"
            + "COALESCE(z.freebie,0) as freebies\n"
            + " FROM\n"
            + " zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_PosFreebieQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.bill_subtotal,z.freebie,\n"
            + "z.actual_packaging_charge,z.action,COALESCE(z.freebie,0) as freebies\n"
            + " FROM\n"
            + " zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_posRecievableDynamicQuery = "SELECT \n"
            + "z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.bill_subtotal,z.freebie,z.actual_packaging_charge,z.action,\n"
            + "COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) as posReceivables\n"
            + "FROM\n"
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_posChargesDynamicQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.bill_subtotal,z.freebie,z.actual_packaging_charge,z.action,\n"
            + "(COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0)+COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0)) as posCharges\n"
            + "FROM\n"
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_allPOSChargesQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,"
            + "z.receipt_number,z.pos_id,z.store_code,z.freebie,z.bill_subtotal,z.actual_packaging_charge,\n"
            + "COALESCE(z.freebie,0) as freebies,\n"
            + "COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) as posCommission,\n"
            + "COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0)+COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0) as posCharges\n"
            + "FROM\n"
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_posCommissionDynamicQuery = "SELECT \n"
            + "z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.bill_subtotal,z.freebie,z.actual_packaging_charge,z.action,\n"
            + " COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) as posCommission\n"
            + "FROM\n"
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String Zomato_receivablesVsReceiptsDynamicQuery = "SELECT z.order_id,z.order_date,z.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,z.business_date,z.receipt_number,z.pos_id,z.store_code,z.payout_date,z.reference_number,z.final_amount,z.bill_subtotal,z.actual_packaging_charge,z.action,z.payout_amount,\n"
            + "    coalesce(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0) as threePOReceivables,\n"
            + "    coalesce(z.freebie,0) as freebies,\n"
            + "    coalesce(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) as posReceivables,\n"
            + "    ABS(coalesce(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0)-coalesce(z.payout_amount,0)) as receivablesVsReceipts\n"
            + "FROM\n"
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String ZomatoReconciledDynamicQuery = "SELECT * FROM zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO' "
            + "WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> "
            + "AND COALESCE(<RECONCILED_AMOUNT_QUERY_PART>, 0) > 0 "
            + "AND (COALESCE(:storeCodes,NULL) is null OR z.store_code IN (:storeCodes))";

    private String ZomatoPromoDynamicQuery = "SELECT \n"
            + "    p.*,\n"
            + "    z.order_id AS orderID,\n"
            + "    z.order_date AS orderDate,\n"
            + "    z.store_code AS storeCode,\n"
            + "    z.invoice_number AS invoiceNumber,\n"
            + "    z.business_date AS businessDate,\n"
            + "    z.receipt_number AS receiptNumber,\n"
            + "    z.dot_pe_order_status_description AS dotPeOrderStatusDescription,\n"
            + "    z.pos_id AS posId,\n"
            + "    z.action AS orderStatus,\n"
            + "    '' AS pickupStatus,\n"
            + "    '' AS cancellationRemark,\n"
            + "    0 AS refundForDisputedOrder,\n"
            + "    z.bill_subtotal AS billSubtotal,\n"
            + "    z.dot_pe_order_cancelled_stage AS dotPeOrderCancelledStage,\n"
            + "    z.freebie AS salt,\n"
            + "    COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,\n"
            + "            0) AS threePOSales,\n"
            + "    COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) AS posSales,\n"
            + "    COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>, 0) AS threePOReceivables,\n"
            + "    COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,\n"
            + "            0) AS posReceivables,\n"
            + "    z.merchant_pack_charge AS threePOPackagingCharge,\n"
            + "    z.actual_packaging_charge AS posPackagingCharge,\n"
            + "    COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,\n"
            + "            0) AS posTDS,\n"
            + "    z.tds_amount AS threePOTDS,\n"
            + "    COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>, 0) AS threePOCommission,\n"
            + "    COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,\n"
            + "            0) AS posCommission,\n"
            + "    z.pg_charge AS threePOPgCharge,\n"
            + "    <CALCULATE_POS_PG_CHARGE_QUERY_PART> AS posPGCharge,\n"
            + "    COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,\n"
            + "            0) AS posChargesGST,\n"
            + "    z.taxes_zomato_fee AS threePOChargesGST,\n"
            + "    z.gst_customer_bill AS threePOConsumerGST,\n"
            + "    z.pos_total_tax AS posConsumerGST,\n"
            + "    COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0) AS unreconciled\n"
            + "FROM\n"
            + "    zomato_promo p\n"
            + "        LEFT JOIN\n"
            + "    zomato z ON z.id = (SELECT \n"
            + "            id\n"
            + "        FROM\n"
            + "            zomato\n"
            + "        WHERE\n"
            + "            order_id = p.tab_id\n"
            + "                AND service_id IN ('DELIVERY' , 'PAY_LATER')\n"
            + "        ORDER BY CASE\n"
            + "            WHEN action = 'sale' THEN 1\n"
            + "            WHEN action = 'addition' THEN 2\n"
            + "            WHEN action = 'cancel' THEN 3\n"
            + "            WHEN action = 'refund' THEN 4\n"
            + "            ELSE 5\n"
            + "        END\n"
            + "        LIMIT 1) LEFT JOIN orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO' where <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or p.store_code in (:storeCodes))\n"
            + "        \n"
            + "\n";

    String ZomatoSaltDynamicQuery = "SELECT \n"
            + "    p.*,\n"
            + "    z.order_id AS orderID,\n"
            + "    z.order_date AS orderDate,\n"
            + "    z.store_code AS storeCode,\n"
            + "    z.invoice_number AS invoiceNumber,\n"
            + "    z.business_date AS businessDate,\n"
            + "    z.receipt_number AS receiptNumber,\n"
            + "    p.freebie_item AS freebieItem,\n"
            + "    p.freebie_cost AS freebieCost,\n"
            + "    p.freebie_sale_price AS freebieSalePrice,\n"
            + "    z.dot_pe_order_status_description AS dotPeOrderStatusDescription,\n"
            + "    z.pos_id AS posId,\n"
            + "    z.action AS orderStatus,\n"
            + "    '' AS pickupStatus,\n"
            + "    '' AS cancellationRemark,\n"
            + "    0 AS refundForDisputedOrder,\n"
            + "    z.bill_subtotal AS billSubtotal,\n"
            + "    z.dot_pe_order_cancelled_stage AS dotPeOrderCancelledStage,\n"
            + "    z.freebie AS salt,\n"
            + "    COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,0) AS threePOSales,\n"
            + "    COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) AS posSales,\n"
            + "    COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>, 0) AS threePOReceivables,\n"
            + "    COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) AS posReceivables,\n"
            + "    z.merchant_pack_charge AS threePOPackagingCharge,\n"
            + "    z.actual_packaging_charge AS posPackagingCharge,\n"
            + "    COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0) AS posTDS,\n"
            + "    z.tds_amount AS threePOTDS,\n"
            + "    COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>, 0) AS threePOCommission,\n"
            + "    COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) AS posCommission,\n"
            + "    z.pg_charge AS threePOPgCharge,\n"
            + "    <CALCULATE_POS_PG_CHARGE_QUERY_PART> AS posPGCharge,\n"
            + "    COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0) AS posChargesGST,\n"
            + "    z.taxes_zomato_fee AS threePOChargesGST,\n"
            + "    z.gst_customer_bill AS threePOConsumerGST,\n"
            + "    z.pos_total_tax AS posConsumerGST,\n"
            + "    COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0) AS unreconciled\n"
            + "FROM\n"
            + "    zomato_salt p\n"
            + "        LEFT JOIN\n"
            + "    zomato z ON z.id = (SELECT \n"
            + "            id\n"
            + "        FROM\n"
            + "            zomato\n"
            + "        WHERE\n"
            + "            order_id = p.tab_id\n"
            + "                AND service_id IN ('DELIVERY' , 'PAY_LATER')\n"
            + "        ORDER BY CASE\n"
            + "            WHEN action = 'sale' THEN 1\n"
            + "            WHEN action = 'addition' THEN 2\n"
            + "            WHEN action = 'cancel' THEN 3\n"
            + "            WHEN action = 'refund' THEN 4\n"
            + "            ELSE 5\n"
            + "        END\n"
            + "        LIMIT 1) LEFT JOIN orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'"
            + " where <DATE_RANGE_REPORT_CLAUSE_CONDITION> and p.salt_discount<>0 AND (COALESCE(:storeCodes,NULL) is null or p.store_code in (:storeCodes))\n";

    /*Swiggy*/
    String Swiggy_threePOSaleQuery = "SELECT * FROM swiggy s WHERE (<DATE_RANGE_REPORT_CLAUSE_CONDITION> "
            + "AND (COALESCE(:storeCodes,NULL) is null OR s.store_code IN (:storeCodes)))";

    String Swiggy_threePOReceivablesQuery = "SELECT s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.net_payable_amount_after_tcs_and_tds,\n"
            + "    coalesce(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0) as threePOReceivables\n"
            + "    FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    String Swiggy_threePOCommissionQuery = "SELECT \n"
            + "s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.total_swiggy_service_fee,\n"
            + "    coalesce(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0) as threePOCommission\n"
            + "    FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    String Swiggy_threePOFreebieQuery = "SELECT \n"
            + "    s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.merchant_discount,\n"
            + "    0 as freebies\n"
            + "    FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    String Swiggy_PosFreebieQuery = "SELECT \n"
            + "    s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.item_total,s.merchant_discount,s.actual_packaging_charge,\n"
            + "s.refund_for_disputed_order,s.order_status,s.pick_up_status,"
            + "0 as freebies\n"
            + " FROM\n"
            + " swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    String Swiggy_allThreePOChargesQuery = "SELECT \n"
            + "s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.total_swiggy_service_fee,s.merchant_discount,s.tds,s.total_gst,\n"
            + "    coalesce(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0) as threePOCommission,\n"
            + "    0 as freebies,\n"
            + "    coalesce(s.merchant_discount,0) as threePODiscounts,\n"
            + "    COALESCE(s.tds + s.total_gst,0) AS threePOCharges\n"
            + "FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    String Swiggy_threePOChargesQuery = "SELECT s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.tds,s.total_gst,\n"
            + " COALESCE(s.tds + s.total_gst,0) AS threePOCharges\n"
            + "FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String Swiggy_posRecievableDynamicQuery = "SELECT s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.item_total,s.merchant_discount,s.actual_packaging_charge,s.refund_for_disputed_order,s.order_status,s.pick_up_status,\n"
            + "    coalesce(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) as posReceivables\n"
            + "   FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String Swiggy_posCommissionDynamicQuery = "SELECT s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,"
            + "s.receipt_number,s.pos_id,s.store_code,s.item_total,s.merchant_discount,s.actual_packaging_charge,s.refund_for_disputed_order,s.order_status,"
            + "s.pick_up_status,\n"
            + "    coalesce(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) as posCommission\n"
            + "FROM\n"
            + "   swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String Swiggy_posChargesDynamicQuery = "SELECT s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,"
            + "s.receipt_number,s.pos_id,s.store_code,s.item_total,s.merchant_discount,s.actual_packaging_charge,s.refund_for_disputed_order,s.order_status,"
            + "s.pick_up_status,\n"
            + " (coalesce(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0)+coalesce(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0)) as posCharges\n"
            + "FROM\n"
            + " swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> "
            + "AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String Swiggy_allPOSChargesDymanicQuery = "SELECT \n"
            + "s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,s.pos_id,s.store_code,s.merchant_discount,s.item_total,s.actual_packaging_charge,s.refund_for_disputed_order,\n"
            + "0 as freebies,\n"
            + "coalesce(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) as posCommission,\n"
            + " (coalesce(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0)+coalesce(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0)) as posCharges\n"
            + "FROM\n"
            + " swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String Swiggy_receivablesVsReceiptsQuery = "SELECT \n"
            + "s.order_no,s.order_date,s.invoice_number,COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,s.business_date,s.receipt_number,"
            + "s.pos_id,s.store_code,s.payout_date,s.reference_number,s.net_payable_amount_after_tcs_and_tds,s.item_total,"
            + "s.merchant_discount,s.actual_packaging_charge,s.refund_for_disputed_order,s.order_status,s.pick_up_status,"
            + "s.payout_amount,\n"
            + " coalesce(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0) as threePOReceivables,\n"
            + " 0 as freebies,\n"
            + " ABS(coalesce(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0)-coalesce(s.payout_amount,0)) as receivablesVsReceipts,\n"
            + " coalesce(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) as posReceivables\n"
            + "FROM\n"
            + " swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String SwiggyReconciledDynamicQuery = "SELECT * FROM swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY' "
            + "WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> "
            + "AND COALESCE(<RECONCILED_AMOUNT_QUERY_PART>, 0)>0 "
            + "AND (COALESCE(:storeCodes,NULL) is null  OR s.store_code IN (:storeCodes))";

    String SwiggyPromoDynamicQuery = "SELECT \n"
            + "    *,\n"
            + "    s.order_no AS orderID,\n"
            + "    s.order_date AS orderDate,\n"
            + "    s.store_code AS storeCode,\n"
            + "    s.invoice_number AS invoiceNumber,\n"
            + "    s.business_date AS businessDate,\n"
            + "    s.receipt_number AS receiptNumber,\n"
            + "    s.pos_id AS posId,\n"
            + "    s.dot_pe_order_status_description as dotPeOrderStatusDescription,\n"
            + "    s.order_status AS orderStatus,\n"
            + "    p.freebie_item AS freebieItem,\n"
            + "    p.freebie_cost AS freebieCost,\n"
            + "    p.freebie_sale_price AS freebieSalePrice,\n"
            + "    s.pick_up_status AS pickupStatus,\n"
            + "    s.cancellation_attribution AS cancellationRemark,\n"
            + "    s.refund_for_disputed_order AS refundForDisputedOrder,\n"
            + "    p.images AS images,\n"
            + "    p.reason AS reason,\n"
            + "    s.cash_pre_payment_at_restaurant AS cashPrePaymentAtRestaurant,\n"
            + "    s.item_total AS billSubtotal,\n"
            + "    s.dot_pe_order_cancelled_stage AS dotPeOrderCancelledStage,\n"
            + "    s.merchant_discount AS salt,\n"
            + "    COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,0) AS threePOSales,\n"
            + "    COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) AS PosSales,\n"
            + "    COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>, 0) AS threePOReceivables,\n"
            + "    COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,\n"
            + "            0) AS posReceivables,\n"
            + "    s.packing_and_service_charges AS threePOPackagingCharge,\n"
            + "    s.actual_packaging_charge AS posPackagingCharge,\n"
            + "    COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0) AS posTDS,\n"
            + "    s.tds AS threePOTDS,\n"
            + "    COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>, 0) AS threePOCommission,\n"
            + "    COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) AS posCommission,\n"
            + "    0 AS threePOPgCharge,\n"
            + "    <CALCULATE_POS_PG_CHARGE_QUERY_PART> AS posPGCharge,\n"
            + "    COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0) AS posChargesGST,\n"
            + "    s.total_gst AS threePOChargesGST,\n"
            + "    s.gst_on_order_including_cess AS threePOConsumerGST,\n"
            + "    s.pos_total_tax AS posConsumerGST,\n"
            + "    (COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0)) AS unreconciled\n"
            + "FROM\n"
            + "    swiggy_promo p\n"
            + "    LEFT JOIN\n"
            + "    swiggy s ON p.order_id = s.order_no LEFT JOIN orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE\n"
            + " <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or p.store_code in (:storeCodes))\n";

    /*zomato3POvsPOS*/
    private String zomato3POvsPOSDynamicQuery = "SELECT \n"
            + "z.order_id as orderID,\n"
            + "z.order_date as orderDate,\n"
            + "z.store_code as storeCode,\n"
            + "z.invoice_number as invoiceNumber,\n"
            + "z.business_date as businessDate,\n"
            + "z.receipt_number as receiptNumber,\n"
            + "z.dot_pe_order_status_description as dotPeOrderStatusDescription,\n"
            + "z.pos_id as posId,\n"
            + "z.action as orderStatus,\n"
            + "'' as pickupStatus,\n"
            + "'' as cancellationRemark,\n"
            + "0 as refundForDisputedOrder,\n"
            + "z.bill_subtotal as billSubtotal,\n"
            + "z.dot_pe_order_cancelled_stage as dotPeOrderCancelledStage,\n"
            + "z.freebie as salt,\n"
            + "COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,0) as threePOSales,\n"
            + "COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as PosSales,\n"
            + "    \n"
            + "COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>,0) as threePOReceivables,\n"
            + "COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) as posReceivables,\n"
            + "    \n"
            + "z.merchant_pack_charge as threePOPackagingCharge,\n"
            + "z.actual_packaging_charge as posPackagingCharge,\n"
            + "    \n"
            + "COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0) as posTDS,\n"
            + "COALESCE(<CALCULATE_THREEPO_TDS_AMOUNT_QUERY_PART>,0) as threePOTDS,\n"
            + "    \n"
            + "COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>,0) as threePOCommission,\n"
            + "COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) as posCommission,\n"
            + "   \n"
            + "z.pg_charge as threePOPgCharge,\n"
            + "z.pg_charge as posPGCharge,\n"
            + "   \n"
            + "COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0) as posChargesGST,\n"
            + "COALESCE(<CALCULATE_THREEPO_CHARGES_GST_QUERY_PART>,0) as threePOChargesGST,\n"
            + "    \n"
            + "COALESCE(<CALCULATE_THREEPO_CONSUMER_GST_QUERY_PART>,0) as threePOConsumerGST,\n"
            + "COALESCE(<CALCULATE_POS_CONSUMER_GST_QUERY_PART>,0) as posConsumerGST,\n"
            + "    \n"
            + "COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0) as unreconciled,\n"
            + "<THREEPO_KEYFIELDS_USE_IN_THREEPOvsPOS>,\n"
            + "<RECONCILED_ACTUAL_CALCULATED_DIFF_CASES>\n"
            + "FROM "
            + "zomato z left join orders o on z.order_id=o.order_id and UPPER(o.source)='ZOMATO'\n"
            + "WHERE "
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";

    private String zomatoExceptionalReportQuery = "SELECT z.order_id,z.order_date,z.action,<EXCEPTIONAL_REPORT_COLUMNS> "
            + "from zomato z where (<EXCEPTIONAL_REPORT_WHERE_CONDITION>)"
                + " AND <DATE_RANGE_REPORT_CLAUSE_CONDITION>"
                + " AND (COALESCE(:storeCodes,NULL) is null or z.store_code in (:storeCodes))";
    
    private String swiggyExceptionalReportQuery = "SELECT s.order_no,s.order_date,s.order_status,<EXCEPTIONAL_REPORT_COLUMNS> from swiggy s where "
                + " (<EXCEPTIONAL_REPORT_WHERE_CONDITION>) AND <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

        
    private String swiggy3POvsPOSDynamicQuery = "SELECT \n"
            + "s.order_no AS orderID,\n"
            + "s.order_date as orderDate,\n"
            + "s.store_code as storeCode,\n"
            + "s.invoice_number as invoiceNumber,\n"
            + "s.business_date as businessDate,\n"
            + "s.receipt_number as receiptNumber,\n"
            + "s.dot_pe_order_status_description as dotPeOrderStatusDescription,\n"
            + "s.pos_id as posId,\n"
            + "s.order_status as orderStatus,\n"
            + "s.pick_up_status as pickupStatus,\n"
            + "s.merchant_cancellation_charges as merchantCancellationCharges,\n"
            + "s.cancellation_attribution as cancellationRemark,\n"
            + "s.refund_for_disputed_order as refundForDisputedOrder,\n"
            + "s.cash_pre_payment_at_restaurant as cashPrePaymentAtRestaurant,\n"
            + "s.item_total as billSubtotal,\n"
            + "s.dot_pe_order_cancelled_stage as dotPeOrderCancelledStage,\n"
            + "s.merchant_discount as salt,"
            + "COALESCE(<CALCULATE_THREE_PO_SALES_QUERY_PART>,0) AS threePOSales,"
            + "COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) AS PosSales,"
            + "COALESCE(<CALCULATE_THREEPO_RECEIVABLES_QUERY_PART>, 0) AS threePOReceivables,"
            + "COALESCE(<CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART>,0) AS posReceivables,"
            + "s.packing_and_service_charges AS threePOPackagingCharge,"
            + "s.actual_packaging_charge AS posPackagingCharge,"
            + "COALESCE(<CALCULATE_POS_TDS_AMOUNT_QUERY_PART>,0) AS posTDS,"
            + "COALESCE(<CALCULATE_THREEPO_TDS_AMOUNT_QUERY_PART>,0) as threePOTDS,"
            + "COALESCE(<CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART>, 0) AS threePOCommission,\n"
            + "COALESCE(<CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART>,0) AS posCommission,"
            + "0 AS threePOPgCharge,"
            + "0 AS posPGCharge,"
            + "COALESCE(<CALCULATE_POS_CHARGES_GST_QUERY_PART>,0) AS posChargesGST,"
            + "s.total_gst AS threePOChargesGST,"
            + "s.gst_on_order_including_cess AS threePOConsumerGST,"
            + "s.pos_total_tax AS posConsumerGST,"
            + "(COALESCE(<UNRECONCILED_AMOUNT_QUERY_PART>,0)) AS unreconciled,\n"
            + "<THREEPO_KEYFIELDS_USE_IN_THREEPOvsPOS>,\n"
            + "<RECONCILED_ACTUAL_CALCULATED_DIFF_CASES>\n"
            + "FROM swiggy s left join orders o on s.order_no=o.order_id and UPPER(o.source)='SWIGGY'\n"
            + "WHERE "
            + "<DATE_RANGE_REPORT_CLAUSE_CONDITION> "
            + "AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))";

    private String ordersNotFoundInZomatoQuery = "SELECT o.id,o.pos_id,o.store_id,o.business_date,o.order_date,o.sale_type,"
            + "o.tender_name,o.order_status,o.invoice_number,o.unique_key,o.receipt_number,"
            + "COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) as sale_amount,o.source,o.order_id"
            + " FROM orders o WHERE (<DATE_RANGE_REPORT_CLAUSE_CONDITION>) AND UPPER(o.source)='ZOMATO' AND NOT EXISTS (select * from zomato z where z.invoice_number=o.invoice_number)"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null";

    private String posSalesZomatoQuery = "SELECT o.id,o.pos_id,o.store_id,o.business_date,o.order_date,o.sale_type,o.tender_name,o.order_status,o.invoice_number,o.unique_key,o.receipt_number,<CALCULATE_POS_SALES_QUERY_PART> as sale_amount,o.total_tax,o.total_amount,o.source,o.order_id "
            + "FROM orders o WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND UPPER(o.source)='ZOMATO'"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null";

    private String posSalesZomatoNextDateQuery = "SELECT o.id,o.pos_id,o.store_id,o.business_date,o.order_date,o.sale_type,o.tender_name,o.order_status,o.invoice_number,o.unique_key,o.receipt_number,<CALCULATE_POS_SALES_QUERY_PART> as sale_amount,o.total_tax,o.total_amount,o.source,o.order_id "
            + "FROM orders o WHERE (<DATE_RANGE_REPORT_CLAUSE_CONDITION>) AND UPPER(o.source)='ZOMATO'"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null";

    String ordersNotFoundInSwiggyQuery = "SELECT id,pos_id,store_id,business_date,order_date,sale_type,tender_name,order_status,invoice_number,unique_key,receipt_number,<CALCULATE_POS_SALES_QUERY_PART> as sale_amount,total_tax,total_amount,source,order_id"
            + " FROM orders o WHERE (<DATE_RANGE_REPORT_CLAUSE_CONDITION>) AND UPPER(o.source)='swiggy' AND NOT EXISTS (select * from swiggy s where s.order_no=o.invoice_number)"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null";

    String posSalesSwiggyQuery = "SELECT o.id,o.pos_id,o.store_id,o.business_date,o.order_date,o.sale_type,o.tender_name,o.order_status,o.invoice_number,o.unique_key,o.receipt_number,<CALCULATE_POS_SALES_QUERY_PART> as sale_amount,o.total_tax,o.total_amount,o.source,o.order_id "
            + "FROM orders o WHERE <DATE_RANGE_REPORT_CLAUSE_CONDITION> AND UPPER(o.source)='swiggy'"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null";

    String posSalesSwiggyNextDateQuery = "SELECT o.id,o.pos_id,o.store_id,o.business_date,o.order_date,o.sale_type,o.tender_name,o.order_status,o.invoice_number,o.unique_key,o.receipt_number,<CALCULATE_POS_SALES_QUERY_PART> as sale_amount,o.total_tax,o.total_amount,o.source,o.order_id "
            + "FROM orders o WHERE (<DATE_RANGE_REPORT_CLAUSE_CONDITION>) AND UPPER(o.source)='swiggy'"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null";

    private String magicpinPOSVSThreePODynamicQuery = "SELECT\n"
            + "    order_id AS orderID,\n"
            + "    date AS orderDate,\n"
            + "    store_code AS storeCode,\n"
            + "    invoice_number AS invoiceNumber,\n"
            + "    business_date AS businessDate,\n"
            + "    receipt_number AS receiptNumber,\n"
            + "    pos_id AS posId,\n"
            + "    dot_pe_order_status_description as dotPeOrderStatusDescription,\n"
            + "    order_status AS orderStatus,\n"
            + "    '' AS pickupStatus,\n"
            + "    '' as cancellationRemark,\n"
            + "    dot_pe_order_cancelled_stage as dotPeOrderCancelledStage,\n"
            + "    debited_amount AS refundForDisputedOrder,\n"
            + "    item_amount AS billSubtotal,\n"
            + "    0 AS salt,\n"
            + "    COALESCE(<CALCULATE_POS_SALES_QUERY_PART>,0) AS PosSales,\n"
            + "    packaging_charge AS threePOPackagingCharge,\n"
            + "    actual_packaging_charge AS posPackagingCharge,\n"
            + "    COALESCE(net_payable, 0) AS threePOReceivables,\n"
            + "    tds AS threePOTDS,\n"
            + "    COALESCE(commission, 0) AS threePOCommission,\n"
            + "    0 AS threePOPgCharge,\n"
            + "    gst_on_commission AS threePOChargesGST,\n"
            + "    0 AS posPGCharge,\n"
            + "    gst AS threePOConsumerGST,\n"
            + "    COALESCE(CALCULATE_THREE_PO_SALES(item_amount,0,packaging_charge,'magicpin',order_status),\n"
            + "            0) AS threePOSales,\n"
            + "    COALESCE(CALCULATE_POS_RECEIVABLES_FOR_THREEPO(item_amount,0,actual_packaging_charge,debited_amount,0,0,'magicpin',order_status,'',dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),\n"
            + "            0) AS posReceivables,\n"
            + "    COALESCE(CALCULATE_TDS(item_amount, 0, actual_packaging_charge,debited_amount, 'magicpin',order_status),\n"
            + "            0) AS posTDS,\n"
            + "    COALESCE(CALCULATE_COMMISSION_AMOUNT(item_amount,0,actual_packaging_charge,debited_amount,'magicpin',order_status,0,0,0,0,0),\n"
            + "            0) AS posCommission,\n"
            + "    COALESCE(CALCULATE_CHARGES_GST(item_amount,\n"
            + "                    0,\n"
            + "                    actual_packaging_charge,\n"
            + "                    debited_amount,0,\n"
            + "                    'magicpin',order_status),\n"
            + "            0) AS posChargesGST,\n"
            + "    pos_total_tax AS posConsumerGST,\n"
            + "    COALESCE(CALCULATE_UNRECONCILED_AMOUNT(item_amount,net_payable,0,gst,commission,packaging_charge,tds,gst_on_commission,0,0,pos_total_amount-pos_total_tax,actual_packaging_charge,debited_amount,'magicpin',order_status,'','',dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),\n"
            + "            0) AS unreconciled\n"
            + "FROM\n"
            + "    magicpin\n"
            + "WHERE\n"
            + "    date BETWEEN :startDate AND :endDate\n"
            + "         AND  (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))";

    public String getZomatoThreePOSummaryDynamicQuery() {
        return ZomatoThreePOSummaryDynamicQuery;
    }

    public void setZomatoThreePOSummaryDynamicQuery(String ZomatoThreePOSummaryDynamicQuery) {
        this.ZomatoThreePOSummaryDynamicQuery = ZomatoThreePOSummaryDynamicQuery;
    }

    public String getSwiggyThreePOSymmaryDynamicQuery() {
        return SwiggyThreePOSymmaryDynamicQuery;
    }

    public void setSwiggyThreePOSymmaryDynamicQuery(String SwiggyThreePOSymmaryDynamicQuery) {
        this.SwiggyThreePOSymmaryDynamicQuery = SwiggyThreePOSymmaryDynamicQuery;
    }

    public String getZomatoReconciledDynamicQuery() {
        return ZomatoReconciledDynamicQuery;
    }

    public void setZomatoReconciledDynamicQuery(String ZomatoReconciledDynamicQuery) {
        this.ZomatoReconciledDynamicQuery = ZomatoReconciledDynamicQuery;
    }

    public String getSwiggyReconciledDynamicQuery() {
        return SwiggyReconciledDynamicQuery;
    }

    public void setSwiggyReconciledDynamicQuery(String SwiggyReconciledDynamicQuery) {
        this.SwiggyReconciledDynamicQuery = SwiggyReconciledDynamicQuery;
    }

    public String getZomato3POvsPOSDynamicQuery() {
        return zomato3POvsPOSDynamicQuery;
    }

    public void setZomato3POvsPOSDynamicQuery(String zomato3POvsPOSDynamicQuery) {
        this.zomato3POvsPOSDynamicQuery = zomato3POvsPOSDynamicQuery;
    }

    public String getSwiggy3POvsPOSDynamicQuery() {
        return swiggy3POvsPOSDynamicQuery;
    }

    public void setSwiggy3POvsPOSDynamicQuery(String swiggy3POvsPOSDynamicQuery) {
        this.swiggy3POvsPOSDynamicQuery = swiggy3POvsPOSDynamicQuery;
    }

    public String getZomato_posRecievableDynamicQuery() {
        return Zomato_posRecievableDynamicQuery;
    }

    public void setZomato_posRecievableDynamicQuery(String Zomato_posRecievableDynamicQuery) {
        this.Zomato_posRecievableDynamicQuery = Zomato_posRecievableDynamicQuery;
    }

    public String getZomato_posChargesDynamicQuery() {
        return Zomato_posChargesDynamicQuery;
    }

    public void setZomato_posChargesDynamicQuery(String Zomato_posChargesDynamicQuery) {
        this.Zomato_posChargesDynamicQuery = Zomato_posChargesDynamicQuery;
    }

    public String getZomato_posCommissionDynamicQuery() {
        return Zomato_posCommissionDynamicQuery;
    }

    public void setZomato_posCommissionDynamicQuery(String Zomato_posCommissionDynamicQuery) {
        this.Zomato_posCommissionDynamicQuery = Zomato_posCommissionDynamicQuery;
    }

    public String getZomato_receivablesVsReceiptsDynamicQuery() {
        return Zomato_receivablesVsReceiptsDynamicQuery;
    }

    public void setZomato_receivablesVsReceiptsDynamicQuery(String Zomato_receivablesVsReceiptsDynamicQuery) {
        this.Zomato_receivablesVsReceiptsDynamicQuery = Zomato_receivablesVsReceiptsDynamicQuery;
    }

    public String getZomato_allPOSChargesQuery() {
        return Zomato_allPOSChargesQuery;
    }

    public void setZomato_allPOSChargesQuery(String Zomato_allPOSChargesQuery) {
        this.Zomato_allPOSChargesQuery = Zomato_allPOSChargesQuery;
    }

    public String getSwiggy_posRecievableDynamicQuery() {
        return Swiggy_posRecievableDynamicQuery;
    }

    public void setSwiggy_posRecievableDynamicQuery(String Swiggy_posRecievableDynamicQuery) {
        this.Swiggy_posRecievableDynamicQuery = Swiggy_posRecievableDynamicQuery;
    }

    public String getSwiggy_posCommissionDynamicQuery() {
        return Swiggy_posCommissionDynamicQuery;
    }

    public void setSwiggy_posCommissionDynamicQuery(String Swiggy_posCommissionDynamicQuery) {
        this.Swiggy_posCommissionDynamicQuery = Swiggy_posCommissionDynamicQuery;
    }

    public String getSwiggy_posChargesDynamicQuery() {
        return Swiggy_posChargesDynamicQuery;
    }

    public void setSwiggy_posChargesDynamicQuery(String Swiggy_posChargesDynamicQuery) {
        this.Swiggy_posChargesDynamicQuery = Swiggy_posChargesDynamicQuery;
    }

    public String getSwiggy_allPOSChargesDymanicQuery() {
        return Swiggy_allPOSChargesDymanicQuery;
    }

    public void setSwiggy_allPOSChargesDymanicQuery(String Swiggy_allPOSChargesDymanicQuery) {
        this.Swiggy_allPOSChargesDymanicQuery = Swiggy_allPOSChargesDymanicQuery;
    }

    public String getSwiggy_receivablesVsReceiptsQuery() {
        return Swiggy_receivablesVsReceiptsQuery;
    }

    public void setSwiggy_receivablesVsReceiptsQuery(String Swiggy_receivablesVsReceiptsQuery) {
        this.Swiggy_receivablesVsReceiptsQuery = Swiggy_receivablesVsReceiptsQuery;
    }

    public String getSwiggyPromoDynamicQuery() {
        return SwiggyPromoDynamicQuery;
    }

    public void setSwiggyPromoDynamicQuery(String SwiggyPromoDynamicQuery) {
        this.SwiggyPromoDynamicQuery = SwiggyPromoDynamicQuery;
    }

    public String getZomatoPromoDynamicQuery() {
        return ZomatoPromoDynamicQuery;
    }

    public void setZomatoPromoDynamicQuery(String ZomatoPromoDynamicQuery) {
        this.ZomatoPromoDynamicQuery = ZomatoPromoDynamicQuery;
    }

    public String getZomatoSaltDynamicQuery() {
        return ZomatoSaltDynamicQuery;
    }

    public void setZomatoSaltDynamicQuery(String ZomatoSaltDynamicQuery) {
        this.ZomatoSaltDynamicQuery = ZomatoSaltDynamicQuery;
    }

    public String getOrdersNotFoundInZomatoQuery() {
        return ordersNotFoundInZomatoQuery;
    }

    public void setOrdersNotFoundInZomatoQuery(String ordersNotFoundInZomatoQuery) {
        this.ordersNotFoundInZomatoQuery = ordersNotFoundInZomatoQuery;
    }

    public String getPosSalesZomatoQuery() {
        return posSalesZomatoQuery;
    }

    public void setPosSalesZomatoQuery(String posSalesZomatoQuery) {
        this.posSalesZomatoQuery = posSalesZomatoQuery;
    }

    public String getPosSalesZomatoNextDateQuery() {
        return posSalesZomatoNextDateQuery;
    }

    public void setPosSalesZomatoNextDateQuery(String posSalesZomatoNextDateQuery) {
        this.posSalesZomatoNextDateQuery = posSalesZomatoNextDateQuery;
    }

    public String getOrdersNotFoundInSwiggyQuery() {
        return ordersNotFoundInSwiggyQuery;
    }

    public void setOrdersNotFoundInSwiggyQuery(String ordersNotFoundInSwiggyQuery) {
        this.ordersNotFoundInSwiggyQuery = ordersNotFoundInSwiggyQuery;
    }

    public String getPosSalesSwiggyQuery() {
        return posSalesSwiggyQuery;
    }

    public void setPosSalesSwiggyQuery(String posSalesSwiggyQuery) {
        this.posSalesSwiggyQuery = posSalesSwiggyQuery;
    }

    public String getPosSalesSwiggyNextDateQuery() {
        return posSalesSwiggyNextDateQuery;
    }

    public void setPosSalesSwiggyNextDateQuery(String posSalesSwiggyNextDateQuery) {
        this.posSalesSwiggyNextDateQuery = posSalesSwiggyNextDateQuery;
    }

    public String getMagicpinPOSVSThreePODynamicQuery() {
        return magicpinPOSVSThreePODynamicQuery;
    }

    public void setMagicpinPOSVSThreePODynamicQuery(String magicpinPOSVSThreePODynamicQuery) {
        this.magicpinPOSVSThreePODynamicQuery = magicpinPOSVSThreePODynamicQuery;
    }

    public String getZomato_threePOReceivablesQuery() {
        return Zomato_threePOReceivablesQuery;
    }

    public void setZomato_threePOReceivablesQuery(String Zomato_threePOReceivablesQuery) {
        this.Zomato_threePOReceivablesQuery = Zomato_threePOReceivablesQuery;
    }

    public String getZomato_threePOCommissionQuery() {
        return Zomato_threePOCommissionQuery;
    }

    public void setZomato_threePOCommissionQuery(String Zomato_threePOCommissionQuery) {
        this.Zomato_threePOCommissionQuery = Zomato_threePOCommissionQuery;
    }

    public String getZomato_threePOChargesQuery() {
        return Zomato_threePOChargesQuery;
    }

    public void setZomato_threePOChargesQuery(String Zomato_threePOChargesQuery) {
        this.Zomato_threePOChargesQuery = Zomato_threePOChargesQuery;
    }

    public String getZomato_allThreePOChargesQuery() {
        return Zomato_allThreePOChargesQuery;
    }

    public void setZomato_allThreePOChargesQuery(String Zomato_allThreePOChargesQuery) {
        this.Zomato_allThreePOChargesQuery = Zomato_allThreePOChargesQuery;
    }

    public String getZomato_threePOFreebieQuery() {
        return Zomato_threePOFreebieQuery;
    }

    public void setZomato_threePOFreebieQuery(String Zomato_threePOFreebieQuery) {
        this.Zomato_threePOFreebieQuery = Zomato_threePOFreebieQuery;
    }

    public String getSwiggy_threePOReceivablesQuery() {
        return Swiggy_threePOReceivablesQuery;
    }

    public void setSwiggy_threePOReceivablesQuery(String Swiggy_threePOReceivablesQuery) {
        this.Swiggy_threePOReceivablesQuery = Swiggy_threePOReceivablesQuery;
    }

    public String getSwiggy_threePOCommissionQuery() {
        return Swiggy_threePOCommissionQuery;
    }

    public void setSwiggy_threePOCommissionQuery(String Swiggy_threePOCommissionQuery) {
        this.Swiggy_threePOCommissionQuery = Swiggy_threePOCommissionQuery;
    }

    public String getSwiggy_threePOFreebieQuery() {
        return Swiggy_threePOFreebieQuery;
    }

    public void setSwiggy_threePOFreebieQuery(String Swiggy_threePOFreebieQuery) {
        this.Swiggy_threePOFreebieQuery = Swiggy_threePOFreebieQuery;
    }

    public String getSwiggy_allThreePOChargesQuery() {
        return Swiggy_allThreePOChargesQuery;
    }

    public void setSwiggy_allThreePOChargesQuery(String Swiggy_allThreePOChargesQuery) {
        this.Swiggy_allThreePOChargesQuery = Swiggy_allThreePOChargesQuery;
    }

    public String getSwiggy_threePOChargesQuery() {
        return Swiggy_threePOChargesQuery;
    }

    public void setSwiggy_threePOChargesQuery(String Swiggy_threePOChargesQuery) {
        this.Swiggy_threePOChargesQuery = Swiggy_threePOChargesQuery;
    }

    public String getZomato_PosFreebieQuery() {
        return Zomato_PosFreebieQuery;
    }

    public void setZomato_PosFreebieQuery(String Zomato_PosFreebieQuery) {
        this.Zomato_PosFreebieQuery = Zomato_PosFreebieQuery;
    }

    public String getSwiggy_PosFreebieQuery() {
        return Swiggy_PosFreebieQuery;
    }

    public void setSwiggy_PosFreebieQuery(String Swiggy_PosFreebieQuery) {
        this.Swiggy_PosFreebieQuery = Swiggy_PosFreebieQuery;
    }

    public String getZomato_threePOSaleQuery() {
        return Zomato_threePOSaleQuery;
    }

    public void setZomato_threePOSaleQuery(String Zomato_threePOSaleQuery) {
        this.Zomato_threePOSaleQuery = Zomato_threePOSaleQuery;
    }

    public String getSwiggy_threePOSaleQuery() {
        return Swiggy_threePOSaleQuery;
    }

    public void setSwiggy_threePOSaleQuery(String Swiggy_threePOSaleQuery) {
        this.Swiggy_threePOSaleQuery = Swiggy_threePOSaleQuery;
    }

    public String getZomatoExceptionalReportQuery() {
        return zomatoExceptionalReportQuery;
    }

    public void setZomatoExceptionalReportQuery(String zomatoExceptionalReportQuery) {
        this.zomatoExceptionalReportQuery = zomatoExceptionalReportQuery;
    }

    public String getSwiggyExceptionalReportQuery() {
        return swiggyExceptionalReportQuery;
    }

    public void setSwiggyExceptionalReportQuery(String swiggyExceptionalReportQuery) {
        this.swiggyExceptionalReportQuery = swiggyExceptionalReportQuery;
    }
}
