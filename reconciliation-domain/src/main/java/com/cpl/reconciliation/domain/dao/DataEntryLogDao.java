package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.DataEntryLog;

import java.time.LocalDate;
import java.util.List;


public interface DataEntryLogDao {

    void save(DataEntryLog dataEntryLog);

    List<DataEntryLog> findDataByDateAndDataSource(String dataSource, LocalDate localDate, LocalDate endDate);
}
