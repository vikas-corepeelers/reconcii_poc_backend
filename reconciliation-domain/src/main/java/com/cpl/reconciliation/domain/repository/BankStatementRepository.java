package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, Long> {
}
