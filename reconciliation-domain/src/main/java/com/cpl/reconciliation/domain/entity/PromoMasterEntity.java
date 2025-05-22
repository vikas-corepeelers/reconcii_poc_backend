package com.cpl.reconciliation.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "promo_master")
public class PromoMasterEntity extends BaseEntity {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String tenderName;
    private double minimumFoodBill;
    private String discountPercentage;
    private double maximumDiscount;
    private double cprplSharePercentage;
}
