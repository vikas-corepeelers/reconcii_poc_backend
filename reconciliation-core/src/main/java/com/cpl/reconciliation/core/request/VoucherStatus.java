package com.cpl.reconciliation.core.request;

import com.cpl.reconciliation.core.enums.VoucherApprovalStage;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import java.util.List;

@Getter
@Setter
public class VoucherStatus {

    private Long voucherId;
    private VoucherStatusRequestEnum voucherApprovalStage;
}
