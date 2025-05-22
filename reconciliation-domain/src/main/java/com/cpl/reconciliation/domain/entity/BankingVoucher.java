package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.enums.VoucherApprovalStage;
import com.cpl.reconciliation.core.request.VoucherType;
import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "banking_vouchers")
public class BankingVoucher extends BaseEntity {

   @JsonManagedReference
   @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "bankingVoucher", cascade = CascadeType.ALL)
   List<BankingVoucherEntries> bankingVoucherEntriesList;
   @Enumerated(value = EnumType.STRING)
   @Column(length = 10)
   private PaymentType paymentType;
  // @Enumerated(value = EnumType.STRING)
   @Column(length = 15)
   private String bank;
   @Enumerated(value = EnumType.STRING)
   @Column(nullable = false)
   private VoucherApprovalStage approvalStage = VoucherApprovalStage.CREATED;

   @Convert(converter = LocalDateConverter.class)
   @JsonFormat(pattern = "dd MMM yyyy")
   private LocalDate startDate;
   @Convert(converter = LocalDateConverter.class)
   @JsonFormat(pattern = "dd MMM yyyy")
   private LocalDate endDate;

   private boolean booked;
   @Enumerated(value = EnumType.STRING)
   private VoucherType voucherType;

}
