package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.BankHolidaysDao;
import com.cpl.reconciliation.domain.entity.BankHolidays;
import com.cpl.reconciliation.domain.repository.BankHolidaysRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class BankHolidaysDaoImpl implements BankHolidaysDao {

    private final BankHolidaysRepository bankHolidaysRepository;

    @Override
    public List<BankHolidays> getAll() {
        return bankHolidaysRepository.findAll();
    }

}
