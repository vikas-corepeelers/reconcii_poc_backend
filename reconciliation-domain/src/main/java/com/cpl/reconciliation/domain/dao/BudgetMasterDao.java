package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.BudgetMasterEntity;

import java.util.List;

public interface BudgetMasterDao {
    List<BudgetMasterEntity> getAll();

    void saveAll(List<BudgetMasterEntity> budgetMasterEntities);
}
