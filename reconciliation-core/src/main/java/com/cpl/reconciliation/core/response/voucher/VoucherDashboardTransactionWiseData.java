package com.cpl.reconciliation.core.response.voucher;

import com.cpl.reconciliation.core.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VoucherDashboardTransactionWiseData extends VoucherDashboardData {
    PaymentType transactionType;
}
