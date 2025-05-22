package com.cpl.reconciliation.domain.models;

import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ZomatoUTRModel {

    @CsvDate("yyyyMMdd")
    private LocalDate payout_date;
    private String utr_number;
    private String order_id;
    private double final_amount;
    private String action;
    private String service_id;


    //     Not parsing and storing
//    private String res_id;
//    private String city_id;
//    private String city_name;
//    private String res_name;
//
//    private String status;
//
//    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime utr_date;
//    private double amount;
//

//    @CsvDate(value = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime order_date;
//    private double customer_compensation;
//    private double total_amount;
//    private double net_amount;
//
//    private String payment_method;
//    private double bill_subtotal;
//    private double zvd;
//    private double mvd;
//    private double total_voucher;
//    private String promo_code;
//    private double source_tax;
//    private double tax_paid_by_customer;
//    private double pro_discount;
//    private double commission_rate;
//    private double commission_value;
//    private double tax_rate;
//    private double taxes_zomato_fee;
//    private double credit_note_amount;
//    private double tcs_amount;
//    private double tds_amount;
//    private double pgCharge;
//    private String pg_applied_on;
//    private double logistics_charge;
//    private double mDiscount;
//    private double pro_discount_passthrough;
//    private double customer_discount;
//    private double rejection_penalty_charge;
//    private double user_credits_charge;
//    private double cancellation_refund;
//    private double promo_recovery_adj;
//    private double icecream_handling;
//    private double icecream_deductions;
//    private double order_support_cost;
//    private double merchant_delivery_charge;
//    private double merchant_pack_charge;
//    private double delivery_charge;
//    private String mid;
//    private String daterange;

}
