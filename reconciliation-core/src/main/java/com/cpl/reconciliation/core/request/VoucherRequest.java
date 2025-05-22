package com.cpl.reconciliation.core.request;

import com.cpl.reconciliation.core.enums.PaymentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
public class VoucherRequest extends BaseRequest {
    @NotNull
    private PaymentType paymentType;

    @NotNull
    private String bank;

    @NotNull
    private VoucherType voucherType;

    private enum Mode {
        BANKING,
        THREEPO
    }

}


