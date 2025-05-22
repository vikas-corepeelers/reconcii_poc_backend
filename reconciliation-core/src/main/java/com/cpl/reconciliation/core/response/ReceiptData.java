package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReceiptData {

    private String paymentType="";
    private String acquirerBank="";
    private String depositBank="";
    private double depositAmount;
    private String valueDate="";
    private String narration="";
}
