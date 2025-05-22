//package com.cpl.reconciliation.tasks.scheduler.ThreePO;
//
//import com.cpl.core.common.annotations.Task;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.tasks.scheduler.AbstractDataScheduler;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//
//@Data
//@Slf4j
//@Component
//@Profile("XXX")
//public class GlobalDataScheduler extends AbstractDataScheduler {
//
//    @Override
//    @Task(name = "Global")
////    @Scheduled(fixedRate = 24*60*60, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
//    public void schedule() throws Exception {
//        dataServiceMap.get(DataSource.GLOBAL).executeTask();
//    }
//}