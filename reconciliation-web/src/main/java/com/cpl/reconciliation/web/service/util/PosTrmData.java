package com.cpl.reconciliation.web.service.util;

import lombok.Getter;
import lombok.Setter;

import static com.cpl.reconciliation.domain.util.Constants.*;

@Getter
@Setter
public class PosTrmData {

    private String businessDate;
    private String orderDate;
    private String invoiceNumber;
    private String storeID;
    private String posID;
    private String tenderName;
    private double totalAmount;
    private String saleType;
    private double tenderAmount;
    private String tenderRRN;
    private String transactionID;
    private String acquirerBank;
    private String authCode;
    private String cardNumber;
    private String cardType;
    private String networkType;
    private String customerVPA;
    private String MID;
    private String paymentType;
    private String trmRRN;
    private String source;
    private String TID;
    private String transactionStatus;
    private String settlementDate;
    private double trmAmount;
    private double amountDifference;
    private String remarks;

    public String getRemarks() {
        if (getTransactionID() == null) {
            return TXN_NOT_FOUND_IN_TRM;
        } else if (getInvoiceNumber() == null) {
            return TXN_NOT_FOUND_IN_POS;

        } else if (getTrmAmount() > getTenderAmount()) {
            return TRM_AMT_GREATER_THAN_POS;

        } else if (getTenderAmount() > getTrmAmount()) {
            return POS_AMT_GREATER_THAN_TRM;
        }
        return "";
    }
}
