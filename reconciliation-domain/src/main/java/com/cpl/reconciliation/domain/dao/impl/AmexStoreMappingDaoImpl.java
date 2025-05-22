package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.AmexStoreMappingDao;
import com.cpl.reconciliation.domain.entity.AmexStoreMapping;
import com.cpl.reconciliation.domain.repository.AmexStoreMappingRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@Slf4j
@Service
public class AmexStoreMappingDaoImpl implements AmexStoreMappingDao {

    private final AmexStoreMappingRepository amexStoreMappingRepository;

    @Override
    public String findStoreCodeByAmexStoreName(String amexStoreName) {
        Optional<AmexStoreMapping> optional = amexStoreMappingRepository.findByAmexStoreName(amexStoreName);
        if (optional.isPresent()) {
            return optional.get().getStoreCode();
        }
        return null;
    }
}
