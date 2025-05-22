package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleDataWrapper {

    private double totalSaleAmt;
    private double totalTaxAmt;
    private double totalTenderAmt;
    private List<SaleData> saleDataList;
}
