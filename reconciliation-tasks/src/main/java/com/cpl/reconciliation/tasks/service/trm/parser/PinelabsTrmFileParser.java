//package com.cpl.reconciliation.tasks.service.trm.parser;
//
//import com.cpl.core.common.annotations.TrackExecutionTime;
//import com.cpl.reconciliation.core.enums.*;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.dao.TRMDao;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.entity.TRMEntity;
//import com.cpl.reconciliation.domain.models.PineLabsTRM;
//import com.cpl.reconciliation.domain.models.PineLabsUPITRM;
//import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.opencsv.CSVReader;
//import com.opencsv.CSVReaderBuilder;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.modelmapper.Converter;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.PostConstruct;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//
//@Data
//@Slf4j
//@Component
//public class PinelabsTrmFileParser {
//    private final static Function<String, String> removeSingleQuotes = (a) -> a.replaceAll("^'+|'+$", "");
//    private final static DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//    private final TRMDao trmDao;
//    private final ModelMapper modelMapper;
//    private final StoreTIDMappingDao storeTIDMappingDao;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss a");
//    private final Utility utility;
//
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
//        log.info("Going to parse TRM file");
//
//        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
//            CsvToBean<PineLabsTRM> csvToBean = new CsvToBeanBuilder<PineLabsTRM>(reader)
//                    .withType(PineLabsTRM.class)
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .withOrderedResults(true)
//                    .build();
//            List<PineLabsTRM> transactionList = csvToBean.parse();
//            List<TRMEntity> transactions=transactionList.stream().map(pineLabsTRM -> {
//                TRMEntity trmEntity=new TRMEntity();
//                trmEntity.setTransactionId(removeSingleQuotes.apply(pineLabsTRM.getTransactionId()));
//                trmEntity.setPosId(pineLabsTRM.getPos());
//                trmEntity.setOrderId(pineLabsTRM.getBillInvoice());
//                trmEntity.setTrmAmount(pineLabsTRM.getAmount());
//                trmEntity.setAcquirerBank(Bank.getBank(pineLabsTRM.getAcquirer()));
//                PaymentType paymentType=PaymentType.getPaymentType(pineLabsTRM.getPaymentMode());
//                trmEntity.setPaymentType(paymentType);
//                trmEntity.setRrn(removeSingleQuotes.apply(pineLabsTRM.getRrn()));
//                trmEntity.setTid(pineLabsTRM.getTid());
//                Optional<StoreTIDMapping> storeTIDMapping=storeTIDMappingDao.findByTid(trmEntity.getTid());
//                if(storeTIDMapping.isPresent()){
//                    trmEntity.setStoreId(storeTIDMapping.get().getStoreCode());
//                }
//                else{
//                    trmEntity.setStoreId(null);
//                }
//                trmEntity.setMid(removeSingleQuotes.apply(pineLabsTRM.getMid()));
//                trmEntity.setAuthCode(removeSingleQuotes.apply(pineLabsTRM.getApprovalCode()));
//                //card entries
////                if(paymentType.equals(PaymentType.CARD)){
////                    trmEntity.setCardNumber(pineLabsTRM.getCustomerPaymentModeId());
////                    trmEntity.setCardType(CardType.getCardType(pineLabsTRM.getCardType()));
////                    trmEntity.setNetworkType(pineLabsTRM.getCardNetwork());
////                }
//                trmEntity.setCardNumber(pineLabsTRM.getCustomerPaymentModeId());
//                trmEntity.setCardType(CardType.getCardType(pineLabsTRM.getCardType()));
//                trmEntity.setNetworkType(pineLabsTRM.getCardNetwork());
//                trmEntity.setTransactionDate(pineLabsTRM.getTransactionDate());
//                trmEntity.setTransactionType(TransactionType.getTransactionType(pineLabsTRM.getType()));
//                trmEntity.setTransactionStatus(TransactionStatus.getTransactionStatus(pineLabsTRM.getTxnStatus()));
//                trmEntity.setSource(TRMSource.PINE_LABS);
//                trmEntity.setZone(pineLabsTRM.getZone());
//                trmEntity.setStoreName(pineLabsTRM.getStoreName());
//                trmEntity.setCity(pineLabsTRM.getCity());
//                trmEntity.setHardwareId(pineLabsTRM.getHardwareId());
//                trmEntity.setHardwareModel(pineLabsTRM.getHardwareModel());
//                trmEntity.setBatchNo(pineLabsTRM.getBatchNo());
//                trmEntity.setCardIssuer(pineLabsTRM.getCardIssuer());
//                trmEntity.setInvoice(pineLabsTRM.getInvoice());
//                trmEntity.setApprovalCode(removeSingleQuotes.apply(pineLabsTRM.getApprovalCode()));
//                trmEntity.setCurrency(pineLabsTRM.getCurrency());
//                trmEntity.setBillInvoice(pineLabsTRM.getBillInvoice());
//                trmEntity.setEmiTxn(pineLabsTRM.getEmiTxn());
//                trmEntity.setEmiTxn(pineLabsTRM.getEmiTxn());
//                trmEntity.setEmiMonth(pineLabsTRM.getEmiMonth());
//                trmEntity.setContactless(pineLabsTRM.getContactless());
//                trmEntity.setContactlessMode(pineLabsTRM.getContactlessMode());
//                trmEntity.setCloudRefId(pineLabsTRM.getCloudRefId());
//                trmEntity.setCardPanCheck(pineLabsTRM.getCardPanCheckForSaleComplete());
//                trmEntity.setRoutePreauthToAcquirer(pineLabsTRM.getRoutePreauthToOtherAcquirer());
//                trmEntity.setBillingTransId(pineLabsTRM.getBillingTransactionId());
//                trmEntity.setName(pineLabsTRM.getName());
//                trmEntity.setTipAmount(pineLabsTRM.getTipAmount());
//                if(trmEntity.getRrn().equals(null) || trmEntity.getRrn().contains("000000000000")){
//                    trmEntity.setUid(trmEntity.getTransactionId());
//                }
//                else{
//                    trmEntity.setUid(trmEntity.getRrn());
//                }
//
//                return trmEntity;
//
//            }).collect(Collectors.toList());
//
//            List<List<TRMEntity>> chunkList=utility.chunkList(transactions,500);
//            for(List<TRMEntity> chunk:chunkList){
//                trmDao.saveAll(chunk);
//                log.info("Pinlabs trm records saved : {}",chunk.size());
//            }
//            transactions.clear();
//            transactionList.clear();
//
//            return transactions.size();
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing Pinelabs TRM: ", e);
//            throw e;
//        }
//    }
//
//    public void readBYColumnName(InputStream file) throws IOException {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.PineLabs_TRM);
//        List<TRMEntity> trmEntryList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                TRMEntity trm = new TRMEntity();
//                Row headerRow = sheet.getRow(0);
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (actualField) {
//                            case "zone" ->
//                                    trm.setZone(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "store_name" -> trm.setStoreName(cell.getStringCellValue().trim());
//                            case "city" -> trm.setCity(cell.getStringCellValue().trim());
//                            case "pos_id" ->
//                                    trm.setPosId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "hardware_model" ->
//                                    trm.setHardwareModel(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "hardware_id" ->
//                                    trm.setHardwareId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "acquirer_bank" -> {
//                                String bank = cell.getStringCellValue().trim();
//                                trm.setAcquirerBank(bank.equalsIgnoreCase("YES BANK QR") ? Bank.YES_BANK_QR : Bank.valueOf(bank));
//                            }
//                            case "tid" ->
//                                    trm.setTid(cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue()));
//                            case "mid" -> trm.setMid(removeSingleQuotes.apply(cell.getStringCellValue()));
//                            case "batch_no" ->
//                                    trm.setBatchNo(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "payment_type" -> trm.setPaymentType(PaymentType.valueOf(cell.getStringCellValue()));
//                            case "customervpa" -> trm.setCustomerVPA(cell == null ? null : cell.getStringCellValue());
//                            case "name" -> trm.setName(cell == null ? null : cell.getStringCellValue());
//                            case "card_issuer" -> trm.setCardIssuer(cell == null ? null : cell.getStringCellValue());
//                            case "card_type" -> trm.setCardType(cell == null ? null : CardType.getCardType(cell.getStringCellValue()));
//                            case "network_type" ->
//                                    trm.setNetworkType(cell == null ? null : cell.getStringCellValue().trim());
//                            case "card_color" -> trm.setCardColor(cell == null ? null : cell.getStringCellValue());
//                            case "transaction_id" ->
//                                    trm.setTransactionId(removeSingleQuotes.apply(cell.getStringCellValue()));
//                            case "invoice" ->
//                                    trm.setInvoice(cell == null ? null : (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue())));
//                            case "approval_code" ->
//                                    trm.setApprovalCode(cell == null ? null : cell.getStringCellValue());
//                            case "transaction_type" ->
//                                    trm.setTransactionType(TransactionType.valueOf(cell.getStringCellValue()));
//                            case "trm_amount" -> trm.setTrmAmount(cell.getNumericCellValue());
//                            case "tip_amount" -> trm.setTipAmount(cell.getNumericCellValue());
//                            case "currency" -> trm.setCurrency(cell.getStringCellValue());
//                            case "transaction_date" ->
//                                    trm.setTransactionDate(LocalDateTime.parse(cell.getStringCellValue(), df));
//                            case "status" -> trm.setStatus(cell.getStringCellValue());
//                            case "transaction_status" -> {
//                                String status = cell.getStringCellValue().toUpperCase();
//                                trm.setTransactionStatus(status.equalsIgnoreCase("SESSION EXPIRED") ? TransactionStatus.SESSION_EXPIRED : TransactionStatus.valueOf(status));
//                            }
//                            case "settlement_date" ->
//                                    trm.setSettlementDate(cell == null ? null : (cell.getStringCellValue().isEmpty() ? null : LocalDateTime.parse(cell.getStringCellValue(), df)));
//                            case "bill_invoice" -> trm.setBillInvoice(cell == null ? null : cell.getStringCellValue());
//                            case "rrn" ->
//                                    trm.setRrn(cell == null ? null : removeSingleQuotes.apply(cell.getStringCellValue()));
//                            case "emi_txn" -> trm.setEmiTxn(cell.getStringCellValue());
//                            case "emi_month" ->
//                                    trm.setEmiMonth(cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue()));
//                            case "contactless" -> trm.setContactless(cell.getStringCellValue());
//                            case "contactless_mode" -> trm.setContactlessMode(cell.getStringCellValue());
//                            case "cloud_ref_id" ->
//                                    trm.setCloudRefId(cell == null ? null : (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue())));
//                            case "card_pan_check" -> trm.setCardPanCheck(cell.getStringCellValue());
//                            case "route_preauth_to_acquirer" ->
//                                    trm.setRoutePreauthToAcquirer(cell.getStringCellValue());
//                            case "billing_trans_id" ->
//                                    trm.setBillingTransId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue()));
//                        }
//                    }
//                }
//                trm.setSource(TRMSource.PINE_LABS);
//                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(trm.getTid());
//                if (storeTIDMapping.isPresent()) {
//                    trm.setStoreId(storeTIDMapping.get().getStoreCode());
//                } else {
//                    trm.setStoreId(null);
//                }
//                if (trm.getAcquirerBank().equals(Bank.HDFC) || trm.getAcquirerBank().equals(Bank.ICICI)) {
//                    trm.setUid(trm.getTransactionId());
//                }
//                trmEntryList.add(trm);
//                if (trmEntryList.size() >= 500) {
//
//                    trmDao.saveAll(trmEntryList);
//                    log.info("TRM Data imported {}", trmEntryList.size());
//                    trmEntryList.clear();
//                }
//            }
//
//            trmDao.saveAll(trmEntryList);
//            log.info("{} zomato records saved", trmEntryList.size());
//
//
//        } catch (Exception e) {
//            log.error("Exception occurred while reading trm file: ", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (file != null) {
//                try {
//                    file.close();
//                } catch (IOException e) {
//                    log.error("Exception occurred while closing the fileinputstream: ", e);
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }
//
//    public void readCSVByColumnName(InputStream file) throws IOException {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.PineLabs_TRM);
//        List<TRMEntity> trmEntryList = new ArrayList<>();
//        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file)).build()) {
//            List<String[]> records = reader.readAll();
//            String[] header = records.get(0);
//
//            for (int i = 1; i < records.size(); i++) {
//                String[] row = records.get(i);
//                TRMEntity trm = new TRMEntity();
//
//                for (int j = 0; j < row.length; j++) {
//                    String cell = row[j];
//                    String headerCellValue = header[j].trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (actualField) {
//                            case "zone" -> trm.setZone(cell.trim());
//                            case "store_name" -> trm.setStoreName(cell.trim());
//                            case "city" -> trm.setCity(cell.trim());
//                            case "pos_id" -> trm.setPosId(cell.trim());
//                            case "hardware_model" -> trm.setHardwareModel(cell.trim());
//                            case "hardware_id" -> trm.setHardwareId(cell.trim());
//                            case "acquirer_bank" -> trm.setAcquirerBank(Bank.valueOf(cell.trim()));
//                            case "tid" -> trm.setTid(cell.trim());
//                            case "mid" -> trm.setMid(cell.trim());
//                            case "batch_no" -> trm.setBatchNo(cell.trim());
//                            case "payment_type" -> trm.setPaymentType(PaymentType.valueOf(cell.trim()));
//                            case "customervpa" -> trm.setCustomerVPA(cell.trim());
//                            case "name" -> trm.setName(cell.trim());
//                            case "card_issuer" -> trm.setCardIssuer(cell.trim());
//                            case "card_type" -> trm.setCardType(CardType.getCardType(cell.trim()));
//                            case "network_type" -> trm.setNetworkType(cell.trim());
//                            case "card_color" -> trm.setCardColor(cell.trim());
//                            case "transaction_id" -> trm.setTransactionId(cell.trim());
//                            case "invoice" -> trm.setInvoice(cell.trim());
//                            case "approval_code" -> trm.setApprovalCode(cell.trim());
//                            case "transaction_type" -> trm.setTransactionType(TransactionType.valueOf(cell.trim()));
//                            case "trm_amount" -> trm.setTrmAmount(Double.parseDouble(cell.trim()));
//                            case "tip_amount" -> trm.setTipAmount(Double.parseDouble(cell.trim()));
//                            case "currency" -> trm.setCurrency(cell.trim());
//                            case "transaction_date" -> trm.setTransactionDate(LocalDateTime.parse(cell.trim()));
//                            case "status" -> trm.setStatus(cell.trim());
//                            case "transaction_status" ->
//                                    trm.setTransactionStatus(TransactionStatus.valueOf(cell.trim()));
//                            case "settlement_date" -> trm.setSettlementDate(LocalDateTime.parse(cell.trim()));
//                            case "bill_invoice" -> trm.setBillInvoice(cell.trim());
//                            case "rrn" -> trm.setRrn(cell.trim());
//                            case "emi_txn" -> trm.setEmiTxn(cell.trim());
//                            case "emi_month" -> trm.setEmiMonth(cell.trim());
//                            case "contactless" -> trm.setContactless(cell.trim());
//                            case "contactless_mode" -> trm.setContactlessMode(cell.trim());
//                            case "cloud_ref_id" -> trm.setCloudRefId(cell.trim());
//                            case "card_pan_check" -> trm.setCardPanCheck(cell.trim());
//                            case "route_preauth_to_acquirer" -> trm.setRoutePreauthToAcquirer(cell.trim());
//                            case "billing_trans_id" -> trm.setBillingTransId(cell.trim());
//                        }
//                    }
//                }
//                trm.setSource(TRMSource.PINE_LABS);
//                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(trm.getTid());
//                if (storeTIDMapping.isPresent()) {
//                    trm.setStoreId(storeTIDMapping.get().getStoreCode());
//                } else {
//                    trm.setStoreId(null);
//                }
//                if (trm.getAcquirerBank().equals(Bank.HDFC) || trm.getAcquirerBank().equals(Bank.ICICI)) {
//                    trm.setUid(trm.getTransactionId());
//                }
//                trmEntryList.add(trm);
//                if (trmEntryList.size() >= 500) {
//                    trmDao.saveAll(trmEntryList);
//                    log.info("TRM Data imported {}", trmEntryList.size());
//                    trmEntryList.clear();
//                }
//            }
//
//            trmDao.saveAll(trmEntryList);
//            log.info("{} zomato records saved", trmEntryList.size());
//
//        } catch (Exception e) {
//            log.error("Exception occurred while reading trm file: ", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (file != null) {
//                try {
//                    file.close();
//                } catch (IOException e) {
//                    log.error("Exception occurred while closing the file input stream: ", e);
//                    throw new RuntimeException(e);
//                }
//            }
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