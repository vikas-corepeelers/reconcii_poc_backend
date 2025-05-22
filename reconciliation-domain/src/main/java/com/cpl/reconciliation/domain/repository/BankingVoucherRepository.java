package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.request.VoucherType;
import com.cpl.reconciliation.domain.entity.BankingVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BankingVoucherRepository extends JpaRepository<BankingVoucher, Long> {
    @Query("SELECT v FROM BankingVoucher v where ((v.startDate between :startDate AND :endDate) OR (v.endDate between :startDate AND :endDate)) AND (v.bank=:bank) AND " +
            "( v.paymentType=:paymentType and v.voucherType=:voucherType)")
    List<BankingVoucher> findByDateBetweenAndBankAndPaymentTypeAndVoucherType(
            LocalDate startDate, LocalDate endDate, String bank,
            PaymentType paymentType, VoucherType voucherType);

}
