package com.cpl.reconciliation.core.response.instore;

import com.cpl.reconciliation.core.enums.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
public class MissingTID {
    private PaymentType tender;
    private String bank;
    private long value;
}
