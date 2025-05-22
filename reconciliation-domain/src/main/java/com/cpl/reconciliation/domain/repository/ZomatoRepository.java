package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.Zomato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ZomatoRepository extends JpaRepository<Zomato, String> {

    @Query(value = "SELECT COUNT(*) from zomato s "
            + "where s.order_date between :startDate AND :endDate AND s.found_in_dot_pe=1"
            + " AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))", nativeQuery = true)
    int getReconciledOrderCountFoundInDotPe(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT z from Zomato z where z.orderId=:orderId ")
//    List<Zomato> findByOrderId(String orderId);

    @Query(value = "SELECT COUNT(*) from zomato s "
            + "where s.order_date between :startDate AND :endDate AND s.found_in_dot_pe=0"
            + " AND (COALESCE(:storeCodes,NULL) is null or s.store_code in (:storeCodes))", nativeQuery = true)
    int getOrderCountNotFoundInDotPe(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT COUNT(*) from zomato s "
//            + "where s.order_date between :startDate AND :endDate AND s.found_in_dot_pe=:foundInDotPe"
//            + " AND (:storeCodes is null or s.store_code in (:storeCodes))", nativeQuery = true)
//    List<Zomato> getOrdersAsPerDotPe(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes, boolean foundInDotPe);

//    @Query(value = "SELECT s from Zomato s "
//            + "where s.orderDate between :startDate AND :endDate"
//            + " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    List<Zomato> findAllByDateAndStoreCodes(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT s from Zomato s "
//            + "where s.orderDate between :startDate AND :endDate AND s.freebie!=0"
//            + " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes)) ")
//    List<Zomato> findAllByDateAndStoreCodesWhereFreebieExists(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT s from Zomato s "
//            + "where s.orderDate between :startDate AND :endDate AND ABS((s.billSubtotal-s.freebie)*0.18-s.commissionValue)>=0.05"
//            + " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes)) ")
//    List<Zomato> findAllByDateAndStoreCodesWhereCommissionDiffExists(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT s from Zomato s "
//            + "where s.orderDate between :startDate AND :endDate AND coalesce(CALCULATE_UNRECONCILED_AMOUNT(bill_subtotal,final_amount,merchant_voucher_discount,gst_customer_bill,commission_value,merchant_pack_charge,tds_amount,taxes_zomato_fee,pg_charge,customer_compensation,bill_subtotal+merchant_pack_charge-merchant_voucher_discount,actual_packaging_charge,0,'zomato',action,'','',dot_pe_order_cancelled_stage,0,0,0,0,0,0,0,0,0),0)=0 "
//            + " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes)) ")
//    List<Zomato> findAllReconciledByDateAndStoreCodes(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    List<Zomato> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query(value = "SELECT COALESCE(COUNT(distinct(res_id)),0) from zomato", nativeQuery = true)
    int getRestaurantIdCount();

    @Query(value = "SELECT COALESCE(COUNT(distinct(res_id)),0) from zomato where store_code is null", nativeQuery = true)
    int getRestaurantIdCountWherestoreCodeIsNull();

    @Query(value = "SELECT distinct(res_id) from zomato where store_code is null", nativeQuery = true)
    List<String> getRestaurantIdsCountWherestoreCodeIsNull();

//    @Query(value = "SELECT z from Zomato z where (z.actualDiscount<>0"
//            + " or z.logisticsCharge<>0"
//            + " or z.proDiscountPassthrough<>0"
//            + " or z.customerDiscount<>0"
//            + " or z.rejectionPenaltyCharge<>0"
//            + " or z.userCreditCharge<>0"
//            + " or z.promoRecoveryAdj<>0"
//            + " or z.icecreamHandling<>0"
//            + " or z.icecreamDeductions<>0"
//            + " or z.oderSupportCost<>0"
//            + " or z.creditNoteAmount<>0"
//            + " or z.merchantDeliveryCharge<>0)"
//            + " and (z.orderDate between :startDate AND :endDate)"
//            + " AND (COALESCE(:storeCodes,NULL) is null or z.storeCode in (:storeCodes))")
//    List<Zomato> getUnknownColumnIfPresent(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);
}
