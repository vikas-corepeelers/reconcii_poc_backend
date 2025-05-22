package com.cpl.reconciliation.domain.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TransactionRequest {

    private String userName;
    private String password;
    //CARD
    private String maxChangeNo;
    //UPI
    private String reportType;
    private String maxChangeNoForPC;
}
