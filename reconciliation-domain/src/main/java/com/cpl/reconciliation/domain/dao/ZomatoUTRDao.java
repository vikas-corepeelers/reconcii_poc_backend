package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.ZomatoUTREntity;

import java.util.List;

public interface ZomatoUTRDao {
    void saveAll(List<ZomatoUTREntity> zomatoUTREntityEntities);
}
