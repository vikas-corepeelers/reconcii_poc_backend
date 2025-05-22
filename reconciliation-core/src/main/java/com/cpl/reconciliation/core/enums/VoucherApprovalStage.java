package com.cpl.reconciliation.core.enums;

public enum VoucherApprovalStage {
    CREATED,
    PENDING,
    APPROVED,
    REJECTED,
    UNKNOWN;

    public static VoucherApprovalStage get(String value) {
        if (value.equalsIgnoreCase("CREATED")) return CREATED;
        if (value.equalsIgnoreCase("PENDING")) return PENDING;
        if (value.equalsIgnoreCase("APPROVED")) return APPROVED;
        if (value.equalsIgnoreCase("REJECTED")) return REJECTED;
        return UNKNOWN;
    }
}
