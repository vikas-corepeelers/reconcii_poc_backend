package com.cpl.reconciliation.core.modal.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConsolidatedTransaction {

    private String storeId;
    private String tenderName;
    private String invoiceNumber;
    private String businessDate;
    private double totalAmount;
    private double totalTax;
    //
    private String orderId;
    private String transactionId;
    private String rrn;
    private String source;
    private String acquirerBank;
    private String transactionDate;
    //
    private double orderAmount;
    private double trmAmount;
    private double mprAmount;
    private double mprCommission;
    private double bankValue;

}
