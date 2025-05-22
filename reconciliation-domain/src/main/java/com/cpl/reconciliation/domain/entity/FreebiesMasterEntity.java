package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "freebies_master")
public class FreebiesMasterEntity extends BaseEntity {
    @Convert(converter = LocalDateConverter.class)
    private LocalDate startDate;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate endDate;
    private String tenderName;
    private double minimumFoodBill;

    @Enumerated(value = EnumType.STRING)
    private DayOfWeek day;
    private double cprplSharePercentage;
    private String itemName;
    private double totalCost;
    private double salePriceDinningOtherState;
    private double salePriceDeliveryNonHighwayOtherState;
    private double salePriceDeliveryHighwayOtherState;
    private double salePriceDinningRajasthan;
    private double salePriceDeliveryNonHighwayRajasthan;
    private double salePriceDeliveryHighwayRajasthan;
}
