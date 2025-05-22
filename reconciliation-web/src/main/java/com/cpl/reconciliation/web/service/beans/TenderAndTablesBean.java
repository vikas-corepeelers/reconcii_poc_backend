package com.cpl.reconciliation.web.service.beans;

import java.util.List;
import lombok.Data;
/*
 * @author Abhishek N
 */
@Data
public class TenderAndTablesBean {
    String tender;
    List<TableAndColumnsBean> dataSourceWiseColumns;
}
