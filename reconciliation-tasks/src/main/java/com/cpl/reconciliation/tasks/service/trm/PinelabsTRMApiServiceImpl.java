//package com.cpl.reconciliation.tasks.service.trm;
//
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.common.annotations.TrackExecutionTime;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.core.enums.TRMSource;
//import com.cpl.reconciliation.domain.dao.TRMDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.PinelabsDataLog;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.entity.TRMEntity;
//import com.cpl.reconciliation.domain.models.TransactionRequest;
//import com.cpl.reconciliation.domain.models.TransactionResponse;
//import com.cpl.reconciliation.domain.models.UPITransaction;
//import com.cpl.reconciliation.domain.repository.PinelabsDataLogRepository;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.service.trm.parser.PinelabsTrmFileParser;
//import com.cpl.reconciliation.tasks.service.trm.parser.PinelabsUPITrmFileParser;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.collections.CollectionUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicReference;
//
//@Data
//@Slf4j
//@Service
//public class PinelabsTRMApiServiceImpl extends AbstractService implements DataService {
//
//    private final TRMDao trmDao;
//    private final PinelabsDataLogRepository pinelabsDataLogRepository;
//    private final PinelabsUPITrmFileParser pinelabsUPITrmFileParser;
//    private final PinelabsTrmFileParser pinelabsTrmFileParser;
//    private final RestTemplate restTemplate;
//    private final String url = "https://analytics.pinelabs.com/public/ rest/data/ pineanalytics/upiTxnDatawithChangeNo.rest";
//    @Value("${trm.pinelabs.username:CONNAUGHT}")
//    private String username;
//    @Value("${trm.pinelabs.password:Pine@321}")
//    private String password;
//
//    @PostConstruct
//    private void init() {
//        log.info("Pinelabs UPI TRM API data Injector Initialized");
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.PineLabs_TRM;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        AtomicReference<Long> offset = new AtomicReference<>(pinelabsDataLogRepository.findMaxOffset(PaymentType.UPI));
//        log.info("PINELABS UPI TRM data sync job Started with offset {}", offset.get());
//        TransactionRequest request = new TransactionRequest();
//        request.setUserName(username);
//        request.setPassword(password);
//        request.setReportType(PaymentType.UPI.name());
//        request.setMaxChangeNoForPC(Objects.isNull(offset.get()) ? "0" : String.valueOf(offset.get()));
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//        HttpEntity<TransactionRequest> requestEntity = new HttpEntity<>(request, headers);
//        ResponseEntity<TransactionResponse> responseEntity = restTemplate.exchange(
//                url,
//                HttpMethod.POST,
//                requestEntity,
//                TransactionResponse.class
//        );
//        if (responseEntity.getStatusCode() == HttpStatus.OK) {
//            List<TRMEntity> trpEntities = new ArrayList<>();
//            TransactionResponse transactionResponse = responseEntity.getBody();
//            log.info("PINELABS UPI TRM data: {}", transactionResponse);
//            List<UPITransaction> transactions = transactionResponse.getUpiTransactionData();
//            if (!CollectionUtils.isEmpty(transactions)) {
//                transactions.stream().forEach(transaction -> {
//                    //if(!"1".equals(transaction.getStatus())) {
//                    log.info(transaction.toString());
//                    //}
//                    TRMEntity trm = new TRMEntity();
//                    trm.setTransactionId(String.valueOf(transaction.getTransactionId()));
//                    trm.setPosId(transaction.getPosId());
//                    Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(transaction.getTid());
//                    if (storeTIDMapping.isPresent()) {
//                        trm.setStoreId(storeTIDMapping.get().getStoreCode());
//                    } else {
//                        trm.setStoreId(null);
//                    }
//                    trm.setOrderId(transaction.getOrderId());
//                    trm.setTrmAmount(transaction.getTxnAmount());
//                    //
//                    trm.setRrn(transaction.getRrn());
//                    trm.setMid(transaction.getMid());
//                    trm.setTid(transaction.getTid());
//                    trm.setPaymentType(PaymentType.UPI);
//                    trm.setAcquirerBank(Bank.getBank(transaction.getAcquirer()));
//                    //UPI Details
//                    trm.setCustomerVPA(transaction.getCustomerVPA());
//                    //
//                    trm.setTransactionDate(transaction.getTxnTime());
//                    trm.setSettlementDate(transaction.getTxnTime());
//                    trm.setTransactionType(transaction.getTransactionType());
//                    trm.setTransactionStatus(transaction.getTransactionStatus());
//                    trm.setSource(TRMSource.PINE_LABS);
//                    trm.setUid(String.valueOf(transaction.getTransactionId()));
//                    trm.setStatus(transaction.getTxnStatus());
//                    trm.setTranscType(transaction.getTxnType());
//                    offset.set(transaction.getChangeNo());
//                    trpEntities.add(trm);
//                    if (trpEntities.size() >= 1000) {
//                        log.info("PINELABS UPI TRM batch {} write invoked", trpEntities.size());
//                        trmDao.saveAll(trpEntities);
//                        trpEntities.clear();
//                    }
//                });
//                log.info("PINELABS UPI TRM batch {} write invoked", trpEntities.size());
//                trmDao.saveAll(trpEntities);
//                PinelabsDataLog pinelabsDataLog = new PinelabsDataLog();
//                pinelabsDataLog.setPaymentType(PaymentType.UPI);
//                pinelabsDataLog.setOffset(offset.get());
//                pinelabsDataLog.setDataSize(transactions.size());
//                pinelabsDataLogRepository.save(pinelabsDataLog);
//                logInDB(LocalDate.now(), 1);
//            }
//        } else {
//            throw new RuntimeException("PINELABS UPI TRM API call failed with status code: " + responseEntity.getStatusCode());
//        }
//        log.info("PINELABS UPI TRM data sync job Stopped with offset {}", offset.get());
//    }
//
//    @TrackExecutionTime
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
//    @TrackExecutionTime
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                try (inputStream) {
//                    pinelabsTrmFileParser.readTransactionsFromCSV(inputStream);
//                }
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//}
