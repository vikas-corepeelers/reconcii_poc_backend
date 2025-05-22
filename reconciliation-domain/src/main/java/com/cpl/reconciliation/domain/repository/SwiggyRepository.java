package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.Swiggy;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SwiggyRepository extends JpaRepository<Swiggy, String> {

    @Query(value = "SELECT COUNT(*) from swiggy where Date(order_date) between :startDate and :endDate "
            + "AND (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))", nativeQuery = true)
    int totalTransactionCount(LocalDate startDate, LocalDate endDate, List<String> storeCodes);

    @Query(value = "SELECT COUNT(*) from swiggy where order_date between :startDate AND :endDate and found_in_dot_pe=1 "
            + " AND (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))", nativeQuery = true)
    int getReconciledOrderCountFoundInDotPe(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COUNT(*) from swiggy "
            + "where order_date between :startDate AND :endDate AND found_in_dot_pe=0"
            + " AND (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))", nativeQuery = true)
    int getOrderCountNotFoundInDotPe(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes);

    @Query(value = "SELECT COALESCE(COUNT(distinct(restaurant_id)),0) from swiggy", nativeQuery = true)
    int getRestaurantIdCount();

    @Query(value = "SELECT COALESCE(COUNT(distinct(restaurant_id)),0) from swiggy where store_code is null", nativeQuery = true)
    int getRestaurantIdCountWherestoreCodeIsNull();

    @Query(value = "SELECT distinct(restaurant_id) from swiggy where store_code is null", nativeQuery = true)
    List<String> getRestaurantIdWherestoreCodeIsNull();

}
