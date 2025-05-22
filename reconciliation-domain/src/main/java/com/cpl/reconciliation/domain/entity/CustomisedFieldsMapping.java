package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "customised_db_fields")
@EntityListeners(AuditingEntityListener.class)
public class CustomisedFieldsMapping implements Serializable {

    @Id
    private String id;
    private String dbColumnName;
    private String excelColumnName;
    private String dataSource;
    private String tableName;
    private String clientName;
    private String tenderName;
    private String description;

    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;

}
