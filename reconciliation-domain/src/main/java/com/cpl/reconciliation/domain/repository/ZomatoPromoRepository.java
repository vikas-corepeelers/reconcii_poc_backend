package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.ZomatoPromoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ZomatoPromoRepository extends JpaRepository<ZomatoPromoEntity, String> {
    @Query("SELECT COALESCE(SUM(COALESCE(burn,0)),0) from ZomatoPromoEntity where aggregation between :startDate and :endDate AND (COALESCE(:storeCodes,NULL) is null or storeCode in (:storeCodes))")
    double getPromoShare(LocalDate startDate, LocalDate endDate, List<String> storeCodes);
}
