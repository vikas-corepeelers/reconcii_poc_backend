package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.RecoLogicsEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author Abhishek N
 */
public interface RecoLogicRepository extends JpaRepository<RecoLogicsEntity, Long> {

    @Query("SELECT r FROM RecoLogicsEntity r WHERE tender= :tender AND recologic= :recoLogic")
    List<RecoLogicsEntity> findByTenderAndRecoLogic(@Param("tender") String tender,
            @Param("recoLogic") String recoLogic);

    @Query("SELECT r FROM RecoLogicsEntity r WHERE tender= :tender")
    List<RecoLogicsEntity> findByTender(@Param("tender") String tender);

    @Query(value = "SELECT effectivefrom FROM reco_logics order by effectivefrom ASC limit 1", nativeQuery = true)
    String findOldestEffectiveDate();

    @Query("SELECT r FROM RecoLogicsEntity r WHERE tender = :tender AND ("
            + "(r.effectiveFrom >= :effectiveFrom AND r.effectiveFrom <= :effectiveTo) OR "
            + "(r.effectiveTo >= :effectiveFrom AND r.effectiveTo <= :effectiveTo) OR "
            + "(r.effectiveFrom <= :effectiveFrom AND r.effectiveTo >= :effectiveTo) OR "
            + "(r.effectiveFrom >= :effectiveFrom AND r.effectiveTo <= :effectiveTo))")
    List<RecoLogicsEntity> findByTenderAndDateRange(@Param("tender") String tender, @Param("effectiveFrom") String effectiveFrom, @Param("effectiveTo") String effectiveTo);

    @Query("SELECT r FROM RecoLogicsEntity r WHERE id <> :id AND tender = :tender AND ("
            + "(r.effectiveFrom >= :effectiveFrom AND r.effectiveFrom <= :effectiveTo) OR "
            + "(r.effectiveTo >= :effectiveFrom AND r.effectiveTo <= :effectiveTo) OR "
            + "(r.effectiveFrom <= :effectiveFrom AND r.effectiveTo >= :effectiveTo) OR "
            + "(r.effectiveFrom >= :effectiveFrom AND r.effectiveTo <= :effectiveTo))")
    List<RecoLogicsEntity> findByTenderAndDateRangeUpdate(@Param("id") Long id, @Param("tender") String tender, @Param("effectiveFrom") String effectiveFrom, @Param("effectiveTo") String effectiveTo);

    @Query("SELECT r FROM RecoLogicsEntity r WHERE status <> 'PROCESSED'")
    List<RecoLogicsEntity> findByNotProcessedStatus();
}
