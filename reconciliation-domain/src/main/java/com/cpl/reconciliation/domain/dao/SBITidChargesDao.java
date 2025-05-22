package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.domain.entity.SBITidChargesEntity;

import java.util.List;

public interface SBITidChargesDao {
    List<SBITidChargesEntity> getAll();

    void saveAll(List<SBITidChargesEntity> sbiTidChargesEntities);
}
