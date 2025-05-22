package com.cpl.reconciliation.web.service.impl;

import com.cpl.core.api.exception.ApiException;
import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.request.ExcelDbColumnMappingRequest;
import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
import com.cpl.reconciliation.domain.entity.CustomisedFieldsMapping;
import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
import com.cpl.reconciliation.web.service.CustomisedFieldsMappingApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Slf4j
@Service
public class CustomisedFieldsMappingApiServiceImpl implements CustomisedFieldsMappingApiService {
    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;

    @Override
    public List<CustomisedFieldsMapping> getAllCustomisedFieldsByDataSource(String datasource) {
        return customisedFieldsMappingRepository.findByDataSource(datasource);
    }

    @Override
    public void uploadCustomisedFieldsMapping(MultipartFile file, DataSource dataSource) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
                Row row = sheet.getRow(i);
                Row headerRow = sheet.getRow(0);
                CustomisedFieldsMapping customisedFieldsMapping = new CustomisedFieldsMapping();
                String excelColumName = null;
                String dbColumnName = null;
                String clientName = null;
                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = cell == null ? null : cell.getStringCellValue().trim();
                    Cell headerCell = headerRow.getCell(j);
                    String headerCellValue = headerCell == null ? null : headerCell.getStringCellValue().trim();
                    if (headerCellValue != null) {
                        switch (headerCellValue) {
                            case "db_column_name" -> dbColumnName = cellValue;
                            case "excel_column_name" -> excelColumName = cellValue;
                            case "client_name" -> clientName = cellValue;
                        }
                    } else {
                        continue;
                    }

                }
                CustomisedFieldsMapping customisedFieldsMappingObject = customisedFieldsMappingRepository.findByDataSourceAndDbColumnNameAndExcelColumnNameAndClientName(dataSource.name(), dbColumnName, excelColumName, clientName);
                if (customisedFieldsMappingObject != null) {
                    customisedFieldsMapping = customisedFieldsMappingObject;
                } else {
                    customisedFieldsMapping.setDbColumnName(dbColumnName);
                    customisedFieldsMapping.setExcelColumnName(excelColumName);
                    customisedFieldsMapping.setDataSource(dataSource.name());
                    customisedFieldsMapping.setClientName(clientName);
                }
                try {
                    customisedFieldsMappingRepository.save(customisedFieldsMapping);
                } catch (Exception e) {
                    log.error("Exception occurred while inserting customised field Mappings: {}", e);
                }
            }
        } catch (Exception e) {
            log.error("Error while uploading customised field mapping", e);
            throw new ApiException(e.getMessage());
        }

    }

    @Override
    public Map<String, String> getActualAndCustomisedFieldsMapByDataSource(String datasource) {
        List<CustomisedFieldsMapping> customisedFieldsMappingList = customisedFieldsMappingRepository.findByDataSource(datasource);
        return customisedFieldsMappingList.stream().collect(Collectors.toMap(CustomisedFieldsMapping::getExcelColumnName, CustomisedFieldsMapping::getDbColumnName, (existingValue, newValue) -> existingValue));
    }

    @Override
    public void updateExcelDbMapping(ExcelDbColumnMappingRequest excelDbColumnMappingRequest) {
        customisedFieldsMappingDao.updateExcelDbMapping(excelDbColumnMappingRequest);
    }

    @Override
    public String findDistinctTenderNamesByDataSource(String dataSource) {
         String distinctTenderList = customisedFieldsMappingRepository.findDistinctTenderNamesByDataSource(dataSource);
         return distinctTenderList;
    }
}


