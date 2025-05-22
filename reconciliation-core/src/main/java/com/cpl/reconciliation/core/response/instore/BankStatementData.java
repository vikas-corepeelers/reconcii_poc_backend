package com.cpl.reconciliation.core.response.instore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BankStatementData {

    private String paymentType;
    private String bank;
    private String mprBank;
    private String depositDate;
    private String settlementDate;
    private double depositAmount;
    private double closingBalance;
    private String narration;
}
