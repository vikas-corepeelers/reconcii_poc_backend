package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.domain.entity.DashBoardEntity;

import java.time.LocalDate;
import java.util.List;

public interface DashboardDao {
    List<DashBoardEntity> getAll();
    DashboardDataResponse getDashboardDataResponse(LocalDate businessDate);

    void getCashDashboardDataResponse(LocalDate businessDate);
}
