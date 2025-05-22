package com.cpl.reconciliation.domain.entity;

import com.opencsv.bean.CsvBindByName;
import com.poiji.annotation.ExcelCellName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table
public class MagicpinMappings implements Serializable {

    @Column(nullable = false)
    @ExcelCellName("Store Code")
    private String storeCode;
    @Column(nullable = false, unique = true)
    @Id
    @ExcelCellName("Mid")
    private String mid;
    @Column
    @ExcelCellName("State Code")
    private String stateCode;
    @Column
    @ExcelCellName("Packaging Charge")
    private double packagingCharge = 37.0;
}
