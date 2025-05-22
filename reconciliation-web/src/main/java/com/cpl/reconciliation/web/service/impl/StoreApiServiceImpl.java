package com.cpl.reconciliation.web.service.impl;

import com.cpl.core.api.exception.ApiException;
import com.cpl.core.api.util.StringUtils;
import com.cpl.reconciliation.core.request.StoreByCitiesRequest;
import com.cpl.reconciliation.core.request.StoreByStatesRequest;
import com.cpl.reconciliation.core.request.StoreRequest;
import com.cpl.reconciliation.core.response.StoreCodeResponse;
import com.cpl.reconciliation.core.response.StoreResponse;
import com.cpl.reconciliation.domain.entity.*;
import com.cpl.reconciliation.domain.repository.*;
import com.cpl.reconciliation.tasks.utils.Utility;
import com.cpl.reconciliation.web.service.StoreApiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poiji.bind.Poiji;
import com.poiji.exception.PoijiExcelType;
import com.poiji.option.PoijiOptions;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH;

@Data
@Slf4j
@Service
public class StoreApiServiceImpl implements StoreApiService {
    private final StoreRepository storeRepository;
 //   private final StoreTIDMappingRepository storeTIDMappingRepository;
 //   private final AmexStoreMappingRepository amexStoreMappingRepository;
 //   private final ZomatoMappingsRepository zomatoMappingsRepository;
//    private final SwiggyMappingsRepository swiggyMappingsRepository;
//    private final MagicpinMappingsRepository magicpinMappingsRepository;
 //   private final SBICardChargesRepository sbiCardChargesRepository;
 //   private final HDFCCardChargesRepository hdfcCardChargesRepository;
 //   private final TRMRepository trmRepository;
  //  private final MPRRepository mprRepository;
  //  private final VoucherConfigEntityRepository voucherConfigEntityRepository;
    private final Utility utility;


    private final ObjectMapper objectMapper;


    @Override
    public StoreResponse getStore(Long id) {
        StoreEntity storeEntity = storeRepository.findById(id).get();
        return mapToStoreResponse(storeEntity);
    }

    @Override
    public StoreResponse getStore(String storeCode) {
        StoreEntity storeEntity = storeRepository.findByStoreCode(storeCode).get();
        return mapToStoreResponse(storeEntity);
    }

    @Override
    public List<String> getStateList() {
        return storeRepository.findDistinctStates();
    }

    @Override
    public List<String> getCityList() {
        return storeRepository.findDistinctCities();
    }

    @Override
    public List<StoreCodeResponse> getStoresByCity(String city) {
        List<StoreEntity> storeEntities;
        if (!StringUtils.isEmpty(city)) {
            storeEntities = storeRepository.findByCity(city);
        } else {
            storeEntities = storeRepository.findAll();
        }
        return storeEntities.stream().map(storeEntity -> {
            StoreCodeResponse storeCodeResponse = new StoreCodeResponse();
            storeCodeResponse.setCode(storeEntity.getStoreCode());
            storeCodeResponse.setName(storeEntity.getStoreName());
            storeCodeResponse.setState(storeEntity.getState());
            storeCodeResponse.setCity(storeEntity.getCity());
            storeCodeResponse.setPosDataSync(true);
            return storeCodeResponse;
        }).collect(Collectors.toList());
    }

    @Override
    public List<StoreCodeResponse> getStoreListByMultipleCities(StoreByCitiesRequest storeByCitiesRequest) {
        List<StoreEntity> storeEntities;
        try {
            LocalDate startDate = LocalDate.parse(storeByCitiesRequest.getStartDate(), YYYYMMDD_DASH);
            LocalDate endDate = LocalDate.parse(storeByCitiesRequest.getEndDate(), YYYYMMDD_DASH);
            if (CollectionUtils.isNotEmpty(storeByCitiesRequest.getCities())) {
                storeEntities = storeRepository.findByCities(storeByCitiesRequest.getCities());
            } else {
                storeEntities = storeRepository.findAll();
            }
            return storeEntities.stream().map(
                    storeEntity -> {
                        StoreCodeResponse storeCodeResponse = new StoreCodeResponse();
                        storeCodeResponse.setCode(storeEntity.getStoreCode());
                        storeCodeResponse.setName(storeEntity.getStoreName());
                        storeCodeResponse.setState(storeEntity.getState());
                        storeCodeResponse.setCity(storeEntity.getCity());
                        storeCodeResponse.setPosDataSync(true);
                        return storeCodeResponse;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception in fetching distinct store data from Mongo: ", e);
            throw new ApiException("Error in getting Store List");
        }
    }

    @Override
    public List<StoreResponse> getStoreList(String state) {
        List<StoreEntity> storeEntities;
        if (!StringUtils.isEmpty(state)) {
            storeEntities = storeRepository.findByState(state);
        } else {
            storeEntities = storeRepository.findAll();
        }
        return storeEntities.stream().map(storeEntity -> mapToStoreResponse(storeEntity)).collect(Collectors.toList());
    }

    @Override
    public List<StoreCodeResponse> getStoreListByMultipleStates(StoreByStatesRequest storebyStatesRequest) {
        List<StoreEntity> storeEntities;
        try {
            LocalDate startDate = LocalDate.parse(storebyStatesRequest.getStartDate(), YYYYMMDD_DASH);
            LocalDate endDate = LocalDate.parse(storebyStatesRequest.getEndDate(), YYYYMMDD_DASH);
            if (CollectionUtils.isNotEmpty(storebyStatesRequest.getStates())) {
                storeEntities = storeRepository.findByStateInAndStoreOpeningDateLessThanEqual(storebyStatesRequest.getStates(), endDate);
            } else {
                storeEntities = storeRepository.findByStoreOpeningDateLessThanEqual(endDate);
            }
            return storeEntities.stream().map(
                    storeEntity -> {
                        StoreCodeResponse storeCodeResponse = new StoreCodeResponse();
                        storeCodeResponse.setCode(storeEntity.getStoreCode());
                        storeCodeResponse.setName(storeEntity.getStoreName());
                        storeCodeResponse.setState(storeEntity.getState());
                        storeCodeResponse.setCity(storeEntity.getCity());
                        storeCodeResponse.setPosDataSync(true);
                        return storeCodeResponse;
                    }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Exception in fetching distinct store data from Mongo: ", e);
            throw new ApiException("Error in getting Store List");
        }
    }

    @Override
    public List<StoreCodeResponse> getStoreCodeList(String state) {
        List<StoreEntity> storeEntities;
        if (!StringUtils.isEmpty(state)) {
            storeEntities = storeRepository.findByState(state);
        } else {
            storeEntities = storeRepository.findAll();
        }
        return storeEntities.stream().map(storeEntity -> {
            StoreCodeResponse storeCodeResponse = new StoreCodeResponse();
            storeCodeResponse.setCode(storeEntity.getStoreCode());
            storeCodeResponse.setName(storeEntity.getStoreName());
            storeCodeResponse.setState(storeEntity.getState());
            storeCodeResponse.setCity(storeEntity.getCity());
            return storeCodeResponse;
        }).collect(Collectors.toList());
    }

//    @Override
//    public StoreResponse createStore(StoreRequest storeRequest) {
//        StoreEntity storeEntity = new StoreEntity();
//        storeEntity = mapToStoreEntity(storeEntity, storeRequest);
//        storeEntity = storeRepository.save(storeEntity);
//        return mapToStoreResponse(storeEntity);
//    }

//    @Override
//    public StoreResponse updateStore(StoreRequest storeRequest) {
//        StoreEntity storeEntity = storeRepository.findById(storeRequest.getId()).get();
//        storeEntity = mapToStoreEntity(storeEntity, storeRequest);
//        storeEntity = storeRepository.save(storeEntity);
//        return mapToStoreResponse(storeEntity);
//    }

//    @Override
//    public void deleteStore(Long id) {
//        storeRepository.deleteById(id);
//    }

//    @Override
//    public String uploadStoreMasterFile(MultipartFile multipartFile) {
//        AtomicInteger insertCounter = new AtomicInteger();
//        AtomicInteger updateCounter = new AtomicInteger();
//        try (Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0);
//            Iterator<Row> iterator = sheet.iterator();
//            iterator.next();
//            while (iterator.hasNext()) {
//                Row row = iterator.next();
//                //String storeCode = String.format("%04d", (long) row.getCell(2).getNumericCellValue());
//                String storeCode = row.getCell(1).getStringCellValue().trim();
//                Optional<StoreEntity> storeEntityOp = storeRepository.findByStoreCode(storeCode);
//                StoreEntity storeEntity;
//                if (storeEntityOp.isPresent()) {
//                    storeEntity = storeEntityOp.get();
//                    updateCounter.incrementAndGet();
//                } else {
//                    storeEntity = new StoreEntity();
//                    insertCounter.incrementAndGet();
//                }
//                storeEntity.setState(row.getCell(2).getStringCellValue().trim());
//                storeEntity.setCity(row.getCell(2).getStringCellValue().trim());
//                storeEntity.setStoreCode(storeCode);
//                storeEntity.setStoreName(row.getCell(0).getStringCellValue().trim());
////                storeEntity.setStoreStatus(row.getCell(4).getStringCellValue().trim());
////                storeEntity.setStoreMailId(row.getCell(5).getStringCellValue().trim());
////                storeEntity.setContactNumber(row.getCell(6).getStringCellValue().trim());
////                storeEntity.setFssaiLicenceNo(row.getCell(7).getStringCellValue().trim());
////                storeEntity.setGstNo(row.getCell(8).getStringCellValue().trim());
////                storeEntity.setEotfStatus(row.getCell(9).getStringCellValue().trim());
////                storeEntity.setMfyGdStore(row.getCell(10).getStringCellValue().trim());
////                String storeOpeningDate = row.getCell(11).getStringCellValue().trim().replaceAll("\\s+", " ");
////                ;
////                storeEntity.setStoreOpeningDate(LocalDate.parse(storeOpeningDate, MMMdyyyy_SOD));
////                storeEntity.setLongitude(Double.parseDouble(row.getCell(12).getStringCellValue()));
////                storeEntity.setLatitude(Double.parseDouble(row.getCell(13).getStringCellValue()));
////                storeEntity.setAddress(row.getCell(14).getStringCellValue().trim());
////                storeEntity.setPinCode(row.getCell(15).getStringCellValue().trim());
////                storeEntity.setOc(row.getCell(16).getStringCellValue().trim());
////                storeEntity.setOcPhoneNo(row.getCell(17).getStringCellValue().trim());
////                storeEntity.setOcEmailId(row.getCell(18).getStringCellValue().trim());
////                storeEntity.setOm(row.getCell(19).getStringCellValue().trim());
////                storeEntity.setOmPhoneNo(row.getCell(20).getStringCellValue().trim());
////                storeEntity.setOmEmailId(row.getCell(21).getStringCellValue().trim());
////                storeEntity.setRm(row.getCell(22).getStringCellValue().trim());
////                storeEntity.setSpod(row.getCell(23).getStringCellValue().trim());
////                storeEntity.setCircuitId(row.getCell(24).getStringCellValue().trim());
////                storeEntity.setIsp(row.getCell(25).getStringCellValue().trim());
////                storeEntity.setBandwidth(row.getCell(26).getStringCellValue().trim());
////                storeEntity.setMedia(row.getCell(27).getStringCellValue().trim());
////                storeEntity.setStoreType(StoreType.getEnum(row.getCell(28).getStringCellValue().trim()));
//                try {
//                    storeRepository.save(storeEntity);
//                } catch (Exception e) {
//                    log.error("Exception occurred while inserting Store: {}", storeEntity.getStoreCode(), e);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error while while uploading Store sheet", e);
//            throw new ApiException(e.getMessage());
//        }
//        String response = "Store Data Insert count: " + insertCounter.get() + " Update count: " + updateCounter.get();
//        log.info(response);
//        return response;
//    }
//
//    @Override
//    public String uploadStoreTIDMapping(MultipartFile multipartFile) {
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .build();
//        try {
//            List<StoreTIDMapping> storeTIDMappings = Poiji.fromExcel(multipartFile.getInputStream(), PoijiExcelType.XLSX, StoreTIDMapping.class, options);
//            AtomicInteger counter = new AtomicInteger();
//            for (StoreTIDMapping storeTIDMapping : storeTIDMappings) {
//                try {
//                    Optional<StoreTIDMapping> storeTidMap = storeTIDMappingRepository.findByTid(storeTIDMapping.getTid());
//                    if (storeTidMap.isPresent()) {
//                        storeTIDMappingRepository.save(storeTidMap.get());
//                    } else {
//                        storeTIDMappingRepository.save(storeTIDMapping);
//                    }
//                    counter.incrementAndGet();
//                } catch (Exception e) {
//                    log.error("Exception occurred while inserting Store: {} TID: {} Mapping: ", storeTIDMapping.getStoreCode(), storeTIDMapping.getTid(), e);
//                }
//            }
//            String response = "Store TID Mapping Data imported - " + counter.get();
//            log.info(response);
//            trmRepository.updateStoreMapping();
//            mprRepository.updateStoreMapping();
//            return response;
//        } catch (Exception e) {
//            log.error("Error while while uploading Store TID Mapping sheet", e);
//            throw new ApiException(e.getMessage());
//        }
//    }

//    @Override
//    public String uploadStoreAmexStoreNameMapping(MultipartFile multipartFile) {
//        AtomicInteger counter = new AtomicInteger();
//        try (Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream())) {
//            Sheet sheet = workbook.getSheetAt(0);
//            Iterator<Row> iterator = sheet.iterator();
//            iterator.next();
//            while (iterator.hasNext()) {
//                Row row = iterator.next();
//                String amexStoreName = row.getCell(0).getStringCellValue().trim();
//                String storeCode = row.getCell(1).getStringCellValue().trim();
//                AmexStoreMapping amexStoreMapping = new AmexStoreMapping();
//                amexStoreMapping.setAmexStoreName(amexStoreName);
//                amexStoreMapping.setStoreCode(storeCode);
//                try {
//                    amexStoreMappingRepository.save(amexStoreMapping);
//                    counter.incrementAndGet();
//                } catch (Exception e) {
//                    log.error("Exception occurred while updating Store: {} AmexStoreName: {} Mapping: ", storeCode, amexStoreName, e);
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error while while uploading Store AmexStoreName Mapping sheet", e);
//            throw new ApiException(e.getMessage());
//        }
//        String response = "Store AmexStoreName Mapping Data imported - " + counter.get();
//        log.info(response);
//        return response;
//    }
//
//
//    @Override
//    public String uploadStoreSwiggyStoreMapping(MultipartFile file) {
//        List<SwiggyMappings> mappings;
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .sheetIndex(0)
//                .build();
//        try {
//            mappings = Poiji.fromExcel(file.getInputStream(), PoijiExcelType.XLSX, SwiggyMappings.class, options);
//            mappings = mappings.stream().filter(k -> k.getSwiggyStoreCode() != null).collect(Collectors.toList());
//            swiggyMappingsRepository.saveAll(mappings);
//            swiggyMappingsRepository.updateMappings();
//        } catch (Exception e) {
//            log.error("Error while reading swiggy sheet", e);
//            throw new RuntimeException(e);
//        }
//
//        return null;
//    }

//    @Override
//    public String uploadStoreZomatoStoreMapping(MultipartFile file) {
//        List<ZomatoMappings> mappings;
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .sheetIndex(0)
//                .build();
//        try {
//            mappings = Poiji.fromExcel(file.getInputStream(), PoijiExcelType.XLSX, ZomatoMappings.class, options);
//            zomatoMappingsRepository.saveAll(mappings);
//            zomatoMappingsRepository.updateMappings();
//            zomatoMappingsRepository.updateSaltMappings();
//            zomatoMappingsRepository.updatePromoMappings();
//        } catch (Exception e) {
//            log.error("Error while reading zomato sheet", e);
//            throw new RuntimeException(e);
//        }
//
//        return null;
//    }

//    @Override
//    public String uploadStoreMagicPinStoreMapping(MultipartFile file) {
//        List<MagicpinMappings> mappings;
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .sheetIndex(0)
//                .build();
//        try {
//            mappings = Poiji.fromExcel(file.getInputStream(), PoijiExcelType.XLSX, MagicpinMappings.class, options);
//            magicpinMappingsRepository.saveAll(mappings);
//            magicpinMappingsRepository.updateMappings();
//        } catch (Exception e) {
//            log.error("Error while reading magicpin sheet", e);
//            throw new RuntimeException(e);
//        }
//
//        return null;
//    }

    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return new BufferedReader(inputStreamReader);
    }

//    @Override
//    public String uploadSBICardCharges(MultipartFile multipartFile) {
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .build();
//        try {
//            List<SBICardCharges> cardCharges = Poiji.fromExcel(multipartFile.getInputStream(), PoijiExcelType.XLSX, SBICardCharges.class, options);
//            AtomicInteger counter = new AtomicInteger();
//            for (SBICardCharges sbiCardCharges : cardCharges) {
//                try {
//                    sbiCardChargesRepository.save(sbiCardCharges);
//                    counter.incrementAndGet();
//                } catch (Exception e) {
//                    log.error("Exception occurred while inserting SBICardCharges: {}", sbiCardCharges, e);
//                }
//            }
//            String response = "SBICardCharges Data imported - " + counter.get();
//            log.info(response);
//            return response;
//        } catch (Exception e) {
//            log.error("Error while while uploading SBICardCharges sheet: ", e);
//            throw new ApiException(e.getMessage());
//        }
//    }

//    @Override
//    public String uploadHDFCCardCharges(MultipartFile multipartFile) {
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .build();
//        try {
//            List<HDFCCardCharges> cardCharges = Poiji.fromExcel(multipartFile.getInputStream(), PoijiExcelType.XLSX, HDFCCardCharges.class, options);
//            AtomicInteger counter = new AtomicInteger();
//            for (HDFCCardCharges hdfcCardCharges : cardCharges) {
//                try {
//                    hdfcCardChargesRepository.save(hdfcCardCharges);
//                    counter.incrementAndGet();
//                } catch (Exception e) {
//                    log.error("Exception occurred while inserting HDFCCardCharges: {}", hdfcCardCharges, e);
//                }
//            }
//            String response = "HDFCCardCharges Data imported - " + counter.get();
//            log.info(response);
//            return response;
//        } catch (Exception e) {
//            log.error("Error while while uploading HDFCCardCharges sheet: ", e);
//            throw new ApiException(e.getMessage());
//        }
//    }
//
//    @Override
//    public String uploadAMEXCardCharges(MultipartFile multipartFile) {
//        return null;
//    }
//
//    @Override
//    public String uploadICICICardCharges(MultipartFile multipartFile) {
//        return null;
//    }
//
//    @Override
//    public String uploadVoucherConfig(MultipartFile multipartFile) {
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .build();
//        try {
//            List<VoucherConfigEntity> voucherConfigEntities = Poiji.fromExcel(multipartFile.getInputStream(), PoijiExcelType.XLSX, VoucherConfigEntity.class, options);
//            AtomicInteger counter = new AtomicInteger();
//            for (VoucherConfigEntity voucherConfigEntity : voucherConfigEntities) {
//                try {
//                    voucherConfigEntityRepository.save(voucherConfigEntity);
//                    counter.incrementAndGet();
//                } catch (Exception e) {
//                    log.error("Exception occurred while inserting VoucherConfigEntity: {}", voucherConfigEntities, e);
//                }
//            }
//            String response = "VoucherConfigEntity Data imported - " + counter.get();
//            log.info(response);
//            return response;
//        } catch (Exception e) {
//            log.error("Error while while uploading Voucher Config sheet", e);
//            throw new ApiException(e.getMessage());
//        }
//    }


    private StoreEntity mapToStoreEntity(StoreEntity storeEntity, StoreRequest storeRequest) {
        storeEntity.setState(storeRequest.getState());
        storeEntity.setCity(storeRequest.getCity());
        storeEntity.setStoreCode(storeRequest.getStoreCode());
        storeEntity.setStoreName(storeRequest.getStoreName());
        storeEntity.setStoreStatus(storeRequest.getStoreStatus());
        storeEntity.setStoreMailId(storeRequest.getStoreMailId());
        storeEntity.setContactNumber(storeRequest.getContactNumber());
        storeEntity.setFssaiLicenceNo(storeRequest.getFssaiLicenceNo());
        storeEntity.setGstNo(storeRequest.getGstNo());
        storeEntity.setEotfStatus(storeRequest.getEotfStatus());
        storeEntity.setMfyGdStore(storeRequest.getMfyGdStore());
        storeEntity.setLongitude(storeRequest.getLongitude());
        storeEntity.setLatitude(storeRequest.getLatitude());
        storeEntity.setAddress(storeRequest.getAddress());
        storeEntity.setPinCode(storeRequest.getPinCode());
        storeEntity.setOc(storeRequest.getOc());
        storeEntity.setOcPhoneNo(storeRequest.getOcPhoneNo());
        storeEntity.setOcEmailId(storeRequest.getOcEmailId());
        storeEntity.setOm(storeRequest.getOm());
        storeEntity.setOmPhoneNo(storeRequest.getOmPhoneNo());
        storeEntity.setOmEmailId(storeRequest.getOmEmailId());
        storeEntity.setRm(storeRequest.getRm());
        storeEntity.setSpod(storeRequest.getSpod());
        storeEntity.setCircuitId(storeRequest.getCircuitId());
        storeEntity.setIsp(storeRequest.getIsp());
        storeEntity.setBandwidth(storeRequest.getBandwidth());
        storeEntity.setMedia(storeRequest.getMedia());
        return storeEntity;
    }

    private StoreResponse mapToStoreResponse(StoreEntity storeEntity) {
        if (storeEntity == null) return null;
        StoreResponse storeResponse = new StoreResponse();
        storeResponse.setId(storeEntity.getId());
        storeResponse.setState(storeEntity.getState());
        storeResponse.setCity(storeEntity.getCity());
        storeResponse.setStoreCode(storeEntity.getStoreCode());
        storeResponse.setStoreName(storeEntity.getStoreName());
        storeResponse.setStoreStatus(storeEntity.getStoreStatus());
        storeResponse.setStoreMailId(storeEntity.getStoreMailId());
        storeResponse.setContactNumber(storeEntity.getContactNumber());
        storeResponse.setFssaiLicenceNo(storeEntity.getFssaiLicenceNo());
        storeResponse.setGstNo(storeEntity.getGstNo());
        storeResponse.setEotfStatus(storeEntity.getEotfStatus());
        storeResponse.setMfyGdStore(storeEntity.getMfyGdStore());
        storeResponse.setLongitude(storeEntity.getLongitude());
        storeResponse.setLatitude(storeEntity.getLatitude());
        storeResponse.setAddress(storeEntity.getAddress());
        storeResponse.setPinCode(storeEntity.getPinCode());
        storeResponse.setOc(storeEntity.getOc());
        storeResponse.setOcPhoneNo(storeEntity.getOcPhoneNo());
        storeResponse.setOcEmailId(storeEntity.getOcEmailId());
        storeResponse.setOm(storeEntity.getOm());
        storeResponse.setOmPhoneNo(storeEntity.getOmPhoneNo());
        storeResponse.setOmEmailId(storeEntity.getOmEmailId());
        storeResponse.setRm(storeEntity.getRm());
        storeResponse.setSpod(storeEntity.getSpod());
        storeResponse.setCircuitId(storeEntity.getCircuitId());
        storeResponse.setIsp(storeEntity.getIsp());
        storeResponse.setBandwidth(storeEntity.getBandwidth());
        storeResponse.setMedia(storeEntity.getMedia());
        storeResponse.setCreatedAt(storeEntity.getAdded());
        storeResponse.setUpdatedAt(storeEntity.getUpdated());
        storeResponse.setCreatedBy(storeEntity.getCreatedBy());
        storeResponse.setUpdatedBy(storeEntity.getUpdatedBy());
        return storeResponse;
    }
}
