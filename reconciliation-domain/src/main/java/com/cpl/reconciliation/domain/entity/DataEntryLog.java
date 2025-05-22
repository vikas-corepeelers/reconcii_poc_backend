package com.cpl.reconciliation.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "data_entry_log")
public class DataEntryLog extends BaseMetaEntity {

    private String dataSource;
    private Integer fileCount;
    private LocalDate businessDate;
    private LocalDate endDate;
    private String storeCode;
    private Integer recordCount;

}
