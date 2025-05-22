package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.BankHolidays;
import com.cpl.reconciliation.domain.entity.SwiggyIGCCEntity;

import java.util.List;

public interface SwiggyIGCCDao {
    List<SwiggyIGCCEntity> getAll();
    void saveAll(List<SwiggyIGCCEntity>swiggyIGCCEntities);
}
