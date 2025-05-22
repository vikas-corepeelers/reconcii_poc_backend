package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.core.api.util.StringUtils;
import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
import com.cpl.reconciliation.domain.repository.StoreTIDMappingRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@Slf4j
@Service
public class StoreTIDMappingDaoImpl implements StoreTIDMappingDao {

    private final StoreTIDMappingRepository storeTIDMappingRepository;

    @Override
    public Optional<StoreTIDMapping> findByTid(String tid) {
        if(StringUtils.isEmpty(tid)) {
            log.error("TID is null");
            return Optional.empty();
        }
        return storeTIDMappingRepository.findByTid(tid);
    }

    @Override
    public Optional<StoreTIDMapping> findByTidLike(String tid) {
        if(StringUtils.isEmpty(tid)) return Optional.empty();
        return storeTIDMappingRepository.findByTidLike(tid);
    }

    @Override
    public String getStoreCodeByMid(String mid) {
        if(StringUtils.isEmpty(mid)) {
            log.error("MID is null");
            return null;
        }
        return storeTIDMappingRepository.getStoreCodeByMid(mid);
    }
}
