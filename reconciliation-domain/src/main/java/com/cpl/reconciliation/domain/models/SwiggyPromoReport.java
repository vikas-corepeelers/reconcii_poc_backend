package com.cpl.reconciliation.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Locale;

@Getter
@Setter
public class SwiggyPromoReport extends ThreePOReport {
    private int dow;

    private String rid;

    private String month;

    private String orderId;

    private int hourOfDay;

    private LocalDate date;

    private String brandName;

    private String couponCode;

    private String userType;

    private String userCohort;

    private double freebieDiscount;

    private double discountTotal;

    private double gmv;

    private String remarks;

    private DayOfWeek day;

    private String storeCode;

    private String freebieItem;

    private double freebieCost;

    private double freebieSalePrice;

    private String images;
    private String reason;

    public boolean isFreebie() {
        return remarks != null && remarks.toLowerCase(Locale.ROOT).contains("freebie");
    }

}

