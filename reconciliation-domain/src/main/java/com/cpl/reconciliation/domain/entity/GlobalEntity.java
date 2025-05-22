package com.cpl.reconciliation.domain.entity;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
@Getter
@Setter
@Entity
@Table(name="global_data")
@EntityListeners(AuditingEntityListener.class)
public class GlobalEntity{
    @Id
    @CsvBindByName(column = "OrderLogID")
    private Double orderLogId;
    @CsvBindByName(column = "ThirdPartyID")
    private String thirdPartyId ;
    @CsvBindByName(column = "VendorOrderID")
    private String vendorOrderId;
    @CsvBindByName(column = "VendorStoreID")
    private String vendorStoreId;
    @CsvBindByName(column = "NPStoreId")
    private String npStoreId;
    @CsvBindByName(column = "CurrentSeqNumber")
    private String CurrentSeqNumber;
    @CsvBindByName(column = "OrderNumber")
    private String orderNUmber;
    @CsvBindByName(column = "FOEResponseCode")
    private String foeResponseCode;
    @CsvBindByName(column = "TPOResponseCode")
    private String tpoResponseCode;
    @CsvBindByName(column = "TPOResponseExplanation")
    private String tpoResponseExplanation;
    @CsvBindByName(column = "ProbelamaticPLUs")
    private String probelamaticPlus;
    @CsvBindByName(column = "InternalValidationErrors")
    private String internalValidationErrors;
    @CsvBindByName(column = "BusinessDate")
    @CsvDate(value = "M/d/yyyy h:mm:ss a")
    private LocalDateTime businessDate;
    @CsvBindByName(column = "CreationTimeUTC")
    @CsvDate(value = "M/d/yyyy h:mm:ss a")
    private LocalDateTime creationTimeUtc;
    @CsvBindByName(column = "CreationTimeInStoreLocal")
    @CsvDate(value = "M/d/yyyy h:mm:ss a z")
    private LocalDateTime creationTimeInStoreLocal;
    @CsvBindByName(column = "Event")
    private String event;
    @CsvBindByName(column = "RequestTimeMs")
    private String requestTimeMs;
    @CsvBindByName(column = "FoeTimeMs")
    private String foeTimeMs;
    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;
    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;
}
