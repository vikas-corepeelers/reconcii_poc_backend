package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreTIDMappingRepository extends JpaRepository<StoreTIDMapping, Long> {

    Optional<StoreTIDMapping> findByTid(String tid);

    Optional<StoreTIDMapping> findByTidLike(String tid);

    @Query(value="SELECT distinct(s.store_code) FROM store_tid_mapping s where s.mid =:midParam",nativeQuery = true)
    String getStoreCodeByMid(@Param("midParam") String midParam);
}
