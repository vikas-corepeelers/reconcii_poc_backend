package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrmData extends SaleData {

    private String source;
    private String acquirerBank;
    private String tid;
    private String mid;
    private String transactionId;
    private String transactionDate;
    private String settlementDate;
    private String mprCommonKey;
    //For UPI
    private String customerVPA;
    //For CARD
    private String cardNo;
    private String cardType;
    private String cardIssuer;
    private String cardNetwork;
    private String approvalCode;
}
