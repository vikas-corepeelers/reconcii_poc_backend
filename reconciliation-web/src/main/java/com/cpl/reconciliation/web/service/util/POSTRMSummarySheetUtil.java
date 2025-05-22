package com.cpl.reconciliation.web.service.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.ss.util.RegionUtil;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.cpl.reconciliation.domain.util.Constants.*;

@Service
public class POSTRMSummarySheetUtil {

    private void setRegionBorderWithMedium(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
    }

//    public void createSummarySheet(Workbook workbook, Sheet sheet, Sheet posVsTrm, Sheet trmVsPos) {
//        Font boldFont = workbook.createFont();
//        boldFont.setBold(true);
//        CellStyle boldStyle = workbook.createCellStyle();
//        boldStyle.setFont(boldFont);
//        int row = 0;
//        Row headerRow = sheet.createRow(row++);
//
//        headerRow.createCell(0).setCellValue("Reconciliation Summary");
//        headerRow.getCell(0).setCellStyle(boldStyle);
//
//
//        headerRow.createCell(1).setCellValue("As per POS data");
//        headerRow.getCell(1).setCellStyle(boldStyle);
//        CellUtil.setAlignment(headerRow.getCell(1), HorizontalAlignment.CENTER);
//
//
//        headerRow.createCell(5).setCellValue("As per TRM Data");
//        headerRow.getCell(5).setCellStyle(boldStyle);
//        CellUtil.setAlignment(headerRow.getCell(5), HorizontalAlignment.CENTER);
//
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 8));
//
//        Row row2 = sheet.createRow(row++);
//        int col = 0;
//        row2.createCell(col++).setCellValue("Parameters");
//        row2.getCell(0).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("No. of orders");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("POS Amount");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("TRM Amount");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("Diff. in Amount");
//
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("No. of orders");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("POS Amount");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("TRM Amount");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//        row2.createCell(col++).setCellValue("Diff. in Amount");
//        row2.getCell(col - 1).setCellStyle(boldStyle);
//
//        saleRow(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
//        tenderWiseSale("UPI", sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
//        tenderWiseSale("Card", sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
//        reconciled(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
//        unreconciled(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
////        amountMismatchPOSvsTRM(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
////        amountMismatchTRMvsPOS(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
//        unreconciledReasonWiseRow(sheet, posVsTrm, trmVsPos, workbook, row++, "Amount Mismatch (POS > TRM)", POS_AMT_GREATER_THAN_TRM, POS_AMT_GREATER_THAN_TRM);
//        unreconciledReasonWiseRow(sheet, posVsTrm, trmVsPos, workbook, row++, "Amount Mismatch (TRM > POS)", TRM_AMT_GREATER_THAN_POS, TRM_AMT_GREATER_THAN_POS);
//        unreconciledReasonWiseRow(sheet, posVsTrm, trmVsPos, workbook, row++, "Order Not found in POS/TRM", TXN_NOT_FOUND_IN_TRM, TXN_NOT_FOUND_IN_POS);
//
//        int rowCount = sheet.getLastRowNum() + 1;
//
//        setRegionBorderWithMedium(CellRangeAddress.valueOf("A1:A" + rowCount), sheet);
//        setRegionBorderWithMedium(CellRangeAddress.valueOf("B1:E" + rowCount), sheet);
//        setRegionBorderWithMedium(CellRangeAddress.valueOf("F1:I" + rowCount), sheet);
//        setRegionBorderWithMedium(CellRangeAddress.valueOf("A2:I2"), sheet);
//
//
//    }

    public void createSummarySheet(Workbook workbook, Sheet sheet, List<Sheet> posVsTrmList, List<Sheet> trmVsPosList) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        int row = 0;
        Row headerRow = sheet.createRow(row++);

        headerRow.createCell(0).setCellValue("Reconciliation Summary");
        headerRow.getCell(0).setCellStyle(boldStyle);


        headerRow.createCell(1).setCellValue("As per POS data");
        headerRow.getCell(1).setCellStyle(boldStyle);
        CellUtil.setAlignment(headerRow.getCell(1), HorizontalAlignment.CENTER);


        headerRow.createCell(5).setCellValue("As per TRM Data");
        headerRow.getCell(5).setCellStyle(boldStyle);
        CellUtil.setAlignment(headerRow.getCell(5), HorizontalAlignment.CENTER);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 4));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 5, 8));

        Row row2 = sheet.createRow(row++);
        int col = 0;
        row2.createCell(col++).setCellValue("Parameters");
        row2.getCell(0).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("No. of orders");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("POS Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("TRM Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Diff. in Amount");

        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("No. of orders");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("POS Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("TRM Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Diff. in Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);

        saleRow(sheet, posVsTrmList, trmVsPosList, boldStyle, row++);
        tenderWiseSale("UPI", sheet, posVsTrmList, trmVsPosList, boldStyle, row++);
        tenderWiseSale("Card", sheet, posVsTrmList, trmVsPosList, boldStyle, row++);
        reconciled(sheet, posVsTrmList, trmVsPosList, boldStyle, row++);
        unreconciled(sheet, posVsTrmList, trmVsPosList, boldStyle, row++);
//        amountMismatchPOSvsTRM(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
//        amountMismatchTRMvsPOS(sheet, posVsTrm, trmVsPos, workbook, boldStyle, row++);
        unreconciledReasonWiseRow(sheet, posVsTrmList, trmVsPosList, workbook, row++, "Amount Mismatch (POS > TRM)", POS_AMT_GREATER_THAN_TRM, POS_AMT_GREATER_THAN_TRM);
        unreconciledReasonWiseRow(sheet, posVsTrmList, trmVsPosList, workbook, row++, "Amount Mismatch (TRM > POS)", TRM_AMT_GREATER_THAN_POS, TRM_AMT_GREATER_THAN_POS);
        unreconciledReasonWiseRow(sheet, posVsTrmList, trmVsPosList, workbook, row++, "Order Not found in POS/TRM", TXN_NOT_FOUND_IN_TRM, TXN_NOT_FOUND_IN_POS);

        int rowCount = sheet.getLastRowNum() + 1;

        setRegionBorderWithMedium(CellRangeAddress.valueOf("A1:A" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("B1:E" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("F1:I" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("A2:I2"), sheet);


    }

    private void unreconciledReasonWiseRow(Sheet sheet, List<Sheet> posVsTRMsheets, List<Sheet> trmVsPOSsheets, Workbook workbook, int rowNumber, String cellName, String reason1, String reason2) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(cellName);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "";
        for (Sheet posVsTRM : posVsTRMsheets) {
            int lastRow = posVsTRM.getLastRowNum() + 1;
            String sheetName = posVsTRM.getSheetName();
            String reason = reason1;
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
                    , "\"" + reason + "\""}, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)}, ",") + ")";
        }
        row.createCell(col++).setCellFormula(formulaBColumn);
        row.createCell(col++).setCellFormula(formulaCColumn);
        row.createCell(col++).setCellFormula(formulaDColumn);
        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));

        String formulaFColumn="",formulaGColumn="",formulaHColumn="";
        for (Sheet trmVsPOS : trmVsPOSsheets) {
            int lastRow = trmVsPOS.getLastRowNum() + 1;
            String sheetName = trmVsPOS.getSheetName();
            String reason = reason2;
            formulaFColumn += (formulaFColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
                    , "\"" + reason + "\""}, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)}, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaFColumn);
        row.createCell(col++).setCellFormula(formulaGColumn);
        row.createCell(col++).setCellFormula(formulaHColumn);
        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-G" + (rowNumber + 1));
    }

    private void amountMismatchPOSvsTRM(Sheet sheet, Sheet posVsTRM, Sheet trmVsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Order Amount Mismatch (POS vs TRM)");
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);


        int lastRow = posVsTRM.getLastRowNum() + 1;

        String sheetName = posVsTRM.getSheetName();

        String formula = "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                , "\">0\""
        }, ",") + ")";
        row.createCell(col++).setCellFormula(formula);


        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                , "\">0\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
        }, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                , "\">0\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)
        }, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));


        row.createCell(col++).setCellValue("-");

        row.createCell(col++).setCellValue("-");

        row.createCell(col++).setCellValue("-");

        row.createCell(col++).setCellValue("-");

    }

    private void amountMismatchTRMvsPOS(Sheet sheet, Sheet posVsTRM, Sheet trmVsPOS, Workbook workbook, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Order Amount Mismatch (TRM vs POS)");
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);


        int lastRow = posVsTRM.getLastRowNum() + 1;

        String sheetName = posVsTRM.getSheetName();

        row.createCell(col++).setCellValue("-");

        row.createCell(col++).setCellValue("-");

        row.createCell(col++).setCellValue("-");

        row.createCell(col++).setCellValue("-");


        lastRow = trmVsPOS.getLastRowNum() + 1;
        sheetName = trmVsPOS.getSheetName();

        String formula = "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                , "\">0\""
        }, ",") + ")";
        row.createCell(col++).setCellFormula(formula);


        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                , "\">0\"", getSheetNameWithRange(sheetName, "G", 2, lastRow)
        }, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        formula = "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                , "\">0\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)
        }, ",") + ")";
        row.createCell(col++).setCellFormula(formula);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-G" + (rowNumber + 1));
    }

    private void unreconciled(Sheet sheet, List<Sheet> posVsTRMsheets, List<Sheet> trmVsPOSsheets, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("UnReconciled");
        row.getCell(0).setCellStyle(boldStyle);

        String posAmountColumn = "I";
        String trmColumn = "Y";

        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "";
        for (Sheet posVsTRM : posVsTRMsheets) {
            int lastRow = posVsTRM.getLastRowNum() + 1;
            String sheetName = posVsTRM.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\">0\""
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\">0\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\">0\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)
            }, ",") + ")";
        }
        row.createCell(col++).setCellFormula(formulaBColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaCColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaDColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaFColumn="",formulaGColumn="",formulaHColumn="";
        for (Sheet trmVsPOS : trmVsPOSsheets) {
            int lastRow = trmVsPOS.getLastRowNum() + 1;
            String sheetName = trmVsPOS.getSheetName();

            formulaFColumn += (formulaFColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\">0\""
            }, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\">0\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\">0\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)
            }, ",") + ")";
        }
        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-G" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);
    }

    private void reconciled(Sheet sheet, List<Sheet> posVsTRMsheets, List<Sheet> trmVsPOSsheets, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Reconciled");
        row.getCell(0).setCellStyle(boldStyle);

        String posAmountColumn = "I";
        String trmColumn = "Y";
        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "";
        for (Sheet posVsTRM : posVsTRMsheets) {
            int lastRow = posVsTRM.getLastRowNum() + 1;
            String sheetName = posVsTRM.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\"=0\""
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\"=0\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\"=0\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)
            }, ",") + ")";
        }
        row.createCell(col++).setCellFormula(formulaBColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaCColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaDColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaFColumn="",formulaGColumn="",formulaHColumn="";
        for (Sheet trmVsPOS : trmVsPOSsheets) {
            int lastRow = trmVsPOS.getLastRowNum() + 1;
            String sheetName = trmVsPOS.getSheetName();

            formulaFColumn += (formulaFColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\"=0\""
            }, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\"=0\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
                    , "\"=0\"", getSheetNameWithRange(sheetName, "Y", 2, lastRow)
            }, ",") + ")";
        }
        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-G" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);
    }

    private void tenderWiseSale(String tender, Sheet sheet, List<Sheet> posVsTRMsheets, List<Sheet> trmVsPOSsheets, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(tender);
        row.getCell(0).setCellStyle(boldStyle);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);


        String posAmountColumn = "I";
        String trmColumn = "Y";
        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "";
        for (Sheet posVsTRM : posVsTRMsheets) {
            int lastRow = posVsTRM.getLastRowNum() + 1;
            String sheetName = posVsTRM.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "F", 2, lastRow)
                    , "\"*" + tender + "*\""
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "F", 2, lastRow)
                    , "\"*" + tender + "*\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "S", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, trmColumn, 2, lastRow)
            }, ",") + ")";
        }
        //Formula B column
        row.createCell(col++).setCellFormula(formulaBColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula C column
        row.createCell(col++).setCellFormula(formulaCColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula D column
        row.createCell(col++).setCellFormula(formulaDColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula E column
        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaFColumn="",formulaGColumn="",formulaHColumn="";
        for (Sheet trmVsPOS : trmVsPOSsheets) {
            int lastRow = trmVsPOS.getLastRowNum() + 1;
            String sheetName = trmVsPOS.getSheetName();
            formulaFColumn += (formulaFColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "S", 2, lastRow)
                    , "\"*" + tender + "*\""
            }, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "F", 2, lastRow)
                    , "\"*" + tender + "*\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "S", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, trmColumn, 2, lastRow)
            }, ",") + ")";
        }
        //Formula F column
        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula G column
        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula H column
        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula I column
        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-G" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

    }

    private void saleRow(Sheet sheet, List<Sheet> posVsTRMsheets, List<Sheet> trmVsPOSsheets, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Sale");
        row.getCell(0).setCellStyle(boldStyle);

        String posAmountColumn = "I";
        String trmColumn = "Y";
        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "";
        for (Sheet posVsTRM : posVsTRMsheets) {
            int lastRow = posVsTRM.getLastRowNum() + 1;
            String sheetName = posVsTRM.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+ "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "F", 2, lastRow)
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, posAmountColumn, 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, trmColumn, 2, lastRow)
            }, ",") + ")";
        }
        //Formula B column
        row.createCell(col++).setCellFormula(formulaBColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula C column
        row.createCell(col++).setCellFormula(formulaCColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula D column
        row.createCell(col++).setCellFormula(formulaDColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula E column
        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaFColumn="",formulaGColumn="",formulaHColumn="";
        for (Sheet trmVsPOS : trmVsPOSsheets) {
            int lastRow = trmVsPOS.getLastRowNum() + 1;
            String sheetName = trmVsPOS.getSheetName();
            formulaFColumn += (formulaFColumn == "" ? "" : "+")+"COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "K", 2, lastRow)}, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+"SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+"SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Y", 2, lastRow)}, ",") + ")";
        }
        //Formula F column
        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula G column
        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula H column
        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
        //Formula I column
        row.createCell(col++).setCellFormula("H" + (rowNumber + 1) + "-G" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);
    }


    private String getSheetNameWithRange(String sheetName, String column, int start, int end) {
        end = Math.max(end,start);
        return "'" + sheetName + "'" + "!" + column + start + ":" + column + end;
    }


}
