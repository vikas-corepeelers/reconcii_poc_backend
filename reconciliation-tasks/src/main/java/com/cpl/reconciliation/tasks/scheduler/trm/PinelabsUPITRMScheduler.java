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
//public class PinelabsUPITRMScheduler extends AbstractDataScheduler {
//
//    @Override
//    @Task(name = "####PINELABS_TRM_UPI_JOB####")
//    @Scheduled(cron = "0 */5 * * * ?")
//    public void schedule() throws Exception {
//        dataServiceMap.get(DataSource.PineLabs_TRM_UPI).executeTask();
//    }
//}
