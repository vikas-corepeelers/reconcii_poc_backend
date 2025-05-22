package com.cpl.reconciliation.core.enums;

public enum PaymentType {
    CARD,
    UPI,
    PPI,
    NET_BANKING,
    PAYTM_DIGITAL_CREDIT,
    UNKNOWN,
    TRANSFER;

    public static PaymentType getPaymentType(String value) {
        if (value != null) {
            if (value.toUpperCase().contains("CARD") || value.toUpperCase().contains("C")) return CARD;
            else if (value.toUpperCase().contains("UPI") || value.toUpperCase().contains("U")) return UPI;
            else if (value.toUpperCase().contains("PPI")) return PPI;
            else if (value.toUpperCase().contains("NET_BANKING")) return NET_BANKING;
            else if (value.toUpperCase().contains("PAYTM_DIGITAL_CREDIT")) return PAYTM_DIGITAL_CREDIT;
            else if (value.toUpperCase().contains("UNKNOWN")) return UNKNOWN;
            else if (value.toUpperCase().contains("TRANSFER")) return TRANSFER;
        }
        return null;
    }
}
