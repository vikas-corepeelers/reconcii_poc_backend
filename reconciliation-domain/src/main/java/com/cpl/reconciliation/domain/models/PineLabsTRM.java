package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import java.time.LocalDateTime;

@Getter
@Setter
public class PineLabsTRM {
    @CsvBindByName(column = "Zone")
    private String zone;

    @CsvBindByName(column = "Store Name")
    private String storeName;

    @CsvBindByName(column = "City")
    private String city;

    @CsvBindByName(column = "POS")
    private String pos;

    @CsvBindByName(column = "Hardware Model")
    private String hardwareModel;

    @CsvBindByName(column = "Hardware ID")
    private String hardwareId;

    @CsvBindByName(column = "Acquirer")
    private String acquirer;

    @CsvBindByName(column = "TID")
    private String tid;

    @CsvBindByName(column = "MID")
    private String mid;

    @CsvBindByName(column = "Batch No")
    private String batchNo;

    @CsvBindByName(column = "Payment Mode")
    private String paymentMode;

    @CsvBindByName(column = "Customer Payment Mode ID")
    private String customerPaymentModeId;

    @CsvBindByName(column = "Name")
    private String name;

    @CsvBindByName(column = "Card Issuer")
    private String cardIssuer;

    @CsvBindByName(column = "Card Type")
    private String cardType;

    @CsvBindByName(column = "Card Network")
    private String cardNetwork;

    @CsvBindByName(column = "Card Colour")
    private String cardColour;

    @CsvBindByName(column = "Transaction ID")
    private String transactionId;

    @CsvBindByName(column = "Invoice")
    private String invoice;

    @CsvBindByName(column = "Approval Code")
    private String approvalCode;

    @CsvBindByName(column = "Type")
    private String type;

    @CsvBindByName(column = "Amount")
    private double amount;

    @CsvBindByName(column = "TIP Amount")
    private double tipAmount;

    @CsvBindByName(column = "Currency")
    private String currency;

    @CsvBindByName(column = "Date")
    @CsvDate(value = "dd/MM/yyyy h:mm:ss a")
//    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime transactionDate;

    @CsvBindByName(column = "Settlement Date")
    @CsvDate(value = "dd/MM/yyyy h:mm:ss a")
//    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime settlementDate;

    @CsvBindByName(column = "Batch Status")
    private String batchStatus;

    @CsvBindByName(column = "Txn Status")
    private String txnStatus;



    @CsvBindByName(column = "Bill Invoice")
    private String billInvoice;

    @CsvBindByName(column = "RRN")
    private String rrn;

    @CsvBindByName(column = "EMI Txn")
    private String emiTxn;

    @CsvBindByName(column = "EMI Month")
    private String emiMonth;

    @CsvBindByName(column = "Contactless")
    private String contactless;

    @CsvBindByName(column = "Contactless Mode")
    private String contactlessMode;

    @CsvBindByName(column = "Cloud Ref ID")
    private String cloudRefId;

    @CsvBindByName(column = "Card Pan Check for Sale Complete")
    private String cardPanCheckForSaleComplete;

    @CsvBindByName(column = "Route Preauth to Other Acquirer")
    private String routePreauthToOtherAcquirer;

    @CsvBindByName(column = "Billing Transaction Id")
    private String billingTransactionId;
}
