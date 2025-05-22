package com.cpl.reconciliation.core.enums;

public enum EntryType {
    SALES,
    REFUNDS,
    COMMISSION,
    BANK,
    TRANSFER;

    public static EntryType get(String value) {
        if (value.equalsIgnoreCase("SALES")) return SALES;
        if (value.equalsIgnoreCase("REFUNDS")) return REFUNDS;
        if (value.equalsIgnoreCase("COMMISSION")) return COMMISSION;
        if (value.equalsIgnoreCase("BANK")) return BANK;
        if (value.equalsIgnoreCase("TRANSFER")) return TRANSFER;
        return null;
    }

}
