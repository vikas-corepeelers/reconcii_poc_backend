package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.ZomatoPromoDao;
import com.cpl.reconciliation.domain.entity.ZomatoPromoEntity;
import com.cpl.reconciliation.domain.repository.ZomatoPromoRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Slf4j
@Service
public class ZomatoPromoDaoImpl implements ZomatoPromoDao {

    private final ZomatoPromoRepository zomatoPromoRepository;

    @Override
    public List<ZomatoPromoEntity> getAll() {
        return zomatoPromoRepository.findAll();
    }

    @Override
    public void saveAll(List<ZomatoPromoEntity> zomatoPromoEntities) {
        zomatoPromoRepository.saveAll(zomatoPromoEntities);
    }


}
