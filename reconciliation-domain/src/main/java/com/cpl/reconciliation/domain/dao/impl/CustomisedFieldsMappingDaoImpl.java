package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.request.ExcelDbColumnMappingRequest;
import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
import com.cpl.reconciliation.domain.entity.CustomisedFieldsMapping;
import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service
public class CustomisedFieldsMappingDaoImpl implements CustomisedFieldsMappingDao {

    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;

    @Override
    public List<CustomisedFieldsMapping> getAllCustomisedFieldsByDataSource(String ds) {
        return customisedFieldsMappingRepository.findByDataSource(ds);
    }

    @Override
    public Map<String, String> getActualAndCustomisedFieldsMapByDataSource(DataSource datasource) {
        List<CustomisedFieldsMapping> customisedFieldsMappingList = customisedFieldsMappingRepository.findByDataSource(String.valueOf(datasource));
        return customisedFieldsMappingList.stream()
                .collect(Collectors.toMap(
                        CustomisedFieldsMapping::getExcelColumnName,
                        CustomisedFieldsMapping::getDbColumnName,
                        (existingValue, newValue) -> existingValue
                ));
    }

    @Override
    public void updateExcelDbMapping(ExcelDbColumnMappingRequest excelDbColumnMappingRequest) {
        DataSource dataSource = excelDbColumnMappingRequest.getDataSource();
        List<CustomisedFieldsMapping> customisedFieldMappings = excelDbColumnMappingRequest.getMapping().stream().map(mapping -> {
            CustomisedFieldsMapping customisedFieldsMapping = new CustomisedFieldsMapping();
            customisedFieldsMapping.setExcelColumnName(mapping.getExcelColumnName());
            customisedFieldsMapping.setDbColumnName(mapping.getDbColumnName());
            customisedFieldsMapping.setDataSource(dataSource.name());
            customisedFieldsMapping.setTenderName(excelDbColumnMappingRequest.getTender()); 
            customisedFieldsMapping.setId(mapping.getExcelColumnName() + "|" + mapping.getDbColumnName() + "|" + dataSource.name());
            return customisedFieldsMapping;
        }).collect(Collectors.toList());
        customisedFieldsMappingRepository.saveAll(customisedFieldMappings);
    }
}
