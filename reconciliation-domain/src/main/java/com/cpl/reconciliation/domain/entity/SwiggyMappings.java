package com.cpl.reconciliation.domain.entity;

import com.poiji.annotation.ExcelCellName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "swiggy_mappings")
public class SwiggyMappings implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    @ExcelCellName("Store Code")
    private String storeCode;
    @Column(nullable = false, unique = true)
    @ExcelCellName("Swiggy Store Code")
    private String swiggyStoreCode;
    @Column
    @ExcelCellName("State Code")
    private String stateCode;
    @Column
    @ExcelCellName("Vendor Code")
    private String vendorCode;
    @Column
    @ExcelCellName("Customer Code")
    private String customerCode;
    @Column
    @ExcelCellName("Packaging Charge")
    private double packagingCharge = 37.0;
}
