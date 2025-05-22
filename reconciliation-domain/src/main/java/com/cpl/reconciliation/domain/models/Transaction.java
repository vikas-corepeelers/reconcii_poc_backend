package com.cpl.reconciliation.domain.models;

import com.cpl.core.api.deserializer.FractionSecondLDDeserializer;
import com.cpl.core.api.util.StringUtils;
import com.cpl.reconciliation.core.enums.CardType;
import com.cpl.reconciliation.core.enums.TransactionStatus;
import com.cpl.reconciliation.core.enums.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

import static com.cpl.reconciliation.core.enums.TransactionStatus.PINELABS_CARD_STATUS;
import static com.cpl.reconciliation.core.enums.TransactionStatus.UNKNOWN;

@Getter
@Setter
public class Transaction {

    private Long transactionId;
    private String store;
    private String posId;
    private Long changeNo;
    private String name;
    private String acquirer;
    private String batchNumber;
    private String transcType;
    private Double amount;
    @JsonDeserialize(using = FractionSecondLDDeserializer.class)
    private LocalDateTime date;
    private String invoice;
    private String apprCode;
    private String status;
    private String billInvoice;
    private Double tipAmount;
    private String cardType;
    private String rrn;
    private String pan;
    private String mid;
    private String tid;
    @JsonProperty("Is_reversed")
    private int isReversed;
    private String cardCategory;
    private String mobNo;
    private String rFU1;
    private String rFU2;
    private String rFU3;
    private String rFU4;
    private String rFU5;
    private String rFU6;
    private String rFU7;
    private String rFU8;
    private String rFU9;
    private String rFU10;

    public String getOrderId() {
        return StringUtils.isEmpty(billInvoice) ? billInvoice : billInvoice.replaceFirst("T", "");
    }

    public CardType getCardCategory() {
        return ("1".equals(this.cardCategory)) ? CardType.Credit : ("2".equals(this.cardCategory)) ? CardType.Debit : CardType.Unknown;
    }

    public TransactionType getTransactionType() {
        if (StringUtils.isEmpty(transcType)) {
            return TransactionType.OTHER;
        }
        switch (transcType) {
            case "4001":
                return TransactionType.SALE;
            case "4002":
                return TransactionType.REFUND;
            case "4006":
                return TransactionType.VOID;
            default:
                return TransactionType.OTHER;
        }
    }

    public TransactionStatus getTransactionStatus() {
        TransactionStatus transactionStatus = PINELABS_CARD_STATUS.get(status);
        if (Objects.isNull(transactionStatus)) {
            return UNKNOWN;
        }
        return transactionStatus;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", store='" + store + '\'' +
                ", posId='" + posId + '\'' +
                ", changeNo=" + changeNo +
                ", name='" + name + '\'' +
                ", acquirer='" + acquirer + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", transcType=" + transcType +
                ", amount=" + amount +
                ", date=" + date +
                ", invoice='" + invoice + '\'' +
                ", apprCode='" + apprCode + '\'' +
                ", status='" + status + '\'' +
                ", billInvoice='" + billInvoice + '\'' +
                ", tipAmount=" + tipAmount +
                ", cardType='" + cardType + '\'' +
                ", rrn='" + rrn + '\'' +
                ", pan='" + pan + '\'' +
                ", mid='" + mid + '\'' +
                ", tid='" + tid + '\'' +
                ", isReversed=" + isReversed +
                ", cardCategory='" + cardCategory + '\'' +
                ", mobNo='" + mobNo + '\'' +
                ", rFU1='" + rFU1 + '\'' +
                ", rFU2='" + rFU2 + '\'' +
                ", rFU3='" + rFU3 + '\'' +
                ", rFU4='" + rFU4 + '\'' +
                ", rFU5='" + rFU5 + '\'' +
                ", rFU6='" + rFU6 + '\'' +
                ", rFU7='" + rFU7 + '\'' +
                ", rFU8='" + rFU8 + '\'' +
                ", rFU9='" + rFU9 + '\'' +
                ", rFU10='" + rFU10 + '\'' +
                '}';
    }
}

