package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.HDFCTidChargesDao;
import com.cpl.reconciliation.domain.entity.HDFCTidChargesEntity;
import com.cpl.reconciliation.domain.repository.HDFCTidChargesRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Slf4j
@Service
public class HDFCTidChargesDaoImpl implements HDFCTidChargesDao {

    private final HDFCTidChargesRepository hdfcTidChargesRepository;

    @Override
    public List<HDFCTidChargesEntity> getAll() {
        return hdfcTidChargesRepository.findAll();
    }

    @Override
    public void saveAll(List<HDFCTidChargesEntity> hdfcTidChargesEntities) {
        hdfcTidChargesRepository.saveAll(hdfcTidChargesEntities);
    }


}
