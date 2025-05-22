package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.*;
import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "voucher_entries")
public class BankingVoucherEntries extends BaseEntity{

//   @EmbeddedId
//   private VoucherEntryId voucherEntryId;
   private String version;
   @Column(columnDefinition="DOUBLE default '0.0'")
   private double amount =0.0;
   private String narration;
   private String chequeNo;

   @Convert(converter = LocalDateConverter.class)
   @JsonFormat(pattern = "dd MMM yyyy")
   private LocalDate date;


   @Enumerated(value = EnumType.STRING)
   @Column(length = 10)
   private PaymentType paymentType;

 //  @Enumerated(value = EnumType.STRING)
   @Column(length = 15)
   private String bank;

   @Column
   private String storeCode;

   @Enumerated(value = EnumType.STRING)
   @Column(length = 15)
   private Ledger ledger;

   @Enumerated(value = EnumType.STRING)
   @Column(length = 2)
   private DC dc;

   @Enumerated(value = EnumType.STRING)
   private EntryType entryType;

   @ManyToOne
   @JoinColumn(name = "voucher_id")
   @JsonBackReference
   private BankingVoucher bankingVoucher;
}
