package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.ZomatoUTRDao;
import com.cpl.reconciliation.domain.entity.ZomatoUTREntity;
import com.cpl.reconciliation.domain.repository.ZomatoUTRRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
public class ZomatoUTRDaoImpl implements ZomatoUTRDao {
    private final ZomatoUTRRepository zomatoUTRRepository;

    @Override
    public void saveAll(List<ZomatoUTREntity> zomatoUTREntityEntities) {
        zomatoUTRRepository.saveAll(zomatoUTREntityEntities);
    }
}
