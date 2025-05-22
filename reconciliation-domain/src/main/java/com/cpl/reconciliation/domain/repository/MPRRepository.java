package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.MPREntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MPRRepository extends JpaRepository<MPREntity, Long> {
    @Query("SELECT e FROM MPREntity e WHERE " +
            "(bank like :bank) AND " +
            "(booked = :booked) AND " +
            "(settledDate between :startDate AND :endDate) AND " +
            "(payment_type like :tender)")
    List<MPREntity> findMPR(
            @Param("bank") String bank,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("tender") String tender,
            @Param("booked") int booked
    );

    @Transactional
    @Modifying
    @Query(value = "UPDATE mpr\n" +
            "        JOIN\n" +
            "    trm ON mpr.uid = trm.uid\n" +
            "        AND trm.transaction_status = 'SUCCESS'\n" +
            "SET\n" +
            "    mpr.tid = trm.tid,\n" +
            "    mpr.store_id = trm.store_id\n" +
            "WHERE\n" +
            "    mpr.store_id IS NULL\n" +
            "        AND mpr.bank = 'ICICI'\n" +
            "        AND mpr.payment_type = 'UPI'", nativeQuery = true)
    void updateStoreTidFromTRMICICIUPI();

    @Query(value ="SELECT count(distinct(tid)) FROM MPREntity where bank = 'AMEX' and storeId is null")
    int getAmexMissingTidCount();

    @Query(value ="SELECT count(distinct(tid)) FROM MPREntity where bank = 'AMEX'")
    int getAmexTotalTidCount();

    @Query(value ="SELECT distinct(tid) FROM MPREntity where bank = 'AMEX' and storeId is null")
    List<String> getAmexMissingTid();

    @Transactional
    @Modifying
    @Query(value = "UPDATE mpr\n" +
            "JOIN store_tid_mapping ON mpr.tid = store_tid_mapping.tid\n" +
            "SET mpr.store_id = store_tid_mapping.store_code where mpr.store_id is null;",nativeQuery = true)
    void updateStoreMapping();

    @Query(value ="SELECT m FROM MPREntity m where m.bank = :bank and m.transactionDate >= :date")
    List<MPREntity> getMPRWhereMDRisNotSet(LocalDateTime date, String bank);
}
