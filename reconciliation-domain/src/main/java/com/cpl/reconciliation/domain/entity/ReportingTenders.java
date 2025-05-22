package com.cpl.reconciliation.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "reporting_tenders")
public class ReportingTenders extends BaseEntity {
    private String category;
    private String displayName;
    private String technicalName;

}
