//package com.cpl.reconciliation.web.service.voucher.impl.reports;
//
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.web.service.voucher.VoucherReportDownloadService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//
//@Data
//@Slf4j
//@Service
//public class ReportFactory {
//    private final HdfcSbiAmex_VoucherReport hdfcSbiAmexVoucherReport;
//    private final ICICIVoucherReport iciciVoucherReport;
//
//
//    public VoucherReportDownloadService getReportService(Bank bank) {
//        if (bank != null) {
//
//            if (bank.equals(Bank.HDFC)
//                    || bank.equals(Bank.SBI)
//                    || bank.equals(Bank.AMEX)) return hdfcSbiAmexVoucherReport;
//            else if (bank.equals(Bank.ICICI)) return iciciVoucherReport;
//        }
//
//
//        return null;
//    }
//}
