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
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Data
//@Slf4j
//@Service
//public class SbiBankStatementServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//    private final DataEntryLogDao dataEntryLogDao;
//    private final BankStatementDao bankStatementDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//    private final MPRDao mprDao;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.SBI_BS;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        //String path = "C:\\Users\\91813\\Documents\\McD\\BankStatements\\sbi";
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            String path = sftpRoot + fileSeparator + "sbi" + fileSeparator + "bankstatement" + fileSeparator + businessDate;
//            File directory = new File(path);
//            if (!directory.exists() || !directory.isDirectory()) {
//                log.error("The specified directory {} does not exist.", path);
//                return;
//            }
//            File[] files = directory.listFiles();
//            log.info("Total file size:{}", files.length);
//            for (File file : files) processFile(new FileInputStream(file));
//            logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//        } else {
//            log.info("File already parsed for Business Date: {}", businessDate);
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
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//        } else {
//            log.info("File already parsed for Business Date: {}", businessDate);
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
//        }
//    }
//
//    public void processFile(InputStream file) throws IOException {
//        List<BankStatement> statementList = new ArrayList<>();
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean foundIdentifier = false;
//            for (Row row : sheet) {
//                if (foundIdentifier) {
//                    String narration = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
//                    if (narration == null) break;
//                    if (narration.contains("BULK POSTING-CR_CONNAUGHT PLAZA")) {
//                        BankStatement statement = new BankStatement();
//                        statement.setDate(parseDate(row.getCell(0).getDateCellValue()));
//                        statement.setValueDate(parseDate(row.getCell(1).getDateCellValue()));
//                        statement.setNarration(narration);
//                        statement.setChqRefNo(row.getCell(3) == null ? null : row.getCell(3).getStringCellValue());
//                        statement.setBranchCode(row.getCell(4) == null ? null : (int) row.getCell(4).getNumericCellValue());
//                        statement.setWithdrawalAmt(parseDouble(row.getCell(5) == null ? null : row.getCell(5).getStringCellValue()));
//                        statement.setDepositAmt(row.getCell(6) == null ? null : row.getCell(6).getNumericCellValue());
//                        statement.setClosingBalance(row.getCell(7) == null ? null : row.getCell(7).getNumericCellValue());
//                        statement.setPaymentType(PaymentType.CARD);
//                        statement.setBank(Bank.SBI);
//                        statement.setSourceBank(Bank.SBI);
//                        statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                        LocalDateTime currentDate = statement.getExpectedActualTransactionDate();
//                        if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                            minSettledDate = currentDate;
//                        }
//                        if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                            maxSettledDate = currentDate;
//                        }
//                        statement.setId(statement.getNarration().replaceAll("\\s+", "") + "|" + statement.getDepositAmt());
//                        statementList.add(statement);
//                    }
//                } else if (rowStartCondition(row)) {
//                    foundIdentifier = true;
//                }
//                if (statementList.size() > 500) {
//                    bankStatementDao.saveAll(statementList);
//                    statementList.clear();
//                }
//            }
//            bankStatementDao.saveAll(statementList);
//            mprDao.getMprBankDifference(Bank.SBI.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//
//        }
//    }
//
//    private LocalDateTime parseDate(Date value) {
//        try {
//            if (value != null) {
//                return value.toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
//
//    private Double parseDouble(String value) {
//        try {
//            if (value != null && !value.trim().isEmpty()) {
//                return Double.parseDouble(value);
//            }
//            return 0.0;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
//
//    public boolean rowStartCondition(Row row) {
//        try {
//            if (row.getCell(0) != null && row.getCell(1) != null && row.getCell(0).getStringCellValue().equals("Txn Date")
//                    && row.getCell(1).getStringCellValue().equals("Value Date")) return true;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return false;
//    }
//}
