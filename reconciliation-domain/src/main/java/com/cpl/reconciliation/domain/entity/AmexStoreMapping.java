package com.cpl.reconciliation.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "amex_store_mapping")
public class AmexStoreMapping extends BaseMetaEntity {

    @Column(nullable = false, unique = true)
    private String amexStoreName;
    @Column(nullable = false)
    private String storeCode;
}
