package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {

    @Query("SELECT DISTINCT s.state FROM StoreEntity s")
    List<String> findDistinctStates();
    
    @Query("SELECT DISTINCT s.city FROM StoreEntity s")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT s.storeCode FROM StoreEntity s where s.state='RAJASTHAN'")
    List<String> findRajasthanStoreCodes();

    List<StoreEntity> findByState(String state);

    List<StoreEntity> findByStoreOpeningDateLessThanEqual(LocalDate end);

    List<StoreEntity> findByStateInAndStoreOpeningDateLessThanEqual(List<String> states, LocalDate end);

    Optional<StoreEntity> findByStoreCode(String storeCode);

    List<StoreEntity> findByCity(String city);


    @Query("SELECT s FROM StoreEntity s where s.city IN (:cities)")
    List<StoreEntity> findByCities(@Param("cities") List<String> cities);
}
