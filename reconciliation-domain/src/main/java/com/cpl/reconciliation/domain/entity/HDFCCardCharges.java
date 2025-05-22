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
@Table(name = "hdfc_card_charges")
public class HDFCCardCharges extends BaseMetaEntity {

    @ExcelCellName("TID")
    @Column(nullable = false, unique = true)
    private String tid;
}
