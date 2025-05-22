package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VoucherApproveRequest {

    private List<VoucherStatus> voucherStatuses;
}
