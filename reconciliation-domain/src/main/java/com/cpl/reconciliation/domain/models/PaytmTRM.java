package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.domain.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PaytmTRM extends BaseEntity {

    private String issuingBank;
    private String customerVPA;
    private String customerDetails;
    private String mid;
    private String merchantName;
    private String posId;
    private String merchantRefId;
    private String storeAddress;
    private String storeCity;
    private String storeState;
    private String cardType;
    private String transactionId;
    private String orderId;
    private LocalDateTime transactionDate;
    private LocalDateTime updDate;
    private String transactionType;
    private String status;
    private double amount;
    private double commission;
    private double gst;
    private String paymentMode;
    private String paymentReferenceNumber;
    private String cardLast4Digits;
    private String authCode;
    private String rrn;
    private String arn;
    private String utrNo;
    private LocalDateTime payoutDate;
    private LocalDateTime settledDate;
    private double settledAmount;
    private String acquiringBank;
    private String merchantRequestId;

}


