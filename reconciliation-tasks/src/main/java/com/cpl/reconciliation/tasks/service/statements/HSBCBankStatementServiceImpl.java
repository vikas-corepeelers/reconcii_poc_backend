//package com.cpl.reconciliation.tasks.service.statements;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.BankStatementDao;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.entity.BankStatement;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
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
//import java.util.List;
//import java.util.Map;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Data
//@Slf4j
//@Service
//public class HSBCBankStatementServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy hh:mm");
//    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//    private final DataEntryLogDao dataEntryLogDao;
//    private final BankStatementDao bankStatementDao;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//    @Value("${sftp.root}")
//    private String sftpRoot;
//    private final MPRDao mprDao;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.HSBC_BS;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        //String path = "C:\\Users\\91813\\Documents\\McD\\BankStatements\\yes";
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//            String path = sftpRoot + fileSeparator + "yesbank" + fileSeparator + "bankstatement" + fileSeparator + businessDate;
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
//            log.info("file already parsed for today");
//
//        }
//
//    }
//
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
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
//
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
//                readByColumnName(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
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
//                    String narration = row.getCell(3) == null ? null : row.getCell(3).getStringCellValue();
//                    if (narration == null) break;
//                    if (narration.contains("AMERICAN EXPRESS")) {
//
//                        BankStatement statement = new BankStatement();
//                        statement.setDate(parseDate(row.getCell(1).getStringCellValue()));
//                        statement.setValueDate(parseDate1(row.getCell(2).getStringCellValue()));
//                        statement.setNarration(narration);
//                        statement.setChqRefNo(row.getCell(4) == null ? null : row.getCell(4).getStringCellValue());
//
//                        statement.setWithdrawalAmt(parseDouble(row.getCell(5).getStringCellValue()));
//                        statement.setDepositAmt(parseDouble(row.getCell(6).getStringCellValue()));
//                        statement.setClosingBalance(parseDouble(row.getCell(7).getStringCellValue()));
//
//                        statement.setPaymentType(PaymentType.CARD);
//
//                        statement.setBank(Bank.YES);
//                        statement.setSourceBank(Bank.AMEX);
//                        statement.setExpectedActualTransactionDate(statement.getValueDate());
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
//
//                } else if (rowStartCondition(row)) {
//                    foundIdentifier = true;
//                }
//
//                if (statementList.size() > 500) {
//                    bankStatementDao.saveAll(statementList);
//                    statementList.clear();
//                }
//            }
//            bankStatementDao.saveAll(statementList);
//            mprDao.getMprBankDifference(Bank.AMEX.name(), PaymentType.CARD.name(), minSettledDate.format(Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//
//        }
//    }
//
//    public void readByColumnName(InputStream file) throws IOException {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.HSBC_BS);
//        List<BankStatement> bankStatementList = new ArrayList<>();
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                BankStatement bankStatement = new BankStatement();
//                Row headerRow = sheet.getRow(0);
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (actualField) {
//                            case "account_name" ->
//                                    bankStatement.setAccountName(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf((long) cell.getNumericCellValue()));
//                            case "account_number" -> bankStatement.setAccountNumber(cell.getStringCellValue().trim());
//                            case "bank" -> bankStatement.setBank(Bank.HSBC);
//                            case "currency" -> bankStatement.setCurrency(cell.getStringCellValue().trim());
//                            case "location" ->
//                                    bankStatement.setLocation(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "iban" -> bankStatement.setIban(cell.getStringCellValue().trim());
//                            case "account_status" -> bankStatement.setAccountStatus(cell.getStringCellValue().trim());
//                            case "account_type" -> bankStatement.setAccountType(cell.getStringCellValue());
//                            case "closing_ledger_balance" ->
//                                    bankStatement.setClosingLedgerBalance(cell.getNumericCellValue());
//                            case "closing_Ledger_brought_forward_from" ->
//                                    bankStatement.setClosingLedgerBroughtForwardFrom(parseDate2(cell.getStringCellValue()));
//                            case "closing_available_balance" ->
//                                    bankStatement.setClosingAvailableBalance(cell.getNumericCellValue());
//                            case "closing_available_brought_forward_from" ->
//                                    bankStatement.setClosingAvailableBroughtForwardFrom(parseDate2(cell.getStringCellValue()));
//                            case "current_ledger_balance" ->
//                                    bankStatement.setCurrentLedgerBalance(cell.getNumericCellValue());
//                            case "Current_ledger_as_at" ->
//                                    bankStatement.setCurrentLedgerAsAt(parseDate3(cell.getStringCellValue()));
//                            case "current_available_balance" ->
//                                    bankStatement.setCurrentAvailableBalance(cell.getNumericCellValue());
//                            case "current_available_as_at" ->
//                                    bankStatement.setCurrentAvailableAsAt(parseDate3(cell.getStringCellValue()));
//                            case "transaction_id" -> bankStatement.setTransactionId(cell.getStringCellValue());
//                            case "narration" -> bankStatement.setNarration(cell.getStringCellValue());
//                            case "customer_reference" -> bankStatement.setCustomerReference(cell.getStringCellValue());
//                            case "trn_type" -> bankStatement.setTrnType(cell.getStringCellValue());
//                            case "value_date" -> bankStatement.setValueDate(parseDate1(cell.getStringCellValue()));
//                            case "deposit_amt" ->
//                                    bankStatement.setDepositAmt(cell.getCellType() == CellType.STRING && cell.getStringCellValue() == "" ? Double.valueOf(0.0) : cell.getNumericCellValue());
//                            case "withdrawal_amt" ->
//                                    bankStatement.setWithdrawalAmt(cell.getCellType() == CellType.STRING && cell.getStringCellValue() == "" ? Double.valueOf(0.0) : cell.getNumericCellValue());
//                            case "closing_balance" -> bankStatement.setClosingBalance(cell.getNumericCellValue());
//                            case "transaction_posted_date" ->
//                                    bankStatement.setTransactionPostedDate(cell.getCellType() == CellType.NUMERIC ? parseDate1(dateFormat.format(cell.getDateCellValue())) : parseDate1(cell.getStringCellValue()));
//                            case "time" -> bankStatement.setTime(cell.getStringCellValue());
//                        }
//                    }
//                }
//                bankStatement.setExpectedActualTransactionDate(bankStatement.getValueDate());
//                LocalDateTime currentDate = bankStatement.getExpectedActualTransactionDate();
//                if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                    minSettledDate = currentDate;
//                }
//                if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                    maxSettledDate = currentDate;
//                }
//                bankStatement.setId(bankStatement.getNarration().replaceAll("\\s+", "") + "|" + bankStatement.getDepositAmt());
//                bankStatementList.add(bankStatement);
//                if (bankStatementList.size() >= 500) {
//                    bankStatementDao.saveAll(bankStatementList);
//                    log.info("bank statement Data imported {}", bankStatementList.size());
//                    bankStatementList.clear();
//                }
//            }
//
//            bankStatementDao.saveAll(bankStatementList);
//            log.info("{} bankStatement records saved", bankStatementList.size());
//
//        } catch (Exception e) {
//            log.error("Exception occurred while reading Zomato file: ", e);
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
//    private Double parseDouble(String value) {
//        try {
//            if (value != null && !value.trim().isEmpty()) {
//                return Double.parseDouble(value.trim().replace(",", ""));
//            }
//            return 0.0;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private LocalDateTime parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateTimeFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private LocalDateTime parseDate1(String value) {
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
//    private LocalDate parseDate2(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDate();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private LocalDateTime parseDate3(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat1.parse(value).toInstant()
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
//            if (row.getCell(1) != null && row.getCell(2) != null && row.getCell(1).getStringCellValue().equalsIgnoreCase("Transaction Date")
//                    && row.getCell(2).getStringCellValue().equalsIgnoreCase("Value Date")) return true;
//        } catch (Exception ignored) {
//        }
//
//        return false;
//    }
//}
