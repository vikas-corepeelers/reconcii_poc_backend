package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "budget_master")
public class BudgetMasterEntity extends BaseEntity {
    @Convert(converter = LocalDateConverter.class)
    private LocalDate startDate;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate endDate;
    private String tenderName;
    private String type;
    private double totalBudget;
    @Enumerated(value = EnumType.STRING)
    private DayOfWeek day;
}
