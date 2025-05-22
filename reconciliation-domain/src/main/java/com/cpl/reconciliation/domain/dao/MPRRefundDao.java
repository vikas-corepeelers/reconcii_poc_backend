package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.MPRRefundEntity;

import java.util.List;

public interface MPRRefundDao {

    void saveAll(List<MPRRefundEntity> iciciRefundReports);
}
