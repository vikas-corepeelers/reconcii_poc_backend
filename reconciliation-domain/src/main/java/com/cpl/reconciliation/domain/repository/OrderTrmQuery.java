package com.cpl.reconciliation.domain.repository;

public interface OrderTrmQuery {

    String sql1 = "SELECT \n" +
            "    t.name,\n" +
            "    COALESCE(SUM(t.amount), 0) AS sales_amount,\n" +
            "    COUNT(*) AS sales_count,\n" +
            "    COALESCE(SUM(trm.trm_amount), 0) AS match_amount,\n" +
            "    COUNT(trm.transaction_id) AS match_count,\n" +
            "    COALESCE(SUM(t.amount), 0) - COALESCE(SUM(trm.trm_amount), 0) AS amount_difference,\n" +
            "    COUNT(*) - COUNT(DISTINCT trm.transaction_id) AS count_difference\n" +
            "FROM\n" +
            "    orders o\n" +
            "        JOIN\n" +
            "    tender t ON (o.id = t.order_id AND t.name IN ('Card', 'UPI'))\n" +
            "   LEFT JOIN\n" +
            "    trm trm ON (o.transaction_number = trm.cloud_ref_id AND trm.transaction_status = 'SUCCESS' AND (t.rrn=trm.rrn || trm.acquirer_bank=\"AMEX\") )\n" +
            "WHERE\n" +
            "    o.order_status = 'Paid'\n" +
            "        AND o.tender_name IS NOT NULL\n" +
            "        AND o.invoice_number IS NOT NULL\n" +
            "        AND o.order_date BETWEEN :startDate AND :endDate\n"+
            "        AND trm.transaction_id is null\n";
    String sql1GroupClause = "GROUP BY t.name;\n";


    String sql2 = "SELECT \n" +
            "    t.payment_type, SUM(t.trm_amount) as receipts, COUNT(*) as receipts_count\n" +
            "FROM\n" +
            "    trm t\n" +
            "WHERE\n" +
            "    t.transaction_status = 'SUCCESS'\n" +
            "        AND t.transaction_date BETWEEN :startDate AND :endDate ";
    String sql2GroupClause = "GROUP BY t.payment_type";

    String posVsTRMDownload = "SELECT \n" +
            "    o.business_date AS businessDate,\n" +
            "    o.order_date AS orderDate,\n" +
            "    o.invoice_number AS invoiceNumber,\n" +
            "    o.store_id AS storeID,\n" +
            "    o.pos_id AS posID,\n" +
            "    o.tender_name AS tenderName,\n" +
            "    o.total_amount AS totalAmount,\n" +
            "    o.sale_type AS saleType,\n" +
            "    o.total_amount AS tenderAmount,\n" +
            "    trm.rrn AS tenderRRN,\n" +
            "    trm.transaction_id AS transactionID,\n" +
            "    trm.acquirer_bank AS acquirerBank,\n" +
            "    trm.auth_code AS authCode,\n" +
            "    trm.card_number AS cardNumber,\n" +
            "    trm.card_type AS cardType,\n" +
            "    trm.network_type AS networkType,\n" +
            "    trm.customervpa AS customerVPA,\n" +
            "    trm.mid AS mid,\n" +
            "    trm.payment_type AS paymentType,\n" +
            "    trm.rrn AS trmRRN,\n" +
            "    trm.source AS source,\n" +
            "    trm.tid AS tID,\n" +
            "    trm.transaction_status AS transactionStatus,\n" +
            "    trm.settlement_date AS settlementDate,\n" +
            "    trm.trm_amount AS trmAmount,\n" +
            "    ABS(COALESCE(o.total_amount,0) - COALESCE(trm.trm_amount, 0)) AS remainingAmount\n" +
            "FROM\n" +
            "    orders o\n" +
//            "        JOIN\n" +
//            "    tender t ON (o.id = t.order_id\n" +
//            "        AND t.name IN ('Card', 'UPI'))\n" +
            "        LEFT JOIN\n" +
            "    trm trm ON o.transaction_number = trm.cloud_ref_id\n" +
            "        AND trm.transaction_status = 'SUCCESS'\n" +
//            "        AND (t.rrn = trm.rrn\n" +
//            "        OR (t.name='Card' AND trm.acquirer_bank = 'AMEX')))\n" +
            "WHERE\n" +
            "    o.order_status = 'Paid'\n" +
            "        AND o.tender_name IS NOT NULL\n" +
            "        AND o.invoice_number IS NOT NULL\n" +
            "        AND o.order_date BETWEEN :startDate AND :endDate\n";

    String trmVsPosDownload = "SELECT \n" +
            "    businessDate,\n" +
            "    orderDate,\n" +
            "    invoiceNumber,\n" +
            "    storeID,\n" +
            "    posID,\n" +
            "    tenderName,\n" +
            "    totalAmount,\n" +
            "    saleType,\n" +
            "    tenderAmount,\n" +
            "    tenderRRN,\n" +
            "    trm.transaction_id AS transactionID,\n" +
            "    trm.acquirer_bank AS acquirerBank,\n" +
            "    trm.auth_code AS authCode,\n" +
            "    trm.card_number AS cardNumber,\n" +
            "    trm.card_type AS cardType,\n" +
            "    trm.network_type AS networkType,\n" +
            "    trm.customervpa AS customerVPA,\n" +
            "    trm.mid AS mid,\n" +
            "    trm.payment_type AS paymentType,\n" +
            "    trm.rrn AS trmRRN,\n" +
            "    trm.source AS source,\n" +
            "    trm.tid AS tID,\n" +
            "    trm.transaction_status AS transactionStatus,\n" +
            "    trm.settlement_date AS settlementDate,\n" +
            "    trm.trm_amount AS trmAmount,\n" +
            "    ABS(COALESCE(trm.trm_amount, 0) - COALESCE(tenderAmount, 0)) AS remainingAmount\n" +
            "FROM\n" +
            "    trm trm\n" +
            "        LEFT JOIN\n" +
            "    (SELECT \n" +
            "        io.business_date AS businessDate,\n" +
            "            io.order_date AS orderDate,\n" +
            "            io.invoice_number AS invoiceNumber,\n" +
            "            io.store_id AS storeID,\n" +
            "            io.pos_id AS posID,\n" +
            "            io.transaction_number AS transaction_number,\n" +
            "            io.tender_name AS tenderName,\n" +
            "            io.total_amount AS totalAmount,\n" +
            "            io.sale_type AS saleType,\n" +
            "            io.total_amount AS tenderAmount,\n" +
            "            '' AS tenderRRN,\n" +
            "            io.tender_name AS tenderType\n" +
            "    FROM\n" +
            "        orders io\n" +
//            "    JOIN tender t ON (io.id = t.order_id\n" +
//            "        AND t.name IN ('Card' , 'UPI'))\n" +
            "    WHERE\n" +
            "        io.order_status = 'Paid'\n" +
            "            AND io.tender_name IS NOT NULL\n" +
            "            AND io.invoice_number IS NOT NULL) o ON o.transaction_number = trm.cloud_ref_id\n" +
//            "        AND (o.tenderRRN = trm.rrn\n" +
//            "        OR (o.tenderType='Card' AND trm.acquirer_bank = 'AMEX'))\n" +
            "WHERE\n" +
            "    trm.transaction_status = 'SUCCESS'\n" +
            "        AND trm.transaction_date BETWEEN :startDate AND :endDate\n";
}
