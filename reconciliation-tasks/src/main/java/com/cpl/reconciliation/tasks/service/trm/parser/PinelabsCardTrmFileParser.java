//package com.cpl.reconciliation.tasks.service.trm.parser;
//
//import com.cpl.core.common.annotations.TrackExecutionTime;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.core.enums.TRMSource;
//import com.cpl.reconciliation.core.enums.TransactionStatus;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.dao.TRMDao;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.entity.TRMEntity;
//import com.cpl.reconciliation.domain.models.PineLabsCardTRM;
//import com.cpl.reconciliation.domain.repository.StoreTIDMappingRepository;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.modelmapper.Converter;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.PostConstruct;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.function.Function;
//
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//
//@Data
//@Slf4j
//@Component
//public class PinelabsCardTrmFileParser {
//    private final static Function<String, String> removeSingleQuotes = (a) -> a.replaceAll("^'+|'+$", "");
//    private final static DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//    private final TRMDao trmDao;
//    private final ModelMapper modelMapper;
//    private final StoreTIDMappingDao storeTIDMappingDao;
//
//    @PostConstruct
//    public void postConstruct() {
//        Converter<String, String> cardNo = ctx -> ctx.getSource().replace("*", "");
//        Converter<String, TransactionStatus> statusMap = ctx -> {
//            String status = ctx.getSource();
//            if (status != null && status.toUpperCase().contains("SETTLED")) {
//                return TransactionStatus.SUCCESS;
//            }
//            return TransactionStatus.FAILED;
//        };
//        modelMapper.typeMap(PineLabsCardTRM.class, TRMEntity.class)
//                .addMapping(PineLabsCardTRM::getPos, TRMEntity::setPosId)
//                .addMapping(PineLabsCardTRM::getBillInvoice, TRMEntity::setOrderId)
//                .addMapping(PineLabsCardTRM::getAmount, TRMEntity::setTrmAmount)
//                .addMapping(PineLabsCardTRM::getTransactionId, TRMEntity::setTransactionId)
//                .addMapping(PineLabsCardTRM::getAcquirer, TRMEntity::setAcquirerBank)
//                .addMapping(PineLabsCardTRM::getMid, TRMEntity::setMid)
//                .addMapping(PineLabsCardTRM::getTid, TRMEntity::setTid)
//                .addMapping(PineLabsCardTRM::getCardType, TRMEntity::setCardType)
//                .addMappings(mapper -> {
//                    mapper.using(cardNo).map(PineLabsCardTRM::getCardNumber, TRMEntity::setCardNumber);
//                })
//                .addMapping(PineLabsCardTRM::getCardColour, TRMEntity::setNetworkType)
//                .addMapping(PineLabsCardTRM::getApprovalCode, TRMEntity::setAuthCode)
//                .addMapping(PineLabsCardTRM::getRrn, TRMEntity::setRrn)
//                .addMapping(PineLabsCardTRM::getDate, TRMEntity::setTransactionDate)
//                .addMappings(mapper -> {
//                    mapper.using(statusMap).map(PineLabsCardTRM::getStatus, TRMEntity::setTransactionStatus);
//                })
//                .addMapping(PineLabsCardTRM::getSettlementDate, TRMEntity::setSettlementDate);
//    }
//
//    @TrackExecutionTime
//    public int readTransactionsFromCSV(InputStream inputStream) throws Exception{
//        int count = 0;
//        List<TRMEntity> transactions = new ArrayList<>();
//        try (InputStreamReader reader = new InputStreamReader(inputStream);
//             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
//            for (CSVRecord csvRecord : csvParser) {
//                if (csvRecord.get(18) == null || !csvRecord.get(18).equalsIgnoreCase("SALE")) continue;
//                PineLabsCardTRM transaction = new PineLabsCardTRM();
//                transaction.setSerialNumber(Integer.parseInt(csvRecord.get(0)));
//                transaction.setStoreName(csvRecord.get(1));
//                transaction.setCity(csvRecord.get(2));
//                transaction.setPos(csvRecord.get(3));
//                transaction.setHardwareModel(csvRecord.get(4));
//                transaction.setAcquirer(csvRecord.get(5));
//                transaction.setTid(csvRecord.get(6));
//                transaction.setMid(removeSingleQuotes.apply(csvRecord.get(7)));
//                transaction.setBatchNumber(csvRecord.get(8));
//                transaction.setCardNumber(csvRecord.get(9));
//                transaction.setName(csvRecord.get(10));
//                transaction.setCardIssuer(csvRecord.get(11));
//                transaction.setCardType(csvRecord.get(12));
//                transaction.setCardNetwork(csvRecord.get(13));
//                transaction.setCardColour(csvRecord.get(14));
//                transaction.setTransactionId(csvRecord.get(15));
//                transaction.setInvoice(csvRecord.get(16));
//                transaction.setApprovalCode(csvRecord.get(17));
//                transaction.setTransactionType(csvRecord.get(18));
//                transaction.setAmount(parseDouble(csvRecord.get(19)));
//                transaction.setTipAmount(parseDouble(csvRecord.get(20)));
//                transaction.setCurrency(csvRecord.get(21));
//                transaction.setDate(parseDate(csvRecord.get(22)));
//                transaction.setStatus(csvRecord.get(23));
//                transaction.setSettlementDate(parseDate(csvRecord.get(24)));
//                transaction.setProductAmount(parseDouble(csvRecord.get(25)));
//                transaction.setInsurer(csvRecord.get(26));
//                transaction.setPlan(csvRecord.get(27));
//                transaction.setInsuranceAmount(parseDouble(csvRecord.get(28)));
//                transaction.setCashier(csvRecord.get(29));
//                transaction.setBillInvoice(csvRecord.get(30).replace("T", ""));
//                transaction.setRrn(removeSingleQuotes.apply(csvRecord.get(31)));
//                transaction.setEmiTxn(csvRecord.get(32));
//                transaction.setEmiMonth(csvRecord.get(33));
//                transaction.setContactless(csvRecord.get(34));
//                transaction.setContactlessMode(csvRecord.get(35));
//                transaction.setCloudRefId(csvRecord.get(36));
//                transaction.setCardPanCheck(csvRecord.get(37));
//                transaction.setRoutePreauthToOtherAcquirer(csvRecord.get(38));
//                transaction.setBillingTransactionId(csvRecord.get(39));
//                transaction.setMerchantInput1(csvRecord.get(40));
//                transaction.setMerchantInput2(csvRecord.get(41));
//                transaction.setMerchantInput3(csvRecord.get(42));
//                TRMEntity trm = modelMapper.map(transaction, TRMEntity.class);
//                trm.setSource(TRMSource.PINE_LABS);
//                trm.setPaymentType(PaymentType.CARD);
//                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(trm.getTid());
//                if(storeTIDMapping.isPresent()) {
//                    trm.setStoreId(storeTIDMapping.get().getStoreCode());
//                }else {
//                    trm.setStoreId(null);
//                }
//                trm.setUid(getComposite(transaction.getRrn(), transaction.getApprovalCode(), transaction.getCardNumber()));
//                transactions.add(trm);
//                if (transactions.size() >= 2000) {
//                    log.info("Pinelabs CARD TRM batch {} write invoked", transactions.size());
//                    trmDao.saveAll(transactions);
//                    count= count+ transactions.size();
//                    transactions.clear();
//                }
//            }
//            log.info("Pinelabs CARD TRM batch {} write invoked", transactions.size());
//            trmDao.saveAll(transactions);
//            count= count+ transactions.size();
//            return count;
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing Pinelabs CARD TRM: ", e);
//            throw e;
//        }
//    }
//
//    private LocalDateTime parseDate(String value) {
//        try {
//            if (StringUtils.hasText(value)) {
//                return LocalDateTime.parse(value, DF);
//            }
//        } catch (Exception e) {
//            log.info("Pinelabs date format Exception: {}", e.getMessage());
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
//            log.info("Pinelabs number format Exception: {}", e.getMessage());
//        }
//        return 0.0;
//    }
//}
