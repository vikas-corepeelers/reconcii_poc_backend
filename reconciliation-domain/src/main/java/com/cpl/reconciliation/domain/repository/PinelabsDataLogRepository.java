package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.domain.entity.PinelabsDataLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PinelabsDataLogRepository extends JpaRepository<PinelabsDataLog, Long> {

    @Query("SELECT MAX(p.offset) FROM PinelabsDataLog p WHERE p.paymentType = :paymentType")
    Long findMaxOffset(@Param("paymentType") PaymentType paymentType);

    @Query(value = "SELECT p.offset FROM pinelabs_data_log p WHERE p.payment_type = :paymentType", nativeQuery = true)
    Long findOffset(@Param("paymentType") String paymentType);

    @Modifying
    @Transactional
    @Query(value = "update pinelabs_data_log p set p.offset=:offset where p.payment_type = :paymentType", nativeQuery = true)
    void setOffset(@Param("paymentType") String paymentType, @Param("offset") Long offset);
}
