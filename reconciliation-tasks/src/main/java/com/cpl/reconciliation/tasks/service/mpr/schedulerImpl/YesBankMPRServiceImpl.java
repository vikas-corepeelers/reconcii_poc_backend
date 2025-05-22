//package com.cpl.reconciliation.tasks.service.mpr.schedulerImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.CardType;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.models.YesBankMPR;
//import com.cpl.reconciliation.domain.repository.StoreTIDMappingRepository;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.service.mpr.processorImpl.YesMPRProcessorImpl;
//import com.cpl.reconciliation.tasks.utils.MailService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.simplejavamail.converter.EmailConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.mail.Message;
//import javax.mail.internet.MimeMessage;
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Function;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.extentionFilter;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.isDirectoryWithContent;
//
//@Data
//@Slf4j
//@Service
//public class YesBankMPRServiceImpl extends AbstractService implements DataService {
//    private final static Function<String, String> removeLeadingEqualDoubleQuotes = (a) -> a.replaceAll("^[=]+|\"+", "");
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final DataEntryLogDao dataEntryLogDao;
//    private final MailService emailService;
//    private final MPRDao mprDao;
//    private final StoreTIDMappingRepository storeTIDMappingRepository;
//    private final Utility utility;
//    private final YesMPRProcessorImpl yesBankMPRService;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.YESBANK_MPR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//            if (dataEntryLogList.isEmpty()) {
//                String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//                String path = sftpRoot + fileSeparator + "yes" + fileSeparator + "mpr" + fileSeparator + businessDate;
//                File folder = new File(path);
//                if (isDirectoryWithContent(folder)) {
//                    File[] files = folder.listFiles(extentionFilter(".eml"));
//                    log.info("YES BANK MPR folder {} file count: {}", path, files.length);
//                    for (File file : files) {
//                        MimeMessage msg = EmailConverter.emlToMimeMessage(file);
//                        try {
//                            yesBankMPRService.processMessages(new Message[]{msg});
//                        } catch (Exception e) {
//                            log.error("Error while parsing Yes BANK MPR mail: " + file.getName() + " : " + e.getMessage(), e);
//                        }
//                    }
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//                } else {
//                    log.error("YES BANK MPR folder does not exists: {}", path);
//                }
//            } else {
//                log.info("file already parsed for today");
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while reading YES BANK MPR email: ", e);
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
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
//        } else {
//            log.info("File already parsed for business Date: {}", businessDate);
//            return false;
//        }
//        return true;
//    }
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
////                yesBankMPRService.parseExcelFileByName(inputStream);
//                readTransactionsFromCSV(inputStream);
//            }
////            mprDao.getMprBankDifference(Bank.YES.name(), PaymentType.UPI.name(), businessDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), endDate.format(Formatter.YYYYMMDD_DASH));
//            mprDao.getMprBankDifference(Bank.YES.name(), PaymentType.CARD.name(), businessDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), endDate.format(Formatter.YYYYMMDD_DASH));
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//
//
//    public void readTransactionsFromCSV(InputStream file) throws IOException {
//        CsvToBean<YesBankMPR> csvToBean = new CsvToBeanBuilder<YesBankMPR>(createReaderFromMultipartFile(file))
//                .withType(YesBankMPR.class)
//                .withIgnoreLeadingWhiteSpace(true)
//                .withOrderedResults(true)
//                .build();
//        List<YesBankMPR> transactionList = csvToBean.parse();
//        List<MPREntity> mprEntities = new ArrayList<>();
//        for (YesBankMPR yesBankMPR : transactionList) {
//            if (yesBankMPR.getTransactionType().equalsIgnoreCase("SALE") && !yesBankMPR.getRrn().equals(null)) {
//                MPREntity mprEntity = new MPREntity();
//                mprEntity.setBank(Bank.getBank(yesBankMPR.getBank()));
//                mprEntity.setTid(removeLeadingEqualDoubleQuotes.apply(yesBankMPR.getTid()));
//                mprEntity.setMid(removeLeadingEqualDoubleQuotes.apply(yesBankMPR.getMid()));
//                String cardType = yesBankMPR.getCardType();
//                mprEntity.setCardType(cardType.contains("CREDIT") ? CardType.Credit.name() : (cardType.contains("DEBIT") ? CardType.Debit.name() : CardType.Unknown.name()));
//                mprEntity.setCardNumber(yesBankMPR.getCardNumber());
//                mprEntity.setTransactionId(yesBankMPR.getTransactionId());
//                mprEntity.setTransactionDate(yesBankMPR.getTransactionDate());
//                mprEntity.setSettledDate(yesBankMPR.getSettlementDate());
//                mprEntity.setAuthCode(yesBankMPR.getAuthCode());
//                mprEntity.setMprAmount(yesBankMPR.getTransactionAmount());
//                mprEntity.setCommission(yesBankMPR.getMdrAmount());
//                mprEntity.setSettledAmount(yesBankMPR.getNetAmount());
//                mprEntity.setCgst(yesBankMPR.getCgstAmount());
//                mprEntity.setSgst(yesBankMPR.getSgstAmount());
//                mprEntity.setIgst(yesBankMPR.getIgstAmount());
//                mprEntity.setGst(yesBankMPR.getGst());
//                mprEntity.setRrn(removeLeadingEqualDoubleQuotes.apply(yesBankMPR.getRrn()));
//                mprEntity.setPayerVA(yesBankMPR.getCustomerId());
//                mprEntity.setPaymentType(PaymentType.getPaymentType(yesBankMPR.getPaymentType()));
//                mprEntity.setExpectedMDR(yesBankMPR.getMdrAmount());
//                mprEntity.setExpectedBankSettlementDate(mprEntity.getSettledDate());
//                mprEntity.setBankCharges(mprEntity.getCommission() + mprEntity.getIgst() + mprEntity.getCgst() + mprEntity.getSgst() + mprEntity.getGst());
//                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingRepository.findByTid(mprEntity.getTid());
//                mprEntity.setStoreId(storeTIDMapping.isPresent() ? storeTIDMapping.get().getStoreCode() : null);
//                if (mprEntity.getRrn().equals(null)) {
//                    log.info("NULL  entry for rrn");
//                    continue;
//                }
//                mprEntity.setUid(mprEntity.getRrn());
//                mprEntity.setId(mprEntity.getUid());
//                mprEntities.add(mprEntity);
//            }
//        }
//
//        List<List<MPREntity>> transactionChunks = utility.chunkList(mprEntities, 500);
//        for (List<MPREntity> chunk : transactionChunks) {
//            mprDao.saveAll(chunk);
//            log.info("YES BANK MPR Saved : {}", chunk.size());
//        }
//    }
//
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//}
