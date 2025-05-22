package com.cpl.reconciliation.core.response.customiseddbfields;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CustomisedFieldsMappingResponse {

    private String dataSource;
    private String tender;
    private List<ExcelDbColumnMappingResponse>mapping;
    private List<String> dbColumns;
    private List<String> excelColumns;
}
