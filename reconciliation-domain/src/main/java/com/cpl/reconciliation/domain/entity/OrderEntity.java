package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateConverter;
import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "orders",
        indexes = {
            @Index(name = "invoiceNumber", columnList = "invoiceNumber"),
            @Index(name = "threePO_composite", columnList = "threePOSource,threePOOrderId")}
)
public class OrderEntity implements Serializable {

    @Id
    private String id;
    private String posId;
    private String storeId;
    @Convert(converter = LocalDateConverter.class)
    private LocalDate businessDate;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime orderDate;
    //
    private String saleType;
    private String tenderName;
    private String orderStatus;
    private String invoiceNumber;
    //
    private String uniqueKey;
    private String receiptNumber;
    private Double totalAmount;
    private Double totalTax;

//    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
//    private Set<TenderEntity> tenders = new HashSet<>();

    private String threePOSource;
    private String threePOOrderId;
    private String storeName;
    private String billUser;
    private Double subTotal;
    private Double discount;
    private Double netSale;
    private Double gstEcomPct;
    private Double gstPct;
    private Double packagingChargeSwiggy;
    private Double restaurantPackagingCharges;
    private String modeName;
    private String transactionNumber;
    private String billTime;
    private String orderId;
    private String billNumber;
    private String channel;
    private String source;
    private String settlementMode;
    private Double grossAmount;
}
