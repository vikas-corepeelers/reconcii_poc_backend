package com.cpl.reconciliation.core.response.threepo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Data {
    protected double posSales;
    protected double posReceivables;
    protected double posCommission;
    protected double posCharges;
    protected double posFreebies;
    protected double posDiscounts;

    protected double threePOSales;
    protected double threePOReceivables;
    protected double threePOCommission;
    protected double threePOCharges;
    protected double threePOFreebies;
    protected double threePODiscounts;

    protected double reconciled;
    protected double posVsThreePO;

    protected double receivablesVsReceipts;


    protected double booked;

    protected double promo;
    protected double deltaPromo;
    protected String businessDate;

    @JsonProperty(value = "allThreePOCharges")
    public double getAllThreePOCharges() {
        return threePOCharges + threePOFreebies + threePODiscounts + threePOCommission;
    }

    @JsonProperty(value = "allPOSCharges")
    public double getAllPOSCharges() {
        return posCharges + posFreebies + posDiscounts + posCommission;
    }

}
