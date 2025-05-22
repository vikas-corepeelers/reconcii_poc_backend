package com.cpl.reconciliation.core.enums;

import lombok.Getter;

@Getter
public enum DataSource {

    POS_ORDERS_STLD(false, "IN-Store", "POS Orders STLD", "STLD", ""),
    POS_ORDERS(false, "IN-Store", "POS Orders", "Posist Order Level Data", "orders"),
    PayTm_TRM(true, "IN-Store", "PayTM", "TRM", "trm"),
    PayTm_MPR(true, "IN-Store", "PayTM", "MPR", "mpr"),
    PineLabs_TRM_CARD(false, "IN-Store", "PineLabs", "TRM CARD", "trm"),
    PineLabs_TRM_UPI(false, "IN-Store", "PineLabs", "TRM UPI", "trm"),
    PineLabs_TRM(false, "IN-Store", "PineLabs", "TRM", "trm"),
    SBI_MPR(true, "IN-Store", "SBI", "MPR", "mpr"),
    AMEX_MPR(true, "IN-Store", "AMEX", "MPR", "mpr"),
    HDFC_MPR(true, "IN-Store", "HDFC", "MPR", "mpr"),
    PHONEPE_MPR(true, "IN-Store", "PHONEPE", "MPR", "mpr"),
    YESBANK_MPR(true, "IN-Store", "YES Bank", "MPR", "mpr"),
    ICICI_MPR_CARD(true, "IN-Store", "ICICI", "MPR CARD", "mpr"),
    ICICI_MPR_UPI(true, "IN-Store", "ICICI", "MPR UPI", "mpr"),
    ICICI_REFUND(true, "IN-Store", "ICICI", "Refund", "icici_refunds"),
    ICICI_REFUND_DEEMED(true, "IN-Store", "ICICI", "Deemed Refund", "icici_refunds"),
    SBI_BS(true, "IN-Store", "SBI", "Bank Statement", "bank_statements"),
    HDFC_BS(true, "IN-Store", "HDFC", "Bank Statement", "bank_statements"),
    ICICI_BS(true, "IN-Store", "ICICI", "Bank Statement", "bank_statements"),
    YESBANK_BS(true, "IN-Store", "YES Bank", "Bank Statement", "bank_statements"),
    HSBC_BS(true, "IN-Store", "HSBC Bank", "Bank Statement", "bank_statements"),
    GLOBAL(false, "3PO", "Global", "Global", "global_data"),
    SWIGGY(true, "3PO", "Swiggy", "Order Level Data", "swiggy"),
    SWIGGY_PROMO(true, "3PO", "Swiggy", "Promo", "swiggy_promo"),
    ZOMATO(true, "3PO", "Zomato", "Order Level Data", "zomato"),
    MAGICPIN(true, "3PO", "Magicpin", "Order Level Data", "magicpin"),
    ZOMATO_SALT(true, "3PO", "Zomato", "Salt", "zomato_salt"),
    ZOMATO_UTR(true, "3PO", "Zomato", "UTR", "zomato_utr"),
    MAGICPIN_UTR(true, "3PO", "Magicpin", "UTR", "magicpin_utr"),
    BUDGET_MASTER(true, "3PO", "Budget Master", "Budget Master", "budget_master"),
    PROMO_MASTER(true, "3PO", "Promo Master", "Promo Master", "promo_master"),
    FREEBIES_MASTER(true, "3PO", "Freebies Master", "Freebies Master", "freebiew_master"),
    SWIGGY_IGCC(true, "3PO", "Swiggy", "Order Level Data", "swiggy_igcc"),
    ZOMATO_PROMO(true, "3PO", "Zomato", "Order Level Data", "zomato_promo"),
    HDFC_TID_CHARGES(true, "3PO", "HDFC", "Order Level Data", "hdfc_tid_charges"),
    SBI_TID_CHARGES(true, "3PO", "SBI", "Order Level Data", "sbi_tid_charges"),
    /*For internal use*/
    INSTORE_DASHBOARD(true, "IN-Store", "INSTORE_DASHBOARD", "Dashboard Data", ""),
    THREEPO_DASHBOARD(true, "IN-Store", "THREEPO_DASHBOARD", "Dashboard Data", ""),
    ZOMATO_POS_ORDERS(false, "IN-Store", "Zomato", "Posist Order Level Data", "orders"),
    SWIGGY_POS_ORDERS(false, "IN-Store", "Swiggy", "Posist Order Level Data", "orders"),
    MAGICPIN_POS_ORDERS(false, "IN-Store", "Magicpin", "Posist Order Level Data", "orders");
    
    private final String tender;
    private final String type;
    private final String dbTable;
    private final boolean manualUpload;
    private final String category;

    DataSource(boolean manualUpload, String category, String tender, String type, String dbTable) {
        this.manualUpload = manualUpload;
        this.category = category;
        this.tender = tender;
        this.type = type;
        this.dbTable = dbTable;
    }

    public boolean isManualUpload() {
        return manualUpload;
    }
}
