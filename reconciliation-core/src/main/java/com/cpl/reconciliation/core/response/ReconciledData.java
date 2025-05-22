package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class ReconciledData extends UnreconciledData{

    private double settledAmount;//12
    private double charges;//13
}
