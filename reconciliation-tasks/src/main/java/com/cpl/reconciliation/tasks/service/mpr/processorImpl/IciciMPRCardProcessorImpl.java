//package com.cpl.reconciliation.tasks.service.mpr.processorImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.util.StringUtils;
//import com.cpl.core.common.utility.ZIPUtils;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.core.util.CPLFileUtils;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.tasks.service.mpr.MessageProcessor;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.springframework.stereotype.Service;
//
//import javax.mail.*;
//import javax.mail.internet.MimeBodyPart;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.DirectoryStream;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//import static com.cpl.reconciliation.tasks.Constants.*;
//
//@Data
//@Slf4j
//@Service
//public class IciciMPRCardProcessorImpl implements MessageProcessor {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy");
//    private final static DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//    private final BankSettlementDateUtil settlementDateUtil;
//    private final StoreTIDMappingDao storeTIDMappingDao;
//
//
//    private final MPRDao mprDao;
//    private final ZIPUtils zipUtils;
//
//    private static boolean isUPIMISExcelFile(String fileName) {
//        return fileName.matches(".*MIS_REPORT.*\\.xlsx");
//    }
//
//    @Override
//    public void processMessages(Message[] messages) throws MessagingException, IOException {
//        Path downloadPath = Paths.get(ICICI_FILE_ATTACHMENT_DOWNLOAD_PATH);
//        if (!Files.exists(downloadPath)) {
//            Files.createDirectories(downloadPath);
//            log.info("ICICI MPR Directory created: " + downloadPath);
//        } else {
//            log.info("ICICI MPR Directory already exists: " + downloadPath);
//        }
//        Path outputPath = Paths.get(ICICI_OUTPUT_PATH);
//        if (!Files.exists(outputPath)) {
//            Files.createDirectories(outputPath);
//            log.info("ICICI MPR Output Directory created: " + outputPath);
//        } else {
//            log.info("ICICI MPR Output Directory already exists: " + outputPath);
//        }
//        boolean fileExtracted = false;
//        boolean fileDownloaded = false;
//        for (Message message : messages) {
//            Multipart multipart = (Multipart) message.getContent();
//            for (int i = 0; i < multipart.getCount(); i++) {
//                BodyPart bodyPart = multipart.getBodyPart(i);
//                if ((bodyPart.getFileName() != null && bodyPart.getFileName().endsWith("zip"))) {
//                    MimeBodyPart part = (MimeBodyPart) bodyPart;
//                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
//                        String path = ICICI_FILE_ATTACHMENT_DOWNLOAD_PATH + part.getFileName();
//                        part.saveFile(path);
//                        fileExtracted = zipUtils.extractZipFileToPath(path, ICICI_OUTPUT_PATH);
//                        Files.delete(Path.of(path));
//                    }
//                } else if ((bodyPart.getFileName() != null && bodyPart.getFileName().matches(".*MIS_REPORT.*\\.xlsx"))) {
//                    MimeBodyPart part = (MimeBodyPart) bodyPart;
//                    String path = ICICI_OUTPUT_PATH + "/" + part.getFileName();
//                    part.saveFile(path);
//                    fileDownloaded = true;
//                }
//            }
//            Flags processedFlag = new Flags(ICICI_PROCESSED_FLAG);
//            message.setFlags(processedFlag, true);
//        }
//        if (fileExtracted || fileDownloaded) processXLSFiles();
//    }
//
//    private void processXLSFiles() {
//        Path outputPath = Paths.get(ICICI_OUTPUT_PATH);
//        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(outputPath)) {
//            for (Path path : directoryStream) {
//                if (isCardMISExcelFile(path.toFile().getName())) {
//                    InputStream inputStream = new FileInputStream(path.toFile());
//                    saveCardFile(inputStream);
//                    inputStream.close();
//                }
//            }
//        } catch (Exception e) {
//            log.error("Exception in listing output: ", e);
//        } finally {
//            CPLFileUtils.deleteDirectory(outputPath);
//        }
//    }
//
//    public void saveCardFile(InputStream inputStream) {
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<MPREntity> iciciMISList = new ArrayList<>();
//        try (inputStream) {
//            try (Workbook workbook = new HSSFWorkbook(inputStream)) {
//                Sheet sheet = workbook.getSheetAt(0);
//                boolean firstRow = true;
//                for (Row row : sheet) {
//                    try {
//                        if (firstRow) firstRow = false;
//                        else {
//                            MPREntity transactionData = new MPREntity();
//                            if (row.getCell(2) != null && row.getCell(2).getStringCellValue().trim().equalsIgnoreCase("BAT")) {
//                                transactionData.setMid(row.getCell(0) != null ? row.getCell(0).getStringCellValue() : null);
//                                transactionData.setTid(row.getCell(1) != null ? row.getCell(1).getStringCellValue() : null);
//                                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(transactionData.getTid());
//                                storeTIDMapping.ifPresent(tidMapping -> transactionData.setStoreId(tidMapping.getStoreCode()));
//                                transactionData.setCardType(row.getCell(4) != null ? row.getCell(4).getStringCellValue() : null);
//                                transactionData.setCardNumber(row.getCell(5) != null ? row.getCell(5).getStringCellValue() : null);
//                                transactionData.setTransactionDate(parseDate2(row.getCell(6) != null ? row.getCell(6).getStringCellValue() : null));
//                                transactionData.setSettledDate(parseDate2(row.getCell(7) != null ? row.getCell(7).getStringCellValue() : null));
//                                transactionData.setAuthCode(row.getCell(8) != null ? row.getCell(8).getStringCellValue() : null);
//                                transactionData.setMprAmount(row.getCell(9) != null ? parseDouble(row.getCell(9).getStringCellValue()) : 0.0);
//                                if (transactionData.getMprAmount() <= 0) {
//                                    transactionData.setMprAmount(row.getCell(10) != null ? parseDouble(row.getCell(10).getStringCellValue()) : 0.0);
//                                }
//                                transactionData.setTransactionId(row.getCell(11) != null ? row.getCell(11).getStringCellValue() : null);
//                                transactionData.setCommission(row.getCell(14) != null ? parseDouble(row.getCell(14).getStringCellValue()) : 0.0);
//                                transactionData.setServiceTax(row.getCell(15) != null ? parseDouble(row.getCell(15).getStringCellValue()) : 0.0);
//                                transactionData.setSbCess(row.getCell(16) != null ? parseDouble(row.getCell(16).getStringCellValue()) : 0.0);
//                                transactionData.setKkCess(row.getCell(17) != null ? parseDouble(row.getCell(17).getStringCellValue()) : 0.0);
//                                transactionData.setCgst(row.getCell(18) != null ? parseDouble(row.getCell(18).getStringCellValue()) : 0.0);
//                                transactionData.setSgst(row.getCell(19) != null ? parseDouble(row.getCell(19).getStringCellValue()) : 0.0);
//                                transactionData.setIgst(row.getCell(20) != null ? parseDouble(row.getCell(20).getStringCellValue()) : 0.0);
//                                transactionData.setUtgst(row.getCell(21) != null ? parseDouble(row.getCell(21).getStringCellValue()) : 0.0);
//                                transactionData.setSettledAmount(row.getCell(22) != null ? row.getCell(22).getNumericCellValue() : 0.0);
//                                transactionData.setArn(row.getCell(24) != null ? row.getCell(24).getStringCellValue() : null);
//                                if (row.getCell(27) != null && !StringUtils.isEmpty(row.getCell(27).getStringCellValue())) {
//                                    transactionData.setRrn(row.getCell(27).getStringCellValue());
//                                } else {
//                                    transactionData.setRrn(transactionData.getTransactionId());
//                                }
//                                transactionData.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.ICICI,transactionData.getSettledDate()));
//                                transactionData.setGstnTransactionId(row.getCell(26) != null ? row.getCell(26).getStringCellValue() : null);
//                                transactionData.setPaymentType(PaymentType.CARD);
//                                transactionData.setBank(Bank.ICICI);
//                                transactionData.setUid(getComposite(transactionData.getRrn(), transactionData.getAuthCode(), transactionData.getCardNumber()));
//                                transactionData.setId(transactionData.getUid());
//                                LocalDateTime currentDate = transactionData.getSettledDate();
//                                if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                                    minSettledDate = currentDate;
//                                }
//                                if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                                    maxSettledDate = currentDate;
//                                }
//                                iciciMISList.add(transactionData);
//                            }
//                        }
//                    } catch (Exception e) {
//                        log.error("Error while parsing row: {} ", row, e);
//                        throw  new RuntimeException(e);
//                    }
//                }
//            }
//        } catch (IOException e) {
//            log.error("Exception occurred while reading ICICI MPR file: ", e);
//            throw  new RuntimeException(e);
//        }
//        mprDao.saveAll(iciciMISList);
//        mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//        log.info("ICICI MPR imported {}", iciciMISList.size());
//    }
//
//    private boolean isCardMISExcelFile(String fileName) {
//        return fileName.startsWith("CONNAUGHT PLAZA") && !fileName.contains("Summary");
//    }
//
//
//    private Double parseDouble(String value) {
//        if (value != null && !value.isEmpty()) {
//            return Double.parseDouble(value);
//        }
//        return 0.0;
//    }
//
//
//    private LocalDateTime parseDate2(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat1.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
