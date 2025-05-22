package com.cpl.reconciliation.core.response.instore;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeltaDTO {
    protected String paymentType;
    protected String acquirerBank;
    protected double delta;

}
