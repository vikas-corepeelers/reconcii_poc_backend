package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.SwiggyIGCCEntity;
import com.cpl.reconciliation.domain.entity.ZomatoPromoEntity;

import java.util.List;

public interface ZomatoPromoDao {
    List<ZomatoPromoEntity> getAll();
    void saveAll(List<ZomatoPromoEntity>zomatoPromoEntities);
}
