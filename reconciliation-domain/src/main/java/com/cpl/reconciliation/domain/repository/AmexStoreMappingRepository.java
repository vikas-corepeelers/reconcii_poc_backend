package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.AmexStoreMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AmexStoreMappingRepository extends JpaRepository<AmexStoreMapping, Long> {

    Optional<AmexStoreMapping> findByAmexStoreName(String amexStoreName);
}
