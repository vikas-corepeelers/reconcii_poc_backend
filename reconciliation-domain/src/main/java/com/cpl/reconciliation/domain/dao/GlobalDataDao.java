package com.cpl.reconciliation.domain.dao;


import com.cpl.reconciliation.domain.entity.GlobalEntity;
import java.util.List;

public interface GlobalDataDao {
    void saveAll(List<GlobalEntity> globalEntities);
}
