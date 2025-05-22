package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.DataSource;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "manual_uploading")

// Banks -> ICICI -> MPR (CARD) Refund
public class ManualUploads implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    private String category;
    private String tender;
    private String type;
    @Enumerated(EnumType.STRING)
    private DataSource dataSource;
    private boolean isActive;

}
