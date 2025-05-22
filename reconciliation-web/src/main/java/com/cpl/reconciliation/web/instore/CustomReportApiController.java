package com.cpl.reconciliation.web.instore;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.core.common.annotations.ActivityLog;
import com.cpl.reconciliation.core.request.CustomOrderDataRequest;
import com.cpl.reconciliation.domain.entity.CustomisedReportFilter;
import com.cpl.reconciliation.web.service.CustomReportApiService;
import com.opencsv.CSVWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Data
@Slf4j
@RestController
@RequestMapping("/public/custom")
public class CustomReportApiController {
    private final CustomReportApiService customReportApiService;

    @RequestMapping(value = "/reportFields", method = RequestMethod.GET)
    public ApiResponse<List<CustomisedReportFilter>> getCustomReportFieldFilters(@RequestParam("category") String category) {
        List<CustomisedReportFilter> response = customReportApiService.getCustomisedReportFields(category);
        return new ApiResponse<>(response);
    }

    @ActivityLog(name = "CUSTOM_REPORT_DOWNLOAD")
    @PostMapping("/download/report")
    public void download(@Validated @RequestBody CustomOrderDataRequest request, HttpServletResponse response) {

        try {
            response.setContentType("text/csv");
            String filename = "CustomisedReport" + request.getStartDate() + "_" + request.getEndDate() + ".csv";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            PrintWriter writer = response.getWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            customReportApiService.getCustomisedData(request, csvWriter);
            csvWriter.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
