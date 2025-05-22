package com.cpl.reconciliation.web.service.beans;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/*
 * @author Abhishek N
 */
@Getter
@Setter
public class TableAndColumnsBean {

    String dataSourceName;
    String tableName;
    List<ColumnsBean> columns;
}
