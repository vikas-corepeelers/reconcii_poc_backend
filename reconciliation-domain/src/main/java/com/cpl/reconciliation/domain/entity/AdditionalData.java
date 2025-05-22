package com.cpl.reconciliation.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import java.time.LocalDate;

@Getter
@Setter
@MappedSuperclass
public class AdditionalData {

    // POS fields
    public double posTotalAmount;
    public double posTotalTax;
    public String invoiceNumber;
    public LocalDate businessDate;
    public String receiptNumber;
    public String posId;

    // UTR fields
    public double payout_amount;
    public LocalDate payout_date;
    public String reference_number;




}
