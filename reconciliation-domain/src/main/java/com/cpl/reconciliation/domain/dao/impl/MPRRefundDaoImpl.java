package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.MPRRefundDao;
import com.cpl.reconciliation.domain.entity.MPRRefundEntity;
import com.cpl.reconciliation.domain.repository.MPRRefundRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class MPRRefundDaoImpl implements MPRRefundDao {

    private final MPRRefundRepository mprRefundRepository;

    @Override
    public void saveAll(List<MPRRefundEntity> iciciRefundReports) {
        mprRefundRepository.saveAll(iciciRefundReports);
    }

}
