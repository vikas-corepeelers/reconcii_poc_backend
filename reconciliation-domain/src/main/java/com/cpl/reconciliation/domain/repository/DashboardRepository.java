package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.DashBoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<DashBoardEntity, Long> {
}
