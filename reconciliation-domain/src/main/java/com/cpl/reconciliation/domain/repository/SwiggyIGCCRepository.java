package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.SwiggyIGCCEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwiggyIGCCRepository extends JpaRepository<SwiggyIGCCEntity, Long> {
}
