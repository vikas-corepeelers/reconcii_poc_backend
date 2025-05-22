//package com.cpl.reconciliation.tasks.service.mpr.processorImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.common.utility.ZIPUtils;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.CardCategory;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.core.util.CPLFileUtils;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
//import com.cpl.reconciliation.tasks.service.mpr.CardChargesUtilImpl;
//import com.cpl.reconciliation.tasks.service.mpr.MessageProcessor;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import com.github.pjfanning.xlsx.StreamingReader;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//
//import javax.mail.*;
//import javax.mail.internet.MimeBodyPart;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.DecimalFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.function.Function;
//
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//import static com.cpl.reconciliation.tasks.Constants.*;
//
//@Data
//@Slf4j
//@Service
//public class SbiMPRProcessorImpl implements MessageProcessor {
//    private final static Function<String, String> removeSingleQuotes = (a) -> a.replaceAll("^'+|'+$", "");
//    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
//    private final static DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private static final SimpleDateFormat settlementDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
//    private final MPRDao mprDao;
//    private final ZIPUtils zipUtils;
//    private final StoreTIDMappingDao storeTIDMappingDao;
//    private final BankSettlementDateUtil settlementDateUtil;
//    private final CardChargesUtilImpl cardChargesUtil;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//
//    private static boolean isExcelFile(File file) {
//        String fileName = file.getName().toLowerCase();
//        return fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
//    }
//
//    @Override
//    public void processMessages(Message[] messages) throws MessagingException, IOException {
//        Path downloadPath = Paths.get(SBI_FILE_ATTACHMENT_DOWNLOAD_PATH);
//        if (!Files.exists(downloadPath)) {
//            Files.createDirectories(downloadPath);
//            log.info("SBI MPR Directory created: " + downloadPath);
//        } else {
//            log.info("SBI MPR Directory already exists: " + downloadPath);
//        }
//        Path outputPath = Paths.get(SBI_OUTPUT_PATH);
//        if (!Files.exists(outputPath)) {
//            Files.createDirectories(outputPath);
//            log.info("SBI MPR Output Directory created: " + outputPath);
//        } else {
//            log.info("SBI MPR Output Directory already exists: " + outputPath);
//        }
//        boolean fileExtracted = false;
//        for (Message message : messages) {
//            Multipart multipart = (Multipart) message.getContent();
//            for (int i = 0; i < multipart.getCount(); i++) {
//                BodyPart bodyPart = multipart.getBodyPart(i);
//                if (bodyPart.getFileName() != null &&
//                        bodyPart.getFileName().matches("^Detail_.*zip$")) {
//                    MimeBodyPart part = (MimeBodyPart) bodyPart;
//                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
//                        String path = SBI_FILE_ATTACHMENT_DOWNLOAD_PATH + part.getFileName();
//                        part.saveFile(path);
//                        fileExtracted = zipUtils.extractZipFileToPath(path, SBI_OUTPUT_PATH);
//                        Files.delete(Path.of(path));
//                    }
//                }
//            }
//            Flags processedFlag = new Flags(SBI_PROCESSED_FLAG);
//            message.setFlags(processedFlag, true);
//        }
//        if (fileExtracted) processXLSFiles();
//    }
//
//    private void processXLSFiles() {
//        Path outputPath = Paths.get(SBI_OUTPUT_PATH);
//        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(outputPath)) {
//            for (Path path : directoryStream) {
//                if (isExcelFile(path.toFile())) {
//                    saveXLSFiles(path.toFile());
//                }
//            }
//        } catch (Exception e) {
//            log.error("Exception in listing output: ", e);
//        } finally {
//            CPLFileUtils.deleteDirectory(outputPath);
//        }
//    }
//
//    public void saveXLSFiles(File xlsFilePath) {
//        List<MPREntity> sbiMPRList = new ArrayList<>();
//        try (FileInputStream file = new FileInputStream(xlsFilePath)) {
//            try (Workbook workbook = new XSSFWorkbook(file)) {
//                Sheet sheet = workbook.getSheetAt(0);
//                boolean foundStartIdentifier = false;
//                boolean skippedHeader = false;
//                String terminalId = null;
//                for (Row row : sheet) {
//                    if (row != null && row.getCell(1) != null && row.getCell(1).getStringCellValue().equals("Transaction Detail")) {
//                        foundStartIdentifier = true;
//                    } else if (row != null && row.getCell(1) != null && row.getCell(1).getStringCellValue().equals("Batch Total")) {
//                        break;
//                    } else if (row != null && row.getCell(0) != null && row.getCell(0).getStringCellValue().equals("Terminal Id")) {
//                        terminalId = row.getCell(1).getStringCellValue();
//                    } else if (row != null && foundStartIdentifier) {
//                        if (!skippedHeader) {
//                            skippedHeader = true;
//                            continue;
//                        }
//                        MPREntity transaction = new MPREntity();
//                        transaction.setTransactionDate(parseDate(row.getCell(1).toString()));
//                        transaction.setMid(row.getCell(2).getStringCellValue());
//                        transaction.setCardNumber(row.getCell(3).getStringCellValue());
//                        transaction.setAuthCode(row.getCell(4).getStringCellValue());
//                        transaction.setRrn(row.getCell(5).getStringCellValue());
//                        transaction.setMprAmount(MessageProcessor.parseDouble(row.getCell(7).getStringCellValue()));
//                        transaction.setCommission(MessageProcessor.parseDouble(row.getCell(8).getStringCellValue()));
//                        transaction.setSettledAmount(MessageProcessor.parseDouble(row.getCell(10).getStringCellValue()));
//                        transaction.setSettledDate(parseSettlementDate(row.getCell(11).toString()));
//                        transaction.setCardType(row.getCell(12).getStringCellValue());
//                        transaction.setCardType(row.getCell(13).getStringCellValue());
//                        transaction.setTid(terminalId);
//                        transaction.setBank(Bank.SBI);
//                        transaction.setPaymentType(PaymentType.CARD);
//                        sbiMPRList.add(transaction);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing SBI MPR: ", e);
//        }
//        mprDao.saveAll(sbiMPRList);
//        log.info("SBI MPR imported {}", sbiMPRList.size());
//    }
//
//    public void parseExcelFile(InputStream file) {
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<MPREntity> sbiMPRList = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean firstRow = true;
//            for (Row row : sheet) {
//                if (!firstRow) {
//                    if (row.getCell(0).getStringCellValue().contains("SBI Service Tax Number")) break;
//                    MPREntity transaction = new MPREntity();
//                    transaction.setMid(row.getCell(0).getStringCellValue());
//                    transaction.setTid(getTID(row.getCell(1)));
//                    Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(transaction.getTid());
//                    if (storeTIDMapping.isPresent()) {
//                        transaction.setStoreId(storeTIDMapping.get().getStoreCode());
//                    } else {
//                        transaction.setStoreId(null);
//                    }
//                    transaction.setCardNumber(row.getCell(3).getStringCellValue());
//                    transaction.setTransactionDate(row.getCell(4).getLocalDateTimeCellValue());
//                    transaction.setSettledDate(row.getCell(5).getLocalDateTimeCellValue());
//                    transaction.setRrn(removeSingleQuotes.apply(row.getCell(7).getStringCellValue()));
//                    transaction.setAuthCode(removeSingleQuotes.apply(row.getCell(8).getStringCellValue()));
//                    String cardRegion = removeSingleQuotes.apply(row.getCell(9).getStringCellValue().trim());
//                    String cardCategory = removeSingleQuotes.apply(row.getCell(30).getStringCellValue().trim());
//                    transaction.setCardCategory(CardCategory.getSBICardCategory(cardRegion, cardCategory));
//                    transaction.setMprAmount(row.getCell(10).getNumericCellValue());
//                    transaction.setCommission(row.getCell(12).getNumericCellValue());
//                    transaction.setServiceTax(row.getCell(13).getNumericCellValue());
//                    transaction.setSettledAmount(row.getCell(14).getNumericCellValue());
//                    transaction.setCustomField1(removeSingleQuotes.apply(row.getCell(15).getStringCellValue().trim()));
//                    transaction.setSbCess(row.getCell(23).getNumericCellValue());
//                    transaction.setKkCess(row.getCell(24).getNumericCellValue());
//                    transaction.setGst(row.getCell(32).getNumericCellValue());
//                    transaction.setCardType(row.getCell(35).getStringCellValue().trim());
//                    transaction.setBank(Bank.SBI);
//                    transaction.setPaymentType(PaymentType.CARD);
//                    transaction.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.SBI, transaction.getSettledDate()));
//                    transaction.setUid(getComposite(transaction.getRrn(), transaction.getAuthCode(), transaction.getCardNumber()));
//                    transaction.setId(transaction.getUid());
//                    LocalDateTime currentDate = transaction.getSettledDate();
//                    if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                        minSettledDate = currentDate;
//                    }
//                    if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                        maxSettledDate = currentDate;
//                    }
//                    cardChargesUtil.setAndUpdateMDR(transaction);
//                    sbiMPRList.add(transaction);
//                    if (sbiMPRList.size() >= 500) {
//                        mprDao.saveAll(sbiMPRList);
//                        log.info("SBI MPR imported {}", sbiMPRList.size());
//                        sbiMPRList.clear();
//                    }
//                } else {
//                    firstRow = false;
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
//    public void parseExcelFileByName(InputStream file) {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.SBI_MPR);
//
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
//    private LocalDateTime parseDate(String value) throws ParseException {
//        if (value != null && !value.isEmpty()) {
//            return dateFormat.parse(value).toInstant()
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDateTime();
//        }
//        return null;
//    }
//
//    private LocalDateTime parseSettlementDate(String value) throws ParseException {
//        if (value != null && !value.isEmpty()) {
//            return settlementDateFormat.parse(value).toInstant()
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDateTime();
//        }
//        return null;
//    }
//}
