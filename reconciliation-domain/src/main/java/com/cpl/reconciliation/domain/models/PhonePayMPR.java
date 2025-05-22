package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.poiji.annotation.ExcelCellName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter

public class PhonePayMPR {

    @ExcelCellName("PaymentType")
    private String PaymentType;

    @ExcelCellName("MerchantReferenceId")
    private String MerchantReferenceId;

    @ExcelCellName("MerchantOrderId")
    private double MerchantOrderId;

    @ExcelCellName("OriginalMerchantReferenceId")
    private String OriginalMerchantReferenceId;

    @ExcelCellName("OriginalTransactionId")
    private String OriginalTransactionId;

    @ExcelCellName("OriginalTransactionDate")
    private String OriginalTransactionDate;

    @ExcelCellName("PhonePeReferenceId")
    private String PhonePeReferenceId;

    @ExcelCellName("TransactionUTR")
    private String TransactionUTR;

    @ExcelCellName("StoreId")
    private String StoreId;

    @ExcelCellName("StoreName")
    private String StoreName;

    @ExcelCellName("TerminalId")
    private String TerminalId;

    @ExcelCellName("TerminalName")
    private String TerminalName;

    @ExcelCellName("From")
    private String From;

    @ExcelCellName("Instrument")
    private String Instrument;

    @ExcelCellName("CreationDate")
    private String CreationDate;

    @ExcelCellName("Transaction Date")
//    @Convert(converter = LocalDateConverter.class)
    private String TransactionDate;

    @ExcelCellName("SettlementDate")
    private String SettlementDate;

    @ExcelCellName("BankReferenceNo")
    private String BankReferenceNo;

    @ExcelCellName("Amount")
    private double Amount;

    @ExcelCellName("Fee")
    private double Fee;

    @ExcelCellName("IGST")
    private double IGST;

    @ExcelCellName("CGST")
    private double CGST;

    @ExcelCellName("SGST")
    private double SGST;

    @ExcelCellName("Store Code")
    private String StoreCode;

    @ExcelCellName("Store Name")
    private String StoreName1;

    @ExcelCellName("Credit Amt")
    private double CreditAmt;

    @ExcelCellName("Bank credit date")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime BankCreditDate;

}
