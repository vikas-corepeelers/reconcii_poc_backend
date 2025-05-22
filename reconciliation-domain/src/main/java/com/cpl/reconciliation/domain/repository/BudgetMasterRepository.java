package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.BudgetMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetMasterRepository extends JpaRepository<BudgetMasterEntity, Long> {
}
