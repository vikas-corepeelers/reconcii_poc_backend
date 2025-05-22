package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "zomato_promo")
@EntityListeners(AuditingEntityListener.class)
public class ZomatoPromoEntity implements Serializable {
    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date")
    protected LocalDateTime added;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date")
    protected LocalDateTime updated;

    @Convert(converter = LocalDateConverter.class)
    private LocalDate aggregation;
    @Id
    private String tabId;
    private String resId;
    private String entityName;
    private String brandName;
    private String accountType;
    private String promoCode;
    private String psSegment;
    private double zvdFinal;
    private double zvd;
    private double mvd;
    private double offlineRecon;
    private double control;
    private double burn;
    private double total;
    private double net;
    private double construct;
    private double commission;
    private double pg;
    private double gstOnCommission;
    private double gstOnPg;
    private double finalAmount;

    private String storeCode;

}
