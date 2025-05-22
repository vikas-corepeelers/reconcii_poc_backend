package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.domain.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PineLabsCardTRM extends BaseEntity {

    private int serialNumber;
    private String storeName;
    private String city;
    private String pos;
    private String hardwareModel;
    private String acquirer;
    private String tid;
    private String mid;
    private String batchNumber;
    private String cardNumber;
    private String name;
    private String cardIssuer;
    private String cardType;
    private String cardNetwork;
    private String cardColour;
    private String transactionId;
    private String invoice;
    private String approvalCode;
    private String transactionType;
    private double amount;
    private double tipAmount;
    private String currency;
    private LocalDateTime date;
    private String status;
    private LocalDateTime settlementDate;

    // 4 fields
    private double productAmount;
    private String insurer;
    private String plan;
    private double insuranceAmount;


    private String cashier;
    private String billInvoice;
    private String rrn;
    private String emiTxn;
    private String emiMonth;
    private String contactless;
    private String contactlessMode;
    private String cloudRefId;
    private String cardPanCheck;
    private String routePreauthToOtherAcquirer;
    private String billingTransactionId;
    private String merchantInput1;
    private String merchantInput2;
    private String merchantInput3;

}