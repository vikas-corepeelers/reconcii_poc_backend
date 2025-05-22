package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.SwiggyPromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SwiggyPromoRepository extends JpaRepository<SwiggyPromo, String> {

    @Query(value = "SELECT COUNT(*) from SwiggyPromo where date between :startDate and :endDate and couponCode='WELCOMEBACK'" +
            " AND (COALESCE(:storeCodes,NULL) is null or storeCode in (:storeCodes))")
    int getWelcomeBackTransactionCount(LocalDate startDate, LocalDate endDate, List<String> storeCodes);

    @Query("SELECT sp from SwiggyPromo sp where sp.date between :startDate and :endDate and sp.remarks != null and sp.remarks like '%Freebie%' and sp.freebieItem is null")
    List<SwiggyPromo> getAllWhereFreebieItemNotSet(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(0.45*SUM(COALESCE(discountTotal,0)),0) from SwiggyPromo sp where sp.date between :startDate and :endDate " +
            "and (sp.remarks is null or sp.remarks not like '%Freebie%') AND (COALESCE(:storeCodes,NULL) is null or storeCode in (:storeCodes))")
    double getPromoShare(LocalDate startDate, LocalDate endDate, List<String> storeCodes);

}
