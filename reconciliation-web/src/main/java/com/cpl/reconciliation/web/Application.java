package com.cpl.reconciliation.web;

import com.cpl.core.api.constant.DateTime;
import com.cpl.core.common.config.CoreConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.TimeZone;

@Slf4j
@SpringBootApplication
@Import({CoreConfiguration.class})
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(DateTime.Timezone.IST_TZ));
        TimeZone defaultTimeZone = TimeZone.getDefault();
        log.info("Default Time Zone ID: {} Display Name: {}", defaultTimeZone.getID(), defaultTimeZone.getDisplayName());
        log.info("Starting Reconciliation Service");
        SpringApplication.run(Application.class, args);
        log.info("Started Reconciliation Service");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Start command line runner");
        log.info("Stop command line runner");
    }
}
