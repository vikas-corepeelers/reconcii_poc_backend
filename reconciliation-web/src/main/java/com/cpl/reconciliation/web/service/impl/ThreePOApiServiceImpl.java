package com.cpl.reconciliation.web.service.impl;

import com.cpl.core.api.constant.Formatter;
import com.cpl.core.api.exception.ApiException;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.core.response.threepo.DataResponse;
import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;
import com.cpl.reconciliation.core.response.threepo.ThreePOData;
import com.cpl.reconciliation.web.service.ThreePoApiService;
import com.cpl.reconciliation.web.service.impl.ThreePo.SwiggyApiService;
import com.cpl.reconciliation.web.service.impl.ThreePo.ZomatoApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Slf4j
@Service
public class ThreePOApiServiceImpl implements ThreePoApiService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ThreePODownloadServiceImpl threePODownloadService;
    private final SwiggyApiService swiggyApiService;
    private final ZomatoApiService zomatoApiService;
//    private final MagicPinApiService magicPinApiService;
    @Autowired
    @Qualifier(value = "asyncExecutor")
    protected Executor asyncExecutor;

    @Override
    public DataResponse getDashboardDataResponse(ThreePODataRequest request) {
        List<ThreePOData> threePODataList = new ArrayList<>();
        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        if (CollectionUtils.isEmpty(request.getStores())) {
            request.setStores(null);
        }
        try {
            if (request.getTender() == null || request.getTender().equals(ThreePO.SWIGGY)) {
                CompletableFuture<Void> swiggyFuture = CompletableFuture.runAsync(() -> {
                    try {
                        threePODataList.add(swiggyApiService.getDashboardDataResponse(startDate, endDate, request.getStores()));
                    } catch (Exception e) {
                        log.error("Error while fetching Swiggy dashboard data for 3PO ", e);
                        throw new ApiException("Error while fetching Swiggy dashboard data for 3PO");
                    }
                }, asyncExecutor);
                futures.add(swiggyFuture);
            }
            if (request.getTender() == null || request.getTender().equals(ThreePO.ZOMATO)) {
                CompletableFuture<Void> zomatoFuture = CompletableFuture.runAsync(() -> {
                    try {
                        threePODataList.add(zomatoApiService.getDashboardDataResponse(startDate, endDate, request.getStores()));
                    } catch (Exception e) {
                        log.error("Error while fetching Zomato dashboard data for 3PO ", e);
                        throw new ApiException("Error while fetching Zomato dashboard data for 3PO");
                    }
                }, asyncExecutor);
                futures.add(zomatoFuture);
            }
//            if (request.getTender() == null || request.getTender().equals(ThreePO.MAGICPIN)) {
//                CompletableFuture<Void> magicPinFuture = CompletableFuture.runAsync(() -> {
//                    try {
//                        threePODataList.add(magicPinApiService.getDashboardDataResponse(startDate, endDate, request.getStores()));
//                    } catch (Exception e) {
//                        log.error("Error while fetching Magicpin dashboard data for 3PO ", e);
//                        throw new ApiException("Error while fetching Magicpin dashboard data for 3PO");
//                    }
//                },asyncExecutor);
//                futures.add(magicPinFuture);
//            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.get();
        } catch (Exception e) {
            throw new ApiException("Error while fetching data");
        }
        return getDashboardResponse(threePODataList);
    }

//    @Override
//    public GeneratedReportResponse getPOSvs3POResponse(ThreePODataRequest request) {
//        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        if (CollectionUtils.isEmpty(request.getStores())) {
//            request.setStores(null);
//        }
//        if (request.getTender() != null && request.getTender().equals(ThreePO.SWIGGY)) {
//            return swiggyApiService.getPOSvs3POResponse(startDate, endDate, request.getStores());
//        } else if (request.getTender() != null && request.getTender().equals(ThreePO.ZOMATO)) {
//            return zomatoApiService.getPOSvs3POResponse(startDate, endDate, request.getStores());
//        } else if (request.getTender() != null && request.getTender().equals(ThreePO.MAGICPIN)) {
//            return magicPinApiService.getPOSvs3POResponse(startDate, endDate, request.getStores());
//        }
//        return null;
//    }
//    @Override
//    public void getPOSvs3PODownload(ThreePODataRequest request, ByteArrayOutputStream outputStream) throws IOException {
//        threePODownloadService.getPOSvs3PODownload(request, outputStream);
//    }
    @Override
    public void reportDownload(ThreePODataRequest request, OutputStream outputStream) throws IOException {
        if (CollectionUtils.isEmpty(request.getStores())) {
            request.setStores(null);
        }
        try {
            threePODownloadService.reportDownload(request, outputStream);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<MissingThreePOMapping> getThreePoMissingMapping() {
        List<MissingThreePOMapping> mappings = new ArrayList<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        try {
            CompletableFuture<Void> zomatoFuture = CompletableFuture.runAsync(() -> {
                mappings.add(zomatoApiService.getThreePoMissingMapping());
            }, asyncExecutor);
            CompletableFuture<Void> swiggyFuture = CompletableFuture.runAsync(() -> {
                mappings.add(swiggyApiService.getThreePoMissingMapping());
            }, asyncExecutor);
//            CompletableFuture<Void> magicpinFuture = CompletableFuture.runAsync(() -> {
//                mappings.add(magicPinApiService.getThreePoMissingMapping());
//            }, asyncExecutor);
            futures.add(zomatoFuture);
            // futures.add(magicpinFuture);
            futures.add(swiggyFuture);

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.get();
        } catch (Exception e) {
            throw new ApiException("Error while fetching 3PO Mapping data");
        }
        return mappings;
    }

    @Override
    public void downloadThreePoMissingMapping(String threePo, ByteArrayOutputStream outputStream) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            if ("zomato".equalsIgnoreCase(threePo)) {
                zomatoApiService.downloadThreePoMissingMapping(workbook);
            } else if ("swiggy".equalsIgnoreCase(threePo)) {
                swiggyApiService.downloadThreePoMissingMapping(workbook);
            }
//        else if ("magicpin".equalsIgnoreCase(threePo)) {
//            magicPinApiService.downloadThreePoMissingMapping(workbook);
//        }
            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    @Override
    public DataResponse getThreePoDashboardDataResponse(ThreePODataRequest request) {
        List<ThreePOData> threePODataList = new ArrayList<>();
        LocalDate startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
        LocalDate endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        if (CollectionUtils.isEmpty(request.getStores())) {
            request.setStores(null);
        }
        try {
            if (request.getTender() == null || request.getTender().equals(ThreePO.SWIGGY)) {
                CompletableFuture<Void> swiggyFuture = CompletableFuture.runAsync(() -> {
                    try {
                        threePODataList.add(swiggyApiService.getThreePODashboardDataResponse(startDate, endDate, request.getStores()));
                    } catch (Exception e) {
                        log.error("Error while fetching Swiggy dashboard data for 3PO ", e);
                        throw new ApiException("Error while fetching Swiggy dashboard data for 3PO");
                    }
                }, asyncExecutor);
                futures.add(swiggyFuture);
            }
            if (request.getTender() == null || request.getTender().equals(ThreePO.ZOMATO)) {
                CompletableFuture<Void> zomatoFuture = CompletableFuture.runAsync(() -> {
                    try {
                        threePODataList.add(zomatoApiService.getThreePODashboardDataResponse(startDate, endDate, request.getStores()));
                    } catch (Exception e) {
                        log.error("Error while fetching Zomato dashboard data for 3PO ", e);
                        throw new ApiException("Error while fetching Zomato dashboard data for 3PO");
                    }
                }, asyncExecutor);
                futures.add(zomatoFuture);
            }
//            if (request.getTender() == null || request.getTender().equals(ThreePO.MAGICPIN)) {
//                CompletableFuture<Void> magicPinFuture = CompletableFuture.runAsync(() -> {
//                    try {
//                        threePODataList.add(magicPinApiService.getDashboardDataResponse(startDate, endDate, request.getStores()));
//                    } catch (Exception e) {
//                        log.error("Error while fetching Magicpin dashboard data for 3PO ", e);
//                        throw new ApiException("Error while fetching Magicpin dashboard data for 3PO");
//                    }
//                },asyncExecutor);
//                futures.add(magicPinFuture);
//            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.get();
        } catch (Exception e) {
            throw new ApiException("Error while fetching data");
        }
        return getDashboardResponse(threePODataList);
    }

    private DataResponse getDashboardResponse(List<ThreePOData> threePODataList) {
        DataResponse response = new DataResponse();
        Collections.sort(threePODataList);
        response.setThreePOData(threePODataList);
        for (ThreePOData threePOData : threePODataList) {
            response.setPosSales(response.getPosSales() + threePOData.getPosSales());
            response.setPosReceivables(response.getPosReceivables() + threePOData.getPosReceivables());
            response.setPosCommission(response.getPosCommission() + threePOData.getPosCommission());
            response.setPosCharges(response.getPosCharges() + threePOData.getPosCharges());
            response.setPosFreebies(response.getPosFreebies() + threePOData.getPosFreebies());
            response.setPosDiscounts(response.getPosDiscounts() + threePOData.getPosDiscounts());
            response.setThreePOSales(response.getThreePOSales() + threePOData.getThreePOSales());
            response.setThreePOReceivables(response.getThreePOReceivables() + threePOData.getThreePOReceivables());
            response.setThreePOCommission(response.getThreePOCommission() + threePOData.getThreePOCommission());
            response.setThreePOCharges(response.getThreePOCharges() + threePOData.getThreePOCharges());
            response.setThreePOFreebies(response.getThreePOFreebies() + threePOData.getThreePOFreebies());
            response.setThreePODiscounts(response.getThreePODiscounts() + threePOData.getThreePODiscounts());
            response.setReconciled(response.getReconciled() + threePOData.getReconciled());
            response.setBooked(response.getBooked() + threePOData.getBooked());
            response.setPosVsThreePO(response.getPosVsThreePO() + threePOData.getPosVsThreePO());
            response.setReceivablesVsReceipts(response.getReceivablesVsReceipts() + threePOData.getReceivablesVsReceipts());
            response.setPromo(response.getPromo() + threePOData.getPromo());
            response.setDeltaPromo(response.getDeltaPromo() + threePOData.getDeltaPromo());
        }
        return response;
    }
}
