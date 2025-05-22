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
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import com.cpl.reconciliation.tasks.utils.PaytmMPRCustomCasting;
//import com.poiji.bind.Poiji;
//import com.poiji.exception.PoijiExcelType;
//import com.poiji.option.PoijiOptions;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.extentionFilter;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.isDirectoryWithContent;
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//
//@Data
//@Slf4j
//@Service
//public class PayTmMPRServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//    private final static DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private final MPRDao mprDao;
//    private final BankSettlementDateUtil settlementDateUtil;
//    private final DataEntryLogDao dataEntryLogDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.PayTm_MPR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//            if (dataEntryLogList.isEmpty()) {
//                String date = DateToString.currentDateString(Formatter.YYYYMMDD);
//                String path = sftpRoot + fileSeparator + "paytmbank" + fileSeparator + "mpr" + fileSeparator + date;
//                File folder = new File(path);
//                if (isDirectoryWithContent(folder)) {
//                    File[] files = folder.listFiles(extentionFilter("xlsx"));
//                    log.info("PayTm MPR folder {} file count: {}", path, files.length);
//                    for (File file : files) {
//                        InputStream inputStream = new FileInputStream(file);
//                        processFile(inputStream);
//                    }
//                    logInDB(stringToLocalDate(date, Formatter.YYYYMMDD), files.length);
//
//                } else {
//                    log.error("PayTm MPR folder does not exists: {}", path);
//                }
//            }
//            else{
//                log.info("file already parsed for today");
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while reading AMEX MPR SFTP: ", e);
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
//
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//
//        } else {
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//    public void upload( LocalDate businessDate,LocalDate endDate,List<InputStream> inputStreams,LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                processFile(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time,businessDate,endDate);
//        } catch (Exception e) {
//            sendFailureMail(time,businessDate,endDate);
//        }
//    }
//
//    public void processFile(InputStream file) {
//        List<MPREntity> payTmMPRS = new ArrayList<>();
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .withCasting(new PaytmMPRCustomCasting())
//                .preferNullOverDefault(true)
//                .build();
//        try {
//            payTmMPRS = Poiji.fromExcel(file, PoijiExcelType.XLSX, MPREntity.class, options);
//        } catch (Exception e) {
//            log.error("PayTm MPR Exception: ", e);
//            throw new RuntimeException(e);
//        }
//        payTmMPRS.forEach(payTmMPR -> {
//            String id = payTmMPR.getTransactionId()+"|"+payTmMPR.getTransactionDate().format(CUSTOM_FORMATTER);
//            payTmMPR.setBank(Bank.PAYTM);
//            payTmMPR.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate
//                    (Bank.PAYTM, payTmMPR.getSettledDate()));
//            if (payTmMPR.getPaymentType().equals(PaymentType.CARD)) {
//                payTmMPR.setUid(getComposite(payTmMPR.getRrn(), payTmMPR.getAuthCode(), payTmMPR.getCardNumber()));
//            } else if (payTmMPR.getPaymentType().equals(PaymentType.NET_BANKING) || payTmMPR.getPaymentType().equals(PaymentType.PPI)) {
//                payTmMPR.setUid(payTmMPR.getTransactionId());
//                payTmMPR.setPaymentType(PaymentType.UPI);
//            } else {
//                payTmMPR.setUid(payTmMPR.getRrn());
//                if (payTmMPR.getPaymentType().equals(PaymentType.PAYTM_DIGITAL_CREDIT)) {
//                    payTmMPR.setPaymentType(PaymentType.UPI);
//                }
//            }
//        });
//        mprDao.saveAll(payTmMPRS);
//    }
//}
