//package com.cpl.reconciliation.tasks.service.mpr.processorImpl;
//
//import com.cpl.reconciliation.core.util.CPLFileUtils;
//import com.cpl.reconciliation.domain.dao.MPRRefundDao;
//import com.cpl.reconciliation.domain.entity.MPRRefundEntity;
//import com.cpl.reconciliation.tasks.service.mpr.MessageProcessor;
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
//import static com.cpl.reconciliation.tasks.Constants.ICICI_PROCESSED_FLAG;
//import static com.cpl.reconciliation.tasks.Constants.ICICI_REFUND_OUTPUT_PATH;
//
//@Data
//@Slf4j
//@Service
//public class IciciRefundDeemedProcessorImpl implements MessageProcessor {
//    private final static DateTimeFormatter CUSTOM_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy");
//    private final MPRRefundDao mprRefundDao;
//
//    private static boolean isRefundDeemedMISReport(String fileName) {
//        return fileName.matches(".*REFUND_DEEMED_MIS_REPORT.*\\.xlsx");
//    }
//
//    @Override
//    public void processMessages(Message[] messages) throws MessagingException, IOException {
//        boolean fileDownloaded = false;
//        Path downloadPath = Paths.get(ICICI_REFUND_OUTPUT_PATH);
//        if (!Files.exists(downloadPath)) {
//            Files.createDirectories(downloadPath);
//            log.info("ICICI Refund Directory created: " + downloadPath);
//        } else {
//            log.info("ICICI Refund Directory already exists: " + downloadPath);
//        }
//        for (Message message : messages) {
//            Multipart multipart = (Multipart) message.getContent();
//            for (int i = 0; i < multipart.getCount(); i++) {
//                BodyPart bodyPart = multipart.getBodyPart(i);
//                String fileName = bodyPart.getFileName();
//                if (fileName != null && (isRefundDeemedMISReport(fileName))) {
//                    MimeBodyPart part = (MimeBodyPart) bodyPart;
//                    String path = ICICI_REFUND_OUTPUT_PATH + "/" + part.getFileName();
//                    part.saveFile(path);
//                    fileDownloaded = true;
//
//                }
//            }
//            Flags processedFlag = new Flags(ICICI_PROCESSED_FLAG);
//            message.setFlags(processedFlag, true);
//        }
//        if (fileDownloaded) processXLSFiles();
//
//    }
//
//    private void processXLSFiles() {
//        Path outputPath = Paths.get(ICICI_REFUND_OUTPUT_PATH);
//        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(outputPath)) {
//            for (Path path : directoryStream) {
//                if (isRefundDeemedMISReport(path.toFile().getName())) {
//                    InputStream inputStream = new FileInputStream(path.toFile());
//                    saveReport(inputStream);
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
//    public void saveReport(InputStream inputStream) {
//        List<MPRRefundEntity> iciciRefundReports = new ArrayList<>();
//        try (inputStream) {
//            try (Workbook workbook = new XSSFWorkbook(inputStream)) {
//                Sheet sheet = workbook.getSheetAt(0);
//                boolean firstRow = true;
//                for (Row row : sheet) {
//                    if (firstRow) firstRow = false;
//                    else {
//                        try {
//                            MPRRefundEntity report = new MPRRefundEntity();
//                            report.setMerchantID(row.getCell(0) == null ? null : row.getCell(0).getStringCellValue());
//                            report.setMerchantName(row.getCell(1) == null ? null : row.getCell(1).getStringCellValue());
//                            report.setSubMerchantID(row.getCell(2) == null ? null : row.getCell(2).getStringCellValue());
//                            report.setSubMerchantName(row.getCell(3) == null ? null : row.getCell(3).getStringCellValue());
//                            report.setMerchantTranID(row.getCell(4) == null ? null : row.getCell(4).getStringCellValue());
//                            report.setOriginalTransactionDateTime(parseDate(
//                                    (row.getCell(5) == null ? "" : row.getCell(5).getStringCellValue()) + " " +
//                                            (row.getCell(6) == null ? "" : row.getCell(6).getStringCellValue())
//                            ));
//                            report.setRefundTransactionDateTime(parseDate(
//                                    (row.getCell(7) == null ? "" : row.getCell(7).getStringCellValue()) + " " +
//                                            (row.getCell(8) == null ? "" : row.getCell(8).getStringCellValue())
//                            ));
////                            String id = row.getCell(10).getStringCellValue()+"|"+parseDate1(row.getCell(7).getStringCellValue()).format(CUSTOM_FORMATTER);
//                            report.setRefundAmount(row.getCell(9) == null ? 0.0 : parseDouble(row.getCell(9).getStringCellValue()));
//                            report.setOriginalBankRRN(row.getCell(10) == null ? null : row.getCell(10).getStringCellValue());
//                            report.setCustomerVPA(row.getCell(11) == null ? null : row.getCell(11).getStringCellValue());
//                            report.setReasonForRefund(row.getCell(12) == null ? null : row.getCell(12).getStringCellValue());
//                            report.setMerchantAccount(row.getCell(13) == null ? null : row.getCell(13).getStringCellValue());
//                            report.setMerchantIFSCCode(row.getCell(14) == null ? null : row.getCell(14).getStringCellValue());
//                            report.setTypeOfRefund(row.getCell(15) == null ? null : row.getCell(15).getStringCellValue());
//                            report.setRefundRRN(row.getCell(16) == null ? null : row.getCell(16).getStringCellValue());
//                            report.setStatus(row.getCell(17) == null ? null : row.getCell(17).getStringCellValue());
//                            report.setOriginalMerchantTransactionId(row.getCell(18) == null ? null : row.getCell(18).getStringCellValue());
//                            report.setStatusUpdateDate(parseDate1(row.getCell(19) == null ? null : row.getCell(19).getStringCellValue()));
//                            report.setOnlineDeemed(true);
//                            report.setId(report.getOriginalMerchantTransactionId() + true);
//                            iciciRefundReports.add(report);
//                        } catch (Exception e) {
//                            log.error("Error while parsing icici refund row", e);
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        mprRefundDao.saveAll(iciciRefundReports);
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
//
//    private LocalDateTime parseDate1(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat1.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
//}
