package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.request.ExcelDbColumnMappingRequest;
import com.cpl.reconciliation.domain.entity.CustomisedFieldsMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CustomisedFieldsMappingApiService {

    List<CustomisedFieldsMapping> getAllCustomisedFieldsByDataSource(String ds);

    String findDistinctTenderNamesByDataSource(String dataSource);

    void uploadCustomisedFieldsMapping(MultipartFile file, DataSource dataSource);

    Map<String, String> getActualAndCustomisedFieldsMapByDataSource(String datasource);

    void updateExcelDbMapping(ExcelDbColumnMappingRequest excelDbColumnMappingRequest);
}
