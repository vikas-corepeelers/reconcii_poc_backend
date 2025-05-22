package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.entity.enums.StoreType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "store")
public class StoreEntity extends BaseMetaEntity {

//    @Column(nullable = false)
    private String state;
//    @Column(nullable = false)
    private String city;
    @Column(nullable = false, unique = true)
    private String storeCode;
    @Column(nullable = false)
    private String storeName;
//    @Column(nullable = false)
    private String storeStatus;
//    @Column(nullable = false)
    private String storeMailId;
//    @Column(nullable = false)
    private String contactNumber;
//    @Column(nullable = false)
    private String fssaiLicenceNo;
//    @Column(nullable = false)
    private String gstNo;
//    @Column(nullable = false)
    private String eotfStatus;
//    @Column(nullable = false)
    private String mfyGdStore;
//    @Column(nullable = false)
    private LocalDate storeOpeningDate;
//    @Column(nullable = false)
    private Double longitude;
//    @Column(nullable = false)
    private Double latitude;
//    @Column(nullable = false)
    private String address;
//    @Column(nullable = false)
    private String pinCode;
//    @Column(nullable = false)
    private String oc;
//    @Column(nullable = false)
    private String ocPhoneNo;
//    @Column(nullable = false)
    private String ocEmailId;
//    @Column(nullable = false)
    private String om;
    private String omPhoneNo;
//    @Column(nullable = false)
    private String omEmailId;
//    @Column(nullable = false)
    private String rm;
//    @Column(nullable = false)
    private String spod;
//    @Column(nullable = false)
    private String circuitId;
//    @Column(nullable = false)
    private String isp;
//    @Column(nullable = false)
    private String bandwidth;
//    @Column(nullable = false)
    private String media;
    @Enumerated(value = EnumType.STRING)
    private StoreType storeType;
}
