package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.Swiggy;

import java.time.LocalDate;
import java.util.List;

public interface SwiggyDao {

    void saveAll(List<Swiggy> trmEntities);

    void save(Swiggy entity);

    void updateDataResponse(LocalDate businessDate);

    String Swiggy_threePOQuery = "SELECT \n" +
            "    s.store_code as store_id,Date(s.order_date) as businessDate,SUM(COALESCE(CALCULATE_THREE_PO_SALES(s.item_total,s.merchant_discount,s.packing_and_service_charges,'swiggy',s.order_status),0)) as threePOSales,COALESCE(SUM(o.total_amount-o.total_tax),0) as posSales,\n" +
            "    SUM(coalesce(s.net_payable_amount_after_tcs_and_tds,0)) as threePOReceivables,\n" +
            "    SUM(coalesce(s.total_swiggy_service_fee,0)) as threePOCommission,\n" +
            "    0 as freebies,sum(coalesce(o.discount,0)) as posDiscounts,sum(coalesce(s.merchant_discount,0)) as threePODiscounts,\n" +
            "    SUM(ABS(coalesce(CALCULATE_POS_RECEIVABLES_FOR_THREEPO(s.item_total,s.merchant_discount,s.packing_and_service_charges,s.refund_for_disputed_order,0,0,'swiggy',s.order_status,s.pick_up_status,s.dot_pe_order_cancelled_stage,s.tcs,s.cash_pre_payment_at_restaurant,s.cancellation_attribution,s.delivery_fee,s.discount_on_swiggy_platform_service_fee,s.collection_fee,s.access_fee,merchant_cancellation_charges,s.call_center_servicefees),0)-coalesce(s.payout_amount,0))) as receivablesVsReceipts,\n" +
            "    SUM(COALESCE(CALCULATE_TOTAL_CHARGES(s.tds,s.total_gst,0),0)) AS threePOCharges,\n" +
            "    SUM(coalesce(CALCULATE_POS_RECEIVABLES_FOR_THREEPO(s.item_total,s.merchant_discount,s.packing_and_service_charges,s.refund_for_disputed_order,0,0,'swiggy',s.order_status,s.pick_up_status,s.dot_pe_order_cancelled_stage,s.tcs,cash_pre_payment_at_restaurant,s.cancellation_attribution,s.delivery_fee,s.discount_on_swiggy_platform_service_fee,s.collection_fee,access_fee,s.merchant_cancellation_charges,s.call_center_servicefees),0)) as posReceivables,\n" +
            "    SUM(coalesce(CALCULATE_COMMISSION_AMOUNT(s.item_total,s.merchant_discount,s.packing_and_service_charges,s.refund_for_disputed_order,'swiggy',s.order_status,s.discount_on_swiggy_platform_service_fee,s.collection_fee,s.access_fee,s.merchant_cancellation_charges,s.call_center_servicefees),0)) as posCommission,\n" +
            "    SUM((coalesce(CALCULATE_TDS(s.item_total,s.merchant_discount,s.packing_and_service_charges,s.refund_for_disputed_order, 'swiggy',s.order_status),0)+coalesce(CALCULATE_CHARGES_GST(s.item_total,s.merchant_discount,s.packing_and_service_charges,s.refund_for_disputed_order,0,'swiggy',s.order_status),0))) as posCharges,\n" +
            "    SUM(coalesce(CALCULATE_UNRECONCILED_AMOUNT(s.item_total,s.net_payable_amount_after_tcs_and_tds,s.merchant_discount,gst_on_order_including_cess,s.total_swiggy_service_fee,s.packing_and_service_charges,s.tds,s.total_gst,0,0,s.item_total+s.packing_and_service_charges-s.merchant_discount,s.actual_packaging_charge,s.refund_for_disputed_order,'swiggy',s.order_status,pick_up_status,s.cancellation_attribution,s.dot_pe_order_cancelled_stage,s.tcs,s.cash_pre_payment_at_restaurant,s.cancellation_attribution,s.delivery_fee,s.discount_on_swiggy_platform_service_fee,s.collection_fee,s.access_fee,s.merchant_cancellation_charges,s.call_center_servicefees),0)) as unreconciled,\n" +
            "    SUM(coalesce(CALCULATE_RECONCILED_AMOUNT(s.item_total,s.net_payable_amount_after_tcs_and_tds,s.merchant_discount,s.gst_on_order_including_cess,s.total_swiggy_service_fee,s.packing_and_service_charges,s.tds,s.total_gst,0,0,s.item_total+s.packing_and_service_charges-s.merchant_discount,s.actual_packaging_charge,s.refund_for_disputed_order,'swiggy',s.order_status,pick_up_status,s.cancellation_attribution,s.dot_pe_order_cancelled_stage,s.tcs,s.cash_pre_payment_at_restaurant,s.cancellation_attribution,s.delivery_fee,s.discount_on_swiggy_platform_service_fee,s.collection_fee,s.access_fee,s.merchant_cancellation_charges,s.call_center_servicefees),0)) as reconciled\n" +
            "FROM\n" +
            "    swiggy s left join orders o on s.order_no=o.threepoorder_id and threeposource='SWIGGY'\n" +
            "WHERE\n" +
            "    Date(s.order_date) <= :businessDate AND s.store_code IS NOT NULL GROUP BY s.store_code,Date(s.order_date)";

}
