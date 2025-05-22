package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MprData {
    
    private String id;
    private String paymentType;
    private String bank;
    private String tid;
    private String mid;
    private String storeId;
    private double mprAmount;
    private double commission;
    private double settledAmount;
    private String rrnNumber;
    private String transactionDate;
    private String settlementDate;

    //For UPI
    private String customerVPA;
    //For CARD
    private String cardNo;
    private String cardType;
    private String cardIssuer;
    private String cardNetwork;
    private String approvalCode;
    //
    private boolean bsMatched;
}
