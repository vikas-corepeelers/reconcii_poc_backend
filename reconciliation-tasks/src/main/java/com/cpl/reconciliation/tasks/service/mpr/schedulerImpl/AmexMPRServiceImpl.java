//package com.cpl.reconciliation.tasks.service.mpr.schedulerImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.CardCategory;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.AmexStoreMappingDao;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.models.AmexMPR;
//import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.service.mpr.CardChargesUtilImpl;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.function.Function;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.extentionFilter;
//import static com.cpl.reconciliation.core.util.CPLFileUtils.isDirectoryWithContent;
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//
//@Data
//@Slf4j
//@Service
//public class AmexMPRServiceImpl extends AbstractService implements DataService {
//    private final static Function<String, String> removeSingleQuotes = (a) -> a.replaceAll("^'+|'+$", "");
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
//    private final MPRDao mprDao;
//    private final ModelMapper modelMapper;
//    private final AmexStoreMappingDao amexStoreMappingDao;
//    private final BankSettlementDateUtil settlementDateUtil;
//    private final CardChargesUtilImpl cardChargesUtil;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//
//    @PostConstruct
//    public void postConstruct() {
//        modelMapper.typeMap(AmexMPR.class, MPREntity.class).addMapping(AmexMPR::getSubmissionDate, MPREntity::setTransactionDate).addMapping(AmexMPR::getSubmissionAmount, MPREntity::setMprAmount).addMapping(AmexMPR::getMerchantServiceFee, MPREntity::setCommission).addMapping(AmexMPR::getSettlementAmount, MPREntity::setSettledAmount).addMapping(AmexMPR::getSubmittingMerchantNumber, MPREntity::setMid).addMapping(AmexMPR::getSubmittingLocationName, MPREntity::setTid).addMapping(AmexMPR::getSettelmentDate, MPREntity::setSettledDate).addMapping(AmexMPR::getTaxAmount, MPREntity::setServiceTax).addMapping(AmexMPR::getIgst, MPREntity::setIgst).addMapping(AmexMPR::getCgst, MPREntity::setCgst).addMapping(AmexMPR::getSgst, MPREntity::setSgst);
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.AMEX_MPR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        try {
//            List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//            if (dataEntryLogList.isEmpty()) {
//                String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//                String path = sftpRoot + fileSeparator + "amex" + fileSeparator + "mpr" + fileSeparator + businessDate;
//                File folder = new File(path);
//                if (isDirectoryWithContent(folder)) {
//                    File[] files = folder.listFiles(extentionFilter("xlsx"));
//                    log.info("Amex MPR folder {} file count: {}", path, files.length);
//                    for (File file : files) {
////                        processFile(new FileInputStream(file));
//                        parseExcelFileByName(new FileInputStream(file));
//                    }
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//                } else {
//                    log.error("Amex MPR folder does not exists: {}", path);
//                }
//            } else {
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
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
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
//                processFile(inputStream);
////                parseExcelFileByName(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            mprDao.getMprBankDifference(Bank.AMEX.name(), PaymentType.CARD.name(), businessDate.format(Formatter.YYYYMMDD_DASH), endDate.format(Formatter.YYYYMMDD_DASH));
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//
//    public void processFile(InputStream file) {
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<MPREntity> amexEntities = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean dataRow = false;
//            int count = 1;
//            for (Row row : sheet) {
//                if (dataRow) {
//                    AmexMPR amexEntity = new AmexMPR();
//                    amexEntity.setSubmissionDate(parseDate(row.getCell(0).getStringCellValue()));
//                    amexEntity.setSocInvoice(row.getCell(1).getCellType() == CellType.STRING ? row.getCell(1).getStringCellValue() : String.valueOf(row.getCell(1).getNumericCellValue()));
//                    amexEntity.setSettelmentNumber(row.getCell(2).getCellType() == CellType.STRING ? row.getCell(2).getStringCellValue() : String.valueOf(row.getCell(2).getNumericCellValue()));
//                    amexEntity.setTotalCharges(row.getCell(3).getCellType() == CellType.NUMERIC ? row.getCell(3).getNumericCellValue() : Double.parseDouble(row.getCell(3).getStringCellValue()));
//                    amexEntity.setCredits(row.getCell(4).getNumericCellValue());
//                    amexEntity.setSubmissionAmount(row.getCell(5).getNumericCellValue());
//                    amexEntity.setMerchantServiceFee(Math.abs(row.getCell(6).getNumericCellValue()));
//                    amexEntity.setFeesAndIncetives(row.getCell(7).getNumericCellValue());
//                    amexEntity.setSettlementAmount(row.getCell(8).getNumericCellValue());
//                    amexEntity.setPayeeMerchantNumber(row.getCell(9).getCellType() == CellType.STRING ? row.getCell(9).getStringCellValue() : String.valueOf((long)row.getCell(9).getNumericCellValue()));
//                    amexEntity.setSubmittingMerchantNumber(row.getCell(10).getCellType() == CellType.STRING ? row.getCell(10).getStringCellValue() : String.valueOf((long)row.getCell(10).getNumericCellValue()));
//                    amexEntity.setSubmittingLocationId(row.getCell(11) == null ? null : row.getCell(11).getStringCellValue());
//                    amexEntity.setTransactionCount(row.getCell(12).getNumericCellValue());
//                    amexEntity.setSettelmentDate(parseDate(row.getCell(13).getStringCellValue()));
//                    amexEntity.setSubmittingLocationName(row.getCell(14).getStringCellValue());
//                    amexEntity.setTaxAmount(Math.abs(row.getCell(15).getNumericCellValue()));
//                    amexEntity.setIgst(Math.abs(row.getCell(16).getNumericCellValue()));
//                    amexEntity.setCgst(row.getCell(17).getNumericCellValue());
//                    amexEntity.setSgst(row.getCell(18).getNumericCellValue());
//                    amexEntity.setAmountOfAdjustmentSummary(row.getCell(19).getNumericCellValue());
//                    amexEntity.setDescription(row.getCell(20).getStringCellValue());
//                    amexEntity.setProcessedDate(parseDate(row.getCell(21).getStringCellValue()));
//                    MPREntity mpr = modelMapper.map(amexEntity, MPREntity.class);
//                    String id = amexEntity.getSocInvoice() + "|" + amexEntity.getSettelmentNumber() + "|" + amexEntity.getSubmissionAmount() + "|" + amexEntity.getSubmittingMerchantNumber() + "|" + amexEntity.getSettelmentDate();
//                    mpr.setId(id);
//                    mpr.setBank(Bank.AMEX);
//                    mpr.setPaymentType(PaymentType.CARD);
//                    mpr.setBankCharges(mpr.getCommission()+mpr.getServiceTax());
//                    String storeCode = storeTIDMappingDao.getStoreCodeByMid(mpr.getMid());
//                    if (storeCode != null) {
//                        mpr.setStoreId(storeCode);
//                    }
//                    mpr.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.AMEX, mpr.getSettledDate()));
//                    LocalDateTime currentDate = mpr.getSettledDate();
//                    if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                        minSettledDate = currentDate;
//                    }
//                    if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                        maxSettledDate = currentDate;
//                    }
//                    cardChargesUtil.setAndUpdateMDR(mpr);
//                    amexEntities.add(mpr);
//                    if (amexEntities.size() >= 500) {
//                        mprDao.saveAll(amexEntities);
//                        log.info("AMEX MPR imported {}", amexEntities.size());
//                        amexEntities.clear();
//                    }
//                } else {
//                    count++;
//                    if (count == 8) {
//                        dataRow = true;
//                    }
//                }
//            }
//            mprDao.saveAll(amexEntities);
//            mprDao.getMprBankDifference(Bank.AMEX.name(), PaymentType.CARD.name(), minSettledDate.format(Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            log.info("AMEX MPR imported {}", amexEntities.size());
//        } catch (Exception e) {
//            log.error("Exception occurred while reading AMEX MPR file: ", e);
//            throw new RuntimeException(e);
//        }
//    }
//
//
//    //File parse By Name
//    public void parseExcelFileByName(InputStream file) {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.AMEX_MPR);
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<MPREntity> sbiMPRList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                MPREntity transaction = new MPREntity();
//                Row headerRow = sheet.getRow(0);
//                String Mid = null;
//                String Tid = null;
//                String cardRegion = null;
//                String cardCategory = null;
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (actualField) {
//                            case "mid" -> {
//                                Mid = removeSingleQuotes.apply(cell.getStringCellValue().trim());
//                                if (Mid.contains("SBI Service Tax Number")) break;
//                                transaction.setMid(Mid);
//                            }
//                            case "tid" -> {
//                                Tid = getTID(cell);
//                                transaction.setTid(Tid);
//                            }
//                            case "card_number" -> transaction.setCardNumber(cell.getStringCellValue().trim());
//                            case "transaction_date" -> transaction.setTransactionDate(cell.getLocalDateTimeCellValue());
//                            case "settled_date" -> transaction.setSettledDate(cell.getLocalDateTimeCellValue());
//                            case "rrn" ->
//                                    transaction.setRrn(removeSingleQuotes.apply(cell.getStringCellValue().trim()));
//                            case "auth_code" ->
//                                    transaction.setAuthCode(removeSingleQuotes.apply(cell.getStringCellValue().trim()));
//                            case "mpr_amount" -> transaction.setMprAmount(cell.getNumericCellValue());
//                            case "commisson" -> transaction.setCommission(cell.getNumericCellValue());
//                            case "service_tax" -> transaction.setServiceTax(cell.getNumericCellValue());
//                            case "settled_amount" -> transaction.setSettledAmount(cell.getNumericCellValue());
//                            case "custom_field1" ->
//                                    transaction.setCustomField1(removeSingleQuotes.apply(cell.getStringCellValue().trim()));
//                            case "sb_cess" -> transaction.setSbCess(cell.getNumericCellValue());
//                            case "kk_cess" -> transaction.setKkCess(cell.getNumericCellValue());
//                            case "gst" -> transaction.setGst(cell.getNumericCellValue());
//                            case "card_type" -> transaction.setCardType(cell.getStringCellValue().trim());
//                        }
//                    }
//                    if (headerCellValue.equalsIgnoreCase("Card Region")) {
//                        cardRegion = cell.getStringCellValue().trim();
//                    }
//                    if (headerCellValue.equalsIgnoreCase("Card Category")) {
//                        cardCategory = cell.getStringCellValue().trim();
//                    }
//                }
//                transaction.setCardCategory(CardCategory.getSBICardCategory(cardRegion, cardCategory));
//                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(transaction.getTid());
//                if (storeTIDMapping.isPresent()) {
//                    transaction.setStoreId(storeTIDMapping.get().getStoreCode());
//                } else {
//                    transaction.setStoreId(null);
//                }
//                transaction.setBank(Bank.SBI);
//                transaction.setPaymentType(PaymentType.CARD);
//                transaction.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.SBI, transaction.getSettledDate()));
//                transaction.setUid(getComposite(transaction.getRrn(), transaction.getAuthCode(), transaction.getCardNumber()));
//                transaction.setId(transaction.getUid());
//                LocalDateTime currentDate = transaction.getSettledDate();
//                if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                    minSettledDate = currentDate;
//                }
//                if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                    maxSettledDate = currentDate;
//                }
//                cardChargesUtil.setAndUpdateMDR(transaction);
//                sbiMPRList.add(transaction);
//                if (sbiMPRList.size() >= 500) {
//                    mprDao.saveAll(sbiMPRList);
//                    log.info("SBI MPR imported {}", sbiMPRList.size());
//                    sbiMPRList.clear();
//                }
//            }
//            mprDao.saveAll(sbiMPRList);
//            mprDao.getMprBankDifference(Bank.SBI.name(), PaymentType.CARD.name(), minSettledDate.format(Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            log.info("SBI MPR imported {}", sbiMPRList.size());
//        } catch (Exception e) {
//            log.error("Exception occurred while reading SBI MPR file: ", e);
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
//
//    }
//
//    private String getTID(Cell cell) {
//        if (cell.getCellType().equals(CellType.NUMERIC)) {
//            DecimalFormat df = new DecimalFormat("0");
//            return df.format(cell.getNumericCellValue()).replace("'", "");
//        } else {
//            return cell.getStringCellValue().replace("'", "");
//        }
//    }
//
//
//    private LocalDateTime parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing AMEX date field: ", e);
//        }
//        return null;
//    }
//}
