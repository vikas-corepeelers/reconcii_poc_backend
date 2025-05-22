package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.SwiggyIGCCDao;
import com.cpl.reconciliation.domain.entity.SwiggyIGCCEntity;
import com.cpl.reconciliation.domain.repository.SwiggyIGCCRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class SwiggyIGCCDaoImpl implements SwiggyIGCCDao {

    private final SwiggyIGCCRepository swiggyIGCCRepository;

    @Override
    public List<SwiggyIGCCEntity> getAll() {
        return swiggyIGCCRepository.findAll();
    }

    @Override
    public void saveAll(List<SwiggyIGCCEntity> swiggyIGCCEntities) {
        swiggyIGCCRepository.saveAll(swiggyIGCCEntities);
    }

}
