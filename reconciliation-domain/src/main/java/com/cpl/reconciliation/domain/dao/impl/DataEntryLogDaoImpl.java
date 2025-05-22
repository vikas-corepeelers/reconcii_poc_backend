package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.DataEntryLogDao;
import com.cpl.reconciliation.domain.entity.DataEntryLog;
import com.cpl.reconciliation.domain.repository.DataEntryLogRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Data
@Slf4j
@Service
public class DataEntryLogDaoImpl implements DataEntryLogDao {
    private final DataEntryLogRepository dataEntryLogRepository;

    @Override
    public void save(DataEntryLog dataEntryLog) {
        dataEntryLogRepository.save(dataEntryLog);
    }

    @Override
    public List<DataEntryLog> findDataByDateAndDataSource(String dataSource, LocalDate localDate, LocalDate endDate) {
        return dataEntryLogRepository.findDataByDateAndDataSource(dataSource, localDate, endDate);
    }
}
