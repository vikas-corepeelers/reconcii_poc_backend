package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.MPRRefundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MPRRefundRepository extends JpaRepository<MPRRefundEntity, Long> {
}
