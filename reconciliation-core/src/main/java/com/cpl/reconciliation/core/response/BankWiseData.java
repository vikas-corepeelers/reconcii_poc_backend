package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class BankWiseData extends DashboardData {

    private String tenderNam;
    private String bankName;
    private String missingTidValue;
}
