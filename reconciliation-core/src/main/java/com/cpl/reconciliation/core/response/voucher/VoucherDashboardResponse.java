package com.cpl.reconciliation.core.response.voucher;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class VoucherDashboardResponse extends VoucherDashboardData {
    List<VoucherDashboardTransactionWiseData> voucherDashboardTransactionWiseData;
}
