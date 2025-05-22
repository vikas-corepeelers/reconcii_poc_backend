package com.cpl.reconciliation.core.request;

import lombok.Data;

@Data
public class MissingTIDReportRequest {
    protected String tender;
    protected String bank;
}
