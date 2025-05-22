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
//@Data
//@Slf4j
//@Component
//@Profile("prod")
//public class PinelabsCardTRMScheduler extends AbstractDataScheduler {
//
//    @Override
//    @Task(name = "####PINELABS_TRM_CARD_JOB####")
//    @Scheduled(cron = "0 */3 * * * ?")
//    public void schedule() throws Exception {
//        try {
//            dataServiceMap.get(DataSource.PineLabs_TRM_CARD).executeTask();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
