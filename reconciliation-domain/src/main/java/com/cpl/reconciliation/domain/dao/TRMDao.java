package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.TRMEntity;

import java.util.List;

public interface TRMDao {

    void saveAll(List<TRMEntity> trmEntities);

    void save(TRMEntity entity);
}
