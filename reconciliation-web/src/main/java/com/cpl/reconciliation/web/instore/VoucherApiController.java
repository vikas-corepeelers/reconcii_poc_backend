//package com.cpl.reconciliation.web.instore;
//
//import com.cpl.core.api.response.ApiResponse;
//import com.cpl.reconciliation.core.enums.EntryType;
//import com.cpl.reconciliation.core.request.*;
//import com.cpl.reconciliation.core.response.voucher.BankingVoucherResponse;
//import com.cpl.reconciliation.core.response.voucher.VoucherDashboardResponse;
//import com.cpl.reconciliation.domain.entity.BankingVoucher;
//import com.cpl.reconciliation.web.service.voucher.VoucherApiService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.util.List;
//
//@Data
//@Slf4j
//@RestController
//@RequestMapping("/public/voucher")
//public class VoucherApiController {
//
//    private final VoucherApiService voucherCreationApiService;
//
//    @RequestMapping(value = "/create", method = RequestMethod.POST)
//    public ApiResponse<BankingVoucher> createVoucher(@Validated @RequestBody VoucherRequest request) {
//        log.info(request.toString());
//        BankingVoucher bankingVoucher= voucherCreationApiService.createVouchers(request);
//        return new ApiResponse<>(bankingVoucher);
//    }
//
//    @RequestMapping(value = "/edit", method = RequestMethod.POST)
//    public ApiResponse<String> editVoucherEntries(@RequestParam("voucherId") Long voucherId, @RequestParam("files") MultipartFile file) {
//        log.info("Going to edit voucher id: {}",voucherId);
//        voucherCreationApiService.editVouchers(voucherId,file);
//        String message = "Voucher Edited Successfully";
//        return new ApiResponse<>(message);
//    }
////    @RequestMapping(value = "/sendForApproval", method = RequestMethod.POST)
////    public ApiResponse<BankingVoucher> sendForApproval(@Validated @RequestBody SendForApprovalRequest request) {
////        log.info(request.toString());
////        BankingVoucher bankingVoucherEntries = voucherCreationApiService.sendForApproval(request);
////        return new ApiResponse<>(bankingVoucherEntries);
////    }
//
////    @RequestMapping(value = "/approve", method = RequestMethod.POST)
////    public ApiResponse<List<BankingVoucher>> approve(@Validated @RequestBody VoucherApproveRequest request) {
////        log.info(request.toString());
////        List<BankingVoucher> bankingVoucherEntries = voucherCreationApiService.approve(request);
////        return new ApiResponse<>(bankingVoucherEntries);
////    }
//
//    @RequestMapping(value = "/getAll", method = RequestMethod.POST)
//    public ApiResponse<List<BankingVoucherResponse>> getAllVoucher(@Validated @RequestBody VoucherRequest request) {
//        log.info(request.toString());
//        List<BankingVoucherResponse> vouchers = voucherCreationApiService.getVouchers(request);
//        return new ApiResponse<>(vouchers);
//    }
//
//    @RequestMapping(value = "/getVoucherType", method = RequestMethod.GET)
//    public ApiResponse<EntryType[]> getAllVoucher() {
//        return new ApiResponse<>(EntryType.values());
//    }
//
//    @RequestMapping(value = "/download", method = RequestMethod.GET)
//    public void downloadReport(@RequestParam("voucherId") Long voucherId, HttpServletResponse response) throws IOException {
//        XSSFWorkbook workbook = voucherCreationApiService.getWorkbook(voucherId);
//        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        response.setHeader("Content-Disposition", "attachment; filename=my_excel_file.xlsx");
//        OutputStream outputStream = response.getOutputStream();
//        workbook.write(outputStream);
//        outputStream.flush();
//        outputStream.close();
//    }
//
//    @RequestMapping(value = "/dashboard", method = RequestMethod.POST)
//    public ApiResponse<VoucherDashboardResponse> getDashboardResponse(@Validated @RequestBody VoucherDashboardRequest request) {
//        log.info(request.toString());
//        VoucherDashboardResponse vouchers = voucherCreationApiService.getDashboardData(request);
//        return new ApiResponse<>(vouchers);
//    }
//
//
//}
