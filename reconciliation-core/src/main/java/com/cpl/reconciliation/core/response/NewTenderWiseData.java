package com.cpl.reconciliation.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class NewTenderWiseData {
    private String tenderName;
    private List<BankWiseData> bankWiseDataList;
    protected double sales;
    protected long salesCount;
    protected double receipts;
    protected long receiptsCount;
    protected double reconciled;
    protected long reconciledCount;
    protected double difference;
    protected long differenceCount;
    //
    protected double charges;
    protected double booked;
    //
    protected double posVsTrm;
    protected double trmVsMpr;
    protected double mprVsBank;

    @JsonProperty(value = "unreconciled")
    public double getUnreconciled() {
        return sales-reconciled;
    }
}
