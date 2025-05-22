package com.cpl.reconciliation.web.service.voucher;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.request.VoucherType;
import com.cpl.reconciliation.domain.entity.BankingVoucher;
import com.cpl.reconciliation.domain.entity.BankingVoucherEntries;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public interface VoucherReportDownloadService {
    XSSFWorkbook downloadReport(List<BankingVoucherEntries> entriesList, PaymentType paymentType, String bank, VoucherType voucherType);
    void editVouchers(Long voucherId, Workbook workbook, BankingVoucher bankingVoucher) ;
}
