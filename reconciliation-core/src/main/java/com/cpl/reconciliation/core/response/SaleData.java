package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaleData {

    protected String bank;
    protected String paymentType="";//1
    protected String businessDate="";//2
    protected String orderDate="";//3
    protected String posId="";//4
    protected String storeId="";//5
    protected String saleType="";//6
    protected String invoiceNumber="";//7
    protected String rrnNumber="";//8
    protected double saleAmount;//9
    protected double saleTax;//10
    protected double tenderAmount;//11
}
