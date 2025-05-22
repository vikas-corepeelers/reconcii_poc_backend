package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UnreconciledDataWrapper {

    private double totalSaleAmt = 0.0;
    private double totalTaxAmt = 0.0;
    private double totalTenderAmt = 0.0;
    private double totalTRMAmt = 0.0;
    private double totalMPRAmt = 0.0;
    private List<UnreconciledData> unreconciledDataList;
}
