package com.cpl.reconciliation.core.enums;

public enum Ledger {
    GL_ACCOUNT("G/L Account"),
    BANK_ACCOUNT("Bank Account"),
    CUSTOMER("Customer");

    private final String name;

    Ledger(String name) {
        this.name = name;
    }

    public String getValue() {
        return name;
    }

    public static Ledger valueOfLedger(String label) {
        for (Ledger e : values()) {
            if (e.name.equals(label)) {
                return e;
            }
        }
        return null;
    }
}
