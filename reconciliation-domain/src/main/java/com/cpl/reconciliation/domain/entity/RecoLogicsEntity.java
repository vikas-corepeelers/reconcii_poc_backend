package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/*
 * @author Abhishek N
 */
@Getter
@Setter
@Entity
@Table(name = "reco_logics")
public class RecoLogicsEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tender;

    @Column(name = "createdby")
    private String createdBy;

    @Column(name = "effectivefrom")
    private String effectiveFrom;

    @Column(name = "effectiveto")
    private String effectiveTo;

    @Column(name = "effectivetype")
    private String effectiveType;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String recologic;

    @Column(length = 50, nullable = false)
    private String status;

    @Column(columnDefinition = "text")
    private String remarks;

    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;

    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;
}
