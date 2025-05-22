package com.cpl.reconciliation.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Setter
public class ZomatoPromoReport extends ThreePOReport {

    private LocalDate aggregation;
    private String tabId;
    private String resId;
    private String entityName;
    private String brandName;
    private String accountType;
    private String promoCode;
    private String psSegment;
    private double zvdFinal;
    private double zvd;
    private double mvd;
    private double offlineRecon;
    private double control;
    private double burn;
    private double total;
    private double net;
    private double construct;
    private double commission;
    private double pg;
    private double gstOnCommission;
    private double gstOnPg;
    private double finalAmount;

    private String storeCode;
    private DayOfWeek dayOfWeek;
    private String freebieItem;
    private double freebieCost;
    private double freebieSalePrice;


}

