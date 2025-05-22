package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.request.ExcelDbColumnMappingRequest;
import com.cpl.reconciliation.domain.entity.CustomisedFieldsMapping;

import java.util.List;
import java.util.Map;

public interface CustomisedFieldsMappingDao {
    List<CustomisedFieldsMapping> getAllCustomisedFieldsByDataSource(String ds);

    Map<String, String> getActualAndCustomisedFieldsMapByDataSource(DataSource datasource);

    void updateExcelDbMapping(ExcelDbColumnMappingRequest excelDbColumnMappingRequest);
}
