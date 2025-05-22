package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Entity
@Table(name = "magicpin_utr")
public class MagicpinUTREntity implements Serializable {
    @Id
    private String order_id;

    @Convert(converter = LocalDateConverter.class)
    private LocalDate payout_date;
    private String utr_number;
    private double final_amount;
}
