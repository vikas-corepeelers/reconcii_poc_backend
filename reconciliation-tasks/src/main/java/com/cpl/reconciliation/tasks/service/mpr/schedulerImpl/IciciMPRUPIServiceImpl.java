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
//import com.cpl.reconciliation.tasks.service.mpr.processorImpl.IciciMPRUpiProcessorImpl;
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
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.extentionFilter;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.isDirectoryWithContent;
//
//@Data
//@Slf4j
//@Service
//public class IciciMPRUPIServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final MailService emailService;
//    private final IciciMPRUpiProcessorImpl iciciMPRProcessor;
//    private final DataEntryLogDao dataEntryLogDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.ICICI_MPR_UPI;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            String businessDate = DateToString.backDateString(Formatter.YYYYMMDD,1);
//            List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//            if (dataEntryLogList.isEmpty()) {
//                String path = sftpRoot + fileSeparator + "icici" + fileSeparator + "mpr" + fileSeparator + businessDate;
//                File folder = new File(path);
//                if (isDirectoryWithContent(folder)) {
//                    List<Message> messages = new ArrayList<>();
//                    File[] files = folder.listFiles(extentionFilter(".eml"));
//                    log.info("ICICI MPR folder {} file count: {}", path, files.length);
//                    for (File file : files) {
//                        MimeMessage msg = EmailConverter.emlToMimeMessage(file);
//                        messages.add(msg);
//                    }
//                    iciciMPRProcessor.processMessages(messages.toArray(new Message[messages.size()]));
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH),files.length);
//                } else {
//                    log.error("ICICI MPR folder does not exists: {}", path);
//                }
//            }
//            else{
//                log.info("File already parsed for Business Date: {}", businessDate);
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while reading ICICI MPR email: ", e);
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
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//        }else {
//            log.info("File already parsed for Business Date: {}", businessDate);
//            return false;
//        }
//        return true;
//    }
//
//    public void upload( LocalDate businessDate,LocalDate endDate,List<InputStream> inputStreams,LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                iciciMPRProcessor.saveXLSFiles(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time,businessDate,endDate);
//        } catch (Exception e) {
//            sendFailureMail(time,businessDate,endDate);
//        }
//    }
//}
