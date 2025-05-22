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
@Table(name = "sbi_card_charges")
public class SBICardCharges extends BaseMetaEntity {

    @ExcelCellName("terminal_id")
    @Column(nullable = false, unique = true)
    private String tid;

}
