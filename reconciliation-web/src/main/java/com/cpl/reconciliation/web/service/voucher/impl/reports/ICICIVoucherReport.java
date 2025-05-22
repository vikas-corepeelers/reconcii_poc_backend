//package com.cpl.reconciliation.web.service.voucher.impl.reports;
//
//
//import com.cpl.reconciliation.core.enums.*;
//import com.cpl.reconciliation.core.request.VoucherType;
//import com.cpl.reconciliation.domain.entity.BankingVoucher;
//import com.cpl.reconciliation.domain.entity.BankingVoucherEntries;
//import com.cpl.reconciliation.domain.repository.BankingVoucherRepository;
//import com.cpl.reconciliation.web.service.util.VoucherCreationUtil;
//import com.cpl.reconciliation.web.service.voucher.VoucherReportDownloadService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.hssf.util.HSSFColor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFCell;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.format.TextStyle;
//import java.util.*;
//import java.util.stream.Collectors;
//
//import static com.cpl.core.api.constant.Formatter.DDMMYYYY_DASH;
//
//@Slf4j
//@Data
//@Service
//public class ICICIVoucherReport implements VoucherReportDownloadService {
//
//    private final BankingVoucherRepository bankingVoucherRepository;
//
//    @Override
//    public XSSFWorkbook downloadReport(List<BankingVoucherEntries> bankingVoucherEntries, PaymentType paymentType, Bank bank, VoucherType voucherType) {
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        XSSFSheet sheet = workbook.createSheet(bank.name() + " " + paymentType.name());
//        if (voucherType.equals(VoucherType.FINAL)){
//            createFinalVoucher(sheet,bankingVoucherEntries);
//        } else if (voucherType.equals(VoucherType.TRANSFER)) {
//            createTransferVoucher(workbook, sheet,bankingVoucherEntries);
//        }
//        return workbook;
//    }
//    private void createFinalVoucher(XSSFSheet sheet,List<BankingVoucherEntries> bankingVoucherEntries){
//        XSSFRow row = sheet.createRow(0);
//        XSSFCell cell = row.createCell(0);
//        cell.setCellValue("Version");
//        Map<EntryType, List<BankingVoucherEntries>> groupedVouchersMap = bankingVoucherEntries.stream()
//                .collect(Collectors.groupingBy(voucher -> voucher.getEntryType()));
//
//        int lastRow = addVouchers(sheet, groupedVouchersMap.getOrDefault(EntryType.SALES, new ArrayList<>()), 0);
//        lastRow = addVouchers(sheet, groupedVouchersMap.getOrDefault(EntryType.COMMISSION, new ArrayList<>()), lastRow);
//        lastRow = addVouchers(sheet, groupedVouchersMap.getOrDefault(EntryType.REFUNDS, new ArrayList<>()), lastRow);
//        addVouchers(sheet, groupedVouchersMap.getOrDefault(EntryType.BANK, new ArrayList<>()), lastRow);
//    }
//
//    private void createTransferVoucher(XSSFWorkbook workbook, XSSFSheet sheet,List<BankingVoucherEntries> bankingVoucherEntries){
//        XSSFRow row = sheet.createRow(0);
//        XSSFCell cell = row.createCell(0);
//        cell.setCellValue("Version");
//        Map<DC, List<BankingVoucherEntries>> groupedEntries = bankingVoucherEntries.stream().collect(Collectors.groupingBy(voucher -> voucher.getDc()));
//        int lastRow = addTransferVouchers(sheet, groupedEntries.getOrDefault(DC.D, new ArrayList<>()), 0, VoucherCreationUtil.getBlackCellStyle(workbook));
//        addTransferVouchers(sheet, groupedEntries.getOrDefault(DC.C, new ArrayList<>()), lastRow, VoucherCreationUtil.getRedCellStyle(workbook));
//    }
//    private int addVouchers(XSSFSheet sheet, List<BankingVoucherEntries> bankingVouchers, int lastRow) {
//        if (!bankingVouchers.isEmpty()) lastRow++;
//        for (BankingVoucherEntries bankingVoucherEntries : bankingVouchers) {
//            lastRow++;
//            XSSFRow row = sheet.createRow(lastRow);
//
//            int year = bankingVoucherEntries.getDate().getYear();
//            int month = bankingVoucherEntries.getDate().getMonthValue();
//            int day = bankingVoucherEntries.getDate().getDayOfMonth();
//
//            int col = 0;
//
//            XSSFCell cell1 = row.createCell(col++);
//            cell1.setCellValue(bankingVoucherEntries.getVersion());
//
//            col += 3;
//
//            XSSFCell cell2 = row.createCell(col++);
//            cell2.setCellValue(year);
//
//            XSSFCell cell3 = row.createCell(col++);
//            cell3.setCellValue(month);
//
//            XSSFCell cell4 = row.createCell(col++);
//            cell4.setCellValue(day);
//
//            XSSFCell cell5 = row.createCell(col++);
//            cell5.setCellValue(bankingVoucherEntries.getAmount());
//
//            XSSFCell cell6 = row.createCell(col++);
//            cell6.setCellValue(0);
//
//            XSSFCell cell7 = row.createCell(col++);
//            cell7.setCellValue(bankingVoucherEntries.getDc().name());
//
//            XSSFCell cell8 = row.createCell(col++);
//            cell8.setCellValue(bankingVoucherEntries.getNarration());
//
//            col += 2;
//
//            XSSFCell cell9 = row.createCell(col++);
//            cell9.setCellValue(year);
//
//            XSSFCell cell10 = row.createCell(col++);
//            cell10.setCellValue(month);
//
//            XSSFCell cell11 = row.createCell(col++);
//            cell11.setCellValue(bankingVoucherEntries.getPaymentType().equals(PaymentType.CARD) ? day : bankingVoucherEntries.getDate().getMonth().length(bankingVoucherEntries.getDate().isLeapYear()));
//
//            XSSFCell cell12 = row.createCell(col++);
//            cell12.setCellValue(bankingVoucherEntries.getStoreCode());
//
//            XSSFCell cell13 = row.createCell(col++);
//            cell13.setCellValue(110);
//
//            col += 2;
//
//            XSSFCell cell14 = row.createCell(col++);
//            cell14.setCellValue("LX");
//
//            XSSFCell cell15 = row.createCell(col++);
//            cell15.setCellValue("X");
//
//            XSSFCell cell17 = row.createCell(col++);
//            cell17.setCellValue(bankingVoucherEntries.getLedger().getValue());
//
//            col += 4;
//
//            XSSFCell cell18 = row.createCell(col++);
//            cell18.setCellValue(bankingVoucherEntries.getNarration());
//
//            if (bankingVoucherEntries.getLedger().equals(Ledger.BANK_ACCOUNT)) {
//                col++;
//                XSSFCell chequeNo = row.createCell(col++);
//                chequeNo.setCellValue(bankingVoucherEntries.getChequeNo());
//
//                XSSFCell date = row.createCell(col++);
//                date.setCellValue(bankingVoucherEntries.getDate().format(DDMMYYYY_DASH));
//            } else col += 3;
//
//            XSSFCell cell19 = row.createCell(col++);
//            cell19.setCellValue(bankingVoucherEntries.getDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase() + (year % 100));
//
//            col++;
//
//            XSSFCell cell20 = row.createCell(col);
//            if (bankingVoucherEntries.getPaymentType().equals(PaymentType.UPI))
//                cell20.setCellValue("SALEUPI");
//            col+=1;
//
//            XSSFCell cell21 = row.createCell(col);
//            cell21.setCellValue(bankingVoucherEntries.getEntryType().toString());
//        }
//        return lastRow;
//
//    }
//
//    private int addTransferVouchers(XSSFSheet sheet, List<BankingVoucherEntries> bankingVouchers, int lastRow, CellStyle cellStyle) {
//        if (!bankingVouchers.isEmpty()) lastRow++;
//        for (BankingVoucherEntries bankingVoucherEntries : bankingVouchers) {
//            lastRow++;
//            XSSFRow row = sheet.createRow(lastRow);
//            row.setRowStyle(cellStyle);
//            int year = bankingVoucherEntries.getDate().getYear();
//            int month = bankingVoucherEntries.getDate().getMonthValue();
//            int day = bankingVoucherEntries.getDate().getDayOfMonth();
//
//            int col = 0;
//
//            XSSFCell cell1 = row.createCell(col++);
//            cell1.setCellValue(bankingVoucherEntries.getVersion());
//
//            col += 3;
//
//            XSSFCell cell2 = row.createCell(col++);
//            cell2.setCellValue(year);
//
//            XSSFCell cell3 = row.createCell(col++);
//            cell3.setCellValue(month);
//
//            XSSFCell cell4 = row.createCell(col++);
//            cell4.setCellValue(day);
//
//            XSSFCell cell5 = row.createCell(col++);
//            cell5.setCellValue(bankingVoucherEntries.getAmount());
//
//            XSSFCell cell6 = row.createCell(col++);
//            cell6.setCellValue(0);
//
//            XSSFCell cell7 = row.createCell(col++);
//            cell7.setCellValue(bankingVoucherEntries.getDc().name());
//
//            XSSFCell cell8 = row.createCell(col++);
//            cell8.setCellValue(bankingVoucherEntries.getNarration());
//
//            col += 2;
//
//            XSSFCell cell9 = row.createCell(col++);
//            cell9.setCellValue(year);
//
//            XSSFCell cell10 = row.createCell(col++);
//            cell10.setCellValue(month);
//
//            XSSFCell cell11 = row.createCell(col++);
//            cell11.setCellValue(bankingVoucherEntries.getPaymentType().equals(PaymentType.CARD) ? day : bankingVoucherEntries.getDate().getMonth().length(bankingVoucherEntries.getDate().isLeapYear()));
//
//            XSSFCell cell12 = row.createCell(col++);
//            cell12.setCellValue(bankingVoucherEntries.getStoreCode());
//
//            XSSFCell cell13 = row.createCell(col++);
//            cell13.setCellValue(110);
//
//            col += 2;
//
//            XSSFCell cell14 = row.createCell(col++);
//            cell14.setCellValue("LX");
//
//            XSSFCell cell15 = row.createCell(col++);
//            cell15.setCellValue("X");
//
//            XSSFCell cell17 = row.createCell(col++);
//            cell17.setCellValue(bankingVoucherEntries.getLedger().getValue());
//
//            col += 4;
//
//            XSSFCell cell18 = row.createCell(col++);
//            cell18.setCellValue(bankingVoucherEntries.getNarration());
//
//            if (bankingVoucherEntries.getLedger().equals(Ledger.BANK_ACCOUNT)) {
//                col++;
//                XSSFCell chequeNo = row.createCell(col++);
//                chequeNo.setCellValue(bankingVoucherEntries.getChequeNo());
//
//                XSSFCell date = row.createCell(col++);
//                date.setCellValue(bankingVoucherEntries.getDate().format(DDMMYYYY_DASH));
//            } else col += 3;
//
//            XSSFCell cell19 = row.createCell(col++);
//            cell19.setCellValue(bankingVoucherEntries.getDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase() + (year % 100));
//
//            col++;
//
//        }
//        return lastRow;
//
//    }
//
//    public void editVouchers(Long voucherId, Workbook workbook, BankingVoucher bankingVoucher) {
//        if (bankingVoucher.getVoucherType().equals(VoucherType.FINAL)){
//            editFinalVouchers(voucherId,workbook,bankingVoucher);
//        } else if (bankingVoucher.getVoucherType().equals(VoucherType.TRANSFER)) {
//            editTransferVouchers(voucherId,workbook,bankingVoucher);
//        }
//            }
//
//    public void editFinalVouchers(Long voucherId, Workbook workbook, BankingVoucher bankingVoucher) {
//        Sheet sheet = workbook.getSheetAt(0);
//        List<BankingVoucherEntries> bankingVoucherEntriesList = new ArrayList<>();
//        boolean foundIdentifier = false;
//        int count = 0;
//        for (Row row : sheet) {
//            if (foundIdentifier) {
//                BankingVoucherEntries bankingVoucherEntry =  new BankingVoucherEntries();
//                bankingVoucherEntry.setBankingVoucher(bankingVoucher);
//                bankingVoucherEntry.setVersion(row.getCell(0).getStringCellValue().trim());
//                int year = 0;
//                int month = 0;
//                int day = 0;
//                year = row.getCell(4) == null ? null : (int) row.getCell(4).getNumericCellValue();
//                month = row.getCell(5) == null ? null : (int) row.getCell(5).getNumericCellValue();
//                day = row.getCell(6) == null ? null : (int) row.getCell(6).getNumericCellValue();
//                bankingVoucherEntry.setDate(LocalDate.of(year, month, day));
//                bankingVoucherEntry.setAmount(row.getCell(7) == null ? null : (double) row.getCell(7).getNumericCellValue());
//                bankingVoucherEntry.setDc( row.getCell(9) == null ? null : DC.valueOf(row.getCell(9).getStringCellValue()));
//                bankingVoucherEntry.setNarration(row.getCell(10) == null ? null : row.getCell(10).getStringCellValue());
//                bankingVoucherEntry.setStoreCode(row.getCell(16) == null ? null : row.getCell(16).getStringCellValue());
//                bankingVoucherEntry.setLedger(row.getCell(22) == null ? null : Ledger.valueOfLedger(row.getCell(22).getStringCellValue()));
//                bankingVoucherEntry.setPaymentType(bankingVoucher.getPaymentType());
//                bankingVoucherEntry.setBank(bankingVoucher.getBank());
//                bankingVoucherEntry.setEntryType(row.getCell(34) == null ? null : EntryType.get(row.getCell(34).getStringCellValue()));
//                bankingVoucherEntriesList.add(bankingVoucherEntry);
//            }
//            else if (rowStartCondition(count,row)) {
//                foundIdentifier = true;
//            }
//            count++;
//        }
//        bankingVoucher.setBankingVoucherEntriesList(bankingVoucherEntriesList);
//        bankingVoucherRepository.save(bankingVoucher);
//        log.info("Voucher id {} edited successfully",voucherId);
//    }
//    public void editTransferVouchers(Long voucherId, Workbook workbook, BankingVoucher bankingVoucher) {
//        Sheet sheet = workbook.getSheetAt(0);
//        List<BankingVoucherEntries> bankingVoucherEntriesList = new ArrayList<>();
//        boolean foundIdentifier = false;
//        int count = 0;
//        for (Row row : sheet) {
//            if (foundIdentifier) {
//                BankingVoucherEntries bankingVoucherEntry =  new BankingVoucherEntries();
//                bankingVoucherEntry.setBankingVoucher(bankingVoucher);
//                bankingVoucherEntry.setVersion(row.getCell(0).getStringCellValue().trim());
//                int year = 0;
//                int month = 0;
//                int day = 0;
//                year = row.getCell(4) == null ? null : (int) row.getCell(4).getNumericCellValue();
//                month = row.getCell(5) == null ? null : (int) row.getCell(5).getNumericCellValue();
//                day = row.getCell(6) == null ? null : (int) row.getCell(6).getNumericCellValue();
//                bankingVoucherEntry.setDate(LocalDate.of(year, month, day));
//                bankingVoucherEntry.setAmount(row.getCell(7) == null ? null : (double) row.getCell(7).getNumericCellValue());
//                bankingVoucherEntry.setDc( row.getCell(9) == null ? null : DC.valueOf(row.getCell(9).getStringCellValue()));
//                bankingVoucherEntry.setNarration(row.getCell(10) == null ? null : row.getCell(10).getStringCellValue());
//                bankingVoucherEntry.setStoreCode(row.getCell(16) == null ? null : row.getCell(16).getStringCellValue());
//                bankingVoucherEntry.setLedger(row.getCell(22) == null ? null : Ledger.valueOfLedger(row.getCell(22).getStringCellValue()));
//                bankingVoucherEntry.setPaymentType(bankingVoucher.getPaymentType());
//                bankingVoucherEntry.setBank(bankingVoucher.getBank());
//                bankingVoucherEntry.setEntryType(row.getCell(34) == null ? null : EntryType.get(row.getCell(34).getStringCellValue()));
//                bankingVoucherEntriesList.add(bankingVoucherEntry);
//            }
//            else if (rowStartCondition(count,row)) {
//                foundIdentifier = true;
//            }
//            count++;
//        }
//        bankingVoucher.setBankingVoucherEntriesList(bankingVoucherEntriesList);
//        bankingVoucherRepository.save(bankingVoucher);
//        log.info("Voucher id {} edited successfully",voucherId);
//    }
//        public boolean rowStartCondition(int count , Row row){
//        if (count ==0 && row.getCell(0)!=null)
//            return true;
//        else return false;
//    }
//}
