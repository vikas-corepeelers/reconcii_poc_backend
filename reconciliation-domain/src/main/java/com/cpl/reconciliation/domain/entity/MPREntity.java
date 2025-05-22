package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.CardCategory;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.cpl.reconciliation.core.enums.PaymentType;
import com.poiji.annotation.ExcelCellName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "MPR",
        indexes = {
                @Index(name = "uid", columnList = "uid"),
                @Index(name = "transactionDate", columnList = "transactionDate,storeId"),
        })
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class MPREntity implements Serializable {

    @Id
    protected String id;

    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;

 //   @Enumerated(value = EnumType.STRING)
    public String bank;
    private String storeId;
    @ExcelCellName("MID")
    @Column(length = 100)
    private String mid;
    @Column(length = 100)
    private String tid;
    @Column(length = 100)
    @ExcelCellName("Card_Type")
    private String cardType;
    @ExcelCellName("Credit/Debit_Card_Last_4_Digits")
    @Column(length = 100)
    private String cardNumber;
    @ExcelCellName("Transaction_Date")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime transactionDate;
    @ExcelCellName("Settled_Date")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime settledDate;
    @Column(length = 100)
    @ExcelCellName("Auth_Code")
    private String authCode;
    @ExcelCellName("Amount")
    private double mprAmount;
    @ExcelCellName("Order_ID")
    @Column(length = 100)
    private String transactionId;
    @ExcelCellName("Commission")
    private double commission;
    private String merchantRefId;
    private String transactionUtr;
    private double serviceTax;
    private double sbCess;
    private double kkCess;
    private double cgst;
    private double sgst;
    private double igst;
    private double utgst;
    @ExcelCellName("Settled_Amount")
    private Double settledAmount;
    @Enumerated(value = EnumType.STRING)
    private CardCategory cardCategory;
    @Column(length = 100)
    @ExcelCellName("ARN")
    public String arn;
    @ExcelCellName("RRN")
    @Column(length = 100)
    private String rrn;
    private String gstnTransactionId;
    @Column(length = 100)
    @ExcelCellName("Customer_VPA")
    private String payerVA;
    @ExcelCellName("GST")
    private double gst;
    @ExcelCellName("Payment_Mode")
    @Column(length = 100)
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;
    @Column
    private String uid;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime expectedBankSettlementDate;
    private boolean refund;
    private boolean bsMatched;
    private String customField1;
    private double expectedMDR;
    private double expectedCardCharges;
    private Double bankCharges;
}


