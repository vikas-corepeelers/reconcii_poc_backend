package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.core.enums.*;
import com.cpl.reconciliation.domain.entity.BankingVoucherEntries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BankingVoucherEntriesRepository extends JpaRepository<BankingVoucherEntries, Long> {

    @Query("SELECT v FROM BankingVoucherEntries v where v.date between :startDate and :endDate AND (v.bank=:bank) AND " +
            "(v.paymentType=:paymentType) and v.bankingVoucher is null")
    List<BankingVoucherEntries> findByDateBetweenAndBankAndPaymentTypeWhereVoucherIsNull(
            LocalDate startDate, LocalDate endDate, String bank,
            PaymentType paymentType);

    @Query("SELECT e FROM BankingVoucherEntries e WHERE e.date between :startDate AND :endDate" +
            " AND e.bank=:bank and e.paymentType=:paymentType AND (e.bankingVoucher.approvalStage=:approvalStage) AND (:entryType is null or e.entryType=:entryType)" +
            " ")
    List<BankingVoucherEntries> findByDateBetweenAndBankAndPaymentTypeAndLedger(
            LocalDate startDate, LocalDate endDate, String bank,
            PaymentType paymentType, EntryType entryType, VoucherApprovalStage approvalStage);

    @Query("SELECT e FROM BankingVoucherEntries e WHERE e.date between :startDate AND :endDate" +
            " AND e.bank=:bank and e.paymentType=:paymentType " +
            " AND e.ledger=:ledger and e.dc=:dc")
    List<BankingVoucherEntries> findByDateBetweenAndBankAndPaymentTypeAndLedgerAndDc(
            LocalDateTime startDate, LocalDateTime endDate, String bank,
            PaymentType paymentType, Ledger ledger, DC dc);


    @Query("SELECT e FROM BankingVoucherEntries e WHERE e.id IN :ids")
    List<BankingVoucherEntries> findAllByVoucherEntryId(@Param("ids") List<Long> ids);

    @Query("SELECT e FROM BankingVoucherEntries e WHERE e.bankingVoucher.id=:id")
    List<BankingVoucherEntries> findAllByVoucherId(@Param("id") Long id);

//    @Query("SELECT e FROM BankingVoucherEntries e WHERE e.voucherEntryId=:entryId")
//    BankingVoucherEntries findByCompositeId(VoucherEntryId entryId);

//    @Transactional
//    @Modifying
//    @Query(value = "UPDATE BankingVoucherEntries e SET e.voucherEntryId.voucherCreated=1,e.voucherEntryId.voucherHash=:hash where e.bankingVoucher=:bankingVoucher")
//    void updateVoucherCreatedFlag(BankingVoucher bankingVoucher, int hash);
}
