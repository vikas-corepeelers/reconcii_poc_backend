package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.HDFCTidChargesEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HDFCTidChargesDao {
    List<HDFCTidChargesEntity> getAll();

    void saveAll(List<HDFCTidChargesEntity> hdfcTidChargesEntities);
}
