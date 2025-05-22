package com.cpl.reconciliation.domain.entity;

import com.poiji.annotation.ExcelCellName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "zomato_mappings")
public class ZomatoMappings implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    @ExcelCellName("Store Code")
    private String storeCode;
    @Column(nullable = false, unique = false)
    @ExcelCellName("Zomato Store Code")
//    @Id
    private String zomatoStoreCode;
    @Column
    @ExcelCellName("Packaging Charge")
    private double packagingCharge = 15.0;
}
