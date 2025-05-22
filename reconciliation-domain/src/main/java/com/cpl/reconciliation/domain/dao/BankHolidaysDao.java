package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.BankHolidays;

import java.util.List;

public interface BankHolidaysDao {
    List<BankHolidays> getAll();
}
