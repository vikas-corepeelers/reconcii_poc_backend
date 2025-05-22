package com.cpl.reconciliation.web.instore;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.*;
import com.cpl.reconciliation.web.service.DashboardApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Data
@Slf4j
@RestController
@RequestMapping("/public/dashboard")
public class DashboardApiController {

    //  private final ZomatoRepository zomatoRepository;
    //private final MprApiService mprApiService;
    //private final TrmApiService trmApiService;
    //private final OrderApiService orderApiService;
    private final DashboardApiService dashboardApiService;
    // private final ReportGenerationServiceImpl reportGenerationService;

    @RequestMapping(value = "/reportingTenders", method = RequestMethod.GET)
    public ApiResponse<List<ReportingTendersDataResponse>> getReportingTenders() {
        List<ReportingTendersDataResponse> responseList = dashboardApiService.getReportingTenders();
        return new ApiResponse<>(responseList);
    }

//     @ActivityLog(name = "IN-STORE_DATA")
//    @RequestMapping(value = "/data", method = RequestMethod.POST)
//    public ApiResponse<DashboardDataResponse> dashboardDataHandler(@Validated @RequestBody DashboardDataRequest request) {
//        log.info(request.toString());
//        DashboardDataResponse response = dashboardApiService.getDashboardDataResponse(request);
//        HashMap<String, HashMap<String, String>> tidMappingList = dashboardApiService.missingTIDMapping();
//        response.getTenderWiseDataList().forEach(tenderWiseData -> tenderWiseData.getBankWiseDataList().
//                forEach(bankWiseData -> bankWiseData.setMissingTidValue(tidMappingList.get(tenderWiseData.getTenderName()).get(bankWiseData.getBankName()))));
//        for (Map.Entry<String, HashMap<String, String>> mapEntry : tidMappingList.entrySet()) {
//            String tender = mapEntry.getKey();
//            Optional<TenderWiseData> tenderWiseDataOptional = response.getTenderWiseDataList().stream().filter(k -> k.getTenderName().equalsIgnoreCase(tender)).findAny();
//            TenderWiseData tenderWiseData;
//            if (tenderWiseDataOptional.isEmpty()) {
//                tenderWiseData = new TenderWiseData();
//                tenderWiseData.setTenderName(tender);
//                tenderWiseData.setBankWiseDataList(new ArrayList<>());
//                response.getTenderWiseDataList().add(tenderWiseData);
//            } else {
//                tenderWiseData = tenderWiseDataOptional.get();
//            }
//            for (Map.Entry<String, String> mapEntry1 : mapEntry.getValue().entrySet()) {
//                String bank = mapEntry1.getKey();
//                BankWiseData bankWiseData;
//                Optional<BankWiseData> bankWiseDataOptional = tenderWiseData.getBankWiseDataList().stream().filter(k -> k.getBankName().equalsIgnoreCase(bank)).findAny();
//                if (bankWiseDataOptional.isEmpty()) {
//                    bankWiseData = new BankWiseData();
//                    bankWiseData.setBankName(bank);
//                    bankWiseData.setMissingTidValue(mapEntry1.getValue());
//                    tenderWiseData.getBankWiseDataList().add(bankWiseData);
//                }
//            }
//        }
//
//        double totalSale = 0;
//        for (TenderWiseData tenderWiseData : response.getTenderWiseDataList()) {
//            if (tenderWiseData.getTenderName().toUpperCase().equalsIgnoreCase("CARD")) {
//                double tenderCardTotalSales = 0;
//                for (BankWiseData bankWiseData : tenderWiseData.getBankWiseDataList()) {
//                    tenderCardTotalSales += bankWiseData.getSales();
//                }
//                tenderWiseData.setSales(tenderCardTotalSales);
//                totalSale += tenderCardTotalSales;
//            } else if (tenderWiseData.getTenderName().toUpperCase().equalsIgnoreCase("UPI")) {
//                double tenderUpiTotalSales = 0;
//                for (BankWiseData bankWiseData : tenderWiseData.getBankWiseDataList()) {
//                    tenderUpiTotalSales += bankWiseData.getSales();
//                }
//                tenderWiseData.setSales(tenderUpiTotalSales);
//                totalSale += tenderUpiTotalSales;
//            }
//        }
//        response.setSales(totalSale);
//
//        return new ApiResponse<>(response);
//    }
    @RequestMapping(value = "/instore-data", method = RequestMethod.POST)
    public ApiResponse<DashboardDataResponse> instoreDashboardDataHandler(@Validated @RequestBody DashboardDataRequest request) {
        log.info(request.toString());
        try {
            DashboardDataResponse response = dashboardApiService.getInstoreDashboardResponse(request);
            HashMap<String, HashMap<String, String>> tidMappingList = dashboardApiService.missingTIDMapping();
            if (response.getTenderWiseDataList() != null) {
                response.getTenderWiseDataList().forEach(tenderWiseData -> {
                    if (tenderWiseData != null && tenderWiseData.getBankWiseDataList() != null) {
                        tenderWiseData.getBankWiseDataList().forEach(bankWiseData -> {
                            if (bankWiseData != null) {
                                if (tidMappingList.get(tenderWiseData.getTenderName()) != null) {
                                    if (bankWiseData.getBankName() != null) {
                                        bankWiseData.setMissingTidValue(tidMappingList.get(tenderWiseData.getTenderName()).get(bankWiseData.getBankName()));
                                    }
                                }
                            }
                        });
                    }
                });
            }
            for (Map.Entry<String, HashMap<String, String>> mapEntry : tidMappingList.entrySet()) {
                String tender = mapEntry.getKey();
                Optional<TenderWiseData> tenderWiseDataOptional = response.getTenderWiseDataList().stream().filter(k -> k.getTenderName().equalsIgnoreCase(tender)).findAny();
                TenderWiseData tenderWiseData;
                if (tenderWiseDataOptional.isEmpty()) {
                    tenderWiseData = new TenderWiseData();
                    tenderWiseData.setTenderName(tender);
                    tenderWiseData.setBankWiseDataList(new ArrayList<>());
                    response.getTenderWiseDataList().add(tenderWiseData);
                } else {
                    tenderWiseData = tenderWiseDataOptional.get();
                }
                for (Map.Entry<String, String> mapEntry1 : mapEntry.getValue().entrySet()) {
                    String bank = mapEntry1.getKey();
                    BankWiseData bankWiseData;
                    Optional<BankWiseData> bankWiseDataOptional = tenderWiseData.getBankWiseDataList().stream().filter(k -> k.getBankName() != null && k.getBankName().equalsIgnoreCase(bank)).findAny();
                    if (bankWiseDataOptional.isEmpty()) {
                        bankWiseData = new BankWiseData();
                        bankWiseData.setBankName(bank);
                        bankWiseData.setMissingTidValue(mapEntry1.getValue());
                        tenderWiseData.getBankWiseDataList().add(bankWiseData);
                    }
                }
            }
            return new ApiResponse<>(response);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

//    @PostMapping(value = "/asyncDownload")
//    public ApiResponse<String> reportDownloadHandlerAsync(@Validated @RequestBody DashboardDataRequest request) throws IOException {
//        log.info(request.toString());
//        if (Objects.isNull(request.getReportType())) {
//            throw new ApiException("Please select report type");
//        }
//        boolean requested = reportGenerationService.downloadInStoreOrThreePoReport(request);
//        
//        if (requested) {
//            return new ApiResponse<>("Report Generation Submitted");
//        }
//        return new ApiResponse<>("Report Generation Failed. Try Again");
//    }
//    @ActivityLog(name = "IN-STORE_DATA")
//    @RequestMapping(value = "/data-new", method = RequestMethod.POST)
//    public ApiResponse<DashboardInStoreDataResponse> dashboardDataHandlerNew(@Validated @RequestBody DashboardDataRequest request) {
//        log.info(request.toString());
//        DashboardInStoreDataResponse response = dashboardApiService.getDashboardInStoreDataResponse(request);
//        return new ApiResponse<>(response);
//    }
//
//    @RequestMapping(value = "/order/vs/trm", method = RequestMethod.POST)
//    public ApiResponse<DashboardDataResponse> orderVsTrmDataHandler(@Validated @RequestBody DashboardDataRequest request) {
//        log.info(request.toString());
//        DashboardDataResponse response = orderApiService.getOrderVsTrmData(request);
//        return new ApiResponse<>(response);
//    }
//
//    @RequestMapping(value = "/trm/vs/mpr", method = RequestMethod.POST)
//    public ApiResponse<DashboardDataResponse> trmVsMprDataHandler(@Validated @RequestBody DashboardDataRequest request) {
//        log.info(request.toString());
//        DashboardDataResponse response = trmApiService.getTrmVsMprData(request);
//        return new ApiResponse<>(response);
//    }
//
//    @RequestMapping(value = "/mpr/vs/bank", method = RequestMethod.POST)
//    public ApiResponse<DashboardDataResponse> mprVsBankDataHandler(@Validated @RequestBody DashboardDataRequest request) {
//        log.info(request.toString());
//        DashboardDataResponse response = mprApiService.getMprVsBankData(request);
//        return new ApiResponse<>(response);
//    }
    //@ActivityLog(name = "IN-STORE_REPORT_DOWNLOAD")
//    @RequestMapping(value = "/download", method = RequestMethod.POST)
//    public ResponseEntity<byte[]> reportDownloadHandler(@Validated @RequestBody DashboardDataRequest request) throws IOException {
//        log.info(request.toString());
//        if (Objects.isNull(request.getReportType())) {
//            throw new ApiException("Please select report type");
//        }
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            dashboardApiService.reportDownload(request, outputStream);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
//            headers.setContentDispositionFormData("attachment", request.getDownloadFileName());
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(outputStream.toByteArray());
//        }
//    }
//    @RequestMapping(value = "/getMprBankDifference", method = RequestMethod.POST)
//    public ApiResponse<List<MprBankDifference>> mprBankDifferenceHandler(@Validated @RequestBody DashboardDataRequest request) {
//        List<MprBankDifference> response = mprApiService.getMprBankDifference(request);
//        return new ApiResponse<>(response);
//    }
//    @RequestMapping(value = "/zomato", method = RequestMethod.POST)
//    public ApiResponse<List<Zomato>> ZomatoHandler(@Validated @RequestBody DashboardDataRequest request) {
//        List<Zomato> response = zomatoRepository.findByOrderDateBetween(request.getStartLocalDate(), request.getEndLocalDate());
//        return new ApiResponse<>(response);
//    }
    @RequestMapping(value = "/missingTIDMapping", method = RequestMethod.GET)
    public ApiResponse<HashMap<String, HashMap<String, String>>> missingTIDMapping() throws IOException {
        HashMap<String, HashMap<String, String>> response = dashboardApiService.missingTIDMapping();
        return new ApiResponse<>(response);
    }

    @RequestMapping(value = "/missingTIDMappingReportDownload", method = RequestMethod.POST)
    public ResponseEntity<byte[]> missingTIDReportDownload(@Validated @RequestBody MissingTIDReportRequest request) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String filename = request.getTender() + "_" + request.getBank() + "_mappings.xlsx";
            dashboardApiService.downloadMissingTIDReport(request, outputStream);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        }
    }
}
