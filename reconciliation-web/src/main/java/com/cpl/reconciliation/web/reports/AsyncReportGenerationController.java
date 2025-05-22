package com.cpl.reconciliation.web.reports;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.request.GeneratedReportRequest;
import com.cpl.reconciliation.core.request.GeneratedReportResponse;
import com.cpl.reconciliation.web.service.impl.ReportGenerationServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Data
@Slf4j
@RestController
@RequestMapping("/public/generated-reports")
public class AsyncReportGenerationController {
    private final ReportGenerationServiceImpl reportGenerationService;

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public ResponseEntity<byte[]> downloadGeneratedReport(@RequestParam("id") long id) {
        try {
            String[] report = reportGenerationService.getGeneratedReportPathAndName(id);
            Path path = Paths.get(report[0]);
            byte[] fileContent = Files.readAllBytes(path);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", report[1]);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/getAll", method = RequestMethod.POST)
    public ApiResponse<List<GeneratedReportResponse>> getAllGeneratedReports
            (@RequestBody GeneratedReportRequest request) {
        List<GeneratedReportResponse> reportResponses = reportGenerationService.getAllGeneratedReports(request);
        return new ApiResponse<>(reportResponses);
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public ApiResponse<GeneratedReportResponse> getGeneratedReport(@RequestParam("id") long id) {
        GeneratedReportResponse reportResponse = reportGenerationService.getGeneratedReport(id);
        return new ApiResponse<>(reportResponse);
    }


}
