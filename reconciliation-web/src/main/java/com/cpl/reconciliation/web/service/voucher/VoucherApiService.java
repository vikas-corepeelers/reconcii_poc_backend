package com.cpl.reconciliation.web.service.voucher;

import com.cpl.reconciliation.core.request.*;
import com.cpl.reconciliation.core.response.voucher.BankingVoucherResponse;
import com.cpl.reconciliation.core.response.voucher.VoucherDashboardResponse;
import com.cpl.reconciliation.domain.entity.BankingVoucher;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VoucherApiService {
    BankingVoucher createVouchers(VoucherRequest request);


    VoucherDashboardResponse getDashboardData(VoucherDashboardRequest request);


    BankingVoucher sendForApproval(SendForApprovalRequest request);

    List<BankingVoucher> approve(VoucherApproveRequest request);

    XSSFWorkbook getWorkbook(Long voucherId);

    List<BankingVoucherResponse> getVouchers(VoucherRequest request);

    void editVouchers(Long voucherId, MultipartFile file);
}
