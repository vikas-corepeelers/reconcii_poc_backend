//package com.cpl.reconciliation.tasks.scheduler.order;
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
//@Profile("prod")
//public class STLDParserScheduler extends AbstractDataScheduler {
//
//    @Override
//    @Task(name = "####STLD_Parser_JOB####")
//    @Scheduled(cron = "0 30 6 * * ?")
//    public void schedule() throws Exception {
//        log.info("STLDParserScheduler triggered");
//        dataServiceMap.get(DataSource.POS_ORDERS).executeTask();
//    }
//}