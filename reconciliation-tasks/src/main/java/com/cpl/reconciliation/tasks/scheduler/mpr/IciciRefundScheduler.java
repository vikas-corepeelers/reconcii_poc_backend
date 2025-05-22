//package com.cpl.reconciliation.tasks.scheduler.mpr;
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
//@Data
//@Slf4j
//@Component
//@Profile("XXX")
//public class IciciRefundScheduler extends AbstractDataScheduler {
//
//    @Override
//    @Task(name = "####ICICI_REFUND_JOB####")
//    @Scheduled(cron = "0 0 * * * ?")
//    public void schedule() throws Exception {
//        dataServiceMap.get(DataSource.ICICI_REFUND).executeTask();
//    }
//}