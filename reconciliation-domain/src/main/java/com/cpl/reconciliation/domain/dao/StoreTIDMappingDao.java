package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.StoreTIDMapping;

import java.util.Optional;

public interface StoreTIDMappingDao {

    Optional<StoreTIDMapping> findByTid(String tid);

    Optional<StoreTIDMapping> findByTidLike(String tid);

    String getStoreCodeByMid(String mid);
}
