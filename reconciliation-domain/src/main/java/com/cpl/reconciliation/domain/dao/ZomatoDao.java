package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.Zomato;

import java.time.LocalDate;
import java.util.List;

public interface ZomatoDao {

    void saveAll(List<Zomato> trmEntities);

    void save(Zomato entity);

    void updateDataResponse(LocalDate businessDate);

    String Zomato_threePOQuery = "SELECT \n" +
            "z.store_code as store_id,Date(z.order_date) as businessDate,"
            + "SUM(COALESCE(CALCULATE_THREE_PO_SALES(z.bill_subtotal,z.merchant_voucher_discount,z.merchant_pack_charge,'zomato',z.action),0)) as threePOSales,"
            + "COALESCE(SUM(o.total_amount-o.total_tax),0) as posSales,\n" +
            "    SUM(coalesce(z.final_amount,0)) as threePOReceivables,\n" +
            "    SUM(coalesce(z.commission_value,0)) as threePOCommission,\n" +
            "    0 as freebies, SUM(coalesce(o.discount,0)) as posDiscounts,sum(coalesce(z.merchant_voucher_discount,0)) as threePODiscounts,\n" +
            "    SUM(COALESCE(CALCULATE_TOTAL_CHARGES(z.tds_amount,z.taxes_zomato_fee,z.pg_charge),0)) AS threePOCharges,\n" +
            "    SUM(coalesce(CALCULATE_POS_RECEIVABLES_FOR_THREEPO(z.bill_subtotal,z.merchant_voucher_discount,z.merchant_pack_charge,0,z.pg_charge,z.customer_compensation,'zomato',z.action,'',z.dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),0)) as posReceivables,\n" +
            "    SUM(coalesce(CALCULATE_COMMISSION_AMOUNT(z.bill_subtotal,z.merchant_voucher_discount,z.merchant_pack_charge,0,'zomato',z.action,0,0,0,0,0),0)) as posCommission,\n" +
            "    SUM(ABS(coalesce(CALCULATE_POS_RECEIVABLES_FOR_THREEPO(z.bill_subtotal,z.merchant_voucher_discount,z.merchant_pack_charge,0,z.pg_charge,z.customer_compensation,'zomato',z.action,'',z.dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),0)-coalesce(z.payout_amount,0))) as receivablesVsReceipts,\n" +
            "    SUM(coalesce(CALCULATE_TDS(z.bill_subtotal, z.merchant_voucher_discount, z.merchant_pack_charge,0, 'zomato',z.action),0)+coalesce(CALCULATE_CHARGES_GST(z.bill_subtotal,z.merchant_voucher_discount,z.merchant_pack_charge,0,z.pg_charge,'zomato',z.action),0)) as posCharges,\n" +
            "    SUM(coalesce(CALCULATE_UNRECONCILED_AMOUNT(z.bill_subtotal,z.final_amount,z.merchant_voucher_discount,z.gst_customer_bill,z.commission_value,z.merchant_pack_charge,z.tds_amount,z.taxes_zomato_fee,z.pg_charge,customer_compensation,z.bill_subtotal+z.merchant_pack_charge-z.merchant_voucher_discount,z.actual_packaging_charge,0,'zomato',z.action,'','',z.dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),0)) as unreconciled,\n" +
            "    SUM(coalesce(CALCULATE_RECONCILED_AMOUNT(z.bill_subtotal,z.final_amount,z.merchant_voucher_discount,z.gst_customer_bill,z.commission_value,z.merchant_pack_charge,z.tds_amount,z.taxes_zomato_fee,z.pg_charge,z.customer_compensation,z.bill_subtotal+z.merchant_pack_charge-z.merchant_voucher_discount,z.actual_packaging_charge,0,'zomato',z.action,'','',z.dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),0)) as reconciled\n" +
            "FROM\n" +
            "    zomato z left join orders o on z.order_id=o.threepoorder_id and threeposource='ZOMATO' \n" +
            "WHERE\n" +
            "    z.order_date <= :businessDate AND z.store_code IS NOT NULL GROUP BY z.store_code,Date(z.order_date)";
       
}
