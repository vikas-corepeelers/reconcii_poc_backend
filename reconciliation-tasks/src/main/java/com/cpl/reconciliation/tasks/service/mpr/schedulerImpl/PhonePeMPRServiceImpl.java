//package com.cpl.reconciliation.tasks.service.mpr.schedulerImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.service.mpr.processorImpl.PhonePayMPRProcessorImpl;
//import com.cpl.reconciliation.tasks.service.mpr.processorImpl.YesMPRProcessorImpl;
//import com.cpl.reconciliation.tasks.utils.MailService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.simplejavamail.converter.EmailConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.mail.Message;
//import javax.mail.internet.MimeMessage;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
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
//public class PhonePeMPRServiceImpl extends AbstractService implements DataService {
//
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final DataEntryLogDao dataEntryLogDao;
//    private final MailService emailService;
//    private final MPRDao mprDao;
//    private final PhonePayMPRProcessorImpl phonePayMPRProcessor;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.PHONEPE_MPR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//            if (dataEntryLogList.isEmpty()) {
//                String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//                String path = sftpRoot + fileSeparator + "phonePe" + fileSeparator + "mpr" + fileSeparator + businessDate;
//                File folder = new File(path);
//                if (isDirectoryWithContent(folder)) {
//                    File[] files = folder.listFiles(extentionFilter(".eml"));
//                    log.info("phonePe MPR folder {} file count: {}", path, files.length);
//                    for (File file : files) {
//                        MimeMessage msg = EmailConverter.emlToMimeMessage(file);
//                        try {
//                            phonePayMPRProcessor.processMessages(new Message[]{msg});
//                        } catch (Exception e) {
//                            log.error("Error while parsing phonePe MPR mail: " + file.getName() + " : " + e.getMessage(), e);
//                        }
//                    }
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//                } else {
//                    log.error("phonePe MPR folder does not exists: {}", path);
//                }
//            } else {
//                log.info("file already parsed for today");
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while reading phonePe MPR email: ", e);
//        }
//    }
//
//    @Override
//    public boolean uploadManually(LocalDate businessDate, LocalDate endDate, List<MultipartFile> files) throws IOException {
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), businessDate, endDate);
//        if (dataEntryLogList.isEmpty()) {
//            List<InputStream> inputStreams = files.stream().map(file -> {
//                try {
//                    return file.getInputStream();
//                } catch (IOException e) {
//                    throw new ApiException("Error reading files");
//                }
//            }).toList();
//            uploadAsync(businessDate, endDate,inputStreams, LocalDateTime.now());
//        } else {
//            log.info("File already parsed for business Date: {}", businessDate);
//            return false;
//        }
//        return true;
//    }
//
//    public void upload( LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
////                phonePayMPRProcessor.parseExcelFileByName(inputStream);
//                phonePayMPRProcessor.processFile(inputStream);
//            }
//            mprDao.getMprBankDifference(Bank.PHONEPE.name(), PaymentType.UPI.name(), businessDate.format(Formatter.YYYYMMDD_DASH), endDate.format(Formatter.YYYYMMDD_DASH));
////            mprDao.getMprBankDifference(Bank.PHONEPE.name(), PaymentType.CARD.name(), businessDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), endDate.format(Formatter.YYYYMMDD_DASH));
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//}
