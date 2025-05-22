package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.CustomisedFieldsMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface CustomisedFieldsMappingRepository extends JpaRepository<CustomisedFieldsMapping, Long> {

    CustomisedFieldsMapping findByDataSourceAndDbColumnNameAndExcelColumnNameAndClientName(String dataSource, String dbColumnName, String excelColumnName, String clientName);

    List<CustomisedFieldsMapping> findByDataSource(String datasource);

    @Query("SELECT DISTINCT c.tenderName FROM CustomisedFieldsMapping c WHERE c.tenderName is not null AND c.dataSource = :dataSource")
    String findDistinctTenderNamesByDataSource(@Param("dataSource") String dataSource);
}
