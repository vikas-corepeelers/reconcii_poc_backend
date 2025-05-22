package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.BudgetMasterDao;
import com.cpl.reconciliation.domain.entity.BudgetMasterEntity;
import com.cpl.reconciliation.domain.repository.BudgetMasterRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class BudgetMasterDaoImpl implements BudgetMasterDao {

    private final BudgetMasterRepository budgetMasterRepository;

    @Override
    public List<BudgetMasterEntity> getAll() {
        return budgetMasterRepository.findAll();
    }

    @Override
    public void saveAll(List<BudgetMasterEntity> budgetMasterEntities) {
        budgetMasterRepository.saveAll(budgetMasterEntities);
    }

}
