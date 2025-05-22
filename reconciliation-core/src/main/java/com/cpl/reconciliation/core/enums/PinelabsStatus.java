package com.cpl.reconciliation.core.enums;

public enum PinelabsStatus {

    PTRM_00("Success"),
    PTRM_10("Unexpected Error"),
    PTRM_13("Last Requested Change No:"),
    PTRM_12("Invalid userName or security Token"),
    PTRM_57("Invalid Request/Tags");

    private String description;

    PinelabsStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
