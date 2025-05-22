package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.HDFCTidChargesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface HDFCTidChargesRepository extends JpaRepository<HDFCTidChargesEntity, Long> {

    @Query(value = "SELECT COALESCE(creditCard,0) as creditCard FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findCdtRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(cmrclOnusRate,0) as cmrclOnusRate FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findCommercialRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(dinersComm,0) as dinersComm FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findDinersRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(creditCard,0) as creditCard FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findDomCdtRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(foreignComm,0) as foreignComm FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findForeignCardRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(ddCommSlb2Above2k,0) as ddCommSlb2Above2k FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findDomDbtRateByDtTIdAbove2k(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(ddCommSlb1Below2k,0) as ddCommSlb1Below2k FROM HDFCTidChargesEntity h where h.effectiveFrom <= :transactionDate and h.effectiveTo > :transactionDate and h.tid = :terminalId")
    Double findDomDbtRateByDtTIdBelow2k(LocalDate transactionDate, String terminalId);
}
