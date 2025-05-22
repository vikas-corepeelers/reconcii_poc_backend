package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.FreebiesMasterEntity;

import java.util.List;

public interface FreebiesMasterDao {
    List<FreebiesMasterEntity> getAll();

    void saveAll(List<FreebiesMasterEntity> freebiesMasterEntities);
}
