package com.cpl.reconciliation.web.api;
/**
 *
 * @author Abhishek N
 */
import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.enums.DSLog;
import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.response.DataSourceLogResponse;
import com.cpl.reconciliation.domain.entity.DataEntryLog;
import com.cpl.reconciliation.domain.repository.DataEntryLogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;

@Data
@Slf4j
@RestController
@RequestMapping("/api/ve1/datalog/")
public class DataSourceLogController {
    @Autowired
    private DataEntryLogRepository dataEntryLogRepository;

    @GetMapping("/lastSynced")
    public ApiResponse<DataSourceLogResponse> dataLogStatusHandler(@RequestParam("dsLog") DSLog dsLog,
                                                                   @RequestParam(value = "storeCode", required = false) String storeCode) {
        PriorityQueue<LocalDate> priorityQueue = new PriorityQueue<>();
        DataSourceLogResponse dataSourceLogResponse = new DataSourceLogResponse();
        List<String> dataSources = new ArrayList<>();
        switch (dsLog) {
            case UPI -> {
                dataSources = List.of(
                        DataSource.PayTm_TRM.name(),
                        DataSource.PineLabs_TRM_UPI.name(),
                        DataSource.HDFC_MPR.name(),
                        DataSource.ICICI_MPR_UPI.name(),
                        DataSource.SBI_BS.name(),
                        DataSource.HDFC_BS.name(),
                        DataSource.ICICI_BS.name(),
                        DataSource.YESBANK_BS.name()
                );
            }
            case CARD -> {
                dataSources = List.of(
                        DataSource.PayTm_TRM.name(),
                        DataSource.PineLabs_TRM_CARD.name(),
                        DataSource.SBI_MPR.name(),
                        DataSource.AMEX_MPR.name(),
                        DataSource.HDFC_MPR.name(),
                        DataSource.ICICI_MPR_CARD.name(),
                        DataSource.SBI_BS.name(),
                        DataSource.HDFC_BS.name(),
                        DataSource.ICICI_BS.name(),
                        DataSource.YESBANK_BS.name()
                );
            }
            case SWIGGY -> {
                dataSources = List.of(
                        DataSource.SWIGGY.name(),
                        DataSource.GLOBAL.name()
                );
            }
            case ZOMATO -> {
                dataSources = List.of(
                        DataSource.ZOMATO.name(),
                        DataSource.GLOBAL.name(),
                        DataSource.ZOMATO_UTR.name(),
                        DataSource.ZOMATO_SALT.name()
                );
            }
        }
        List<DataEntryLog> dataEntryLogList = dataEntryLogRepository.findLatestRecordsByDataSources(dataSources);
        dataSourceLogResponse.setLastSyncList(dataEntryLogList.stream().map(d -> {
            DataSourceLogResponse.LastSync lastSync = new DataSourceLogResponse.LastSync();
            DataSource dataSource = DataSource.valueOf(d.getDataSource());
            lastSync.setDataSource(dataSource);
            lastSync.setType(dataSource.getType());
            lastSync.setTender(dataSource.getTender());
            lastSync.setLastSynced(d.getEndDate());
            priorityQueue.add(d.getEndDate());
            return lastSync;
        }).collect(Collectors.toList()));
       
        DataEntryLog dataEntryLog=dataEntryLogRepository.findByDatasource(DataSource.POS_ORDERS.name());
        
        if (dataEntryLog != null) {
            DataSourceLogResponse.LastSync lastSync = new DataSourceLogResponse.LastSync();
            lastSync.setDataSource(DataSource.POS_ORDERS);
            lastSync.setType(DataSource.POS_ORDERS.getType());
            lastSync.setTender(DataSource.POS_ORDERS.getTender());
            lastSync.setLastSynced(dataEntryLog.getEndDate());
            dataSourceLogResponse.getLastSyncList().add(lastSync);
        }
        
        dataSourceLogResponse.setLastReconciled(priorityQueue.peek());
        return new ApiResponse<>(dataSourceLogResponse);
    }
}







