package com.cpl.reconciliation.core.modal.query;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MprBankDifference {

    private String settlementDate;
    private String bank;
    private String tender;
    private long mprCount;
    private double mprAmount;
    private double bankAmount;
    private double refundAmount;
    private double difference;
}
