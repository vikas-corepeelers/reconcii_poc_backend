package com.cpl.reconciliation.web.service.util;

import com.cpl.core.api.constant.Formatter;
import com.cpl.reconciliation.core.enums.ThreePO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static com.cpl.reconciliation.domain.util.Constants.*;
import static com.cpl.reconciliation.domain.util.QueryConfig.TENDER_WISE_UNRECONCILED_REASONS_MAP;
import java.util.Map;
import org.apache.poi.ss.util.CellReference;

@Service
public class POSThreePoSummarySheetUtil {

    private void setRegionBorderWithMedium(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
    }

    private void addBusinessTransactionDateBifurcations(Sheet sheet, int row, CellStyle boldStyle, Sheet s2) {
        Row row1 = sheet.createRow(row++);
        row1.createCell(1).setCellValue("No. of orders");
        row1.getCell(1).setCellStyle(boldStyle);
        row1.createCell(2).setCellValue("POS Amount");
        row1.getCell(2).setCellStyle(boldStyle);

        row1 = sheet.createRow(row++);
        int lastS2Row = s2.getLastRowNum() + 1;
        row1.createCell(0).setCellValue("POS Sale as per Business Date (S1+S2)");
        row1.getCell(0).setCellStyle(boldStyle);
        String formula = "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange("S2", "H", 2, lastS2Row)
        }, ",") + ")" + "+B5";
        row1.createCell(1).setCellFormula(formula);
        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange("S2", "L", 2, lastS2Row)
        }, ",") + ")" + "+C5";
        row1.createCell(2).setCellFormula(formula);

        row1 = sheet.createRow(row++);
        row1.createCell(0).setCellValue("POS Sale as per Transaction Date (S1)");
        row1.getCell(0).setCellStyle(boldStyle);
        row1.createCell(1).setCellFormula("=B10");
        row1.createCell(2).setCellFormula("=C10");

        row1 = sheet.createRow(row++);
        row1.createCell(0).setCellValue("Difference in POS Sale that falls in subsequent time period (S2)");
        row1.getCell(0).setCellStyle(boldStyle);
        row1.createCell(1).setCellFormula("=B4-B5");
        row1.createCell(2).setCellFormula("=C4-C5");

    }

    public void createSummarySheet(Workbook workbook, Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, ThreePO threePO, Sheet s2, LocalDateTime startDate, LocalDateTime endDate, Row[] rowHeaders) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        int row = 0;

        Row row1 = sheet.createRow(row++);
        row1.createCell(0).setCellValue("Debtor Name");
        row1.getCell(0).setCellStyle(boldStyle);

        row1.createCell(1).setCellValue(threePO.displayName);

        row1 = sheet.createRow(row++);
        row1.createCell(0).setCellValue("Recon Period");
        row1.getCell(0).setCellStyle(boldStyle);
        String date = startDate.format(Formatter.MMMddyyyy) + " - " + endDate.format(Formatter.MMMddyyyy);
        row1.createCell(1).setCellValue(date);

        addBusinessTransactionDateBifurcations(sheet, row, boldStyle, s2);
        row += 5;

        Row headerRow = sheet.createRow(row++);

        headerRow.createCell(0).setCellValue("Reconciliation Summary");
        headerRow.getCell(0).setCellStyle(boldStyle);

        headerRow.createCell(1).setCellValue("As per POS data (POS vs 3PO)");
        headerRow.getCell(1).setCellStyle(boldStyle);
        CellUtil.setAlignment(headerRow.getCell(1), HorizontalAlignment.CENTER);

        headerRow.createCell(6).setCellValue("As per 3PO Data (3PO vs POS)");
        headerRow.getCell(6).setCellStyle(boldStyle);
        CellUtil.setAlignment(headerRow.getCell(6), HorizontalAlignment.CENTER);

        sheet.addMergedRegion(new CellRangeAddress(7, 7, 1, 5));
        sheet.addMergedRegion(new CellRangeAddress(7, 7, 6, 10));

        Row row2 = sheet.createRow(row++);
        int col = 0;
        row2.createCell(col++).setCellValue("Parameters");
        row2.getCell(0).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("No. of orders");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("POS Amount/Calculated");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("3PO Amount/Actual");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Diff. in Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Amount Receivable");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("No. of orders");
        row2.getCell(col - 1).setCellStyle(boldStyle);

        row2.createCell(col++).setCellValue("3PO Amount/Actual");
        row2.getCell(col - 1).setCellStyle(boldStyle);

        row2.createCell(col++).setCellValue("POS Amount/Calculated");
        row2.getCell(col - 1).setCellStyle(boldStyle);

        row2.createCell(col++).setCellValue("Diff. in Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Amount Receivable");
        row2.getCell(col - 1).setCellStyle(boldStyle);

        saleRow(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++);
        switch (threePO) {
            case ZOMATO -> {
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Sale", "K", HorizontalAlignment.RIGHT);
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Addition", "K", HorizontalAlignment.RIGHT);
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Refund", "K", HorizontalAlignment.RIGHT);
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Cancel", "K", HorizontalAlignment.RIGHT);
            }
            case SWIGGY -> {
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Delivered", "K", HorizontalAlignment.RIGHT);
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Cancelled", "K", HorizontalAlignment.RIGHT);
            }
            case MAGICPIN -> {
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Delivered", "K", HorizontalAlignment.CENTER);
                addSaleBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Cancelled", "K", HorizontalAlignment.CENTER);
                addMagicpinCancelledBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "60%", "Cancelled", "K", "=60");
                addMagicpinCancelledBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "89.56% and above", "Cancelled", "K", ">=89.56");
                addMagicpinCancelledBifurcations(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++, "Nil Payment", "Cancelled", "K", "0");
            }
            default -> {
            }
        }

        getReconciledOrderRow(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++);
        cancelledByMerchantFoundInPos(sheet, posVsThreePO, threePOvsPOS, workbook, row++);
        cancelledByMerchantNotFoundInPos(sheet, posVsThreePO, threePOvsPOS, workbook, row++);
        unreconciledOrders(sheet, posVsThreePO, threePOvsPOS, workbook, boldStyle, row++);

        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, ORDER_NOT_FOUND_IN_THREE_PO, "*" + ORDER_NOT_FOUND_IN_POS, "Order Not found in 3PO/POS", "H", "I");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, PACK_CHARGE_MISMATCH, PACK_CHARGE_MISMATCH, PACK_CHARGE_MISMATCH, "R", "S");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, THREE_PO_AMOUNT_GREATER, THREE_PO_AMOUNT_GREATER, THREE_PO_AMOUNT_GREATER, "H", "I");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, POS_AMOUNT_GREATER, POS_AMOUNT_GREATER, POS_AMOUNT_GREATER, "H", "I");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, COMMISSION_MISMATCH, COMMISSION_MISMATCH, COMMISSION_MISMATCH, "V", "W");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, PG_MISMATCH, PG_MISMATCH, PG_MISMATCH, "X", "Y");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, COMMISSION_PG_BOTH_MISMATCH, COMMISSION_PG_BOTH_MISMATCH, COMMISSION_PG_BOTH_MISMATCH, "X", "Y");
//
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, TDS_MISMATCH, TDS_MISMATCH, TDS_MISMATCH, "T", "U");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, GST_CHARGES_MISMATCH, GST_CHARGES_MISMATCH, GST_CHARGES_MISMATCH, "Z", "AA");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, CONSUMER_GST_MISMATCH, CONSUMER_GST_MISMATCH, CONSUMER_GST_MISMATCH, "AB", "AC");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, RECEIVABLE_MISMATCH_POS_GREATER + "*off", RECEIVABLE_MISMATCH_POS_GREATER + "*off", RECEIVABLE_MISMATCH_POS_GREATER + ROUNDING_OFF, "P", "Q");
//        unreconciledShortPaymentRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, RECEIVABLE_MISMATCH_POS_GREATER + "*", RECEIVABLE_MISMATCH_POS_GREATER + "*", "<>*off", RECEIVABLE_MISMATCH_POS_GREATER, "P", "Q");
//        unreconciledReasonWiseRow(sheet, posVsThreePO, threePOvsPOS, workbook, row++, RECEIVABLE_MISMATCH_THREE_PO_GREATER, RECEIVABLE_MISMATCH_THREE_PO_GREATER, RECEIVABLE_MISMATCH_THREE_PO_GREATER, "P", "Q");
        if (ThreePO.ZOMATO.equals(threePO)) {
            Map<String, String> posLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("ZOMATO_POS");
            if (posLogicsMap != null) {
                for (Map.Entry<String, String> keyValue : posLogicsMap.entrySet()) {
                    String resonDiff = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_MISMATCH";
                    String threePOHeaderActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_ACTUAL";
                    String threePOHeaderCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_CALCULATED";
                    unreconciledReasonWiseRowDynamicCases(sheet, posVsThreePO, threePOvsPOS, row++, resonDiff, resonDiff,
                            resonDiff, "Delta ThreePO Difference", threePOHeaderActual, rowHeaders, threePOHeaderCalculated, "POS");
                }
            }
            Map<String, String> selfLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("ZOMATO_SELF");
            if (selfLogicsMap != null) {
                for (Map.Entry<String, String> keyValue : selfLogicsMap.entrySet()) {
                    String resonDiff = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_MISMATCH";
                    String threePOHeaderActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_ACTUAL";
                    String threePOHeaderCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_CALCULATED";
                    unreconciledReasonWiseRowDynamicCases(sheet, posVsThreePO, threePOvsPOS, row++, resonDiff, resonDiff,
                            resonDiff, "Delta ThreePO Difference", threePOHeaderActual, rowHeaders, threePOHeaderCalculated, "SELF");
                }
            }
            Map<String, String> customLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("ZOMATO_CUSTOM");
            if (customLogicsMap != null) {
                for (Map.Entry<String, String> keyValue : customLogicsMap.entrySet()) {
                    String resonDiff = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_MISMATCH";
                    String threePOHeaderActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_ACTUAL";
                    String threePOHeaderCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_CALCULATED";
                    unreconciledReasonWiseRowDynamicCases(sheet, posVsThreePO, threePOvsPOS, row++, resonDiff, resonDiff,
                            resonDiff, "Delta ThreePO Difference", threePOHeaderActual, rowHeaders, threePOHeaderCalculated, "CUSTOM");
                }
            }
            addDeductionRow(sheet, threePOvsPOS, workbook, boldStyle, row++, "Deduction", "K");
        } else if (ThreePO.SWIGGY.equals(threePO)) {
            Map<String, String> posLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("SWIGGY_POS");
            if (posLogicsMap != null) {
                for (Map.Entry<String, String> keyValue : posLogicsMap.entrySet()) {
                    String resonDiff = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_MISMATCH";
                    String threePOHeaderActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_ACTUAL";
                    String threePOHeaderCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_CALCULATED";
                    unreconciledReasonWiseRowDynamicCases(sheet, posVsThreePO, threePOvsPOS, row++, resonDiff, resonDiff,
                            resonDiff, "Delta ThreePO Difference", threePOHeaderActual, rowHeaders, threePOHeaderCalculated, "POS");
                }
            }
            Map<String, String> selfLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("SWIGGY_SELF");
            if (selfLogicsMap != null) {
                for (Map.Entry<String, String> keyValue : selfLogicsMap.entrySet()) {
                    String resonDiff = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_MISMATCH";
                    String threePOHeaderActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_ACTUAL";
                    String threePOHeaderCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_CALCULATED";
                    unreconciledReasonWiseRowDynamicCases(sheet, posVsThreePO, threePOvsPOS, row++, resonDiff, resonDiff,
                            resonDiff, "Delta ThreePO Difference", threePOHeaderActual, rowHeaders, threePOHeaderCalculated, "SELF");
                }
            }
            Map<String, String> customLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("SWIGGY_CUSTOM");
            if (customLogicsMap != null) {
                for (Map.Entry<String, String> keyValue : customLogicsMap.entrySet()) {
                    String resonDiff = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_MISMATCH";
                    String threePOHeaderActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_ACTUAL";
                    String threePOHeaderCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_CALCULATED";
                    unreconciledReasonWiseRowDynamicCases(sheet, posVsThreePO, threePOvsPOS, row++, resonDiff, resonDiff,
                            resonDiff, "Delta ThreePO Difference", threePOHeaderActual, rowHeaders, threePOHeaderCalculated, "CUSTOM");
                }
            }
            addMerchantCancellationRow(sheet, workbook, boldStyle, row++);
        }

        int rowCount = sheet.getLastRowNum() + 1;

        setRegionBorderWithMedium(CellRangeAddress.valueOf("A8:A" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("A8:K8"), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("A9:K9"), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("B8:F" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("G8:K" + rowCount), sheet);

    }

    private void addDeductionRow(Sheet sheet, Sheet threePOvsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber, String orderStatus, String statusColumn) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(orderStatus);
        row.getCell(0).setCellStyle(boldStyle);

        int lastRow = threePOvsPOS.getLastRowNum() + 1;
        String sheetName = threePOvsPOS.getSheetName();

        col += 5;
        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, statusColumn, 2, lastRow),
            "\"<>\"",
            getSheetNameWithRange(sheetName, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Q", 2, lastRow),
            getSheetNameWithRange(sheetName, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
    }

    private void addMerchantCancellationRow(Sheet sheet, Workbook workbook, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Merchant Cancellation Charges Levied");
        row.getCell(0).setCellStyle(boldStyle);

        Sheet threePOvsPOS = workbook.getSheet("Non Zero Merch. Cancel. Charges");
        if (threePOvsPOS != null) {
            int lastRow = threePOvsPOS.getLastRowNum() + 1;
            String sheetName = threePOvsPOS.getSheetName();

            col += 5;
            String formula = "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "G", 2, lastRow)

            }, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow)
            }, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));

            formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow)
            }, ",") + ")";
            row.createCell(col++).setCellFormula(formula);
        }

    }

    private void addMagicpinCancelledBifurcations(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber, String cellName, String orderStatus, String statusColumn, String condition) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(cellName);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        String posAmountColumn = "H";
        String threePOColumn = "I";
        String amountReceivableColumn = "P";
        String percentReceivableColumn = "AP";

        String sheetNameThreePOvsPOS = threePOvsPOS.getSheetName();

        col++;
        col++;
        col++;
        col++;
        col++;

        int lastRow = threePOvsPOS.getLastRowNum() + 1;

//        String formula = "COUNTA(" +  StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow)}, ",") + ")";
        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, "G", 2, lastRow),
            "\"<>\"",
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\"",
            getSheetNameWithRange(sheetNameThreePOvsPOS, percentReceivableColumn, 2, lastRow),
            "\"" + condition + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        //        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, threePOColumn, 2, lastRow)}, ",") + ")";
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, threePOColumn, 2, lastRow),
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\"", getSheetNameWithRange(sheetNameThreePOvsPOS, percentReceivableColumn, 2, lastRow),
            "\"" + condition + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

//        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow)}, ",") + ")";
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow),
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\"", getSheetNameWithRange(sheetNameThreePOvsPOS, percentReceivableColumn, 2, lastRow),
            "\"" + condition + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

//        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, amountReceivableColumn, 2, lastRow)}, ",") + ")";
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, amountReceivableColumn, 2, lastRow),
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\"", getSheetNameWithRange(sheetNameThreePOvsPOS, percentReceivableColumn, 2, lastRow),
            "\"" + condition + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);
    }

    private void addSaleBifurcations(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber, String orderStatus, String statusColumn, HorizontalAlignment horizontalAlignment) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(orderStatus);
        CellUtil.setAlignment(row.getCell(0), horizontalAlignment);

        String posAmountColumn = "H";
        String threePOColumn = "I";
        String amountReceivableColumn = "P";

        String sheetNameThreePOvsPOS = threePOvsPOS.getSheetName();

        col++;
        col++;
        col++;
        col++;
        col++;

        int lastRow = threePOvsPOS.getLastRowNum() + 1;

//        String formula = "COUNTA(" +  StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow)}, ",") + ")";
        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, "G", 2, lastRow),
            "\"<>\"",
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        //        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, threePOColumn, 2, lastRow)}, ",") + ")";
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, threePOColumn, 2, lastRow),
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

//        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow)}, ",") + ")";
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow),
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

//        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, amountReceivableColumn, 2, lastRow)}, ",") + ")";
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, amountReceivableColumn, 2, lastRow),
            getSheetNameWithRange(sheetNameThreePOvsPOS, statusColumn, 2, lastRow),
            "\"" + orderStatus + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);
    }

    private void saleRow(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Sale (As per transaction date)");
        row.getCell(0).setCellStyle(boldStyle);

        String posAmountColumn = "H";
        String threePOColumn = "I";
        String amountReceivableColumn = "P";
        int lastRow = posVsThreePO.getLastRowNum() + 1;

        String sheetNamePosVsThreePo = posVsThreePO.getSheetName();
        String sheetNameThreePOvsPOS = threePOvsPOS.getSheetName();

        String formula = "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNamePosVsThreePo, posAmountColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNamePosVsThreePo, posAmountColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNamePosVsThreePo, threePOColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNamePosVsThreePo, amountReceivableColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        lastRow = threePOvsPOS.getLastRowNum() + 1;

        formula = "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, "G", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, threePOColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, posAmountColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetNameThreePOvsPOS, amountReceivableColumn, 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

    }

    private void getReconciledOrderRow(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Reconciled Orders");
        row.getCell(0).setCellStyle(boldStyle);

        String posAmountColumn = "H";
        String threePOColumn = "I";
        String amountReceivableColumn = "P";
        int lastRow = posVsThreePO.getLastRowNum() + 1;

        String sheetName = posVsThreePO.getSheetName();

        String formula = "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow), "\"reconciled\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow),
            getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"reconciled\"",
            getSheetNameWithRange(sheetName, "M", 2, lastRow),
            "\"<>MERCHANT\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"reconciled\"",
            getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"reconciled\"",
            getSheetNameWithRange(sheetName, "P", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();

        formula = "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow), "\"reconciled\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"reconciled\"",
            getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow),
            getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"reconciled\"",
            getSheetNameWithRange(sheetName, "M", 2, lastRow),
            "\"<>MERCHANT\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"reconciled\"",
            getSheetNameWithRange(sheetName, "P", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

    }

    private void cancelledByMerchantFoundInPos(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Cancelled by Merchant and found in POS");
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        int lastRow = posVsThreePO.getLastRowNum() + 1;
        String sheetName = posVsThreePO.getSheetName();

        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "M", 2, lastRow),
            "\"merchant\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>0\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>0\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.createCell(col++).setCellValue("NA");
        CellUtil.setAlignment(row.getCell(col - 1), HorizontalAlignment.CENTER);

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();

        formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "M", 2, lastRow),
            "\"merchant\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>0\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>0\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"<>0\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

    }

    private void cancelledByMerchantNotFoundInPos(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Cancelled by Merchant and not found in POS");
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        int lastRow = posVsThreePO.getLastRowNum() + 1;
        String sheetName = posVsThreePO.getSheetName();

        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "M", 2, lastRow),
            "\"merchant\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();

        formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "M", 2, lastRow),
            "\"merchant\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "H", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow),
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"Merchant cancelled\"",
            getSheetNameWithRange(sheetName, "C", 2, lastRow),
            "\"\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
    }

    private void unreconciledOrders(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Unreconciled Orders");
        row.getCell(0).setCellStyle(boldStyle);

        int lastRow = posVsThreePO.getLastRowNum() + 1;
        String sheetName = posVsThreePO.getSheetName();

        String formula = "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"", getSheetNameWithRange(sheetName, "H", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"", getSheetNameWithRange(sheetName, "P", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();

        formula = "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"", getSheetNameWithRange(sheetName, "H", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"", getSheetNameWithRange(sheetName, "P", 2, lastRow)}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
        row.getCell(col - 1).setCellStyle(boldStyle);

    }

    private void unreconciledShortPaymentRow(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, int rowNumber, String reason1, String reason2, String reason3, String cellName, String posColumn, String threepoColumn) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(cellName);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        int lastRow = posVsThreePO.getLastRowNum() + 1;
        String sheetName = posVsThreePO.getSheetName();
        String reason = reason1;

        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, posColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();
        reason = reason2;

        formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, posColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason3 + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
    }

    private void unreconciledReasonWiseRow(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, Workbook workbook, int rowNumber, String reason1, String reason2, String cellName, String posColumn, String threepoColumn) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(cellName);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        int lastRow = posVsThreePO.getLastRowNum() + 1;
        String sheetName = posVsThreePO.getSheetName();
        String reason = reason1;

        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        if (!posColumn.isEmpty()) {
            formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, posColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);
        } else {
            row.createCell(col++).setBlank();
        }

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();
        reason = reason2;

        formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, posColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-I" + (rowNumber + 1));

        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "P", 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);
    }

    private void unreconciledReasonWiseRowDynamicCases(Sheet sheet, Sheet posVsThreePO, Sheet threePOvsPOS, int rowNumber, String reason1, String reason2, String cellName, String threepoDiffHeader, String threePOHeaderActual, Row[] rowHeaders, String threePOHeaderCalculated, String reasonType) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(cellName);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        int lastRow = posVsThreePO.getLastRowNum() + 1;
        String sheetName = posVsThreePO.getSheetName();
        String reason = reason1;
        String formula;
        if (reasonType.equals("POS")) {
            formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            String threepoColumnCalculated = getColumnLetterFromHeader(posVsThreePO, threePOHeaderCalculated, rowHeaders[0]);
            formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumnCalculated, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            String threepoColumn = getColumnLetterFromHeader(threePOvsPOS, threePOHeaderActual, rowHeaders[1]);
            formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            String threepoDiffColumn = getColumnLetterFromHeader(posVsThreePO, threepoDiffHeader, rowHeaders[0]);
            formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoDiffColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);

            row.createCell(col++).setBlank();
        } else {
            row.createCell(col++).setBlank();
            row.createCell(col++).setBlank();
            row.createCell(col++).setBlank();
            row.createCell(col++).setBlank();
            row.createCell(col++).setBlank();
        }

        lastRow = threePOvsPOS.getLastRowNum() + 1;
        sheetName = threePOvsPOS.getSheetName();
        reason = reason2;

        formula = "COUNTIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        if (!threePOHeaderActual.isEmpty()) {
            String threepoColumn = getColumnLetterFromHeader(threePOvsPOS, threePOHeaderActual, rowHeaders[1]);
            formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);
        } else {
            row.createCell(col++).setBlank();
        }

        if (!threePOHeaderCalculated.isEmpty()) {
            String threepoColumnPos = getColumnLetterFromHeader(threePOvsPOS, threePOHeaderCalculated, rowHeaders[1]);
            formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoColumnPos, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
                "\"unreconciled\"",
                getSheetNameWithRange(sheetName, "AM", 2, lastRow),
                "\"" + reason + "\""}, ",") + ")";
            row.createCell(col++).setCellFormula(formula);
        } else {
            row.createCell(col++).setBlank();
        }

        String threepoDiffColumn = getColumnLetterFromHeader(threePOvsPOS, threepoDiffHeader, rowHeaders[1]);
        formula = "SUMIFS(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, threepoDiffColumn, 2, lastRow), getSheetNameWithRange(sheetName, "AL", 2, lastRow),
            "\"unreconciled\"",
            getSheetNameWithRange(sheetName, "AM", 2, lastRow),
            "\"" + reason + "\""}, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setBlank();
    }

    private String getSheetNameWithRange(String sheetName, String column, int start, int end) {
        end = Math.max(end, start);
        return "'" + sheetName + "'" + "!" + column + start + ":" + column + end;
    }

    public String getColumnLetterFromHeader(Sheet sheet, String headerName, Row headerRow) {
        if (headerRow != null) {
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null && cell.getStringCellValue().equals(headerName)) {
                    return CellReference.convertNumToColString(i);  // Using Apache POI utility
                }
            }
        }
        return "";  // Return an empty string if not found
    }

}
