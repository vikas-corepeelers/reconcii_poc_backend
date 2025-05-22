package com.cpl.reconciliation.web.reports;

import com.cpl.core.api.exception.ApiException;
import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.web.service.impl.ReportGenerationServiceImpl;
import java.io.IOException;
import java.util.Objects;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/*
 * @author Abhishek N
 */
@Data
@Slf4j
@RestController
@RequestMapping("/public/dashboard")
public class ReportsController {

    private final ReportGenerationServiceImpl reportGenerationService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostMapping(value = "/asyncDownload")
    public ApiResponse<String> reportDownloadHandlerAsync(@Validated @RequestBody DashboardDataRequest request) throws IOException {
        log.info(request.toString());
        if (Objects.isNull(request.getReportType())) {
            throw new ApiException("Please select report type");
        }
        boolean requested;
        if (request.getTender()!=null && (request.getTender().equalsIgnoreCase("Zomato") || request.getTender().equalsIgnoreCase("Swiggy")
                || request.getTender().equalsIgnoreCase("Magicpin"))) {
            ThreePODataRequest threePOrequest = new ThreePODataRequest();
            if (request.getTender().equalsIgnoreCase("Zomato")) {
                threePOrequest.setTender(ThreePO.ZOMATO);
            } else if (request.getTender().equalsIgnoreCase("Swiggy")) {
                threePOrequest.setTender(ThreePO.SWIGGY);
            } else if (request.getTender().equalsIgnoreCase("Magicpin")) {
            }
            threePOrequest.setReportType(request.getReportType());
            threePOrequest.setStores(request.getStores());
            threePOrequest.setStartDate(request.getStartDate());
            threePOrequest.setEndDate(request.getEndDate());
            requested = reportGenerationService.downloadThreePOReport(threePOrequest);
        } else {
            requested = reportGenerationService.downloadInStoreReport(request);
        }
        if (requested) {
            return new ApiResponse<>("Report Generation Submitted");
        }
        return new ApiResponse<>("Report Generation Failed. Try Again");
    }

//    public Map<String, String> getColumnDetails(String tableName) {
//        String query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE "
//                + "TABLE_NAME = ? AND TABLE_SCHEMA = 'subway'";
//        Map<String, String> columnDetailsMap = new ConcurrentHashMap<>();
//        jdbcTemplate.query(query, (rs, rowNum) -> {
//            String columnName = rs.getString("COLUMN_NAME");
//            String dataType = rs.getString("DATA_TYPE");
//            columnDetailsMap.put(columnName, dataType);
//            return null;
//        }, new Object[]{tableName});
//
//        return columnDetailsMap;
//    }
}
