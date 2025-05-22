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
@Table(name = "threepo_dashboard")
public class ThreePoDashBoardEntity implements Serializable {
    @Id
    private String id;
    private String storeCode;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate businessDate;
    private String category;
    private String tenderName;
    private String bank;
    private String paymentType;
    private Double posSales;
    private Double posReceivables;
    private Double posCommission;
    private Double posCharges;
    private Double posFreebies;
    private Double posDiscounts;
    private Double threePoSales;
    private Double threePoReceivables;
    private Double threePoCommission;
    private Double threePoCharges;
    private Double threePoFreebies;
    private Double threePoDiscounts;
    private Double reconciled;
    private Double unReconciled;
    private Double posVsThreePo;
    private Double receivablesVsReceipts;
    private  Double booked;
    private Double promo;
    private Double deltaPromo;
}
