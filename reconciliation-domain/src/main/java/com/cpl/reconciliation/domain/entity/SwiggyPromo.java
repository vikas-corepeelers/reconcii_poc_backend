package com.cpl.reconciliation.domain.entity;

import com.poiji.annotation.ExcelCellName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "swiggy_promo")
public class SwiggyPromo implements Serializable {
   @ExcelCellName("DOW")
   private int dow;

   @ExcelCellName("RID")
   private String rid;

   @ExcelCellName("Month")
   private String month;

   @ExcelCellName("ORDER_ID")
   @Id
   private String orderId;

   @ExcelCellName("Hour of the day")
   private int hourOfDay;

   @ExcelCellName("Date")
   private LocalDate date;

   @ExcelCellName("BRAND_NAME")
   private String brandName;

   @ExcelCellName("COUPON_CODE")
   private String couponCode;

   @ExcelCellName("User Type")
   private String userType;

   @ExcelCellName("User Cohort")
   private String userCohort;

   @ExcelCellName("Freebie discount")
   private double freebieDiscount;

   @ExcelCellName("DISCOUNT_TOTAL")
   private double discountTotal;

   @ExcelCellName("GMV")
   private double gmv;

   @ExcelCellName("Remarks")
   private String remarks;

   // Additional Required static data

   private String storeCode;
   @Enumerated(value = EnumType.STRING)
   private DayOfWeek day;


   private String freebieItem;
   private double freebieCost;
   private double freebieSalePrice;

   // Disc capped upto 80
//   private String maximumDiscount;

   // From Swiggy order level data
//   private String posAmount;
//   private String threePoReceivable;
//   private String refundForDisputedOrder;
//   private String orderStatus;
//   private String dotPeOrderCancelledStage;
//   private String dotPeOrderStatusDescription;
//
   // From IGCC
   private String images;
   private String reason;
}
