//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.ZomatoPromoDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.ZomatoMappings;
//import com.cpl.reconciliation.domain.entity.ZomatoPromoEntity;
//import com.cpl.reconciliation.domain.repository.ZomatoMappingsRepository;
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
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@Data
//@Slf4j
//public class ZomatoPromoServiceImpl extends AbstractService implements DataService {
//    private final ZomatoPromoDao zomatoPromoDao;
//    private final ZomatoMappingsRepository zomatoMappingsRepository;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.ZOMATO_PROMO;
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
//        List<ZomatoPromoEntity> zomatoPromoEntityList = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(1);
//            boolean firstRow = true;
//            for (Row row : sheet) {
//                if (!firstRow) {
//                    ZomatoPromoEntity zomatoPromoEntity = new ZomatoPromoEntity();
//                    zomatoPromoEntity.setAggregation(LocalDate.parse(row.getCell(0).getStringCellValue(), Formatter.YYYYMMDD));
//                    zomatoPromoEntity.setTabId(row.getCell(1).getStringCellValue());
//                    zomatoPromoEntity.setResId(row.getCell(2).getStringCellValue());
//                    zomatoPromoEntity.setEntityName(row.getCell(3).getStringCellValue());
//                    zomatoPromoEntity.setBrandName(row.getCell(4).getStringCellValue());
//                    zomatoPromoEntity.setAccountType(row.getCell(5).getStringCellValue());
//                    zomatoPromoEntity.setPromoCode(row.getCell(6).getStringCellValue());
//                    zomatoPromoEntity.setPsSegment(row.getCell(7).getStringCellValue());
//                    zomatoPromoEntity.setZvdFinal(row.getCell(8).getNumericCellValue());
//                    zomatoPromoEntity.setZvd(row.getCell(9).getNumericCellValue());
//                    zomatoPromoEntity.setMvd(row.getCell(10) == null ? 0 : row.getCell(10).getNumericCellValue());
//                    zomatoPromoEntity.setOfflineRecon(row.getCell(11) == null ? 0 : row.getCell(11).getNumericCellValue());
//                    zomatoPromoEntity.setControl(row.getCell(12).getNumericCellValue());
//                    zomatoPromoEntity.setBurn(row.getCell(13).getNumericCellValue());
//                    zomatoPromoEntity.setTotal(row.getCell(14).getNumericCellValue());
//                    try {
//                        zomatoPromoEntity.setNet(row.getCell(15) == null ? 0 : row.getCell(15).getNumericCellValue());
//                    } catch (Exception ignored) {
//
//                    }
//                    zomatoPromoEntity.setConstruct(row.getCell(16).getNumericCellValue());
//                    zomatoPromoEntity.setCommission(row.getCell(17).getNumericCellValue());
//                    zomatoPromoEntity.setPg(row.getCell(18).getNumericCellValue());
//                    zomatoPromoEntity.setGstOnCommission(row.getCell(19).getNumericCellValue());
//                    zomatoPromoEntity.setGstOnPg(row.getCell(20).getNumericCellValue());
//                    zomatoPromoEntity.setFinalAmount(row.getCell(21).getNumericCellValue());
//                    zomatoPromoEntityList.add(zomatoPromoEntity);
//                    if (zomatoPromoEntityList.size() >= 500) {
//                        updateAndSaveInDb(zomatoPromoEntityList);
//                    }
//                } else {
//                    firstRow = false;
//                }
//            }
//            updateAndSaveInDb(zomatoPromoEntityList);
//        } catch (Exception e) {
//            log.error("Exception occurred while reading Zomato Promo: ", e);
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
//    public void prePersist(ZomatoPromoEntity entity) {
//        String resId = entity.getResId();
//        ZomatoMappings mapping = zomatoMappingsRepository.findByZomatoStoreCode(resId);
//        if (mapping != null) {
//            entity.setStoreCode(mapping.getStoreCode());
//        }
//    }
//
//    private void updateAndSaveInDb(List<ZomatoPromoEntity> promos) {
//        promos.forEach(this::prePersist);
//        zomatoPromoDao.saveAll(promos);
//        log.info("{} zomato promo saved", promos.size());
//        promos.clear();
//    }
//}
