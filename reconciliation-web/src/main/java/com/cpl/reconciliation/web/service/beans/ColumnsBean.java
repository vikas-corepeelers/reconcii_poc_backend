package com.cpl.reconciliation.web.service.beans;

import lombok.Data;

/*
 * @author Abhishek N
 */
@Data
public class ColumnsBean {

    String dbcolumnName;
    String excelColumnName;

    public ColumnsBean(String dbcolumnName, String excelColumnName) {
        this.dbcolumnName = dbcolumnName;
        this.excelColumnName = excelColumnName;
    }
}
