//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.BudgetMasterDao;
//import com.cpl.reconciliation.domain.entity.BudgetMasterEntity;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.github.pjfanning.xlsx.StreamingReader;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.FileFilter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//@Service
//@Slf4j
//@Data
//public class BudgetMasterServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//    private final BudgetMasterDao budgetMasterDao;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.BUDGET_MASTER;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//
//    }
//
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
//        } else {
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                parseExcelFile(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//
//        }
//    }
//
//    public void parseExcelFile(InputStream file) {
//        List<BudgetMasterEntity> budgetMasterEntityList = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean firstRow = true;
//            for (Row row : sheet) {
//                if (!firstRow) {
//                    BudgetMasterEntity budgetMasterEntity = new BudgetMasterEntity();
//                    budgetMasterEntity.setStartDate(row.getCell(0) != null ? LocalDate.parse(row.getCell(0).getStringCellValue(), Formatter.MDYY_SLASH) : null);
//                    budgetMasterEntity.setEndDate(row.getCell(1) != null ? LocalDate.parse(row.getCell(1).getStringCellValue(), Formatter.MDYY_SLASH) : null);
//                    budgetMasterEntity.setTenderName(row.getCell(2).getStringCellValue());
//                    budgetMasterEntity.setType(row.getCell(3).getStringCellValue());
//                    budgetMasterEntity.setTotalBudget(Double.parseDouble(row.getCell(4).getStringCellValue()));
//                    try {
//                        String day = row.getCell(5).getStringCellValue();
//                        DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
//                        budgetMasterEntity.setDay(dayOfWeek);
//                    } catch (Exception ignored) {
//
//                    }
//                    budgetMasterEntityList.add(budgetMasterEntity);
//                    if (budgetMasterEntityList.size() >= 500) {
//                        budgetMasterDao.saveAll(budgetMasterEntityList);
//                        log.info("Budget Master imported {}", budgetMasterEntityList.size());
//                        budgetMasterEntityList.clear();
//                    }
//                } else {
//                    firstRow = false;
//                }
//            }
//            budgetMasterDao.saveAll(budgetMasterEntityList);
//            log.info("Budget Master imported {}", budgetMasterEntityList.size());
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
//
//    private LocalDateTime parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
