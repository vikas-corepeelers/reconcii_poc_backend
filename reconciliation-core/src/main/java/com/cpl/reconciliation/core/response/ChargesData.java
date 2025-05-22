package com.cpl.reconciliation.core.response;

import com.cpl.reconciliation.core.enums.CardType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ChargesData extends SaleData{

    private double trmAmount;
    private double mprAmount;
    private double settledAmount;
    private double charges;
    private String cardType="";
    private String cardNetwork="";
}
