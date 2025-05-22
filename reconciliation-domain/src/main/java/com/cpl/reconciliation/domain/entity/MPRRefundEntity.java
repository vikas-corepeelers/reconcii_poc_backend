package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.core.enums.PaymentType;
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
@Table(name = "mpr_refunds")
@EntityListeners(AuditingEntityListener.class)
public class MPRRefundEntity implements Serializable {
    @Id
    protected String id;
    @CreatedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "created_date", nullable = false, updatable = false)
    protected LocalDateTime added;
    @LastModifiedDate
    @Convert(converter = LocalDateTimeConverter.class)
    @Column(name = "updated_date", nullable = false)
    protected LocalDateTime updated;
   // @Enumerated(value = EnumType.STRING)
    private String bank;
    @Enumerated(value = EnumType.STRING)
    private PaymentType paymentType;
    private String merchantID;
    private String merchantName;
    private String subMerchantID;
    private String subMerchantName;
    private String merchantTranID;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime originalTransactionDateTime;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime refundTransactionDateTime;
    private double refundAmount;
    private String originalBankRRN;
    private String customerVPA;
    private String reasonForRefund;
    private String merchantAccount;
    private String merchantIFSCCode;
    private String typeOfRefund;
    private String refundRRN;
    private String status;
    private String refundInitiationSource;
    private String originalMerchantTransactionId;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime statusUpdateDate;
    private boolean isOnlineDeemed;
}
