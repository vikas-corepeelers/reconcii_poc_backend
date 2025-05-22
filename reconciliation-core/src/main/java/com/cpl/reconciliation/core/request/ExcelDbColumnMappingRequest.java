package com.cpl.reconciliation.core.request;

import com.cpl.reconciliation.core.enums.DataSource;
import com.cpl.reconciliation.core.modal.ExcelDbColumnMapping;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ExcelDbColumnMappingRequest {
    private DataSource dataSource;
    private String tender;
    private List<ExcelDbColumnMapping> mapping;
}
