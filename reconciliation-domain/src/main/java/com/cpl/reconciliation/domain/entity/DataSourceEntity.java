package com.cpl.reconciliation.domain.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "data_source")
public class DataSourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "source_name")
    private String sourceName;
    @Column(name = "data_type")
    private String dataType;
    @Column(name = "description", columnDefinition = "text")
    private String description;
}

