package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.VoucherConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface VoucherConfigEntityRepository extends JpaRepository<VoucherConfigEntity,Long> {
}
