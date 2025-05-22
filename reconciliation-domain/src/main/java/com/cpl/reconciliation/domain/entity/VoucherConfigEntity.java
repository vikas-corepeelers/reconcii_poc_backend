package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "voucher_config")
public class VoucherConfigEntity implements Serializable {
    @Id
    private String id;
    private String bank;
    private String tender;
    private String bankAccount;
    private String bankCode;
    private String bankStoreCode;
    private String glAccount;
    private double commisionPerc;
    private String tdsDeduction;
    private String gstGroupCode;
    private double gstCredit;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate startDate;

    @Convert(converter = LocalDateConverter.class)
    private LocalDate endDate;

    @Convert(converter = LocalDateConverter.class)
    private LocalDate payoutDate;

    private String navision_account;
}
