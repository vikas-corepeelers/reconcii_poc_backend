package com.cpl.reconciliation.domain.util;

public class Constants {
    public static String MERCHANT_CANCELLED = "Merchant Cancelled";
    public static String ORDER_NOT_FOUND_IN_POS = "Order Not found in POS";
    public static String ORDER_NOT_FOUND_IN_THREE_PO = "Order Not found in 3PO";

    public static String POS_AMOUNT_GREATER = "Order Amount Mismatch (POS>3PO)";
    public static String THREE_PO_AMOUNT_GREATER = "Order Amount Mismatch (3PO>POS)";
    public static String RECEIVABLE_MISMATCH_POS_GREATER = "Short Payment";
    public static String RECEIVABLE_MISMATCH_THREE_PO_GREATER = "Excess Payment";
    public static String ROUNDING_OFF = ", due to rounding off";

    public static String PACK_CHARGE_MISMATCH = "Packaging Charge Mismatch";
    public static String TDS_MISMATCH = "TDS Mismatch";
    public static String COMMISSION_MISMATCH = "Commission Mismatch";
    public static String PG_MISMATCH = "PG Charges Mismatch";
    public static String COMMISSION_PG_BOTH_MISMATCH = "Commission PG Charges Both Mismatch";

    public static String GST_CHARGES_MISMATCH = "GST on charges Mismatch";
    public static String CONSUMER_GST_MISMATCH = "Consumer GST Mismatch";
    

    // TRM vs MPR Reasons
    public static String TXN_NOT_FOUND_IN_MPR = "Txn not found in MPR";
    public static String TXN_NOT_FOUND_IN_TRM = "Txn not found in TRM";
    public static String MPR_AMT_GREATER = "Amount MPR > TRM";
    public static String TRM_AMT_GREATER = "Amount TRM > MPR";


    // POS vs TRM
    public static String TXN_NOT_FOUND_IN_POS = "Txn not found in POS";
    public static String POS_AMT_GREATER_THAN_TRM = "POS Amount > TRM Amount";
    public static String TRM_AMT_GREATER_THAN_POS = "TRM Amount > POS Amount";

}
