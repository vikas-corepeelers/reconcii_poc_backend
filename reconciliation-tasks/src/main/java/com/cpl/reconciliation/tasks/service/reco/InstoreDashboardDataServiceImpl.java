package com.cpl.reconciliation.tasks.service.reco;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.domain.dao.impl.DashboardDaoImpl;
import com.cpl.reconciliation.tasks.service.DataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Data
@Slf4j
public class InstoreDashboardDataServiceImpl implements DataService {
    private final DashboardDaoImpl dashBoardService;

    @Override
    public DataSource getDataSource() {
        return DataSource.INSTORE_DASHBOARD;
    }

    @Override
    public void executeTask() throws Exception {
        try {
            LocalDate businessDate=LocalDate.now();
            dashBoardService.getDashboardDataResponse(businessDate);
            dashBoardService.getCashDashboardDataResponse(businessDate);
            log.info("service executed successfully");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
