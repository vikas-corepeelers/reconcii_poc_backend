package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.PromoMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoMasterRepository extends JpaRepository<PromoMasterEntity, Long> {
}
