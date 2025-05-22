package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.ManualUploads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManualUploadingRepository extends JpaRepository<ManualUploads, Long> {

}
