package com.cpl.reconciliation.domain.dao;

import com.cpl.reconciliation.core.modal.query.MprBankDifference;
import com.cpl.reconciliation.domain.entity.MPREntity;

import java.util.List;

public interface MPRDao {
    void saveAll(List<MPREntity> mprEntities);

    List<MPREntity> findMPR(String bank, String startDate, String endDate, String tender, int booked);

    List<MprBankDifference> getMprBankDifference(String bank, String tender, String startDate, String endDate);

    void updateStoreTidFromTRMICICIUPI();
}
