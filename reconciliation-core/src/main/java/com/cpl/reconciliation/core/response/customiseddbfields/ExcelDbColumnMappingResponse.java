package com.cpl.reconciliation.core.response.customiseddbfields;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExcelDbColumnMappingResponse {

    private String dbColumnName;
    private String excelColumnName;
    private String description;
}
