package com.cpl.reconciliation.domain.entity;

import com.poiji.annotation.ExcelCellName;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "store_tid_mapping")
public class StoreTIDMapping extends BaseMetaEntity {

    @ExcelCellName("TID")
    @Column(nullable = false, unique = true)
    private String tid;
    @ExcelCellName("Store Code")
    @Column(nullable = false)
    private String storeCode;

    @ExcelCellName("Acquirer")
    @Column(nullable = false)
    private String bank;

    @ExcelCellName("POS")
    @Column(nullable = false)
    private String pos;

    @ExcelCellName("Type")
    @Column(nullable = false)
    private String type;

    @ExcelCellName("Hardware Id")
    @Column(nullable = false)
    private String hardwareId;

    @ExcelCellName("Hardware Model")
    @Column(nullable = false)
    private String hardwareModel;

    @ExcelCellName("MID")
    @Column(nullable = false)
    private String mid;

    @ExcelCellName("City")
    @Column(nullable = false)
    private String city;
}


