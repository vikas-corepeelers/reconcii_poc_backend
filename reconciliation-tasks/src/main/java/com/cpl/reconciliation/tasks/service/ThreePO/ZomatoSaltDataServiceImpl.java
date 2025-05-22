//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.entity.*;
//import com.cpl.reconciliation.domain.entity.enums.StoreType;
//import com.cpl.reconciliation.domain.repository.*;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.opencsv.CSVReader;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@Data
//@Slf4j
//
//public class ZomatoSaltDataServiceImpl extends AbstractService implements DataService {
//    private final Utility utility;
//    private final ZomatoSaltRepository zomatoSaltRepository;
//    private final ZomatoRepository zomatoRepository;
//    private final ZomatoMappingsRepository zomatoMappingsRepository;
//    private final StoreRepository storeRepository;
//    private final FreebiesMasterRepository freebiesMasterRepository;
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.ZOMATO_SALT;
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
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//        } else {
//            log.info("File already parsed for {}", businessDate);
//            return false;
//        }
//        return true;
//    }
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                parseFile(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//
//    public void parseFile(InputStream file) {
//        List<ZomatoSalt> zomatoSalts;
//        try (CSVReader reader = new CSVReader(createReaderFromMultipartFile(file))) {
//            CsvToBean<ZomatoSalt> csvToBean = new CsvToBeanBuilder<ZomatoSalt>(reader)
//                    .withType(ZomatoSalt.class)
//                    .build();
//
//            zomatoSalts = csvToBean.parse();
//            int count = 1;
//            List<List<ZomatoSalt>> chunks = utility.chunkList(zomatoSalts, 500);
//            log.info("Total Zomato salt entries {}, chunk size: {}", zomatoSalts.size(), 500);
//            List<Zomato> toBeUpdated = new ArrayList<>();
//            for (List<ZomatoSalt> chunk : chunks) {
//                chunk.forEach(k -> prePersist(k, toBeUpdated));
//                zomatoSaltRepository.saveAll(chunk);
//                zomatoRepository.saveAll(toBeUpdated);
//                toBeUpdated.clear();
//                log.info("{}/{} chunk saved", count++, chunks.size());
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void prePersist(ZomatoSalt salt, List<Zomato> toBeUpdated) {
//        if (salt.getSaltDiscount() == 0) return;
//        String resId = salt.getResId();
//        ZomatoMappings mapping = zomatoMappingsRepository.findByZomatoStoreCode(resId);
//        if (mapping != null) {
//            salt.setStoreCode(mapping.getStoreCode());
//        }
//        List<Zomato> zomatoes1 = zomatoRepository.findByOrderId(salt.getTabId());
//        zomatoes1.forEach(z -> z.setFreebie(salt.getSaltDiscount()));
//        toBeUpdated.addAll(zomatoes1);
//        setFreebieItem(salt);
//    }
//
//    private void setFreebieItem(ZomatoSalt entity) {
//        String storeCode = entity.getStoreCode();
//        if (storeCode.equalsIgnoreCase("5004")) {
//            storeCode = "0066";
//        }
//        Optional<StoreEntity> storeEntity = storeRepository.findByStoreCode(storeCode);
//        if (storeEntity.isPresent()) {
//            StoreType storeType = storeEntity.get().getStoreType();
//            boolean isRajasthan = "RAJASTHAN".equalsIgnoreCase(storeEntity.get().getState());
//            DayOfWeek day = entity.getCreatedAt().getDayOfWeek();
//            LocalDate date = entity.getCreatedAt().toLocalDate();
//            List<FreebiesMasterEntity> freebiesMasterEntityList = freebiesMasterRepository.getFreebieList(day, date, "Zomato");
//
//            List<FreebiesMasterEntity> closestItems = new ArrayList<>();
//            double minDifference = Double.MAX_VALUE;
//            double freebieAmount = entity.getSaltDiscount();
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
//
//    public void updateAllSaltWithFreebies(LocalDateTime startDate,LocalDateTime endDate){
//        List<ZomatoSalt> zomatoSalts = zomatoSaltRepository.getAllWhereFreebieItemNotSet(startDate,endDate);
//        zomatoSalts.forEach(this::setFreebieItem);
//        zomatoSaltRepository.saveAll(zomatoSalts);
//    }
//
//}
