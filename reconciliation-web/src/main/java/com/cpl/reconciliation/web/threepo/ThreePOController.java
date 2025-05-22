package com.cpl.reconciliation.web.threepo;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.core.response.threepo.DataResponse;
import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;
import com.cpl.reconciliation.web.service.ThreePoApiService;
import com.cpl.reconciliation.web.service.impl.ReportGenerationServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Data
@Slf4j
@RestController
@RequestMapping("/public/threepo")
public class ThreePOController {

    private final ThreePoApiService threePoApiService;
    private final ReportGenerationServiceImpl reportGenerationService;

    @RequestMapping(value = "/threePODashboardData", method = RequestMethod.POST)
    public ApiResponse<DataResponse> threePODashboardDataHandler(@Validated @RequestBody ThreePODataRequest request) {
        DataResponse response = threePoApiService.getThreePoDashboardDataResponse(request);
        return new ApiResponse<>(response);
    }

//    @ActivityLog(name = "3PO_DATA")
//    @RequestMapping(value = "/dashboardData", method = RequestMethod.POST)
//    public ApiResponse<DataResponse> dashboardDataHandler(@Validated @RequestBody ThreePODataRequest request) {
//        log.info(request.toString());
//        DataResponse response = threePoApiService.getDashboardDataResponse(request);
//        return new ApiResponse<>(response);
//    }

//    @ActivityLog(name = "3PO_REPORT_DOWNLOAD")
//    @RequestMapping(value = "/dashboardData/download", method = RequestMethod.POST)
//    public ResponseEntity<byte[]> reportDownloadHandler(@Validated @RequestBody ThreePODataRequest request) throws IOException {
//        log.info(request.toString());
//        if (Objects.isNull(request.getReportType())) {
//            throw new ApiException("Please select report type");
//        }
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            String filename = request.getFileName();
//            threePoApiService.reportDownload(request, outputStream);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
//            headers.setContentDispositionFormData("attachment", filename);
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(outputStream.toByteArray());
//        }
//    }
//
//    @RequestMapping(value = "/dashboardData/asyncDownload", method = RequestMethod.POST)
//    public ApiResponse<String> reportDownloadHandlerAsync(@Validated @RequestBody ThreePODataRequest request) throws IOException {
//        log.info(request.toString());
//        if (Objects.isNull(request.getReportType())) {
//            throw new ApiException("Please select report type");
//        }
//        boolean requested = reportGenerationService.downloadThreePOReport(request);
//        if (requested) {
//            return new ApiResponse<>("Report Generation Submitted");
//        }
//        return new ApiResponse<>("Report Generation Failed. Try Again");
//    }
//
//    @ActivityLog(name = "3PO_DATA")
//    @RequestMapping(value = "/pos/vs/3po", method = RequestMethod.POST)
//    public ApiResponse<GeneratedReportResponse> getPOSvs3POResponse(@Validated @RequestBody ThreePODataRequest request) {
//        log.info(request.toString());
//        GeneratedReportResponse response = threePoApiService.getPOSvs3POResponse(request);
//        return new ApiResponse<>(response);
//    }

    @RequestMapping(value = "/missingStoreMappings", method = RequestMethod.GET)
    public ApiResponse<List<MissingThreePOMapping>> getThreePoMissingMapping() {
        List<MissingThreePOMapping> response = threePoApiService.getThreePoMissingMapping();
        return new ApiResponse<>(response);
    }

    @RequestMapping(value = "/missingStoreMappings/download/{threepo}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getThreePoMissingMapping(@RequestParam("threepo") String threepo) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String filename = threepo + "_mappings.xlsx";
            threePoApiService.downloadThreePoMissingMapping(threepo, outputStream);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        }
    }

//    @ActivityLog(name = "3PO_REPORT_DOWNLOAD")
//    @RequestMapping(value = "/pos/vs/3po/download", method = RequestMethod.POST)
//    public ResponseEntity<byte[]> psoVs3PODownloadHandler(@Validated @RequestBody ThreePODataRequest request) throws IOException {
//        log.info(request.toString());
//        if (Objects.isNull(request.getReportType())) {
//            throw new ApiException("Please select report type");
//        }
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            String filename = "POSvs3PO" + "_" + request.getStartDate() + "_" + request.getEndDate() + ".xlsx";
//            threePoApiService.getPOSvs3PODownload(request, outputStream);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
//            headers.setContentDispositionFormData("attachment", filename);
//            return ResponseEntity.ok()
//                    .headers(headers)
//                    .body(outputStream.toByteArray());
//        }
//    }
}
