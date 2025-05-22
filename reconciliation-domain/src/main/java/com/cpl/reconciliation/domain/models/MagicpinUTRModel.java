package com.cpl.reconciliation.domain.models;

import com.poiji.annotation.ExcelCellName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MagicpinUTRModel {

    @ExcelCellName("Date")
    private LocalDate payoutDate;

    @ExcelCellName("payment_reference_number")
    private String referenceNumber;

    @ExcelCellName("order_id")
    private String orderId;

    @ExcelCellName("net_payble")
    private double finalAmount;


}
