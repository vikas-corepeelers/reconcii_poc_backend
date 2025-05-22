package com.cpl.reconciliation.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TransactionResponse {

    @JsonProperty("msgid")
    private String msgId;
    private List<Transaction> transactionData = new ArrayList<>();
    private List<UPITransaction> upiTransactionData = new ArrayList<>();

    @Override
    public String toString() {
        return "TransactionResponse{" +
                "msgId='" + msgId + '\'' +
                ", CardTransactionData=" + (CollectionUtils.isEmpty(transactionData)?0:transactionData.size()) +
                ", UPITransactionData=" + (CollectionUtils.isEmpty(upiTransactionData)?0:upiTransactionData.size()) +
                '}';
    }
}
