package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bank_holidays")
public class BankHolidays extends BaseEntity {
    private String name;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime date;
}
