/*
 * package com.cpl.reconciliation.tasks.scheduler.reco;
 * 
 * import com.cpl.core.common.annotations.Task; import
 * com.cpl.reconciliation.core.enums.DataSource; import
 * com.cpl.reconciliation.tasks.scheduler.AbstractDataScheduler; import
 * lombok.Data; import lombok.extern.slf4j.Slf4j; import
 * org.springframework.scheduling.annotation.Scheduled; import
 * org.springframework.stereotype.Component;
 * 
 * import java.util.concurrent.TimeUnit; import
 * org.springframework.context.annotation.Profile;
 * 
 * @Data
 * 
 * @Slf4j
 * 
 * @Component
 * 
 * @Profile("stage") public class ThreePoDashboardScheduler extends
 * AbstractDataScheduler {
 * 
 * @Override
 * 
 * @Task(name = "threepo_dashboard")
 * 
 * @Scheduled(fixedDelay = 900, initialDelay = 30, timeUnit = TimeUnit.SECONDS)
 * public void schedule() throws Exception {
 * dataServiceMap.get(DataSource.THREEPO_DASHBOARD).executeTask(); } }
 */