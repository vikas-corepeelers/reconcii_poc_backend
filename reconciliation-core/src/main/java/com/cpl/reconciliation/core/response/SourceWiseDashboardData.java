package com.cpl.reconciliation.core.response;

import com.cpl.reconciliation.core.response.TenderWiseData;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class SourceWiseDashboardData {
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

    private List<NewTenderWiseData> tenderWiseDataList;

    @JsonProperty(value = "unreconciled")
    public double getUnreconciled() {
        return sales - reconciled;
    }
}
