package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.HDFCCardCharges;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping
public interface HDFCCardChargesRepository extends JpaRepository<HDFCCardCharges, Long> {
}
