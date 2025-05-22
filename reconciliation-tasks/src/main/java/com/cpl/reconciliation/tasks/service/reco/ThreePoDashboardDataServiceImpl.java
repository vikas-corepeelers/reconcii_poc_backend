package com.cpl.reconciliation.tasks.service.reco;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.domain.dao.impl.ThreePoDashboardDaoImpl;
import com.cpl.reconciliation.tasks.service.AbstractService;
import com.cpl.reconciliation.tasks.service.DataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Data
@Slf4j
public class ThreePoDashboardDataServiceImpl extends AbstractService implements DataService {
    private final ThreePoDashboardDaoImpl threePodashBoardService;

    @Override
    public DataSource getDataSource() {
        return DataSource.THREEPO_DASHBOARD;
    }

    @Override
    public void executeTask() throws Exception {
        try {
            LocalDate businessDate=LocalDate.now();
            threePodashBoardService.updateThreePOData(businessDate);
            log.info("service executed successfully");
        }
        catch (Exception e){
            e.printStackTrace();
            log.error("error : {}",e.getMessage());
        }
    }

    @Override
    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) throws IOException {

    }
}
