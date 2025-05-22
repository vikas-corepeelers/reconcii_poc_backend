package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VoucherDashboardRequest {
    String startDate;
    String endDate;
    String type;

}
