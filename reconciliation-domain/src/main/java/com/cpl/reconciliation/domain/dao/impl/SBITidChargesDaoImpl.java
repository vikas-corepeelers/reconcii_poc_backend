package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.SBITidChargesDao;
import com.cpl.reconciliation.domain.entity.SBITidChargesEntity;
import com.cpl.reconciliation.domain.repository.SBITidChargesRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class SBITidChargesDaoImpl implements SBITidChargesDao {

    private final SBITidChargesRepository sbiTidChargesRepository;

    @Override
    public List<SBITidChargesEntity> getAll() {
        return sbiTidChargesRepository.findAll();
    }

    @Override
    public void saveAll(List<SBITidChargesEntity> sbiTidChargesEntities) {
        sbiTidChargesRepository.saveAll(sbiTidChargesEntities);
    }
}
