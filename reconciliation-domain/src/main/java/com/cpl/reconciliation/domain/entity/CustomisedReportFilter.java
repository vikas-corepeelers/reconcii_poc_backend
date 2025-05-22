package com.cpl.reconciliation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "customised_report_filters")
@AllArgsConstructor
@NoArgsConstructor
public class CustomisedReportFilter extends BaseEntity {
    String name;
    String technicalName;
    String sqlField;
    String category;
}
