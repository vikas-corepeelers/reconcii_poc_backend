package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoucherReportDownloadRequest {
    private VoucherRequest voucherRequest;
    private Long voucherId;
}
