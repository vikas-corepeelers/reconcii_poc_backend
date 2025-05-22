package com.cpl.reconciliation.web.customisedfields;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.core.common.annotations.ActivityLog;
import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.request.ExcelDbColumnMappingRequest;
import com.cpl.reconciliation.core.response.customiseddbfields.CustomisedFieldsMappingResponse;
import com.cpl.reconciliation.core.response.customiseddbfields.ExcelDbColumnMappingResponse;
import com.cpl.reconciliation.domain.entity.CustomisedFieldsMapping;
import com.cpl.reconciliation.web.service.CustomisedFieldsMappingApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@RestController
@RequestMapping("/api/ve1/customisedfields")
public class CustomisedFieldsController {

    private final CustomisedFieldsMappingApiService customisedFieldsMappingApiService;

    @ActivityLog(name = "GetExcelDbColumMapping")
    @RequestMapping(value = "/getExcelDbColumMappingByDataSource/{dataSource}", method = RequestMethod.GET)
    public ApiResponse<CustomisedFieldsMappingResponse> excelDbColumMapping(@PathVariable DataSource dataSource) {
        String tender = customisedFieldsMappingApiService.findDistinctTenderNamesByDataSource(dataSource.name());
        List<CustomisedFieldsMapping> customisedFieldsMappings = customisedFieldsMappingApiService.getAllCustomisedFieldsByDataSource(dataSource.name());
        List<String> dbColumns = customisedFieldsMappings.stream().map(x -> x.getDbColumnName()).collect(Collectors.toList());
        List<String> excelColumns = customisedFieldsMappings.stream().map(x -> x.getExcelColumnName()).collect(Collectors.toList());

        List<ExcelDbColumnMappingResponse> fieldsMapping = customisedFieldsMappings.stream()
                .map(mapping -> {
                    ExcelDbColumnMappingResponse excelDbColumnMappingResponse = new ExcelDbColumnMappingResponse();
                    excelDbColumnMappingResponse.setDbColumnName(mapping.getDbColumnName());
                    excelDbColumnMappingResponse.setExcelColumnName(mapping.getExcelColumnName());
                    excelDbColumnMappingResponse.setDescription(mapping.getDescription() == null ? "" : mapping.getDescription());
                    return excelDbColumnMappingResponse;
                })
                .collect(Collectors.toList());

        CustomisedFieldsMappingResponse customisedFieldsMappingResponse = new CustomisedFieldsMappingResponse();
        customisedFieldsMappingResponse.setDataSource(dataSource.name());
        customisedFieldsMappingResponse.setTender(tender);
        customisedFieldsMappingResponse.setDbColumns(dbColumns);
        customisedFieldsMappingResponse.setExcelColumns(excelColumns);
        customisedFieldsMappingResponse.setMapping(fieldsMapping);
        return new ApiResponse<>(customisedFieldsMappingResponse);
    }

    @ActivityLog(name = "UpdateExcelDbColumMapping")
    @RequestMapping(value = "/updateExcelDbColumMapping", method = RequestMethod.POST)
    public ApiResponse<String> customisedFieldsUploadHandler(@RequestBody ExcelDbColumnMappingRequest excelDbColumnMappingRequest) {
        customisedFieldsMappingApiService.updateExcelDbMapping(excelDbColumnMappingRequest);
        return new ApiResponse<>("Excel Db column Updated");
    }
}
