package com.cpl.reconciliation.core.modal;

import com.cpl.reconciliation.core.enums.DataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ExcelDbColumnMapping {
    private String excelColumnName;
    private String dbColumnName;
}
