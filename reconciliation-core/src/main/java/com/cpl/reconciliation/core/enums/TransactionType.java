package com.cpl.reconciliation.core.enums;

public enum TransactionType {
    SALE, VOID, REFUND, OTHER;

    public static TransactionType getTransactionType(String type) {
        if (type.toUpperCase().contains("SALE")) {
            return SALE;
        } else if (type.toUpperCase().contains("REFUND")) {
            return REFUND;
        } else if (type.toUpperCase().contains("VOID")) {
            return VOID;
        } else {
            return OTHER;
        }
    }
}
