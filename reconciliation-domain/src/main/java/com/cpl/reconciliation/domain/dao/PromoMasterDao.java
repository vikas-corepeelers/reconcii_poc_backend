package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.PromoMasterEntity;

import java.util.List;

public interface PromoMasterDao {
    List<PromoMasterEntity> getAll();

    void saveAll(List<PromoMasterEntity> promoMasterEntities);
}
