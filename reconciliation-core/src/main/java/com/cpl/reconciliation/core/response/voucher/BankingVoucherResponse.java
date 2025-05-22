package com.cpl.reconciliation.core.response.voucher;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.enums.VoucherApprovalStage;
import com.cpl.reconciliation.core.request.VoucherType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BankingVoucherResponse {
    private Long id;
    private PaymentType paymentType;
    private String bank;
    private VoucherApprovalStage approvalStage;
    private String startDate;
    private String endDate;
    private VoucherType voucherType;
}
