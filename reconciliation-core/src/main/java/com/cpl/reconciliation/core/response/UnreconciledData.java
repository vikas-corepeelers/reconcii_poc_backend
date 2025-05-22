package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class UnreconciledData extends SaleData{

    private double trmAmount;//10
    private double mprAmount;//11
    private boolean bsMatched;//12
}
