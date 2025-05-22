package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.poiji.annotation.ExcelCellName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Table(indexes = {
        @Index(name = "orderId", columnList = "orderId"),
        @Index(name = "composite_date_storeCode", columnList = ("date,storeCode")),
})
@Entity
public class Magicpin extends AdditionalData implements Serializable {

    @ExcelCellName("date")
    @Convert(converter = LocalDateConverter.class)
    private LocalDate date;

    @ExcelCellName("order_id")
    @Id
    private String orderId;

    @ExcelCellName("city")
    private String city;

    @ExcelCellName("locality")
    private String locality;

    @ExcelCellName("pid")
    private String pid;

    @ExcelCellName("mid")
    private String mid;

    @ExcelCellName("merchant_name")
    private String merchantName;

    @ExcelCellName("rejection_id")
    private String rejectionId;

    @ExcelCellName("rejection_reason")
    private String rejectionReason;

    @ExcelCellName("order_status")
    private String orderStatus;

    @ExcelCellName("merchant_gmv")
    private String merchantGmv;

    @ExcelCellName("item_amount")
    private double itemAmount;

    @ExcelCellName("packaging_charge")
    private double packagingCharge;

    @ExcelCellName("GST")
    private double gst;

    @ExcelCellName("mfp")
    private double mfp;

    @ExcelCellName("commission")
    private double commission;

    @ExcelCellName("GST_on_commission")
    private double gstOnCommission;

    @ExcelCellName("Debited_amount")
    private double debitedAmount;

    @ExcelCellName("TDS")
    private double tds;

    @ExcelCellName("net_payble")
    private String netPayable;

    private boolean foundInSTLD;
    private boolean foundInDotPe;
    private String storeCode;
    private double actualPackagingCharge;

}
