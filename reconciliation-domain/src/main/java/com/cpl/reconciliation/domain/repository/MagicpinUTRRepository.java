package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.MagicpinUTREntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MagicpinUTRRepository extends JpaRepository<MagicpinUTREntity, String> {
}
