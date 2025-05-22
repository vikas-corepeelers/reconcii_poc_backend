package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.GlobalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalDataRepository extends JpaRepository<GlobalEntity, Long> {
}
