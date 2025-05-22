package com.cpl.reconciliation.web.service.impl;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.tasks.service.DataService;
import com.cpl.reconciliation.web.service.JobApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Data
@Slf4j
@Service
public class JobApiServiceImpl implements JobApiService {

    @Autowired
    protected List<DataService> dataServiceList;
    protected Map<DataSource, DataService> dataServiceMap = new HashMap<>();
    @Autowired
    @Qualifier(value = "asyncExecutor")
    protected Executor asyncExecutor;

    @PostConstruct
    public void init() {
        dataServiceList.stream().forEach(dataService -> {
            dataServiceMap.put(dataService.getDataSource(), dataService);
        });
    }

    @Override
    public void runJob(DataSource jobName) {
        DataService dataService = dataServiceMap.get(jobName);
        CompletableFuture.runAsync(()-> {
            try {
                dataService.executeTask();
            } catch (Exception e) {
                log.error("Exception occurred in JOB {}", jobName, e);
            }
        },asyncExecutor);
    }
}
