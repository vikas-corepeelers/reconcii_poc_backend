package com.cpl.reconciliation.domain.models;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class PineLabsUPITRM {
    @CsvBindByName(column = "Sr. No.")
    private int srNo;

    @CsvBindByName(column = "Store Name")
    private String storeName;

    @CsvBindByName(column = "POS Id")
    private String posId;

    @CsvBindByName(column = "Acquirer")
    private String acquirer;

    @CsvBindByName(column = "TID")
    private String tid;

    @CsvBindByName(column = "MID")
    private String mid;

    @CsvBindByName(column = "Account Type")
    private String accountType;

    @CsvBindByName(column = "Host Txn Id")
    private String hostTxnId;

    @CsvBindByName(column = "Transaction Id")
    private String transactionId;

    @CsvBindByName(column = "Original Transaction ID")
    private String originalTransactionId;

    @CsvBindByName(column = "Txn Id Prefix")
    private String txnIdPrefix;

    @CsvBindByName(column = "Edc Batch ID")
    private String edcBatchId;

    @CsvBindByName(column = "Edc Roc")
    private String edcRoc;

    @CsvBindByName(column = "Orig Edc Batch ID")
    private String origEdcBatchId;

    @CsvBindByName(column = "Orig Edc Roc")
    private String origEdcRoc;

    @CsvBindByName(column = "Txn Amt")
    private double txnAmt;

    @CsvBindByName(column = "Currency")
    private String currency;

    @CsvBindByName(column = "Txn Time")
    private String txnTime;

    @CsvBindByName(column = "Txn Type")
    private String txnType;

    @CsvBindByName(column = "Pay Mode")
    private String payMode;

    @CsvBindByName(column = "Payment Model")
    private String paymentModel;

    @CsvBindByName(column = "Txn Status")
    private String txnStatus;

    @CsvBindByName(column = "Customer Name")
    private String customerName;

    @CsvBindByName(column = "Customer VPA")
    private String customerVpa;

    @CsvBindByName(column = "Merchant VPA")
    private String merchantVpa;

    @CsvBindByName(column = "Merchant City")
    private String merchantCity;

    @CsvBindByName(column = "Merchant Txn PAN")
    private String merchantTxnPAN;

    @CsvBindByName(column = "Debit Account No.")
    private String debitAccountNo;

    @CsvBindByName(column = "Merchant Account No.")
    private String merchantAccountNo;

    @CsvBindByName(column = "NPCI Merchant PAN")
    private String npciMerchantPAN;

    @CsvBindByName(column = "IFSC Code")
    private String ifscCode;

    @CsvBindByName(column = "Provider Id")
    private String providerId;

    @CsvBindByName(column = "RRN")
    private String rrn;

    @CsvBindByName(column = "Bill Reference No.")
    private String billReferenceNo;

    @CsvBindByName(column = "Google Pay Sale")
    private String googlePaySale;

    @CsvBindByName(column = "Customer RRN Number")
    private String customerRRNNumber;

    @CsvBindByName(column = "Response Code Description")
    private String responseCodeDescription;

    @CsvBindByName(column = "uuid")
    private String uuid;

    @CsvBindByName(column = "umn")
    private String umn;

    @CsvBindByName(column = "Is Prepaid Voucher Txn")
    private String isPrepaidVoucherTxn;

    @CsvBindByName(column = "Ppv Host Mid")
    private String ppvHostMid;

    @CsvBindByName(column = "Ppv Host Tid")
    private String ppvHostTid;

    @CsvBindByName(column = "Ppv Mcc")
    private String ppvMcc;

    @CsvBindByName(column = "Ppv Host SubmerchantId")
    private String ppvHostSubmerchantId;

    @CsvBindByName(column = "Is Partially Refunded")
    private String isPartiallyRefunded;

    @CsvBindByName(column = "Bank Response Code")
    private String bankResponseCode;

    @CsvBindByName(column = "Bank Response Message")
    private String bankResponseMessage;

    @CsvBindByName(column = "Source")
    private String source;

    @CsvBindByName(column = "Billing Transaction Id")
    private String billingTransactionId;

    @CsvBindByName(column = "Merchant Input 1")
    private String merchantInput1;

    @CsvBindByName(column = "Merchant Input 2")
    private String merchantInput2;

    @CsvBindByName(column = "Merchant Input 3")
    private String merchantInput3;

    @CsvBindByName(column = "Sound Played")
    private String soundPlayed;

    @CsvBindByName(column = "Payment Instrument")
    private String paymentInstrument;

    public void setBillReferenceNo(String billReferenceNo) {
        this.billReferenceNo = StringUtils.hasText(billReferenceNo) ? billReferenceNo.replace("T", "") : null;
    }

    public String getStoreName() {
//        int zerosToAdd = Math.max(0, 4 - storeName.length());
//        StringBuilder paddedString = new StringBuilder();
//        for (int i = 0; i < zerosToAdd; i++) {
//            paddedString.append('0');
//        }
//        paddedString.append(storeName);
//        return paddedString.toString();
        return storeName;
    }
}
