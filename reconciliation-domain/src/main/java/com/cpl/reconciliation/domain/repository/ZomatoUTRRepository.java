package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.ZomatoUTREntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZomatoUTRRepository extends JpaRepository<ZomatoUTREntity, String> {
}
