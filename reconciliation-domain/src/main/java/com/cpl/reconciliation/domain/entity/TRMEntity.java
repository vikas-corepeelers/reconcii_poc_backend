package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.*;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
        (name = "trm",
        indexes = {
                @Index(name = "uid", columnList = "uid"),
                @Index(name = "transactionDate", columnList = "transactionDate,storeId"),
        })
public class TRMEntity implements Serializable {

    @Id
    private String transactionId;
    private String posId;
    private String storeId;
    private String orderId;
    private double trmAmount;
    //
//    @Enumerated(value = EnumType.STRING)
    private String acquirerBank;
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;
    private String rrn;
    private String mid;
    private String tid;
    // Card related Fields
    private String authCode;
    @Enumerated(value = EnumType.STRING)
    private CardType cardType;
    private String cardNumber;
    private String networkType;
    // UPI Fields
    private String customerVPA;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime transactionDate;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime settlementDate;
    @Enumerated(value = EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(value = EnumType.STRING)
    private TransactionStatus transactionStatus;
    @Enumerated(value = EnumType.STRING)
    private TRMSource source;
    @Column
    private String uid;
    @Column
    private int isReversed;
    //TODO need to remove these two fields
    private String status;
    private String transcType;
    private String zone;
    private String storeName;
    private String city;
    private String hardwareModel;
    private String hardwareId;
    private String batchNo;
    private String cardIssuer;
    private String cardColor;
    private String invoice;
    private String approvalCode;
    private String currency;
    private String billInvoice;
    private String emiTxn;
    private String emiMonth;
    private String contactless;
    private String contactlessMode;
    private String cloudRefId;
    private String cardPanCheck;
    private String routePreauthToAcquirer;
    private String billingTransId;
    private String name;
    private Double tipAmount;
}


