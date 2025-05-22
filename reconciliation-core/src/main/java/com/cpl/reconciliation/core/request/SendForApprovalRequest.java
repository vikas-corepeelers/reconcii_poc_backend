package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SendForApprovalRequest {

    private VoucherRequest request;
    private List<String> emails;
}
