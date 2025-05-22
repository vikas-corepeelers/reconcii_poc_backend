package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.PromoMasterDao;
import com.cpl.reconciliation.domain.entity.PromoMasterEntity;
import com.cpl.reconciliation.domain.repository.PromoMasterRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class PromoMasterDaoImpl implements PromoMasterDao {

    private final PromoMasterRepository promoMasterRepository;

    @Override
    public List<PromoMasterEntity> getAll() {
        return promoMasterRepository.findAll();
    }

    @Override
    public void saveAll(List<PromoMasterEntity> promoMasterEntities) {
        promoMasterRepository.saveAll(promoMasterEntities);
    }

}
