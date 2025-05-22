//package com.cpl.reconciliation.tasks.scheduler.trm;
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
//@Profile("prod")
//public class PaytmTRMScheduler extends AbstractDataScheduler {
//
//    @Override
//    @Task(name = "####PAYTM_TRM_JOB####")
//    @Scheduled(fixedRate = 70, initialDelay = 2, timeUnit = TimeUnit.MINUTES)
//    public void schedule() throws Exception {
//        dataServiceMap.get(DataSource.PayTm_TRM).executeTask();
//    }
//}
