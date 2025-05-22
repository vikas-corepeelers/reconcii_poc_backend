package com.cpl.reconciliation.tasks.scheduler;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.tasks.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataScheduler {

    @Autowired
    protected List<DataService> dataServiceList;
    protected Map<DataSource, DataService> dataServiceMap = new HashMap<>();

    @PostConstruct
    public void init() {
        dataServiceList.stream().forEach(dataService -> {
            dataServiceMap.put(dataService.getDataSource(), dataService);
        });
    }

    public abstract void schedule() throws Exception;
}
