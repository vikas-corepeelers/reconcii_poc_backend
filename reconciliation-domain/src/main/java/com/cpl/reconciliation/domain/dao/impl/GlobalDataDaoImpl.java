package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.GlobalDataDao;
import com.cpl.reconciliation.domain.entity.GlobalEntity;
import com.cpl.reconciliation.domain.repository.GlobalDataRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
@Data
@Slf4j
@Service
public class GlobalDataDaoImpl implements GlobalDataDao {
    private final GlobalDataRepository globalDataRepository;

    @Override
    public void saveAll(List<GlobalEntity> globalEntities){
        globalDataRepository.saveAll(globalEntities);
    }
}
