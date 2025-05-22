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
//import com.cpl.reconciliation.tasks.utils.SFTPService;
//import com.jcraft.jsch.ChannelSftp;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Vector;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//
//@Data
//@Slf4j
//@Service
//public class IciciBankStatementServiceImpl extends AbstractService implements DataService {
//    private final static String fileSeparator = System.getProperty("file.separator");
//    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
//    private static final SimpleDateFormat dateFormatManual = new SimpleDateFormat("dd/MMM/yyyy");
//
//    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
//    private final DataEntryLogDao dataEntryLogDao;
//    private final BankStatementDao bankStatementDao;
//    private final SFTPService sftpService;
//    @Value("${sftp.root}")
//    private String localSftpRoot;
//    @Value("${sftp.icici.bankstatement.host}")
//    private String sftpHost;
//    @Value("${sftp.icici.bankstatement.port}")
//    private Integer sftpPort;
//    @Value("${sftp.icici.bankstatement.user}")
//    private String sftpUser;
//    @Value("${sftp.icici.bankstatement.password}")
//    private String sftpPassword;
//    @Value("${sftp.icici.bankstatement.root}")
//    private String bankstatementRoot;
//    private final MPRDao mprDao;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.ICICI_BS;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), yesterday, yesterday);
//        if (dataEntryLogList.isEmpty()) {
//            ChannelSftp sftp = sftpService.connectSftp(sftpUser, sftpHost, sftpPort, sftpPassword);
//            String businessDate = DateToString.backDateString(Formatter.DDMMYYYY, 1);
//            String file1 = "000705052855" + "_" + businessDate + ".xls";
//            String file2 = "000705053461" + "_" + businessDate + ".xls";
//            int file_count = 0;
//            sftp.cd(bankstatementRoot);
//            Vector<ChannelSftp.LsEntry> lsEntries = sftp.ls(sftp.pwd());
//            try {
//                for (ChannelSftp.LsEntry file : lsEntries) {
//                    if (file.getFilename().equals(file1) || file.getFilename().equals(file2)) {
//                        log.info("Bank Statement matched: {}", file.getFilename());
//
//                        InputStream inputStream = sftp.get(file.getFilename());
//                        Workbook workbook = new HSSFWorkbook(inputStream);
//                        String localFilePath = localSftpRoot + fileSeparator + "icici" + fileSeparator + "bankstatement" + fileSeparator + file.getFilename();
//                        try (FileOutputStream fileOutputStream = new FileOutputStream(localFilePath)) {
//                            workbook.write(fileOutputStream);
//                            processFile(new FileInputStream(localFilePath));
//                            file_count++;
//                        }
//                    }
//                }
//                logInDB(stringToLocalDate(businessDate, Formatter.DDMMYYYY), file_count);
//            } catch (Exception e) {
//                log.error("error while parsing icici data for date {}", businessDate, e);
//            }
//            sftp.disconnect();
//        } else {
//            log.info("File already parsed for date: {}", yesterday);
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
//
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//        } else {
//            log.info("file already parsed for today");
//        }
//        return true;
//    }
//
//
//    public void upload( LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams,LocalDateTime time)  {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                processFileManual2(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//            log.error("Error in parsing ICICI bank statement",e);
//            e.printStackTrace();
//        }
//    }
//
//    public void processFileManual2(InputStream inputStream) throws IOException {
//        List<BankStatement> statementList = new ArrayList<>();
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        String accountNo = "";
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean foundIdentifier = false;
//            for (Row row : sheet) {
//                if (foundIdentifier) {
//                    String narration = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
//                    if (narration == null) break;
//                    if (narration.startsWith("CMS/ UPI SETTLEMENT") || narration.contains("CCARD PMT") || narration.startsWith("TRF AS PER AGMT")) {
//                        BankStatement statement = new BankStatement();
//                        statement.setDate(parseDate(row.getCell(0).getStringCellValue()));
//                        statement.setValueDate(parseDate(row.getCell(1).getStringCellValue()));
//                        statement.setNarration(narration);
//                        statement.setChqRefNo(row.getCell(3) == null ? null : row.getCell(3).getStringCellValue());
//                        statement.setWithdrawalAmt(row.getCell(5) == null ? null : row.getCell(5).getNumericCellValue());
//                        statement.setDepositAmt(row.getCell(6) == null ? null : row.getCell(6).getNumericCellValue());
//                        statement.setClosingBalance(row.getCell(7) == null ? null : row.getCell(7).getNumericCellValue());
//                        statement.setAccountNumber(accountNo);
//                        if (narration.startsWith("CMS/ UPI SETTLEMENT")) {
//                            Pattern pattern = Pattern.compile("\\b\\d{2} \\d{2} \\d{4}\\b");
//                            Matcher matcher = pattern.matcher(narration);
//                            if (matcher.find()) {
//                                String date = matcher.group();
//                                statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYYYY_SPACE).atStartOfDay());
//                            } else {
//                                statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                                log.error("Date not found in the ICICI UPI Narration. {}, setting T-1 valueDate", narration);
//                            }
//                            statement.setPaymentType(PaymentType.UPI);
//                        }
//                        else if (narration.startsWith("TRF AS PER AGMT")) {
//                            //Date not received in TRF narration
//                            statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                            statement.setPaymentType(PaymentType.TRANSFER);
//                            log.debug("Saving TRF narration", narration);
//                        } else {
//                            Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
//                            Matcher matcher = pattern.matcher(narration);
//                            if (matcher.find()) {
//                                String date = matcher.group();
//                                statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYY).atStartOfDay());
//                            } else {
//                                statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                                log.error("Date not found in the ICICI CARD Narration. {}, setting T-1 valueDate", narration);
//                            }
//                            statement.setPaymentType(PaymentType.CARD);
//                        }
//                        LocalDateTime currentDate = statement.getExpectedActualTransactionDate();
//                        if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                            minSettledDate = currentDate;
//                        }
//                        if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                            maxSettledDate = currentDate;
//                        }
//                        statement.setBank(Bank.ICICI);
//                        statement.setSourceBank(Bank.ICICI);
//                        statement.setId(statement.getNarration().replaceAll("\\s+", "") + "|" + statement.getDepositAmt());
//                        statementList.add(statement);
//                    }
//                } else if (rowStartCondition(row)) {
//                    foundIdentifier = true;
//                }
//                else if(accountNumberCondition(row)){
//                    accountNo = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
//                }
//                if (statementList.size() > 500) {
//                    bankStatementDao.saveAll(statementList);
//                    statementList.clear();
//                }
//            }
//            bankStatementDao.saveAll(statementList);
//            mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.UPI.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//        }
//    }
//    public void processFileManual(InputStream inputStream) throws IOException {
//        List<BankStatement> statementList = new ArrayList<>();
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        String accountNo = "";
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean foundIdentifier = false;
//            for (Row row : sheet) {
//                if (foundIdentifier) {
//                    String narration = row.getCell(6) == null ? null : row.getCell(6).getStringCellValue();
//                    if (narration == null) break;
//                    if (narration.startsWith("CMS/ UPI SETTLEMENT") || narration.contains("CCARD PMT") || narration.startsWith("TRF AS PER AGMT")) {
//
//                        BankStatement statement = new BankStatement();
//                        statement.setTransactionId(row.getCell(1).getStringCellValue());
//                        statement.setValueDate(parseDateManual(row.getCell(2).getStringCellValue()));
//                        statement.setDate(parseDateManual(row.getCell(3).getStringCellValue()));
//                        statement.setTransactionPostedDate(parseDateTimeFormat(row.getCell(4).getStringCellValue()));
//                        statement.setChqRefNo(row.getCell(5) == null ? null : row.getCell(5).getStringCellValue());
//
//                        statement.setNarration(narration);
//
//                        statement.setWithdrawalAmt(parseDouble(row.getCell(7) == null ? null : row.getCell(7).getStringCellValue()));
//                        statement.setDepositAmt(parseDouble(row.getCell(8) == null ? null : row.getCell(8).getStringCellValue()));
//                        statement.setClosingBalance(parseDouble(row.getCell(9) == null ? null : row.getCell(9).getStringCellValue()));
//                        if (narration.startsWith("CMS/ UPI SETTLEMENT")) {
//                            Pattern pattern = Pattern.compile("\\b\\d{2} \\d{2} \\d{4}\\b");
//                            Matcher matcher = pattern.matcher(narration);
//                            if (matcher.find()) {
//                                String date = matcher.group();
//                                statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYYYY_SPACE).atStartOfDay());
//                            } else {
//                                statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                                log.error("Date not found in the ICICI UPI Narration. {}, setting T-1 valueDate", narration);
//                            }
//                            statement.setPaymentType(PaymentType.UPI);
//                        } else if (narration.startsWith("TRF AS PER AGMT")) {
//                            //Date not received in TRF narration
//                            statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                            statement.setPaymentType(PaymentType.TRANSFER);
//                            log.debug("Saving TRF narration", narration);
//                        }else {
//                            Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
//                            Matcher matcher = pattern.matcher(narration);
//                            if (matcher.find()) {
//                                String date = matcher.group();
//                                statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYY).atStartOfDay());
//                            } else {
//                                statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                                log.error("Date not found in the ICICI CARD Narration. {}, setting T-1 valueDate", narration);
//                            }
//                            statement.setPaymentType(PaymentType.CARD);
//                        }
//                        LocalDateTime currentDate = statement.getExpectedActualTransactionDate();
//                        if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                            minSettledDate = currentDate;
//                        }
//                        if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                            maxSettledDate = currentDate;
//                        }
//                        statement.setBank(Bank.ICICI);
//                        statement.setSourceBank(Bank.ICICI);
//                        statement.setId(statement.getNarration().replaceAll("\\s+", "") + "|" + statement.getDepositAmt());
//                        statement.setAccountNumber(accountNo);
//                        statementList.add(statement);
//                    }
//                } else if (rowStartCondition(row)) {
//                    foundIdentifier = true;
//                }
//                if(accountNumberCondition(row)){
//                    accountNo = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
//                }
//                if (statementList.size() > 500) {
//                    bankStatementDao.saveAll(statementList);
//                    statementList.clear();
//                }
//            }
//            mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.UPI.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            bankStatementDao.saveAll(statementList);
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
//    private LocalDateTime parseDateTimeFormat(String value) {
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
//    public void processFile(InputStream inputStream) throws IOException {
//        List<BankStatement> statementList = new ArrayList<>();
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        String accountNo = "";
//        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean foundIdentifier = false;
//            for (Row row : sheet) {
//                if (foundIdentifier) {
//                    String narration = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
//                    if (narration == null) break;
//                    if (narration.startsWith("CMS/ UPI SETTLEMENT") || narration.contains("CCARD PMT") || narration.startsWith("TRF AS PER AGMT")) {
//                        BankStatement statement = new BankStatement();
//                        statement.setDate(parseDate(row.getCell(0).getStringCellValue()));
//                        statement.setValueDate(parseDate(row.getCell(1).getStringCellValue()));
//                        statement.setNarration(narration);
//                        statement.setChqRefNo(row.getCell(3) == null ? null : row.getCell(3).getStringCellValue());
//                        statement.setWithdrawalAmt(row.getCell(5) == null ? null : row.getCell(5).getNumericCellValue());
//                        statement.setDepositAmt(row.getCell(6) == null ? null : row.getCell(6).getNumericCellValue());
//                        statement.setClosingBalance(row.getCell(7) == null ? null : row.getCell(7).getNumericCellValue());
//                        statement.setAccountNumber(accountNo);
//                        if (narration.startsWith("CMS/ UPI SETTLEMENT")) {
//                            Pattern pattern = Pattern.compile("\\b\\d{2} \\d{2} \\d{4}\\b");
//                            Matcher matcher = pattern.matcher(narration);
//                            if (matcher.find()) {
//                                String date = matcher.group();
//                                statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYYYY_SPACE).atStartOfDay());
//                            } else {
//                                statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                                log.error("Date not found in the ICICI UPI Narration. {}, setting T-1 valueDate", narration);
//                            }
//                            statement.setPaymentType(PaymentType.UPI);
//                        }
//                        else if (narration.startsWith("TRF AS PER AGMT")) {
//                            //Date not received in TRF narration
//                            statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                            statement.setPaymentType(PaymentType.TRANSFER);
//                            log.debug("Saving TRF narration", narration);
//                        } else {
//                            Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
//                            Matcher matcher = pattern.matcher(narration);
//                            if (matcher.find()) {
//                                String date = matcher.group();
//                                statement.setExpectedActualTransactionDate(LocalDate.parse(date, Formatter.DDMMYY).atStartOfDay());
//                            } else {
//                                statement.setExpectedActualTransactionDate(statement.getValueDate().minusDays(1));
//                                log.error("Date not found in the ICICI CARD Narration. {}, setting T-1 valueDate", narration);
//                            }
//                            statement.setPaymentType(PaymentType.CARD);
//                        }
//                        LocalDateTime currentDate = statement.getExpectedActualTransactionDate();
//                        if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                            minSettledDate = currentDate;
//                        }
//                        if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                            maxSettledDate = currentDate;
//                        }
//                        statement.setBank(Bank.ICICI);
//                        statement.setSourceBank(Bank.ICICI);
//                        statement.setId(statement.getNarration().replaceAll("\\s+", "") + "|" + statement.getDepositAmt());
//                        statementList.add(statement);
//                    }
//                } else if (rowStartCondition(row)) {
//                    foundIdentifier = true;
//                }
//                else if(accountNumberCondition(row)){
//                    accountNo = row.getCell(2) == null ? null : row.getCell(2).getStringCellValue();
//                }
//                if (statementList.size() > 500) {
//                    bankStatementDao.saveAll(statementList);
//                    statementList.clear();
//                }
//            }
//            bankStatementDao.saveAll(statementList);
//            mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            mprDao.getMprBankDifference(Bank.ICICI.name(), PaymentType.UPI.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
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
//            log.error("Exception in parsing date: {}", e.getMessage());
//        }
//        return null;
//    }
//
//    private LocalDateTime parseDateManual(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormatManual.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (Exception e) {
//            log.error("Exception in parsing date: {}", e.getMessage());
//        }
//        return null;
//    }
//
//    public boolean rowStartCondition(Row row) {
//        try {
//            if (row.getCell(0) != null && row.getCell(0).getStringCellValue().equals("Tran Date")) return true;
//        } catch (Exception e) {
//            return false;
//        }
//        return false;
//    }
//
//    public boolean accountNumberCondition(Row row) {
//        try {
//            if (row.getCell(0) != null && row.getCell(0).getStringCellValue().equals("ACCOUNT NUMBER")) return true;
//        } catch (Exception e) {
//            return false;
//        }
//        return false;
//    }
//
//    public boolean rowStartConditionManual(Row row) {
//        try {
//            if (row.getCell(0) != null && row.getCell(1) != null && row.getCell(0).getStringCellValue().equals("S.N.")
//                    && row.getCell(1).getStringCellValue().equals("Tran. Id")) return true;
//        } catch (Exception ignored) {
//        }
//
//        return false;
//    }
//}
