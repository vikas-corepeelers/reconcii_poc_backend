package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.BankStatementDao;
import com.cpl.reconciliation.domain.entity.BankStatement;
import com.cpl.reconciliation.domain.repository.BankStatementRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class BankStatementDaoImpl implements BankStatementDao {

    private final BankStatementRepository bankStatementRepository;

    @Override
    public void saveAll(List<BankStatement> bankStatements) {
        bankStatementRepository.saveAll(bankStatements);
    }

}
