package com.cpl.reconciliation.core.response.instore;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DeltaDTOScheduled {
    protected String date;
    protected String storeId;
    protected String paymentType;
    protected String acquirerBank;
    protected double delta;

}
