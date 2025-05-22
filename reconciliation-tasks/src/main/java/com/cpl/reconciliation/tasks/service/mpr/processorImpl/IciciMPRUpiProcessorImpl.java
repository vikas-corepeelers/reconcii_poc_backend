//package com.cpl.reconciliation.tasks.service.mpr.processorImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.common.utility.ZIPUtils;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.core.util.CPLFileUtils;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.tasks.service.mpr.CardChargesUtilImpl;
//import com.cpl.reconciliation.tasks.service.mpr.MessageProcessor;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.cpl.reconciliation.tasks.Constants.*;
//
//@Data
//@Slf4j
//@Service
//public class IciciMPRUpiProcessorImpl implements MessageProcessor {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MMM-yy");
//    private final static DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private final BankSettlementDateUtil settlementDateUtil;
//    private final CardChargesUtilImpl cardChargesUtil;
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
//                if (isUPIMISExcelFile(path.toFile().getName())) {
//                    InputStream inputStream = new FileInputStream(path.toFile());
//                    saveXLSFiles(inputStream);
//                    inputStream.close();
//                }
//
//            }
//        } catch (Exception e) {
//            log.error("Exception in listing output: ", e);
//        } finally {
//            CPLFileUtils.deleteDirectory(outputPath);
//        }
//    }
//
//    public void saveXLSFiles(InputStream inputStream) {
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<MPREntity> iciciMISList = new ArrayList<>();
//        try (inputStream) {
//            try (Workbook workbook = new XSSFWorkbook(inputStream)) {
//                Sheet sheet = workbook.getSheetAt(0);
//                boolean firstRow = true;
//                for (Row row : sheet) {
//                    if (firstRow) firstRow = false;
//                    else {
//                        MPREntity mpr = new MPREntity();
//                        mpr.setTransactionDate(parseDate(row.getCell(7).getStringCellValue() + " " + row.getCell(8).getStringCellValue()));
//                        mpr.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.ICICI, mpr.getTransactionDate()));
//                        mpr.setMid(row.getCell(1).getStringCellValue());
//                        mpr.setSettledDate(parseDate(row.getCell(7).getStringCellValue() + " " + row.getCell(8).getStringCellValue()));
//                        mpr.setPayerVA(row.getCell(10).getStringCellValue());
//                        mpr.setTransactionId(row.getCell(5).getStringCellValue());
//                        mpr.setRrn(row.getCell(6).getStringCellValue());
//                        mpr.setMprAmount(parseDouble(row.getCell(9).getStringCellValue()));
//                        mpr.setCommission(parseDouble(row.getCell(12).getStringCellValue()));
//                        mpr.setGst(parseDouble(row.getCell(13).getStringCellValue()));
//                        mpr.setSettledAmount(mpr.getMprAmount());
//                        mpr.setBank(Bank.ICICI);
//                        mpr.setPaymentType(PaymentType.UPI);
//                        mpr.setUid(mpr.getTransactionId());
//                        mpr.setId(mpr.getUid());
//                        LocalDateTime currentDate = mpr.getSettledDate();
//                        if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                            minSettledDate = currentDate;
//                        }
//                        if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                            maxSettledDate = currentDate;
//                        }
//                        cardChargesUtil.setAndUpdateMDR(mpr);
//                        iciciMISList.add(mpr);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error while saving ICICI MPR UPI {}", e);
//            throw new RuntimeException(e);
//        }
//        mprDao.saveAll(iciciMISList);
//        mprDao.updateStoreTidFromTRMICICIUPI();
//        mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.UPI.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//    }
//
//    private Double parseDouble(String value) {
//        if (value != null && !value.isEmpty()) {
//            return Double.parseDouble(value);
//        }
//        return 0.0;
//    }
//
//    private LocalDateTime parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
//}
