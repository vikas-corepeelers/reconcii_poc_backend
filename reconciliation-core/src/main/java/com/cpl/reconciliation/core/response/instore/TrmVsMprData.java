package com.cpl.reconciliation.core.response.instore;

import com.cpl.reconciliation.core.response.TrmData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrmVsMprData extends TrmData {

    private String mprId;
    private String mprPaymentType;
    private String mprBank;
    private String mprTid;
    private String mprMid;
    private String mprStoreId;
    private double mprAmount;
    private double mprCommission;
    private double mprSettleAmount;
    private String mprBankRRN;
    private String mprTransactionDate;
    private String mprSettlementDate;
    //MPR UPI
    private String mprCustomerVPA;
}
