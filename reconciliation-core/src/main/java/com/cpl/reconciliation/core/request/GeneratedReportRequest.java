package com.cpl.reconciliation.core.request;

import com.cpl.reconciliation.core.enums.ReportType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class GeneratedReportRequest {
    private String bank;
    private String tender;
    private ReportType reportType;

}
