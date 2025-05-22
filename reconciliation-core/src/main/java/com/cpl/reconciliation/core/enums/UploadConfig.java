package com.cpl.reconciliation.core.enums;

public enum UploadConfig {

    StoreMaster("Store Master"),
    StoreTIDMapping("Store TID Mapping"),
    AmexStoreNameMapping("Amex StoreName Mapping"),
    SwiggyStoreMapping("Swiggy Store Mapping"),
    ZomatoStoreMapping("Zomato Store Mapping"),
    MagicPinStoreMapping("MagicPin Store Mapping"),
    SBICardCharges("SBI Card Charges"),
    HDFCardCharges("HDFC Card Charges"),
    AMEXCardCharges("AMEX Card Charges"),
    ICICICardCharges("ICICI Card Charges"),
    VOUCHERCONFIG("Voucher Config values");
    private String uiValue;
    UploadConfig(String uiValue) {
        this.uiValue = uiValue;
    }

    public String getUiValue() {
        return uiValue;
    }
}