package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.BankStatement;

import java.util.List;

public interface BankStatementDao {
    void saveAll(List<BankStatement> bankStatements);
}
