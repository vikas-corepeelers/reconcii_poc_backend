package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import javax.persistence.Id;

@Getter
@Setter
@Entity
@Table(name = "dashboard_data")
public class DashBoardEntity implements Serializable {

    @Id
    private String id;
    private String storeCode;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate businessDate;
    private String category;
    private String tenderName;
    private String bank;
    private String paymentType;
    private Double Sales;
    private Double Receipts;
    private Double Charges;
    private Double posCommission;
    private Double posReceivables;
    private Double reconciled;
    private Double unReconciled;
    private Double trmVsMpr;
    private Double mprVsBank;
    private Double posVsTrm;
    private Double receivablesVsReceipts;
    private Double posVsThreepo;
    private Double salesVsPickup;
    private Double pickupVsReceipts;
}
