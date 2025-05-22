package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.core.api.exception.ApiException;
import com.cpl.reconciliation.domain.dao.SwiggyDao;
import com.cpl.reconciliation.domain.dao.ThreePoDashboardDao;
import com.cpl.reconciliation.domain.dao.ZomatoDao;
import com.cpl.reconciliation.domain.entity.ThreePoDashBoardEntity;
import com.cpl.reconciliation.domain.repository.ThreePoDashboardRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Data
@Slf4j
@Service
public class ThreePoDashboardDaoImpl implements ThreePoDashboardDao {

    private final ThreePoDashboardRepository threePoDashboardRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SwiggyDao swiggyService;
    private final ZomatoDao zomatoService;
    @Autowired
    @Qualifier(value = "asyncExecutor")
    protected Executor asyncExecutor;

    @Override
    public List<ThreePoDashBoardEntity> getAll() {
        return threePoDashboardRepository.findAll();
    }

    @Override
    public void updateThreePOData(LocalDate businessDate) {
        try {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    zomatoService.updateDataResponse(businessDate);
                    swiggyService.updateDataResponse(businessDate);
                } catch (Exception e) {
                    log.error("Error while fetching Swiggy dashboard data for 3PO ", e);
                    throw new ApiException("Error while fetching Swiggy dashboard data for 3PO");
                }
            }, asyncExecutor);
            future.get();
        } catch (Exception e) {
            throw new ApiException("Error while fetching data");
        }
    }

}
