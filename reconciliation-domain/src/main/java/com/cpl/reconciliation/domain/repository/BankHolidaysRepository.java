package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.BankHolidays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankHolidaysRepository extends JpaRepository<BankHolidays, Long> {
}
