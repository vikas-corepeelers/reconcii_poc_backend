//package com.cpl.reconciliation.domain.repository;
//
//import com.cpl.reconciliation.domain.entity.Magicpin;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public interface MagicpinRepository extends JpaRepository<Magicpin, String> {
//
//    @Query(value = "SELECT z from Magicpin z where z.orderId=:orderId ")
//    List<Magicpin> findByOrderId(String orderId);
//
//    @Query(value = "SELECT COUNT(*) from Magicpin s " +
//            "where s.date between :startDate AND :endDate AND s.foundInDotPe=1" +
//            "  AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    int getReconciledOrderCountFoundInDotPe(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
//
//    @Query(value = "SELECT COUNT(*) from Magicpin s " +
//            "where s.date between :startDate AND :endDate AND s.foundInDotPe=0 " +
//            " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    int getOrderCountNotFoundInDotPe(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
//
//    @Query(value = "SELECT s from Magicpin s " +
//            "where s.date between :startDate AND :endDate AND s.foundInDotPe=:foundInDotPe " +
//            " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    List<Magicpin> getOrdersAsPerDotPe(LocalDate startDate, LocalDate endDate, List<String> storeCodes, boolean foundInDotPe);
//
//    @Query(value = "SELECT s from Magicpin s " +
//            "where s.date between :startDate AND :endDate " +
//            " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    List<Magicpin> findAllByDateAndStoreCodes(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
//
//    @Query(value = "SELECT s from Magicpin s " +
//            "where s.date between :startDate AND :endDate and s.foundInDotPe=1 and s.foundInSTLD=1 " +
//            " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    List<Magicpin> findAllReconciledByDateAndStoreCodes(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
//
//    @Query(value = "SELECT s from Magicpin s " +
//            "where s.date between :startDate AND :endDate and s.foundInSTLD=1 " +
//            " AND (COALESCE(:storeCodes,NULL) is null or s.storeCode in (:storeCodes))")
//    List<Magicpin> findAllOrdersByDateAndStoreCodesFoundInPOS(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
//
//    @Query(value = "SELECT COALESCE(COUNT(distinct(mid)),0) from Magicpin")
//    int getRestaurantIdCount();
//
//    @Query(value = "SELECT COALESCE(COUNT(distinct(mid)),0) from Magicpin where storeCode is null")
//    int getRestaurantIdCountWherestoreCodeIsNull();
//
//    @Query(value = "SELECT distinct(mid) from Magicpin where storeCode is null")
//    List<String> getRestaurantIdWherestoreCodeIsNull();
//
//    @Query(value = "SELECT m from Magicpin m where m.mfp <> 0 and m.date between :startDate AND :endDate and (COALESCE(:storeCodes,NULL) is null or m.storeCode in (:storeCodes)) ")
//    List<Magicpin> getUnknownColumnIfPresent(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
//
//}
