package com.cpl.reconciliation.domain.entity;

import com.mysql.cj.protocol.ColumnDefinition;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hdfc_tid_charges")
public class HDFCTidChargesEntity extends BaseEntity {
    private String meCode;
    private String tid;
    @Column(columnDefinition = "text")
    private String legalName;
    private String dbaName;
    @Column(columnDefinition = "text")
    private String address;
    private String city;
    private String pin;
    private String tel;
    private String accountNo2;
    private double creditCard;
    private double foreignComm;
    private double dinersComm;
    private double dinersCommOn;
    private String ruPayFlag;
    private double ddCommSlb1Below2k;
    private double ddCommSlb2Above2k;
    private double cmrclOnusRate;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
