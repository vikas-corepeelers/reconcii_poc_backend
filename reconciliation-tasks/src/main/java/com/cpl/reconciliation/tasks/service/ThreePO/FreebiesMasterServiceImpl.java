//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.FreebiesMasterDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.FreebiesMasterEntity;
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
//public class FreebiesMasterServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
//    private final FreebiesMasterDao freebiesMasterDao;
//    private final ZomatoSaltDataServiceImpl zomatoSaltDataService;
//    private final SwiggyPromoServiceImpl swiggyPromoService;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.FREEBIES_MASTER;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
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
//        List<FreebiesMasterEntity> freebiesMasterEntityList = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            LocalDate minStartDate = LocalDate.MAX;
//            LocalDate maxEndDate = LocalDate.MIN;
//            boolean firstRow = true;
//            for (Row row : sheet) {
//                if (!firstRow) {
//                    FreebiesMasterEntity freebiesMasterEntity = new FreebiesMasterEntity();
//                    freebiesMasterEntity.setStartDate(row.getCell(0) != null ? LocalDate.parse(row.getCell(0).getStringCellValue(), Formatter.MDYY_SLASH) : null);
//                    freebiesMasterEntity.setEndDate(row.getCell(1) != null ? LocalDate.parse(row.getCell(1).getStringCellValue(), Formatter.MDYY_SLASH) : null);
//                    if (freebiesMasterEntity.getStartDate().isBefore(minStartDate)) {
//                        minStartDate = freebiesMasterEntity.getStartDate();
//                    }
//                    if (freebiesMasterEntity.getEndDate().isAfter(maxEndDate)) {
//                        maxEndDate = freebiesMasterEntity.getEndDate();
//                    }
//                    freebiesMasterEntity.setTenderName(row.getCell(2).getStringCellValue());
//                    freebiesMasterEntity.setMinimumFoodBill(Double.parseDouble(row.getCell(3).getStringCellValue()));
//                    String day = row.getCell(4).getStringCellValue();
//                    DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
//                    freebiesMasterEntity.setDay(dayOfWeek);
//                    freebiesMasterEntity.setCprplSharePercentage(Double.parseDouble(row.getCell(5).getStringCellValue()));
//                    freebiesMasterEntity.setItemName(row.getCell(6).getStringCellValue());
//                    freebiesMasterEntity.setTotalCost(Double.parseDouble(row.getCell(7).getStringCellValue()));
//                    freebiesMasterEntity.setSalePriceDinningOtherState(Double.parseDouble(row.getCell(8).getStringCellValue()));
//                    freebiesMasterEntity.setSalePriceDeliveryNonHighwayOtherState(Double.parseDouble(row.getCell(9).getStringCellValue()));
//                    freebiesMasterEntity.setSalePriceDeliveryHighwayOtherState(Double.parseDouble(row.getCell(10).getStringCellValue()));
//                    freebiesMasterEntity.setSalePriceDinningRajasthan(Double.parseDouble(row.getCell(11).getStringCellValue()));
//                    freebiesMasterEntity.setSalePriceDeliveryNonHighwayRajasthan(Double.parseDouble(row.getCell(12).getStringCellValue()));
//                    freebiesMasterEntity.setSalePriceDeliveryHighwayRajasthan(Double.parseDouble(row.getCell(13).getStringCellValue()));
//                    freebiesMasterEntityList.add(freebiesMasterEntity);
//                    if (freebiesMasterEntityList.size() >= 500) {
//                        freebiesMasterDao.saveAll(freebiesMasterEntityList);
//                        log.info("Freebies Matster imported {}", freebiesMasterEntityList.size());
//                        freebiesMasterEntityList.clear();
//                    }
//                } else {
//                    firstRow = false;
//                }
//            }
//            freebiesMasterDao.saveAll(freebiesMasterEntityList);
//            if (!minStartDate.equals(LocalDate.MAX) && !maxEndDate.equals(LocalDate.MIN)) {
//                zomatoSaltDataService.updateAllSaltWithFreebies(minStartDate.atStartOfDay(), maxEndDate.plusDays(1).atStartOfDay());
//                swiggyPromoService.updateAllSaltWithFreebies(minStartDate.atStartOfDay(), maxEndDate.plusDays(1).atStartOfDay());
//            }
//            log.info("Freebies Master imported {}", freebiesMasterEntityList.size());
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
//    private LocalDate parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDate();
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
