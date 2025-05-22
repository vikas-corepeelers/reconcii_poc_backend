package com.cpl.reconciliation.domain.models;
import com.cpl.reconciliation.domain.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
public class AmexMPR extends BaseEntity {
    private LocalDateTime submissionDate;
    private String socInvoice;
    private String settelmentNumber;
    private double totalCharges;
    private double credits;
    private double submissionAmount;
    private double merchantServiceFee;
    private double feesAndIncetives;
    private double settlementAmount;
    private String payeeMerchantNumber;
    private String submittingMerchantNumber;
    private String submittingLocationId;
    private double transactionCount;
    private LocalDateTime settelmentDate;
    private String submittingLocationName;
    private double taxAmount;
    private double igst;
    private double cgst;
    private double sgst;
    private double amountOfAdjustmentSummary;
    private String description;
    private LocalDateTime processedDate;


}
