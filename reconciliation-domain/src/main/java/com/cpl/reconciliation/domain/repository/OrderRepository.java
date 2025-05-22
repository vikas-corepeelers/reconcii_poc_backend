package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {

//    @Query(value = "SELECT o FROM OrderEntity o where o.threePOSource=:threePOSource AND  o.threePOOrderId=:orderId and o.orderStatus='Paid' and o.invoiceNumber is not null")
//    List<OrderEntity> findByThreePOSourceAndOrderId(String threePOSource, String orderId);

    @Query(value = "SELECT COALESCE(SUM(total_amount-total_tax),0) FROM orders o WHERE (business_date between :startDate and :endDate)"
            + " AND threeposource=:threePOSource "
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    double getTotalAmountByDateAndThreePOSource(LocalDate startDate, LocalDate endDate, String threePOSource, List<String> storeCodes);

//    @Query(value = "SELECT o FROM OrderEntity o WHERE (o.businessDate between :startDate and :endDate)"
//            + " AND o.threePOSource=:threePOSource "
//            + " AND (COALESCE(:storeCodes,null) is null or o.storeId in (:storeCodes)) and o.orderStatus='Paid' and o.invoiceNumber is not null")
//    List<OrderEntity> getAllOrdersByDateAndThreePoAndStores(LocalDate startDate, LocalDate endDate, String threePOSource, List<String> storeCodes);

//    @Query(value = "SELECT o FROM OrderEntity o WHERE DATE(o.orderDate)=:nextDate AND o.businessDate=:endDate "
//            + " AND o.threePOSource=:threePOSource "
//            + " AND (COALESCE(:storeCodes,null) is null or o.storeId in (:storeCodes)) and o.orderStatus='Paid' and o.invoiceNumber is not null")
//    List<OrderEntity> getSubsequentDateOrders(LocalDate endDate, Date nextDate, String threePOSource, List<String> storeCodes);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount-o.total_tax),0) FROM orders o WHERE (o.order_date between :startDate and :endDate) AND o.threeposource='swiggy' AND EXISTS (select * from swiggy s where s.order_no=o.threepoorder_id)"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    double getTotalSalesReconciledInSwiggy(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount-o.total_tax),0) FROM orders o WHERE (o.order_date between :startDate and :endDate) AND o.threeposource='zomato' AND EXISTS (select * from zomato z where z.order_id=o.threepoorder_id)"
            + " AND (COALESCE(:storeCodes,null) is null or storeId in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    double getTotalSalesReconciledInZomato(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount-o.total_tax),0) FROM orders o WHERE (o.order_date between :startDate and :endDate) AND o.threeposource='zomato' AND NOT EXISTS (select * from zomato z where z.order_id=o.threepoorder_id)"
            + " AND (COALESCE(:storeCodes,null) is null or o.storeId in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    double getTotalSalesNotFoundInZomato(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount-o.total_tax),0) FROM orders o WHERE (o.order_date between :startDate and :endDate) AND o.threeposource='magicpin' AND NOT EXISTS (select * from magicpin z where z.order_id=o.threepoorder_id)"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    double getTotalSalesNotFoundInMagicpin(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT o FROM OrderEntity o WHERE (o.orderDate between :startDate and :endDate) AND o.threePOSource='zomato' AND NOT EXISTS (select z from Zomato z where z.invoiceNumber=o.invoiceNumber)"
//            + " AND (COALESCE(:storeCodes,null) is null or o.storeId in (:storeCodes)) and o.orderStatus='Paid' and o.invoiceNumber is not null")
//    List<OrderEntity> getAllOrdersNotFoundInZomato(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT o FROM OrderEntity o WHERE (o.orderDate between :startDate and :endDate) AND o.threePOSource='swiggy' AND NOT EXISTS (select z from Swiggy z where z.orderNo=o.threePOOrderId)"
//            + " AND (COALESCE(:storeCodes,null) is null or o.storeId in (:storeCodes)) and o.orderStatus='Paid' and o.invoiceNumber is not null")
//    List<OrderEntity> getAllOrdersNotFoundInSwiggy(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT o FROM OrderEntity o WHERE (o.orderDate between :startDate and :endDate) AND o.threePOSource='magicpin' AND NOT EXISTS (select z from Magicpin z where z.orderId=o.threePOOrderId)"
//            + " AND (COALESCE(:storeCodes,null) is null or o.storeId in (:storeCodes)) and o.orderStatus='Paid' and o.invoiceNumber is not null")
//    List<OrderEntity> getAllOrdersNotFoundInMagicpin(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COALESCE(SUM(o.total_amount-o.total_tax),0) FROM orders o WHERE (o.order_date between :startDate and :endDate) AND o.threeposource='swiggy' AND NOT EXISTS (select * from swiggy z where z.order_no=o.threepoorder_id)"
            + " AND (COALESCE(:storeCodes,null) is null or o.store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    double getTotalSalesNotFoundInSwiggy(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

//    @Query(value = "SELECT COALESCE(SUM(o.total_amount-o.total_tax),0) FROM orders o WHERE (o.order_date between :startDate and :endDate) AND o.threeposource='magicpin' AND EXISTS (select * from magicpin m where m.order_id=o.threepoorder_id)"
//            + " AND (COALESCE(:storeCodes,null) is null or store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
//    double getTotalSalesReconciledInMagicPin(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COUNT(*) from orders o "
            + "where o.threeposource=:threePo and o.order_date between :startDate and :endDate"
            + "  AND (COALESCE(:storeCodes,null) is null or store_id in (:storeCodes)) and o.order_status='Paid' and o.invoice_number is not null", nativeQuery = true)
    int getTotalPOSOrderCount(LocalDateTime startDate, LocalDateTime endDate, String threePo, List<String> storeCodes);

}
