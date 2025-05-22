package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.ThreePoDashBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThreePoDashboardRepository extends JpaRepository<ThreePoDashBoardEntity, Long> {
}
