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
        @Index(name = "orderId", columnList = "orderId"),
        @Index(name = "composite_orderDate_storeCode", columnList = ("orderDate,storeCode")),
})
public class Zomato extends AdditionalData implements Serializable {

    @Id
    private String id;

    @CsvBindByName(column = "res_id")
    private String resId;

    @CsvBindByName(column = "res_name")
    private String resName;

    @CsvBindByName(column = "business_id")
    private String businessId;

    @CsvBindByName(column = "service_id")
    private String serviceId;

    @CsvBindByName(column = "order_id")
    private String orderId;

    @CsvBindByName(column = "order_date")
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime orderDate;

    @CsvBindByName(column = "action")
    private String action;

    @CsvBindByName(column = "city_id")
    private String cityId;

    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "promo_code")
    private String promoCode;

    @CsvBindByName(column = "bill_subtotal")
    private double billSubtotal;

    @CsvBindByName(column = "gst_consumer_bill")
    private double gstCustomerBill;

    @CsvBindByName(column = "source_tax")
    private double sourceTax;

    @CsvBindByName(column = "merchant_voucher_discount")
    private double merchantVoucherDiscount;

    @CsvBindByName(column = "zomato_voucher_discount")
    private double zomatoVoucherDiscount;

    @CsvBindByName(column = "payment_method")
    private String paymentMethod;

    @CsvBindByName(column = "actual_discount")
    private double actualDiscount;

    @CsvBindByName(column = "total_amount")
    private double totalAmount;

    @CsvBindByName(column = "net_amount")
    private double netAmount;

    @CsvBindByName(column = "final_amount")
    private double finalAmount;

    @CsvBindByName(column = "currency")
    private String currency;

    @CsvBindByName(column = "customer_compensation")
    private double customerCompensation;

    @CsvBindByName(column = "commission_rate")
    private double commissionRate;

    @CsvBindByName(column = "commission_value")
    private double commissionValue;

    @CsvBindByName(column = "pg_charge")
    private double pgCharge;

    @CsvBindByName(column = "pg_charge_base")
    private double pgChargeBase;

    @CsvBindByName(column = "tax_rate")
    private double taxRate;

    @CsvBindByName(column = "taxes_zomato_fee")
    private double taxesZomatoFee;

    @CsvBindByName(column = "tcs_amount")
    private double tcsAmount;

    @CsvBindByName(column = "tcs_base")
    private double tcsBase;

    @CsvBindByName(column = "tds_amount")
    private double tdsAmount;

    @CsvBindByName(column = "logistics_charge")
    private double logisticsCharge;

    @CsvBindByName(column = "pro_discount_passthrough")
    private double proDiscountPassthrough;

    @CsvBindByName(column = "customer_discount")
    private double customerDiscount;

    @CsvBindByName(column = "rejection_penalty_charge")
    private double rejectionPenaltyCharge;

    @CsvBindByName(column = "user_credit_charge")
    private double userCreditCharge;

    @CsvBindByName(column = "cancellation_refund")
    private double cancellationRefund;

    @CsvBindByName(column = "promo_recovery_adj")
    private double promoRecoveryAdj;

    @CsvBindByName(column = "icecream_handling")
    private double icecreamHandling;

    @CsvBindByName(column = "icecream_deductions")
    private double icecreamDeductions;

    @CsvBindByName(column = "order_support_cost")
    private double oderSupportCost;

    @CsvBindByName(column = "credit_note_amount")
    private double creditNoteAmount;

    @CsvBindByName(column = "merchant_pack_charge")
    private double merchantPackCharge;

    @CsvBindByName(column = "merchant_delivery_charge")
    private double merchantDeliveryCharge;

    @CsvBindByName(column = "source_tax_base")
    private double sourceTaxBase;

    @CsvBindByName(column = "status")
    private String status;

    @CsvBindByName(column = "utr_number")
    private String utrNumber;

    @CsvBindByName(column = "utr_date")
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime utrDate;

    @CsvBindByName(column = "order_inv_no")
    private String orderInvNo;

    @CsvBindByName(column = "start_order_date")
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime startOrderDate;

    @CsvBindByName(column = "end_order_date")
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime endOrderDate;

    @CsvBindByName(column = "mid")
    private String mid;

    @CsvBindByName(column = "date_range")
    private String dateRange;


    private boolean foundInSTLD;
    private boolean foundInDotPe;
    private String storeCode;

    private double freebie;

    private double actualPackagingCharge;
}
