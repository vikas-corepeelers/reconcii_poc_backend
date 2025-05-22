package com.cpl.reconciliation.domain.entity.enums;

public enum StoreType {

    DELIVERY_HIGHWAY,
    DELIVERY_NON_HIGHWAY,
    DINING,
    UNKNOWN;


    public static StoreType getEnum(String value) {
        if (DELIVERY_HIGHWAY.name().equalsIgnoreCase(value)) return DELIVERY_HIGHWAY;
        else if (DELIVERY_NON_HIGHWAY.name().equalsIgnoreCase(value)) return DELIVERY_NON_HIGHWAY;
        else if (DINING.name().equalsIgnoreCase(value)) return DINING;
        return UNKNOWN;
    }
}
