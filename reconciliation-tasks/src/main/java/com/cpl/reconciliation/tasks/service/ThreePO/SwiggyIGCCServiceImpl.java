//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.SwiggyIGCCDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.SwiggyIGCCEntity;
//import com.cpl.reconciliation.domain.entity.SwiggyPromo;
//import com.cpl.reconciliation.domain.repository.SwiggyPromoRepository;
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
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@Data
//@Slf4j
//public class SwiggyIGCCServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private final SwiggyIGCCDao swiggyIGCCDao;
//    private final SwiggyPromoRepository promoRepository;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.SWIGGY_IGCC;
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
//        List<SwiggyIGCCEntity> swiggyIGCCEntityList = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(2);
//            boolean firstRow = true;
//            for (Row row : sheet) {
//                if (!firstRow) {
//                    SwiggyIGCCEntity swiggyIGCCEntity = new SwiggyIGCCEntity();
//                    swiggyIGCCEntity.setDt(parseDate(row.getCell(0).getStringCellValue()));
//                    swiggyIGCCEntity.setRestaurantId(row.getCell(1).getStringCellValue());
//                    swiggyIGCCEntity.setOrderId(row.getCell(2).getStringCellValue());
//                    swiggyIGCCEntity.setOrderedTime(parseDate1(row.getCell(3).getStringCellValue()));
//                    swiggyIGCCEntity.setArrivedTime(parseDate1(row.getCell(4).getStringCellValue()));
//                    swiggyIGCCEntity.setPickedupTime(parseDate1(row.getCell(5).getStringCellValue()));
//                    swiggyIGCCEntity.setDeliveredTime(parseDate1(row.getCell(6).getStringCellValue()));
//                    swiggyIGCCEntity.setP2d(row.getCell(7).getStringCellValue());
//                    swiggyIGCCEntity.setDeWaitTime(row.getCell(8).getStringCellValue());
//                    swiggyIGCCEntity.setFoodIssueId(row.getCell(9).getStringCellValue());
//                    swiggyIGCCEntity.setL1Issue(row.getCell(10).getStringCellValue());
//                    swiggyIGCCEntity.setL2Issue(row.getCell(11) == null ? null : row.getCell(11).getStringCellValue());
//                    swiggyIGCCEntity.setResolutionsAmount(row.getCell(12).getNumericCellValue());
//                    swiggyIGCCEntity.setFraudFlag(row.getCell(13).getStringCellValue());
//                    swiggyIGCCEntity.setImpactedItemId(row.getCell(14).getStringCellValue());
//                    swiggyIGCCEntity.setImpactedItem(row.getCell(15).getStringCellValue());
//                    swiggyIGCCEntity.setBrandName(row.getCell(16).getStringCellValue());
//                    swiggyIGCCEntity.setBusinessEntity(row.getCell(17).getStringCellValue());
//                    swiggyIGCCEntity.setGroupName(row.getCell(18).getStringCellValue());
//                    swiggyIGCCEntity.setKamPoc(row.getCell(19).getStringCellValue());
//                    swiggyIGCCEntity.setVmPoc(row.getCell(20).getStringCellValue());
//                    swiggyIGCCEntity.setImages(row.getCell(21) == null ? null : row.getCell(21).getStringCellValue());
//                    swiggyIGCCEntity.setComments(row.getCell(22) == null ? null : row.getCell(22).getStringCellValue());
//                    swiggyIGCCEntityList.add(swiggyIGCCEntity);
//                    if (swiggyIGCCEntityList.size() >= 500) {
//                        saveEntities(swiggyIGCCEntityList);
//                    }
//                } else {
//                    firstRow = false;
//                }
//            }
//            saveEntities(swiggyIGCCEntityList);
//        } catch (Exception e) {
//            log.error("Exception occurred while reading Swiggy IGCC: ", e);
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
//    public void saveEntities(List<SwiggyIGCCEntity> swiggyIGCCEntityList) {
//        List<SwiggyPromo> toBeUpdated = new ArrayList<>();
//        swiggyIGCCEntityList.forEach(k -> prePersist(k, toBeUpdated));
//        swiggyIGCCDao.saveAll(swiggyIGCCEntityList);
//        promoRepository.saveAll(toBeUpdated);
//        log.info("Swiggy IGCC imported {}", swiggyIGCCEntityList.size());
//        swiggyIGCCEntityList.clear();
//        toBeUpdated.clear();
//    }
//
//    public void prePersist(SwiggyIGCCEntity igcc, List<SwiggyPromo> toBeUpdated) {
//        Optional<SwiggyPromo> swiggyPromoOptional = promoRepository.findById(igcc.getOrderId());
//        if (swiggyPromoOptional.isPresent()) {
//            SwiggyPromo swiggyPromo = swiggyPromoOptional.get();
//            swiggyPromo.setImages(igcc.getImages());
//            swiggyPromo.setReason(igcc.getComments());
//            toBeUpdated.add(swiggyPromo);
//        }
//    }
//
//
//    private LocalDate parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault()).toLocalDate()
//                        ;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
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
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
