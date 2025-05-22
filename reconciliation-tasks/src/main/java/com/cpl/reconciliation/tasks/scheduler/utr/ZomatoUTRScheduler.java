//package com.cpl.reconciliation.tasks.scheduler.utr;
//
//import com.cpl.core.common.annotations.Task;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.tasks.scheduler.AbstractDataScheduler;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Data
//@Slf4j
//@Component
//public class ZomatoUTRScheduler extends AbstractDataScheduler {
//    @Override
//    @Task(name = "ZomatoUTR")
////    @Scheduled(cron = "0 0 9 * * ?")
////    @Scheduled(fixedRate = 24*60*60, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
//    public void schedule() throws Exception {
//        dataServiceMap.get(DataSource.ZOMATO_UTR).executeTask();
//    }
//}
