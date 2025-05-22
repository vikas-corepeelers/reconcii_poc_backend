package com.cpl.reconciliation.core.response.threepo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GeneratedReportResponse {
    protected Double sales;
    protected Double receipts;
    protected Double charges;
    protected Double reconciled;
    protected Double booked;
    protected Double difference;
    protected Integer n_matched;
}
