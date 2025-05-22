package com.cpl.reconciliation.domain.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseMetaEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false,  columnDefinition = "varchar(255) default 'SYSTEM_GENERATED'")
    protected String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, columnDefinition = "varchar(255) default 'SYSTEM_GENERATED'")
    protected String updatedBy;


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}