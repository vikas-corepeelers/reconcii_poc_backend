package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.domain.entity.OrderEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnreconciledOrderEntity extends OrderEntity {
    private double threePOAmount;
    private double billSubtotal;
    private double merchantPackCharge;
    private double freebie;
    private double customTaxValue;


}
