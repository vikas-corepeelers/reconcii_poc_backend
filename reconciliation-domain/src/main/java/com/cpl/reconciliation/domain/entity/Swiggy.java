package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
        (indexes = {
        @Index(name = "order_no", columnList = "orderNo"),
        @Index(name = "composite_orderDate_storeCode", columnList = ("orderDate,storeCode")),
})
public class Swiggy extends AdditionalData implements Serializable {
    @CsvBindByName(column = "Order No")
    @Id
    private String orderNo;

    @CsvBindByName(column = "Restaurant ID")
    private String restaurantId;
    @CsvBindByName(column = "Restaurant Name")
    private String restaurantName;
    @CsvBindByName(column = "City")
    private String city;
    @CsvBindByName(column = "Area")
    private String area;

    @CsvBindByName(column = "Order Date")
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime orderDate;

    @CsvBindByName(column = "Order Status")
    private String orderStatus;
    @CsvBindByName(column = "Cancellation Attribution")
    private String cancellationAttribution;
    @CsvBindByName(column = "Item Total")
    private double itemTotal;
    @CsvBindByName(column = "Packing & Service charges")
    private double packingAndServiceCharges;
    @CsvBindByName(column = "Merchant Discount")
    private double MerchantDiscount;

    @CsvBindByName(column = "Net bill value (without taxes)")
    private double netBillValueWithoutTax;

    @CsvBindByName(column = "GST on order (Including Cess)")
    private double gstOnOrderIncludingCess;

    @CsvBindByName(column = "Customer payable(Net bill value after taxes & discount)")
    private double customerPayable;

    @CsvBindByName(column = "Swiggy Platform Service Fee Chargeable On")
    private double swiggyPlatformFeeChargeableOn;

    @CsvBindByName(column = "Swiggy Platform Service Fee % (%)")
    private String swiggyPlatformServicefeePercent;

    @CsvBindByName(column = "Swiggy Platform Service Fee")
    private double swiggyPlatformServiceFee;

    @CsvBindByName(column = "Discount On Swiggy Platform Service Fee")
    private double discountOnSwiggyPlatformServiceFee;

    @CsvBindByName(column = "Long Distance applicable on order")
    private String longDistanceApplicableOnOrder;

    @CsvBindByName(column = "Last Mile distance")
    private double lastMileDistance;

    @CsvBindByName(column = "Long Distance Fee")
    private double longDistanceFee;

    @CsvBindByName(column = "Discount on Long Distance Fee")
    private double discountOnLongDistanceFee;

    @CsvBindByName(column = "Collection Fee")
    private double collectionFee;
    @CsvBindByName(column = "Access Fee")
    private double accessFee;
    @CsvBindByName(column = "Merchant Cancellation Charges")
    private double MerchantCancellationCharges;
    @CsvBindByName(column = "Call Center Service Fees")
    private double callCenterServicefees;
    @CsvBindByName(column = "Total Swiggy Service Fee(without taxes)")
    private double totalSwiggyServiceFee;

    @CsvBindByName(column = "Delivery fee (sponsored by merchant w/o tax)")
    private double DeliveryFee;
    @CsvBindByName(column = "Total GST")
    private double totalGst;
    @CsvBindByName(column = "Total Swiggy fee(including taxes)")
    private double totalSwiggyFee;
    @CsvBindByName(column = "Cash prepayment at Restaurant")
    private double cashPrePaymentAtRestaurant;

    @CsvBindByName(column = "Buyer Cancellation Cost Sharing")
    private double buyerCancellationCostSharing;
    @CsvBindByName(column = "GST Deduction U/S 9(5)")
    private double gstDeduction;
    @CsvBindByName(column = "Refund For Disputed Order")
    private double refundForDisputedOrder;
    @CsvBindByName(column = "Disputed Order Remarks")
    private String disputedOrderRemarks;

    @CsvBindByName(column = "Total of Order Level Adjustments")
    private double totalOfOrderLevelAdjustments;
    @CsvBindByName(column = "Net Payable Amount (before TCS and TDS deduction)")
    private double netPayableAmountBeforeTcsAndTds;
    @CsvBindByName(column = "TCS")
    private double tcs;
    @CsvBindByName(column = "TDS")
    private double tds;

    @CsvBindByName(column = "Net Payable Amount (After TCS and TDS deduction)")
    private double netPayableAmountAfterTcsAndTds;

    @CsvBindByName(column = "MFR Pressed?")
    private String mfrPressed;
    @CsvBindByName(column = "Cancellation Policy Applied")
    private String cancellationPolicyApplied;
    @CsvBindByName(column = "Coupon Code Applied")
    private String couponCodeApplied;

    @CsvBindByName(column = "Discount Campaign ID")
    private String discountCampaignId;
    @CsvBindByName(column = "Is_replicated")
    private String isReplicated;
    @CsvBindByName(column = "Base order ID")
    private String baseOrderId;

    @CsvBindByName(column = "MRP Items")
    private String mrpItems;
    @CsvBindByName(column = "Order Payment Type")
    private String orderPaymentType;
    @CsvBindByName(column = "Cancellation Time")
    private String cancellationTime;
    @CsvBindByName(column = "Pick Up Status")
    private String pickUpStatus;
    @CsvBindByName(column = "Order Category")
    private String orderCategory;
    @CsvBindByName(column = "Cancelled By?")
    private String cancelled_by;

    @CsvBindByName(column = "Nodal UTR")
    private String nodalUtr;

    @CsvBindByName(column = "Current UTR")
    private String currentUtr;

    private boolean foundInSTLD;
    private boolean foundInDotPe;
    private String storeCode;
    private double actualPackagingCharge;
}
