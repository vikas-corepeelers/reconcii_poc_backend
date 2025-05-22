package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "sbi_tid_charges")
public class SBITidChargesEntity extends BaseEntity {
    private String mid;
    private String mename;
    private String accountNumber;
    private double mcc;
    private String limit_sbi;
    private String terminalId;
    private String branchCode;
    private String sponsorBank;
    private double onusDebitMdrRateGreater2000Amt;
    private double onusDebitMdrRateLess2000Amt;
    private double offusDebitMdrRateGreater2000Amt;
    private double offusDebitMdrRateLess2000Amt;
    private double intnlMdrRate;
    private double onusCreditMdr;
    private double offusCreditMdr;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
