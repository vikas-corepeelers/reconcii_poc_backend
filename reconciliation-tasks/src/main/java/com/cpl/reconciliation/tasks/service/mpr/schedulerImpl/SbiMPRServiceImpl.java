//package com.cpl.reconciliation.tasks.service.mpr.schedulerImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.service.mpr.processorImpl.SbiMPRProcessorImpl;
//import com.cpl.reconciliation.tasks.utils.MailService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.extentionFilter;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.isDirectoryWithContent;
//
//@Data
//@Slf4j
//@Service
//public class SbiMPRServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final MailService emailService;
//    private final SbiMPRProcessorImpl sbiProcessor;
//    private final DataEntryLogDao dataEntryLogDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//
//    @Value("${temp.mprpath}")
//    private String tempFolderPath;
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.SBI_MPR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//            List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//            if (dataEntryLogList.isEmpty()) {
//                String path = sftpRoot + fileSeparator + "sbi" + fileSeparator + "mpr" + fileSeparator + businessDate;
//                File folder = new File(path);
//                if (isDirectoryWithContent(folder)) {
//                    File[] files = folder.listFiles(extentionFilter("xlsx"));
//                    log.info("SBI MPR folder {} file count: {}", path, files.length);
//                    for (File file : files) sbiProcessor.parseExcelFile(new FileInputStream(file));
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//                } else {
//                    log.error("SBI MPR folder does not exists: {}", path);
//                }
//            } else {
//                log.info("File already parsed for Business Date: {}", businessDate);
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while reading SBI MPR SFTP: ", e);
//        }
//    }
//
//    @Override
//    public boolean uploadManually(LocalDate businessDate, LocalDate endDate, List<MultipartFile> files) throws IOException {
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), businessDate, endDate);
//        if (dataEntryLogList.isEmpty()) {
//            List<InputStream> inputStreams = files.stream().map(file -> {
//                        try {
//                            String path = tempFolderPath+fileSeparator + "sbi";
//                            Files.createDirectories(Paths.get(path));
//                            File tempFile = new File(path+fileSeparator+new java.sql.Timestamp(System.currentTimeMillis()).getTime()+file.getOriginalFilename());
//                            file.transferTo(tempFile);
//                            InputStream fileInputStream = new FileInputStream(tempFile);
//                            return fileInputStream;
//                        } catch (Exception e) {
//                            log.error("Exception occurred while parsing SBI MPR: ", e);
//                            throw new RuntimeException(e);
//                        }
//                    }
//            ).toList();
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
//        } else {
//            log.info("File already parsed for Business Date: {}", businessDate);
//            return false;
//        }
//        return true;
//    }
//
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
////                sbiProcessor.parseExcelFile(inputStream);
//                sbiProcessor.parseExcelFileByName(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//}
