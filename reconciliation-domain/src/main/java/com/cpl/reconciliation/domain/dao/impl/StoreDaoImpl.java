package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.domain.dao.StoreDao;
import com.cpl.reconciliation.domain.repository.StoreRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Data
@Slf4j
@Service
public class StoreDaoImpl implements StoreDao {

    private final StoreRepository storeRepository;
}
