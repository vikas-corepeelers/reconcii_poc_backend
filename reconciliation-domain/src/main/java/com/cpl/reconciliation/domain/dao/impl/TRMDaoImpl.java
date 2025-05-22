package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.TRMDao;
import com.cpl.reconciliation.domain.entity.TRMEntity;
import com.cpl.reconciliation.domain.repository.TRMRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class TRMDaoImpl implements TRMDao {

    private final TRMRepository trmRepository;

    @Override
    public void saveAll(List<TRMEntity> trmEntities) {
        trmRepository.saveAll(trmEntities);
    }

    @Override
    public void save(TRMEntity entity) {
        trmRepository.save(entity);
    }
}
