package com.cpl.reconciliation.domain.entity;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Entity
@Table(name = "zomato_utr")
public class ZomatoUTREntity implements Serializable {
    @Id
    private String id;
    private LocalDate payout_date;
    private String utr_number;
    private String order_id;
    private double final_amount;
}
