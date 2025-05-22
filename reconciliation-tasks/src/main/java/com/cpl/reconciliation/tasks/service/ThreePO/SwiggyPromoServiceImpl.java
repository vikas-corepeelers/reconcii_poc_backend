//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.entity.*;
//import com.cpl.reconciliation.domain.entity.enums.StoreType;
//import com.cpl.reconciliation.domain.repository.*;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.github.pjfanning.xlsx.StreamingReader;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.logging.log4j.util.Strings;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//@Data
//@Slf4j
//@Service
//public class SwiggyPromoServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private final SwiggyRepository swiggyRepository;
//    private final SwiggyPromoRepository swiggyPromoRepository;
//    private final SwiggyMappingsRepository swiggyMappingsRepository;
//    private final StoreRepository storeRepository;
//    private final FreebiesMasterRepository freebiesMasterRepository;
//    private final Utility utility;
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) throws IOException {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.SWIGGY_PROMO;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
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
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
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
//                try (inputStream) {
//                    readTransactionsFromCSV(inputStream);
//                }
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendFailureMail(time, businessDate, endDate);
//
//        }
//    }
//
//    public void readTransactionsFromCSV(InputStream file) throws IOException {
//        List<SwiggyPromo> promos = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(1);
//            int dataStartRow = 3;
//            int rowCount = 0;
//            for (Iterator<Row> it = sheet.rowIterator(); it.hasNext(); ) {
//                Row row = it.next();
//                if (rowCount++ < dataStartRow) continue;
//                SwiggyPromo promo = new SwiggyPromo();
//                String dow = row.getCell(1) != null ? row.getCell(1).getStringCellValue() : "";
//                if (Strings.isBlank(dow)) break;
//                promo.setDow(Integer.parseInt(dow));
//                promo.setRid(row.getCell(2).getStringCellValue());
//                promo.setMonth(row.getCell(3).getStringCellValue());
//                double orderId = row.getCell(4).getNumericCellValue();
//                String formattedString = String.format("%.10f", orderId);
//                promo.setOrderId(formattedString.split("\\.")[0]);
//                promo.setHourOfDay(Integer.parseInt(row.getCell(5).getStringCellValue()));
//                promo.setDate(LocalDate.parse(row.getCell(6).getStringCellValue(), Formatter.MMDDYY_SLASH));
//                promo.setDay(promo.getDate().getDayOfWeek());
//                promo.setBrandName(row.getCell(7).getStringCellValue());
//                promo.setCouponCode(row.getCell(8).getStringCellValue());
//                promo.setUserType(row.getCell(9).getStringCellValue());
//                promo.setUserCohort(row.getCell(10).getStringCellValue());
//                promo.setFreebieDiscount(row.getCell(11).getNumericCellValue());
//                promo.setDiscountTotal(row.getCell(12).getNumericCellValue());
//                promo.setGmv(row.getCell(13).getNumericCellValue());
//                promo.setRemarks(row.getCell(14) != null ? row.getCell(14).getStringCellValue() : "");
//
//                promos.add(promo);
//                if (promos.size() >= 1000) {
//                    updateAndSaveInDb(promos);
//                }
//            }
//            updateAndSaveInDb(promos);
//        }
//    }
//
//    private void updateAndSaveInDb(List<SwiggyPromo> promos) {
//        promos.forEach(this::prePersist);
//        swiggyPromoRepository.saveAll(promos);
//        log.info("{} swiggy promo saved", promos.size());
//        promos.clear();
//    }
//
//    public void prePersist(SwiggyPromo entity) {
//        String resId = entity.getRid();
//        SwiggyMappings mapping = swiggyMappingsRepository.findBySwiggyStoreCode(resId);
//        if (mapping != null) {
//            entity.setStoreCode(mapping.getStoreCode());
//            String remarks = entity.getRemarks();
//            if (remarks != null && remarks.contains("Freebie")) {
//                setFreebieItem(entity);
//            }
//        }
//    }
//
//    private void setFreebieItem(SwiggyPromo entity) {
//        String storeCode = entity.getStoreCode();
//        if (storeCode.equalsIgnoreCase("5004")) {
//            storeCode = "0066";
//        }
//        Optional<StoreEntity> storeEntity = storeRepository.findByStoreCode(storeCode);
//        if (storeEntity.isPresent()) {
//            StoreType storeType = storeEntity.get().getStoreType();
//            boolean isRajasthan = "RAJASTHAN".equalsIgnoreCase(storeEntity.get().getState());
//            DayOfWeek day = entity.getDay();
//            LocalDate date = entity.getDate();
//            List<FreebiesMasterEntity> freebiesMasterEntityList = freebiesMasterRepository.getFreebieList(day, date, "Swiggy");
//
//            List<FreebiesMasterEntity> closestItems = new ArrayList<>();
//            double minDifference = Double.MAX_VALUE;
//            double freebieAmount = entity.getFreebieDiscount();
//
//            for (FreebiesMasterEntity item : freebiesMasterEntityList) {
//                double price = getPrice(storeType, item, isRajasthan);
//                double difference = Math.abs(price - freebieAmount);
//                if (difference < minDifference) {
//                    minDifference = difference;
//                    closestItems.clear();
//                    closestItems.add(item);
//                } else if (difference == minDifference) {
//                    closestItems.add(item);
//                }
//            }
//
//            FreebiesMasterEntity closestWithLowestCost = closestItems.stream()
//                    .min(Comparator.comparingDouble(FreebiesMasterEntity::getTotalCost))
//                    .orElse(null);
//            if (closestWithLowestCost != null) {
//                entity.setFreebieItem(closestWithLowestCost.getItemName());
//                entity.setFreebieCost(closestWithLowestCost.getTotalCost());
//                if (isRajasthan) {
//                    if (StoreType.DELIVERY_HIGHWAY.equals(storeType)) {
//                        entity.setFreebieSalePrice(closestWithLowestCost.getSalePriceDeliveryHighwayRajasthan());
//                    } else if (StoreType.DELIVERY_NON_HIGHWAY.equals(storeType)) {
//                        entity.setFreebieSalePrice(closestWithLowestCost.getSalePriceDeliveryNonHighwayRajasthan());
//                    } else {
//                        entity.setFreebieSalePrice(closestWithLowestCost.getSalePriceDinningRajasthan());
//                    }
//                } else {
//                    if (StoreType.DELIVERY_HIGHWAY.equals(storeType)) {
//                        entity.setFreebieSalePrice(closestWithLowestCost.getSalePriceDeliveryHighwayOtherState());
//                    } else if (StoreType.DELIVERY_NON_HIGHWAY.equals(storeType)) {
//                        entity.setFreebieSalePrice(closestWithLowestCost.getSalePriceDeliveryNonHighwayOtherState());
//                    } else {
//                        entity.setFreebieSalePrice(closestWithLowestCost.getSalePriceDinningOtherState());
//                    }
//                }
//            }
//        }
//    }
//
//    private double getPrice(StoreType storeType, FreebiesMasterEntity item, boolean isRajasthan) {
//        if (StoreType.DELIVERY_HIGHWAY.equals(storeType)) {
//            return isRajasthan ? item.getSalePriceDeliveryHighwayRajasthan() : item.getSalePriceDeliveryHighwayOtherState();
//        } else if (StoreType.DELIVERY_NON_HIGHWAY.equals(storeType)) {
//            return isRajasthan ? item.getSalePriceDeliveryNonHighwayRajasthan() : item.getSalePriceDeliveryNonHighwayOtherState();
//        }
//        return isRajasthan ? item.getSalePriceDinningRajasthan() : item.getSalePriceDinningOtherState();
//    }
//
//    public void updateAllSaltWithFreebies(LocalDateTime startDate,LocalDateTime endDate){
//        List<SwiggyPromo> swiggyPromos = swiggyPromoRepository.getAllWhereFreebieItemNotSet(startDate.toLocalDate(),endDate.toLocalDate());
//        swiggyPromos.forEach(this::setFreebieItem);
//        swiggyPromoRepository.saveAll(swiggyPromos);
//    }
//}
