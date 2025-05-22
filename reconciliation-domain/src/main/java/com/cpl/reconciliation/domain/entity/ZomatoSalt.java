package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table
@EntityListeners(AuditingEntityListener.class)
public class ZomatoSalt implements Serializable {

    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;

    @CsvBindByName(column = "merchant_id")
    private String merchantId;

    @CsvBindByName(column = "tab_id")
    @Id
    private String tabId;

    @CsvBindByName(column = "res_id")
    private String resId;

    @CsvBindByName(column = "created_at")
    @Convert(converter = LocalDateTimeConverter.class)
    @CsvDate(value = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @CsvBindByName(column = "order_type")
    private String orderType;

    @CsvBindByName(column = "city_name")
    private String cityName;

    @CsvBindByName(column = "pro_discount")
    private String proDiscount;

    @CsvBindByName(column = "status")
    private String status;

    @CsvBindByName(column = "promo_code")
    private String promoCode;

    @CsvBindByName(column = "merchant_voucher_discount")
    private double merchantVoucherDiscount;

    @CsvBindByName(column = "zomato_voucher_discount")
    private double zomatoVoucherDiscount;

    @CsvBindByName(column = "voucher_discount")
    private double voucherDiscount;

    @CsvBindByName(column = "salt_discount")
    private double saltDiscount;

    @CsvBindByName(column = "logistics_partner_id")
    private String logisticsPartnerId;

    @CsvBindByName(column = "delivery_charge")
    private double deliveryCharge;

    @CsvBindByName(column = "gold_discount")
    private double goldDiscount;

    @CsvBindByName(column = "dc_discount")
    private double dcDiscount;

    @CsvBindByName(column = "user_paid_amount")
    private double userPaidAmount;

    @CsvBindByName(column = "bill_subtotal")
    private double billSubtotal;

    @CsvBindByName(column = "tax")
    private double tax;

    @CsvBindByName(column = "packaging_charges")
    private double packagingCharges;

    private String storeCode;

    private String freebieItem;
    private double freebieCost;
    private double freebieSalePrice;

}
