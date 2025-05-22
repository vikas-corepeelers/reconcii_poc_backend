package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DashboardInStoreDataResponse {
    private SourceWiseDashboardData posData;
    private SourceWiseDashboardData trmData;
}
