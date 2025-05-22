//package com.cpl.reconciliation.tasks.scheduler.ThreePO;
//
//import com.cpl.core.common.annotations.Task;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.tasks.scheduler.AbstractDataScheduler;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.TimeUnit;
//
//@Data
//@Slf4j
//@Component
//@Profile("XXX")
//public class MagicpinDataScheduler extends AbstractDataScheduler{
//    @Override
//    @Task(name = "MagicPin")
//    @Scheduled(fixedRate = 1, initialDelay = 0, timeUnit = TimeUnit.DAYS)
//    public void schedule() throws Exception {
//        dataServiceMap.get(DataSource.MAGICPIN).executeTask();
//    }
//}
