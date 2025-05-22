package com.cpl.reconciliation.domain.models;

import com.cpl.core.api.deserializer.DMYHMSDeserializer;
import com.cpl.core.api.util.StringUtils;
import com.cpl.reconciliation.core.enums.TransactionType;
import com.cpl.reconciliation.core.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.cpl.reconciliation.core.enums.TransactionStatus.PINELABS_UPI_STATUS;
import static com.cpl.reconciliation.core.enums.TransactionStatus.UNKNOWN;

@Getter
@Setter
@ToString
public class UPITransaction {

    @JsonProperty("transactionId")
    private Long transactionId;

    @JsonProperty("merchantName")
    private String merchantName;

    @JsonProperty("storeName")
    private String storeName;

    @JsonProperty("posId")
    private String posId;

    @JsonProperty("changeNo")
    private Long changeNo;

    @JsonProperty("acquirer")
    private String acquirer;

    @JsonProperty("tid")
    private String tid;

    @JsonProperty("mid")
    private String mid;

    @JsonProperty("phonePayTid")
    private String phonePayTid;

    @JsonProperty("phonePayTxnId")
    private String phonePayTxnId;

    @JsonProperty("hostTxnId")
    private String hostTxnId;

    @JsonProperty("originalTxnID")
    private String originalTxnID;

    @JsonProperty("txnIdPrefix")
    private String txnIdPrefix;

    @JsonProperty("edcBatchID")
    private String edcBatchID;

    @JsonProperty("edcRoc")
    private String edcRoc;

    @JsonProperty("origEdcBatchID")
    private String origEdcBatchID;

    @JsonProperty("origEdcRoc")
    private String origEdcRoc;

    @JsonProperty("txnAmount")
    private Double txnAmount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("txnTime")
    @JsonDeserialize(using = DMYHMSDeserializer.class)
    private LocalDateTime txnTime;

    @JsonProperty("txnType")
    private String txnType;

    @JsonProperty("payMode")
    private String payMode;

    @JsonProperty("paymentModel")
    private String paymentModel;

    @JsonProperty("txnStatus")
    private String txnStatus;

    @JsonProperty("userEmailId")
    private String userEmailId;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("customerName")
    private String customerName;

    @JsonProperty("subMerchantCategory")
    private String subMerchantCategory;

    @JsonProperty("customerVPA")
    private String customerVPA;

    @JsonProperty("merchantVPA")
    private String merchantVPA;

    @JsonProperty("merchantCity")
    private String merchantCity;

    @JsonProperty("merchantTxnPAN")
    private String merchantTxnPAN;

    @JsonProperty("debitAccountNo")
    private String debitAccountNo;

    @JsonProperty("merchantAccountNo")
    private String merchantAccountNo;

    @JsonProperty("nPCIMerchantPAN")
    private String nPCIMerchantPAN;

    @JsonProperty("iFSCCode")
    private String iFSCCode;

    @JsonProperty("invoiceNumber")
    private String invoiceNumber;

    @JsonProperty("providerId")
    private String providerId;

    @JsonProperty("rrn")
    private String rrn;

    @JsonProperty("billReferenceNo")
    private String billReferenceNo;

    @JsonProperty("googlePaySale")
    private String googlePaySale;

    @JsonProperty("customerRRNNumber")
    private String customerRRNNumber;

    @JsonProperty("transactionInitiationMode")
    private String transactionInitiationMode;

    @JsonProperty("responseCodeDescription")
    private String responseCodeDescription;

    @JsonProperty("tipType")
    private String tipType;

    @JsonProperty("hostCategory")
    private String hostCategory;

    public TransactionType getTransactionType() {
        if (StringUtils.isEmpty(txnType)) {
            return TransactionType.OTHER;
        }
        switch (txnType) {
            case "SALE":
                return TransactionType.SALE;
            default:
                return TransactionType.OTHER;
        }
    }

    public TransactionStatus getTransactionStatus() {
        TransactionStatus transactionStatus = PINELABS_UPI_STATUS.get(txnStatus);
        if (Objects.isNull(transactionStatus)) {
            return UNKNOWN;
        }
        return transactionStatus;
    }

    public String getOrderId() {
        return StringUtils.isEmpty(billReferenceNo) ? billReferenceNo : billReferenceNo.replaceFirst("T", "");
    }

}
