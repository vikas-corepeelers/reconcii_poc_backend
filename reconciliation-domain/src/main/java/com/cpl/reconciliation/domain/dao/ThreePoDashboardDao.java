package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.threepo.ThreePOData;
import com.cpl.reconciliation.domain.entity.DashBoardEntity;
import com.cpl.reconciliation.domain.entity.ThreePoDashBoardEntity;

import java.time.LocalDate;
import java.util.List;

public interface ThreePoDashboardDao {
    List<ThreePoDashBoardEntity> getAll();

    void updateThreePOData(LocalDate businessDate);
}
