package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter

public class YesBankMPR {

    @CsvBindByName(column = "TRANSACTION ID")
    private String transactionId;

    @CsvBindByName(column = "PROFILE ID")
    private String profileId;

    @CsvBindByName(column = "CUSTOMER ID")
    private String customerId;

    @CsvBindByName(column = "BANK")
    private String bank;

    @CsvBindByName(column = "MID")
    private String mid;

    @CsvBindByName(column = "TID")
    private String tid;

    @CsvBindByName(column = "RRN")
    private String rrn;

    @CsvBindByName(column = "PMID")
    private String pmid;

    @CsvBindByName(column = "MERCHANT DBA NAME")
    private String merchantDbaName;

    @CsvBindByName(column = "ISO ID")
    private String isoId;

    @CsvBindByName(column = "ISO NAME")
    private String isoName;

    @CsvBindByName(column = "MERCHANT ACCOUNT NUMBER")
    private String merchantAccountNumber;

    @CsvBindByName(column = "ME BRANCH CODE")
    private String meBranchCode;

    @CsvBindByName(column = "PAYMENTCATEGORY")
    private String paymentCategory;

    @CsvBindByName(column = "PAYMENT TYPE")
    private String paymentType;

    @CsvBindByName(column = "CARD NUMBER")
    private String cardNumber;

    @CsvBindByName(column = "TRANSACTION AMOUNT")
    private double transactionAmount;

    @CsvBindByName(column = "MDR PERCENT")
    private String mdrPercent;

    @CsvBindByName(column = "MDR AMOUNT")
    private double mdrAmount;

    @CsvBindByName(column = "CGST")
    private double cgst;

    @CsvBindByName(column = "SGST")
    private double sgst;

    @CsvBindByName(column = "IGST")
    private double igst;

    @CsvBindByName(column = "GST AMOUNT")
    private double gst;

    @CsvBindByName(column = "NET AMOUNT")
    private double netAmount;

    @CsvBindByName(column = "TRANSACTION DATE")
    @CsvDate(value = "dd-MM-yyyy HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime transactionDate;

    @CsvBindByName(column = "SETTLEMENT DATE")
    @CsvDate(value = "dd-MM-yyyy HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime settlementDate;

    @CsvBindByName(column = "BATCH NUMBER")
    private String batchNumber;

    @CsvBindByName(column = "CARD PRODUCT")
    private String cardProduct;

    @CsvBindByName(column = "CARD TYPE")
    private String cardType;

    @CsvBindByName(column = "SCHEME")
    private String scheme;

    @CsvBindByName(column = "PAID DATE")
    @CsvDate(value = "dd-MM-yyyy")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDate paidDate;

    @CsvBindByName(column = "TRANSACTION TYPE")
    private String transactionType;

    @CsvBindByName(column = "TRANSACTION STATUS")
    private String transactionStatus;

    @CsvBindByName(column = "SETTLEMENT STATUS")
    private String settlementStatus;

    @CsvBindByName(column = "PAYMENT STATUS")
    private String paymentStatus;

    @CsvBindByName(column = "FIRC FLAG")
    private String fircFlag;

    @CsvBindByName(column = "GROUPING MID")
    private String groupingMid;

    @CsvBindByName(column = "AUTH CODE")
    private String authCode;

    @CsvBindByName(column = "TXN CARD TYPE")
    private String txnCardType;

    @CsvBindByName(column = "REFUND RRN")
    private String refundRrn;

    @CsvBindByName(column = "CONVERTION RATE")
    private String convertionRate;

    @CsvBindByName(column = "CGST AMOUNT")
    private double cgstAmount;

    @CsvBindByName(column = "SGST AMOUNT")
    private double sgstAmount;

    @CsvBindByName(column = "IGST AMOUNT")
    private double igstAmount;

    @CsvBindByName(column = "CARD CATEGORY")
    private String cardCategory;

    @CsvBindByName(column = "DESCRIPTION")
    private String description;

    @CsvBindByName(column = "MCC")
    private String mcc;

    @CsvBindByName(column = "PAYMENT CALCULATION")
    private String paymentCalculation;

    @CsvBindByName(column = "TRANSACTION CURRENCY")
    private String transactionCurrency;

    @CsvBindByName(column = "SETTLEMENT CURRENCY")
    private String settlementCurrency;

    @CsvBindByName(column = "PAYMENT MODE TYPE")
    private String paymentModeType;

    @CsvBindByName(column = "ONUS FLAG")
    private String onusFlag;

    @CsvBindByName(column = "SUB CUSTOMERID")
    private String subCustomerId;

    @CsvBindByName(column = "BANK INCENTIVE")
    private String bankIncentive;

    @CsvBindByName(column = "DCC FLAG")
    private String dccFlag;

    @CsvBindByName(column = "BIN TYPE")
    private String binType;

    @CsvBindByName(column = "FIRC DATE")
    private String fircDate;

    @CsvBindByName(column = "NEFT IFSC CODE")
    private String neftIfscCode;

    @CsvBindByName(column = "SCHEME CODE TEXT")
    private String schemeCodeText;

    @CsvBindByName(column = "TRANSACTION MID")
    private String transactionMid;

    @CsvBindByName(column = "TRANSACTION TID")
    private String transactionTid;

    @CsvBindByName(column = "INVOICE")
    private String invoice;

    @CsvBindByName(column = "ADDITIONAL INFO LABEL")
    private String additionalInfoLabel;

    @CsvBindByName(column = "ADDITIONAL INFO VALUE")
    private String additionalInfoValue;

    @CsvBindByName(column = "STATEMENT MID")
    private String statementMid;

    @CsvBindByName(column = "FEE TYPE")
    private String feeType;

    @CsvBindByName(column = "ORIGINAL TXNAMOUNT")
    private String originalTxnAmount;

    @CsvBindByName(column = "TXN SOURCE TYPE")
    private String txnSourceType;

    @CsvBindByName(column = "BANK CODE")
    private String bankCode;

    @CsvBindByName(column = "REGION")
    private String region;

    @CsvBindByName(column = "APPROVED TXN AMOUNT")
    private String approvedTxnAmount;

    @CsvBindByName(column = "DECLINED TXN AMOUNT")
    private String declinedTxnAmount;

    @CsvBindByName(column = "MSF")
    private String msf;

    @CsvBindByName(column = "TPC AMOUNT")
    private String tpcAmount;

    @CsvBindByName(column = "TPC CGST")
    private String tpcCgst;

    @CsvBindByName(column = "TPC SGST")
    private String tpcSgst;

    @CsvBindByName(column = "TPC IGST")
    private String tpcIgst;

    @CsvBindByName(column = "CHALLAN NUMBER")
    private String challanNumber;

    @CsvBindByName(column = "GRN NUMBER")
    private String grnNumber;

}
