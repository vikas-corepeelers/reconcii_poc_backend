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
public class TRMMPRSummarySheetUtil {

    private void setRegionBorderWithMedium(CellRangeAddress region, Sheet sheet) {
        RegionUtil.setBorderBottom(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderLeft(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderRight(BorderStyle.MEDIUM, region, sheet);
        RegionUtil.setBorderTop(BorderStyle.MEDIUM, region, sheet);
    }

    public void createSummarySheet(Workbook workbook, Sheet sheet, List<Sheet> trmVsMprList, List<Sheet> mprVsTrmList) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        CellStyle boldStyle = workbook.createCellStyle();
        boldStyle.setFont(boldFont);
        int row = 0;
        Row headerRow = sheet.createRow(row++);

        headerRow.createCell(0).setCellValue("Summary Sheet");
        headerRow.getCell(0).setCellStyle(boldStyle);


        headerRow.createCell(1).setCellValue("As per TRM data");
        headerRow.getCell(1).setCellStyle(boldStyle);
        CellUtil.setAlignment(headerRow.getCell(1), HorizontalAlignment.CENTER);


        headerRow.createCell(7).setCellValue("As per MPR Data");
        headerRow.getCell(7).setCellStyle(boldStyle);
        CellUtil.setAlignment(headerRow.getCell(7), HorizontalAlignment.CENTER);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 6));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 7, 12));

        Row row2 = sheet.createRow(row++);
        int col = 0;
        row2.createCell(col++).setCellValue("Parameters");
        row2.getCell(0).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("No. of orders");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("TRM Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("MPR Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Diff. in Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Charges");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Amount Receivable");

        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("No. of orders");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("TRM Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("MPR Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Diff. in Amount");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Charges");
        row2.getCell(col - 1).setCellStyle(boldStyle);
        row2.createCell(col++).setCellValue("Amount Receivable");
        row2.getCell(col - 1).setCellStyle(boldStyle);


        saleRow(sheet, trmVsMprList, mprVsTrmList, boldStyle, row++);
        tenderWiseSale("UPI", sheet, trmVsMprList, mprVsTrmList, boldStyle, row++);
        tenderWiseSale("Card", sheet, trmVsMprList, mprVsTrmList, boldStyle, row++);
        reconciled(sheet, trmVsMprList, mprVsTrmList, boldStyle, row++);
        unreconciled(sheet, trmVsMprList, mprVsTrmList, boldStyle, row++);
        unreconciledReasonWiseRow(sheet, trmVsMprList, mprVsTrmList, row++, "Order Amount Mismatch (TRM > MPR)", TRM_AMT_GREATER, TRM_AMT_GREATER);
        unreconciledReasonWiseRow(sheet, trmVsMprList, mprVsTrmList, row++, "Order Amount Mismatch (MPR > TRM)", MPR_AMT_GREATER, MPR_AMT_GREATER);
        unreconciledReasonWiseRow(sheet, trmVsMprList, mprVsTrmList, row++, "Order Not found in TRM/MPR", TXN_NOT_FOUND_IN_MPR, TXN_NOT_FOUND_IN_TRM);


        int rowCount = sheet.getLastRowNum() + 1;

        setRegionBorderWithMedium(CellRangeAddress.valueOf("A1:A" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("B1:G" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("H1:M" + rowCount), sheet);
        setRegionBorderWithMedium(CellRangeAddress.valueOf("A2:M2"), sheet);

        sheet.createRow(row + 3).createCell(0).setCellValue("*MPR vs TRM does not restrict the date range due to settle date difference");

    }

    private void unreconciledReasonWiseRow(Sheet sheet, List<Sheet> trmVsMprList, List<Sheet>  MprvsTrmList, int rowNumber, String cellName, String reason1, String reason2) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(cellName);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);

        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "",formulaEColumn = "";
        for (Sheet trmVsMpr : trmVsMprList) {
            int lastRow = trmVsMpr.getLastRowNum() + 1;
            String sheetName = trmVsMpr.getSheetName();
            String reason = reason1;
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+"COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\""}, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)}, ",") + ")";
            formulaEColumn += (formulaEColumn == "" ? "" : "+")+"SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AH", 2, lastRow)}, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaBColumn);

        row.createCell(col++).setCellFormula(formulaCColumn);

        row.createCell(col++).setCellFormula(formulaDColumn);

        row.createCell(col++).setCellFormula(formulaEColumn);

        row.createCell(col++).setCellValue("-");
        row.createCell(col++).setCellValue("-");


        String formulaHColumn="",formulaIColumn="",formulaJColumn="",formulaKColumn="";
        for (Sheet mprvsTrm : MprvsTrmList) {
            int lastRow = mprvsTrm.getLastRowNum() + 1;
            String sheetName = mprvsTrm.getSheetName();
            String reason = reason2;
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\""}, ",") + ")";
            formulaIColumn += (formulaIColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)}, ",") + ")";
            formulaJColumn += (formulaJColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)}, ",") + ")";
            formulaKColumn += (formulaKColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"" + reason + "\"", getSheetNameWithRange(sheetName, "AH", 2, lastRow)}, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaHColumn);

        row.createCell(col++).setCellFormula(formulaIColumn);

        row.createCell(col++).setCellFormula(formulaJColumn);

        row.createCell(col++).setCellFormula(formulaKColumn);

        row.createCell(col++).setCellValue("-");
        row.createCell(col++).setCellValue("-");
    }


    private void unreconciled(Sheet sheet, List<Sheet> trmVsMprList, List<Sheet>  MprvsTrmList, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Unreconciled Orders");
        row.getCell(0).setCellStyle(boldStyle);
        String formulaBColumn = "", formulaCColumn = "";
        for (Sheet trmVsMpr : trmVsMprList) {
            int lastRow = trmVsMpr.getLastRowNum() + 1;
            String sheetName = trmVsMpr.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AH", 2, lastRow)
                    , "\">0\""
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AH", 2, lastRow)
                    , "\">0\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";

        }
        row.createCell(col++).setCellFormula(formulaBColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaCColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellValue(0);

        row.createCell(col++).setCellFormula("C" + (rowNumber + 1) + "-D" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellValue("");

        row.createCell(col++).setCellValue(0);

        String formulaHColumn="",formulaJColumn="";
        for (Sheet mprvsTrm : MprvsTrmList) {
            int lastRow = mprvsTrm.getLastRowNum() + 1;
            String sheetName = mprvsTrm.getSheetName();
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AH", 2, lastRow)
                    , "\">0\""
            }, ",") + ")";
            formulaJColumn += (formulaJColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AH", 2, lastRow)
                    , "\">0\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellValue(0);

        row.createCell(col++).setCellFormula(formulaJColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("J" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellValue("");

        row.createCell(col++).setCellValue(0);

    }

    private void reconciled(Sheet sheet, List<Sheet> trmVsMprList, List<Sheet>  MprvsTrmList , CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Reconciled Orders");
        row.getCell(0).setCellStyle(boldStyle);
        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "",formulaFColumn = "",formulaGColumn = "";
        for (Sheet trmVsMpr : trmVsMprList) {
            int lastRow = trmVsMpr.getLastRowNum() + 1;
            String sheetName = trmVsMpr.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\""
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
            formulaFColumn += (formulaFColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "AA", 2, lastRow)
            }, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "AB", 2, lastRow)
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

        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaHColumn="",formulaIColumn="",formulaJColumn="",formulaLColumn="",formulaMColumn="";
        for (Sheet mprvsTrm : MprvsTrmList) {
            int lastRow = mprvsTrm.getLastRowNum() + 1;
            String sheetName = mprvsTrm.getSheetName();
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\""
            }, ",") + ")";
            formulaIColumn += (formulaIColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaJColumn += (formulaJColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
            formulaLColumn += (formulaLColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "AA", 2, lastRow)
            }, ",") + ")";
            formulaMColumn += (formulaMColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AG", 2, lastRow)
                    , "\"MATCHED\"", getSheetNameWithRange(sheetName, "AB", 2, lastRow)
            }, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaIColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaJColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("J" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaLColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaMColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
    }

    private void tenderWiseSale(String tender,Sheet sheet, List<Sheet> trmVsMprList, List<Sheet>  MprvsTrmList, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue(tender);
        row.getCell(0).setCellStyle(boldStyle);
        CellUtil.setAlignment(row.getCell(0), HorizontalAlignment.RIGHT);
        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "",formulaFColumn = "",formulaGColumn = "";
        for (Sheet trmVsMpr : trmVsMprList) {
            int lastRow = trmVsMpr.getLastRowNum() + 1;
            String sheetName = trmVsMpr.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "B", 2, lastRow)
                    , "\"*" + tender + "*\""
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "B", 2, lastRow)
                    , "\"*" + tender + "*\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
            formulaFColumn += (formulaFColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, "AA", 2, lastRow)
            }, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, "AB", 2, lastRow)
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

        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaHColumn="",formulaIColumn="",formulaJColumn="",formulaLColumn="",formulaMColumn="";
        for (Sheet mprvsTrm : MprvsTrmList) {
            int lastRow = mprvsTrm.getLastRowNum() + 1;
            String sheetName = mprvsTrm.getSheetName();
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+ "COUNTIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"*" + tender + "*\""
            }, ",") + ")";
            formulaIColumn += (formulaIColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "B", 2, lastRow)
                    , "\"*" + tender + "*\"", getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaJColumn += (formulaJColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
            formulaLColumn += (formulaLColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, "AA", 2, lastRow)
            }, ",") + ")";
            formulaMColumn += (formulaMColumn == "" ? "" : "+")+ "SUMIF(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "U", 2, lastRow)
                    , "\"" + tender + "\"", getSheetNameWithRange(sheetName, "AB", 2, lastRow)
            }, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaIColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaJColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);


        row.createCell(col++).setCellFormula("J" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaLColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaMColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

    }

    private void saleRow(Sheet sheet, List<Sheet> trmVsMprList, List<Sheet>  MprvsTrmList, CellStyle boldStyle, int rowNumber) {
        Row row = sheet.createRow(rowNumber);
        int col = 0;
        row.createCell(col++).setCellValue("Sale");
        row.getCell(0).setCellStyle(boldStyle);
        String formulaBColumn = "", formulaCColumn = "", formulaDColumn = "",formulaFColumn = "",formulaGColumn = "";
        for (Sheet trmVsMpr : trmVsMprList) {
            int lastRow = trmVsMpr.getLastRowNum() + 1;
            String sheetName = trmVsMpr.getSheetName();
            formulaBColumn += (formulaBColumn == "" ? "" : "+")+ "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "B", 2, lastRow)
            }, ",") + ")";
            formulaCColumn += (formulaCColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaDColumn += (formulaDColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
            formulaFColumn += (formulaFColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
            }, ",") + ")";
            formulaGColumn += (formulaGColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AB", 2, lastRow)
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

        row.createCell(col++).setCellFormula(formulaFColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaGColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        String formulaHColumn="",formulaIColumn="",formulaJColumn="",formulaLColumn="",formulaMColumn="";
        for (Sheet mprvsTrm : MprvsTrmList) {
            int lastRow = mprvsTrm.getLastRowNum() + 1;
            String sheetName = mprvsTrm.getSheetName();
            formulaHColumn += (formulaHColumn == "" ? "" : "+")+ "COUNTA(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaIColumn += (formulaIColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "I", 2, lastRow)
            }, ",") + ")";
            formulaJColumn += (formulaJColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "Z", 2, lastRow)
            }, ",") + ")";
            formulaLColumn += (formulaLColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AA", 2, lastRow)
            }, ",") + ")";
            formulaMColumn += (formulaMColumn == "" ? "" : "+")+ "SUM(" + StringUtils.join(new String[]{getSheetNameWithRange(sheetName, "AB", 2, lastRow)
            }, ",") + ")";
        }

        row.createCell(col++).setCellFormula(formulaHColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaIColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaJColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula("J" + (rowNumber + 1) + "-I" + (rowNumber + 1));
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaLColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);

        row.createCell(col++).setCellFormula(formulaMColumn);
        row.getCell(col - 1).setCellStyle(boldStyle);
    }


    private String getSheetNameWithRange(String sheetName, String column, int start, int end) {
        end = Math.max(end,start);
        return "'" + sheetName + "'" + "!" + column + start + ":" + column + end;
    }


}
