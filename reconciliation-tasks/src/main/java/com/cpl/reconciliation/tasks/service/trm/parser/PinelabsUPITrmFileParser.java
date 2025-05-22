//package com.cpl.reconciliation.tasks.service.trm.parser;
//
//import com.cpl.core.common.annotations.TrackExecutionTime;
//import com.cpl.reconciliation.core.enums.*;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.dao.TRMDao;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.entity.TRMEntity;
//import com.cpl.reconciliation.domain.models.PineLabsUPITRM;
//import com.cpl.reconciliation.domain.repository.StoreTIDMappingRepository;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
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
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Function;
//
//@Data
//@Slf4j
//@Component
//public class PinelabsUPITrmFileParser {
//    private final static Function<String, String> removeSingleQuotes = (a) -> a.replaceAll("^'+|'+$", "");
//    private final static DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//    private final TRMDao trmDao;
//    private final ModelMapper modelMapper;
//    private final StoreTIDMappingDao storeTIDMappingDao;
//
//    @PostConstruct
//    public void postConstruct() {
//        Converter<String, TransactionStatus> statusMap = ctx -> {
//            String status = ctx.getSource();
//            if (status != null) {
//                if (status.toUpperCase().contains("SUCCESS")) return TransactionStatus.SUCCESS;
//                else if (status.toUpperCase().contains("FAILED")) return TransactionStatus.FAILED;
//                else if (status.toUpperCase().contains("PENDING")) return TransactionStatus.PENDING;
//                else if (status.toUpperCase().contains("EXPIRED")) return TransactionStatus.SESSION_EXPIRED;
//                else if (status.toUpperCase().contains("CANCELLED")) return TransactionStatus.CANCELLED;
//            }
//            return TransactionStatus.UNKNOWN;
//        };
//        Converter<String, TransactionType> typeMap = ctx -> {
//            String type = ctx.getSource();
//            if (type != null) {
//                if (TransactionType.SALE.name().equalsIgnoreCase(type)) return TransactionType.SALE;
//                else if (TransactionType.VOID.name().equalsIgnoreCase(type)) return TransactionType.VOID;
//                else if (TransactionType.REFUND.name().equalsIgnoreCase(type)) return TransactionType.REFUND;
//            }
//            return TransactionType.OTHER;
//        };
//        Converter<String, Bank> acquirer = ctx -> Bank.getBank(ctx.getSource());
//        Converter<String, LocalDateTime> date = ctx -> {
//            String src = ctx.getSource();
//            return parseDate(src);
//        };
//        modelMapper.typeMap(PineLabsUPITRM.class, TRMEntity.class)
//                .addMapping(PineLabsUPITRM::getPosId, TRMEntity::setPosId)
//                .addMapping(PineLabsUPITRM::getBillReferenceNo, TRMEntity::setOrderId)
//                .addMapping(PineLabsUPITRM::getTxnAmt, TRMEntity::setTrmAmount)
//                .addMapping(PineLabsUPITRM::getTransactionId, TRMEntity::setTransactionId)
//                .addMappings(mapper -> {
//                    mapper.using(acquirer).map(PineLabsUPITRM::getAcquirer, TRMEntity::setAcquirerBank);
//                })
//                .addMapping(PineLabsUPITRM::getMid, TRMEntity::setMid)
//                .addMapping(PineLabsUPITRM::getTid, TRMEntity::setTid)
//                .addMapping(PineLabsUPITRM::getRrn, TRMEntity::setRrn)
//                .addMappings(mapper -> {
//                    mapper.using(date).map(PineLabsUPITRM::getTxnTime, TRMEntity::setTransactionDate);
//                    mapper.using(date).map(PineLabsUPITRM::getTxnTime, TRMEntity::setSettlementDate);
//                })
//                .addMappings(mapper -> {
//                    mapper.using(statusMap).map(PineLabsUPITRM::getTxnStatus, TRMEntity::setTransactionStatus);
//                })
//                .addMappings(mapper -> {
//                    mapper.using(typeMap).map(PineLabsUPITRM::getTxnType, TRMEntity::setTransactionType);
//                })
//                .addMapping(PineLabsUPITRM::getCustomerVpa, TRMEntity::setCustomerVPA);
//    }
//
//    @TrackExecutionTime
//    public int readTransactionsFromCSV(InputStream inputStream) throws Exception {
//        log.info("Going to parse TRM file for UPI");
//        AtomicInteger count = new AtomicInteger();
//        List<TRMEntity> transactions = new ArrayList<>();
//        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
//            CsvToBean<PineLabsUPITRM> csvToBean = new CsvToBeanBuilder<PineLabsUPITRM>(reader)
//                    .withType(PineLabsUPITRM.class)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .withOrderedResults(true)
//                    .build();
//            List<PineLabsUPITRM> transactionList = csvToBean.parse();
//            log.info("Going to parse TRM file for UPI: {}", transactionList.size());
//            transactionList.stream().filter(txn -> txn.getTxnType() != null && txn.getTxnType().equalsIgnoreCase("SALE"))
//                    .forEach(txn -> {
//                        TRMEntity entity = modelMapper.map(txn, TRMEntity.class);
//                        entity.setPaymentType(PaymentType.UPI);
//                        entity.setSource(TRMSource.PINE_LABS);
//                        Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(entity.getTid());
//                        if (storeTIDMapping.isPresent()) {
//                            entity.setStoreId(storeTIDMapping.get().getStoreCode());
//                        }else {
//                            entity.setStoreId(null);
//                        }
//                        if (entity.getAcquirerBank().equals(Bank.HDFC) || entity.getAcquirerBank().equals(Bank.ICICI)) {
//                            entity.setUid(entity.getTransactionId());
//                        }
//                        transactions.add(entity);
//                        if (transactions.size() >= 2000) {
//                            log.info("Pinelabs UPI TRM batch {} write invoked", transactions.size());
//                            trmDao.saveAll(transactions);
//                            count.set(count.get() + transactions.size());
//                            transactions.clear();
//                        }
//                    });
//            log.info("Pinelabs UPI TRM batch {} write invoked", transactions.size());
//            trmDao.saveAll(transactions);
//            count.set(count.get() + transactions.size());
//            return count.get();
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing Pinelabs UPI TRM: ", e);
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