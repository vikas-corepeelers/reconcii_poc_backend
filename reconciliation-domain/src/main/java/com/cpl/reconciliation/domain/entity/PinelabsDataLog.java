package com.cpl.reconciliation.domain.entity;


import com.cpl.reconciliation.core.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name="pinelabs_data_log")
public class PinelabsDataLog extends BaseEntity{

    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;
    private Long offset;
    private int dataSize;

}
