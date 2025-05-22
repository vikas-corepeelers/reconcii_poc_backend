package com.cpl.reconciliation.core.response.voucher;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VoucherDashboardData {
    double totalAmount;
    double booked;
    double pending;
    double approved;

}
