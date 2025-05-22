package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.GeneratedReports;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GeneratedReportsRepository extends JpaRepository<GeneratedReports, Long> {
    @Query(value = "SELECT g FROM GeneratedReports g where (g.added between :startDate and :endDate) " +
            " AND (:tender is null OR g.tender=:tender)" +
            " AND (:reportType is null OR g.reportType=:reportType)  " +
            " AND (:bank is null OR g.bank=:bank) AND (g.createdBy =:username) order by g.added desc")
    List<GeneratedReports> getReports(LocalDateTime startDate, LocalDateTime endDate, String tender, String reportType, String bank,String username);
}
