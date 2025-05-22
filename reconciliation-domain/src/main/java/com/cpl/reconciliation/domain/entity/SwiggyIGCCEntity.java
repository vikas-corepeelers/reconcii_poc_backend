package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "swiggy_igcc")
public class SwiggyIGCCEntity extends BaseEntity {
    private LocalDate dt;
    private String restaurantId;
    private String orderId;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime orderedTime;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime arrivedTime;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime pickedupTime;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime deliveredTime;
    private String p2d;
    private String deWaitTime;
    private String foodIssueId;
    private String l1Issue;
    private String l2Issue;
    private double resolutionsAmount;
    private String fraudFlag;
    private String impactedItemId;
    private String impactedItem;
    private String brandName;
    private String businessEntity;
    private String groupName;
    private String kamPoc;
    private String vmPoc;
    @Column(columnDefinition = "text")
    private String images;
    @Column(columnDefinition = "text")
    private String comments;
}
