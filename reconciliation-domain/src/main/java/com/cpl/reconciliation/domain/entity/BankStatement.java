package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bank_statements")
@EntityListeners(AuditingEntityListener.class)
public class BankStatement implements Serializable {

    @Id
    protected String id;

    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;
    private LocalDateTime date;
    private String narration;
    private String chqRefNo;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime valueDate;
    private Double withdrawalAmt;
    private Double depositAmt;
    private Double closingBalance;
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;
    private Integer branchCode;
    private String transactionId;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime transactionPostedDate;
  //  @Enumerated(value = EnumType.STRING)
    private String bank;
 //   @Enumerated(value = EnumType.STRING)
    private String sourceBank;
    private String accountNumber;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime expectedActualTransactionDate;
    private String accountName;
    private String currency;
    private String location;
    private String iban;
    private String accountStatus;
    private String accountType;
    private Double closingLedgerBalance;
    private LocalDate closingLedgerBroughtForwardFrom;
    private Double closingAvailableBalance;
    private LocalDate closingAvailableBroughtForwardFrom;
    private Double CurrentLedgerBalance;
    private Double currentAvailableBalance;
    private LocalDateTime currentLedgerAsAt;
    private LocalDateTime currentAvailableAsAt;
    private String customerReference;
    private String trnType;
    private String Time;
}


