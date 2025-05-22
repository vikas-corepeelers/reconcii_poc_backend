//package com.cpl.reconciliation.tasks.service.statements;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.BankStatementDao;
//import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.BankStatement;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Data
//@Slf4j
//@Service
//public class HdfcBankStatementServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private static final FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//    private final DataEntryLogDao dataEntryLogDao;
//    private final BankStatementDao bankStatementDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//    private final MPRDao mprDao;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.HDFC_BS;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//            String path = sftpRoot + fileSeparator + "hdfc" + fileSeparator + "bankstatement" + fileSeparator + businessDate;
//            File directory = new File(path);
//            if (!directory.exists() || !directory.isDirectory()) {
//                log.error("The specified directory {} does not exist.", path);
//                return;
//            }
//            File[] files = directory.listFiles(isXLSXFile);
//            log.info("Total file size:{}", files.length);
//            for (File file : files) processFile(new FileInputStream(file));
//            logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//        } else {
//            log.info("file already parsed for today");
//        }
//
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
//
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//
//        } else {
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//    public void upload(LocalDate businessDate,LocalDate endDate,List<InputStream> inputStreams,LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                processFile(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time,businessDate,endDate);
//        } catch (Exception e) {
//            sendFailureMail(time,businessDate,endDate);
//
//        }
//    }
//
//    public void processFile(InputStream file) {
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<BankStatement> statementList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean foundIdentifier = false;
//            boolean skipFirst = true;
//            for (Row row : sheet) {
//                if (foundIdentifier) {
//                    if (skipFirst) {
//                        skipFirst = false;
//                        continue;
//                    }
//                    String narration = row.getCell(1) == null ? null : row.getCell(1).getStringCellValue();
//                    if (narration != null && (narration.contains("CARDS SETTL.") || narration.contains("UPI SETTLEMENT"))) {
//
//                        BankStatement statement = new BankStatement();
//                        statement.setDate(parseDate(row.getCell(0) == null ? null : row.getCell(0).getStringCellValue()));
//                        statement.setNarration(row.getCell(1) == null ? null : row.getCell(1).getStringCellValue());
//                        statement.setChqRefNo(row.getCell(2) == null ? null : row.getCell(2).getStringCellValue());
//                        statement.setValueDate(parseDate(row.getCell(3) == null ? null : row.getCell(3).getStringCellValue()));
//                        statement.setWithdrawalAmt(row.getCell(4) == null ? null : row.getCell(4).getNumericCellValue());
//                        statement.setDepositAmt(row.getCell(5) == null ? null : row.getCell(5).getNumericCellValue());
//                        statement.setClosingBalance(row.getCell(6) == null ? null : row.getCell(6).getNumericCellValue());
//
//                        if (narration.contains("UPI SETTLEMENT")) statement.setPaymentType(PaymentType.UPI);
//                        else statement.setPaymentType(PaymentType.CARD);
//
//                        try {
//                            String date = narration.substring(narration.length() - 8);
//                            statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYY_SLASH).atStartOfDay());
//                        } catch (Exception e) {
//                            statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                            log.error("Date not found in the HDFC Narration. {}, setting T-1 valueDate", narration);
//                        }
//                        LocalDateTime currentDate = statement.getExpectedActualTransactionDate();
//                        if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                            minSettledDate = currentDate;
//                        }
//                        if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                            maxSettledDate = currentDate;
//                        }
//
//                        statement.setBank(Bank.HDFC);
//                        statement.setSourceBank(Bank.HDFC);
//                        statement.setId(statement.getNarration().replaceAll("\\s+", "") + "|" + statement.getDepositAmt());
//                        statementList.add(statement);
//                    }
//                } else if (rowStartCondition(row)) {
//                    foundIdentifier = true;
//                }
//                if (statementList.size() > 500) {
//                    log.info("Going to save bach size: {}", statementList.size());
//                    bankStatementDao.saveAll(statementList);
//                    statementList.clear();
//                }
//            }
//            log.info("Going to save bach size: {}", statementList.size());
//            bankStatementDao.saveAll(statementList);
//            mprDao.getMprBankDifference(Bank.HDFC.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            mprDao.getMprBankDifference(Bank.HDFC.name(), PaymentType.UPI.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//
//        } catch (Exception e) {
//            log.info("Error occurred while opening file", e);
//            throw new RuntimeException(e);
//        }
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
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public boolean rowStartCondition(Row row) {
//        try {
//            if (row.getCell(0) != null && row.getCell(1) != null && row.getCell(0).getStringCellValue().equals("Date")
//                    && row.getCell(1).getStringCellValue().equals("Narration")) return true;
//        } catch (Exception ignored) {
//        }
//
//        return false;
//    }
//}
