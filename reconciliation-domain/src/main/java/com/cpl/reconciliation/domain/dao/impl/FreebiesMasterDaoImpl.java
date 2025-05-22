package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.FreebiesMasterDao;
import com.cpl.reconciliation.domain.entity.FreebiesMasterEntity;
import com.cpl.reconciliation.domain.repository.FreebiesMasterRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class FreebiesMasterDaoImpl implements FreebiesMasterDao {

    private final FreebiesMasterRepository freebiesMasterRepository;

    @Override
    public List<FreebiesMasterEntity> getAll() {
        return freebiesMasterRepository.findAll();
    }

    @Override
    public void saveAll(List<FreebiesMasterEntity> freebiesMasterEntities) {
        freebiesMasterRepository.saveAll(freebiesMasterEntities);
    }

}
