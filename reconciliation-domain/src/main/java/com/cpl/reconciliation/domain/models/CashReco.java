package com.cpl.reconciliation.domain.models;
import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.cpl.reconciliation.domain.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CashReco {
    private String storeCode;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate businessDate;
    private String tenderName;
    private String paymentType;
    private Double Sales;
    private Double cashPickUp;
    private Double totalCashPickUp;
    private Double depositAmount;
    private Double Charges;
    private Double reconciled;
    private Double unReconciled;
    private Double salesVsPickup;
    private Double pickupVsDeposit;
    private String reason;
}
