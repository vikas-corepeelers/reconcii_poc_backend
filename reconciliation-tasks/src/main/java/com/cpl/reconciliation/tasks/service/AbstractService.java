package com.cpl.reconciliation.tasks.service;

import com.cpl.core.api.constant.Formatter;
import com.cpl.core.api.util.AuthUtils;
import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
import com.cpl.reconciliation.domain.dao.StoreDao;
import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
import com.cpl.reconciliation.domain.entity.DataEntryLog;
import com.cpl.reconciliation.tasks.utils.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.cpl.core.api.constant.Formatter.DDMMMYYYYHHMMSS;
import static com.cpl.core.api.constant.Formatter.MMMddyyyy;

@Slf4j
public abstract class AbstractService implements DataService {

    @Autowired
    protected Environment env;
    @Autowired
    protected StoreDao storeDao;
    @Autowired
    protected MailService mailService;
    @Autowired
    protected DataEntryLogDao dataEntryLogDao;
    @Autowired
    protected StoreTIDMappingDao storeTIDMappingDao;

    @Autowired
    @Qualifier(value = "asyncExecutor")
    protected Executor asyncExecutor;

    public void logInDB(LocalDate businessDate, LocalDate endDate, int length) {
        DataEntryLog dataEntryLog = new DataEntryLog();
        dataEntryLog.setDataSource(getDataSource().name());
        dataEntryLog.setBusinessDate(businessDate);
        dataEntryLog.setEndDate(endDate);
        dataEntryLog.setFileCount(length);
        dataEntryLogDao.save(dataEntryLog);
    }

    public void logInDB(LocalDate businessDate, LocalDate endDate, int length, String startDate, String lastDate, String storeCodes) {
        DataEntryLog dataEntryLog = new DataEntryLog();
        dataEntryLog.setDataSource(getDataSource().name());
        dataEntryLog.setBusinessDate(businessDate);
        dataEntryLog.setEndDate(endDate);
        dataEntryLog.setFileCount(length);
        dataEntryLogDao.save(dataEntryLog);
    }

    public void logInDB(LocalDate businessDate, int length) {
        logInDB(businessDate, businessDate, length);
    }

    public void logInDB(LocalDate businessDate, int fileLength, int recordCount) {
        DataEntryLog dataEntryLog = new DataEntryLog();
        dataEntryLog.setDataSource(getDataSource().name());
        dataEntryLog.setBusinessDate(businessDate);
        dataEntryLog.setEndDate(businessDate);
        dataEntryLog.setFileCount(fileLength);
        dataEntryLog.setRecordCount(recordCount);
        dataEntryLogDao.save(dataEntryLog);
    }

    public void logInDB(LocalDate businessDate, String storeCode, int recordCount) {
        DataEntryLog dataEntryLog = new DataEntryLog();
        dataEntryLog.setDataSource(getDataSource().name());
        dataEntryLog.setBusinessDate(businessDate);
        dataEntryLog.setRecordCount(recordCount);
        dataEntryLog.setStoreCode(storeCode);
        dataEntryLog.setEndDate(businessDate);
        dataEntryLogDao.save(dataEntryLog);
    }

    public void sendSuccessMail(LocalDateTime timestamp, LocalDate startDate, LocalDate endDate) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.smtp.host", env.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", env.getProperty("mail.smtp.port"));
        final String username = env.getProperty("mail.smtp.username");
        final String senderEmail = env.getProperty("mail.smtp.mail");
        final String password = env.getProperty("mail.smtp.password");
        String body = "";
        if (endDate == null) {
            body = String.format("The %s %s report you submitted at %s for date %s has been successfully uploaded.",
                     getDataSource().getTender(),
                     getDataSource().getType(), timestamp.format(DDMMMYYYYHHMMSS), startDate.format(Formatter.MMMddyyyy)
            );
        } else {
            body = String.format("The %s %s report you submitted at %s for date range %s - %s has been successfully uploaded.",
                     getDataSource().getTender(),
                     getDataSource().getType(), timestamp.format(DDMMMYYYYHHMMSS), startDate.format(Formatter.MMMddyyyy), endDate.format(Formatter.MMMddyyyy)
            );
        }
        String[] recipient;
        if (Arrays.stream(env.getActiveProfiles()).anyMatch(k -> k.matches("prod"))) {
            recipient = new String[]{AuthUtils.principal().getEmail()};
        } else {
            recipient = env.getProperty("manualUpload.email", "").split(",");
        }
        String header = env.getProperty("manualUpload.header", "");
        log.info("Going to send Mail: {}", body);
        for (String str : recipient) {
            mailService.sendMail(str, "Manual Upload Status " + header, senderEmail, username, password, props, body);
        }
    }

    public void sendFailureMail(LocalDateTime timestamp, LocalDate startDate, LocalDate endDate) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", env.getProperty("mail.smtp.auth"));
        props.put("mail.smtp.starttls.enable", env.getProperty("mail.smtp.starttls.enable"));
        props.put("mail.smtp.host", env.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", env.getProperty("mail.smtp.port"));
        final String username = env.getProperty("mail.smtp.username");
        final String senderEmail = env.getProperty("mail.smtp.mail");
        final String password = env.getProperty("mail.smtp.password");
        String body = "";
        if (endDate == null) {
            body = String.format("Regret to inform you that the %s %s report you attempted to submit at %s for date %s has encountered an upload failure.",
                     getDataSource().getTender(),
                     getDataSource().getType(), timestamp.format(DDMMMYYYYHHMMSS), startDate.format(MMMddyyyy)
            );
        } else {
            body = String.format("Regret to inform you that the %s %s report you attempted to submit at %s for date range %s %s has encountered an upload failure.",
                     getDataSource().getTender(),
                     getDataSource().getType(), timestamp.format(DDMMMYYYYHHMMSS), startDate.format(MMMddyyyy), endDate.format(MMMddyyyy)
            );
        }
        String[] recipient;
        if (Arrays.stream(env.getActiveProfiles()).anyMatch(k -> k.matches("prod"))) {
            recipient = new String[]{AuthUtils.principal().getEmail()};
        } else {
            recipient = env.getProperty("manualUpload.email", "").split(",");
        }
        String header = env.getProperty("manualUpload.header", "");
        for (String str : recipient) {
            mailService.sendMail(str, "Manual Upload Status " + header, senderEmail, username, password, props, body);
        }
    }

    public void uploadAsync(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
        CompletableFuture.runAsync(() -> {
            try {
                upload(businessDate, endDate, inputStreams, time);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, asyncExecutor);
    }

    public abstract void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) throws IOException;
}
