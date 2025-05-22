//package com.cpl.reconciliation.tasks.service.trm;
//
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.*;
//import com.cpl.reconciliation.domain.dao.TRMDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.TRMEntity;
//import com.cpl.reconciliation.domain.models.PaytmTRM;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.modelmapper.Converter;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//
//import static com.cpl.reconciliation.core.util.CPLFileUtils.isDirectoryWithContent;
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//import static com.cpl.reconciliation.tasks.Constants.REGEX_SINGLE_QUOTES;
//
//@Data
//@Slf4j
//@Service
//public class PayTmTRMServiceImpl extends AbstractService implements DataService {
//    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private final TRMDao trmDao;
//    private final ModelMapper modelMapper;
//
//    @PostConstruct
//    public void postConstruct() {
//        Converter<String, PaymentType> paymentModeMap = ctx -> {
//            String paymentMode = ctx.getSource();
//            if (paymentMode != null) {
//                if (paymentMode.toUpperCase().contains("UPI")) {
//                    return PaymentType.UPI;
//                } else if (paymentMode.toUpperCase().contains("CARD")) {
//                    return PaymentType.CARD;
//                } else if (paymentMode.toUpperCase().contains("PAYTM_DIGITAL_CREDIT")) {
//                    return PaymentType.PAYTM_DIGITAL_CREDIT;
//                } else if (paymentMode.toUpperCase().contains("NET_BANKING")) {
//                    return PaymentType.NET_BANKING;
//                } else if (paymentMode.toUpperCase().contains("PPI")) {
//                    return PaymentType.PPI;
//                }
//            }
//            return PaymentType.UNKNOWN;
//        };
//        Converter<String, String> cardTypeMap = ctx -> {
//            String cardType = ctx.getSource();
//            if (cardType != null && !cardType.toUpperCase().contains("UPI")) return cardType;
//            return null;
//        };
//        Converter<String, TransactionStatus> statusMap = ctx -> {
//            String status = ctx.getSource();
//            if (status != null) {
//                if (status.toUpperCase().contains("SUCCESS")) {
//                    return TransactionStatus.SUCCESS;
//                } else if (status.toUpperCase().contains("FAILURE")) {
//                    return TransactionStatus.FAILED;
//                } else if (status.toUpperCase().contains("PENDING")) {
//                    return TransactionStatus.PENDING;
//                }
//            }
//            return null;
//        };
//        Converter<String, String> orderId = ctx -> {
//            String status = ctx.getSource();
//            if (status != null) {
//                return status.replaceAll("T", "");
//            }
//            return null;
//        };
//        modelMapper.typeMap(PaytmTRM.class, TRMEntity.class)
//                .addMapping(PaytmTRM::getPosId, TRMEntity::setPosId)
//                .addMappings(mapper -> {
//                    mapper.using(orderId).map(PaytmTRM::getMerchantRequestId, TRMEntity::setOrderId);
//                })
//                .addMapping(PaytmTRM::getAmount, TRMEntity::setTrmAmount)
//                .addMapping(PaytmTRM::getTransactionId, TRMEntity::setTransactionId)
//                .addMapping(PaytmTRM::getAcquiringBank, TRMEntity::setAcquirerBank)
//                .addMapping(PaytmTRM::getMid, TRMEntity::setMid)
//                .addMappings(mapper -> {
//                    mapper.using(paymentModeMap).map(PaytmTRM::getPaymentMode, TRMEntity::setPaymentType);
//                    mapper.using(cardTypeMap).map(PaytmTRM::getPaymentMode, TRMEntity::setCardType);
//                })
//                .addMapping(PaytmTRM::getCardLast4Digits, TRMEntity::setCardNumber)
//                .addMapping(PaytmTRM::getAuthCode, TRMEntity::setAuthCode)
//                .addMapping(PaytmTRM::getRrn, TRMEntity::setRrn)
//                .addMapping(PaytmTRM::getTransactionDate, TRMEntity::setTransactionDate)
//                .addMappings(mapper -> {
//                    mapper.using(statusMap).map(PaytmTRM::getStatus, TRMEntity::setTransactionStatus);
//                })
//                .addMapping(PaytmTRM::getSettledDate, TRMEntity::setSettlementDate)
//                .addMapping(PaytmTRM::getCustomerVPA, TRMEntity::setCustomerVPA);
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.PayTm_TRM;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            String path = "/var/sftp/paytmbank/trm";
//            File folder = new File(path);
//            if (isDirectoryWithContent(folder)) {
//                File[] files = folder.listFiles();
//                log.info("PayTm TRM folder {} file count: {}", path, files.length);
//                for (File file : files) readTransactionsFromCSV(new FileInputStream(file));
//            } else {
//                log.error("PayTm TRM folder does not exists: {}", path);
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while reading PayTm TRM SFTP: ", e);
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
//        } else {
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                readTransactionsFromCSV(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//
//    public void readTransactionsFromCSV(InputStream inputStream) {
//        log.info("Going to read PayTm TRM file");
//        List<TRMEntity> transactions = new ArrayList<>();
//        Function<String, String> removeSingleQuotes = (a) -> a.replaceAll(REGEX_SINGLE_QUOTES, "");
//        try (CSVParser csvParser = CSVParser.parse(inputStream, Charset.defaultCharset(), CSVFormat.DEFAULT)) {
//            for (CSVRecord csvRecord : csvParser) {
//                PaytmTRM transaction = new PaytmTRM();
//                transaction.setIssuingBank(removeSingleQuotes.apply(csvRecord.get(0)));
//                transaction.setCustomerVPA(removeSingleQuotes.apply(csvRecord.get(1)));
//                transaction.setCustomerDetails(removeSingleQuotes.apply(csvRecord.get(2)));
//                transaction.setMid(removeSingleQuotes.apply(csvRecord.get(3)));
//                transaction.setMerchantName(removeSingleQuotes.apply(csvRecord.get(4)));
//                transaction.setPosId(removeSingleQuotes.apply(csvRecord.get(5)));
//                transaction.setMerchantRefId(removeSingleQuotes.apply(csvRecord.get(6)));
//                transaction.setStoreAddress(removeSingleQuotes.apply(csvRecord.get(7)));
//                transaction.setStoreCity(removeSingleQuotes.apply(csvRecord.get(8)));
//                transaction.setStoreState(removeSingleQuotes.apply(csvRecord.get(9)));
//                transaction.setCardType(removeSingleQuotes.apply(csvRecord.get(10)));
//                transaction.setTransactionId(removeSingleQuotes.apply(csvRecord.get(11)));
//                transaction.setOrderId(removeSingleQuotes.apply(csvRecord.get(12)));
//                transaction.setTransactionDate(parseDate(removeSingleQuotes.apply(csvRecord.get(13))));
//                transaction.setUpdDate(parseDate(removeSingleQuotes.apply(csvRecord.get(14))));
//                transaction.setTransactionType(removeSingleQuotes.apply(csvRecord.get(15)));
//                transaction.setStatus(removeSingleQuotes.apply(csvRecord.get(16)));
//                transaction.setAmount(parseDouble(removeSingleQuotes.apply(csvRecord.get(17))));
//                transaction.setCommission(parseDouble(removeSingleQuotes.apply(csvRecord.get(18))));
//                transaction.setGst(parseDouble(removeSingleQuotes.apply(csvRecord.get(19))));
//                transaction.setPaymentMode(removeSingleQuotes.apply(csvRecord.get(20)));
//                transaction.setPaymentReferenceNumber(removeSingleQuotes.apply(csvRecord.get(21)));
//                transaction.setCardLast4Digits(removeSingleQuotes.apply(csvRecord.get(22)));
//                transaction.setAuthCode(removeSingleQuotes.apply(csvRecord.get(23)));
//                transaction.setRrn(removeSingleQuotes.apply(csvRecord.get(24)));
//                transaction.setArn(removeSingleQuotes.apply(csvRecord.get(25)));
//                transaction.setUtrNo(removeSingleQuotes.apply(csvRecord.get(26)));
//                transaction.setPayoutDate(parseDate(removeSingleQuotes.apply(csvRecord.get(27))));
//                transaction.setSettledDate(parseDate(removeSingleQuotes.apply(csvRecord.get(28))));
//                transaction.setSettledAmount(parseDouble(removeSingleQuotes.apply(csvRecord.get(29))));
//                transaction.setAcquiringBank(removeSingleQuotes.apply(csvRecord.get(30)));
//                transaction.setMerchantRequestId(removeSingleQuotes.apply(csvRecord.get(31)));
//                TRMEntity trm = modelMapper.map(transaction, TRMEntity.class);
//                trm.setSource(TRMSource.PAYTM);
//                trm.setAcquirerBank(Bank.PAYTM);
//                if (trm.getPaymentType().equals(PaymentType.CARD)) {
//                    trm.setAcquirerBank(Bank.HDFC);
//                    trm.setUid(getComposite(trm.getRrn(), trm.getAuthCode(), trm.getCardNumber()));
//                } else if (trm.getPaymentType().equals(PaymentType.NET_BANKING) || trm.getPaymentType().equals(PaymentType.PPI)) {
//                    trm.setUid(transaction.getOrderId());
//                    trm.setPaymentType(PaymentType.UPI);
//                } else {
//                    trm.setUid(trm.getRrn());
//                    if (trm.getPaymentType().equals(PaymentType.PAYTM_DIGITAL_CREDIT)) {
//                        trm.setPaymentType(PaymentType.UPI);
//                    }
//                }
//                transactions.add(trm);
//                if (transactions.size() >= 500) {
//                    log.info("PayTm TRM batch {} write invoked", transactions.size());
//                    trmDao.saveAll(transactions);
//                    transactions.clear();
//                }
//            }
//            log.info("PayTm TRM batch {} write invoked", transactions.size());
//            trmDao.saveAll(transactions);
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing PayTm TRM: ", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//    private LocalDateTime parseDate(String value) {
//        try {
//            if (StringUtils.hasText(value)) {
//                return LocalDateTime.parse(value, DF);
//            }
//        } catch (Exception e) {
//            log.info("PayTm date formatter Exception: {}", e.getMessage());
//        }
//        return null;
//    }
//
//    private Double parseDouble(String value) {
//        try {
//            if (StringUtils.hasText(value)) {
//                return Double.parseDouble(value);
//            }
//        } catch (Exception e) {
//            log.info("PayTm number formatter Exception: {}", e.getMessage());
//        }
//        return 0.0;
//    }
//}
