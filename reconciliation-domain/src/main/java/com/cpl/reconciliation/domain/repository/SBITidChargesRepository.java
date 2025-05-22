package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.SBITidChargesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface SBITidChargesRepository extends JpaRepository<SBITidChargesEntity, Long> {
    @Query(value = "SELECT COALESCE(intnlMdrRate,0) as intnlMdrRate FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findIntRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(offusDebitMdrRateGreater2000Amt,0) as offusDebitMdrRateGreater2000Amt FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findOffUsDbtDomRateByDtTIdAmtGt2k(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(offusDebitMdrRateLess2000Amt,0) as offusDebitMdrRateLess2000Amt FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findOffUsDbtDomRateByDtTIdAmtLt2k(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(offusCreditMdr,0) as offusCreditMdr FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findOffUsCrdRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(onusCreditMdr,0) as onusCreditMdr FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findOnUsCrdRateByDtTId(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(onusDebitMdrRateGreater2000Amt,0) as onusDebitMdrRateGreater2000Amt FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findOnUsDbtDomRateByDtTIdAmtGt2k(LocalDate transactionDate, String terminalId);

    @Query(value = "SELECT COALESCE(onusDebitMdrRateLess2000Amt,0) as onusDebitMdrRateLess2000Amt FROM SBITidChargesEntity s where s.effectiveFrom <= :transactionDate and s.effectiveTo > :transactionDate and s.terminalId = :terminalId")
    Double findOnUsDbtDomRateByDtTIdAmtLt2k(LocalDate transactionDate, String terminalId);
}
