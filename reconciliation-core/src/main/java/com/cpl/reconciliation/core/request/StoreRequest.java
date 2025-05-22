package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StoreRequest {

    private Long id;
    private String state;
    private String city;
    private String storeCode;
    private String storeName;
    private String storeStatus;
    private String storeMailId;
    private String contactNumber;
    private String fssaiLicenceNo;
    private String gstNo;
    private String eotfStatus;
    private String mfyGdStore;
    private double longitude;
    private double latitude;
    private String address;
    private String pinCode;
    private String oc;
    private String ocPhoneNo;
    private String ocEmailId;
    private String om;
    private String omPhoneNo;
    private String omEmailId;
    private String rm;
    private String spod;
    private String circuitId;
    private String isp;
    private String bandwidth;
    private String media;
}
