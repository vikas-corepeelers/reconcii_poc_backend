package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.*;
import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Embeddable
public class VoucherEntryId implements Serializable {

    @Convert(converter = LocalDateConverter.class)
    @JsonFormat(pattern = "dd MMM yyyy")
    private LocalDate date;


    @Enumerated(value = EnumType.STRING)
    @Column(length = 10)
    private PaymentType paymentType;

  //  @Enumerated(value = EnumType.STRING)
    @Column(length = 15)
    private String bank;


    @Column(length = 10)
    private String storeCode;


    @Enumerated(value = EnumType.STRING)
    @Column(length = 15)
    private Ledger ledger;


    @Enumerated(value = EnumType.STRING)
    @Column(length = 2)
    private DC dc;


    @Enumerated(value = EnumType.STRING)
    private EntryType entryType;

    private boolean voucherCreated;
    private int voucherHash;

}
