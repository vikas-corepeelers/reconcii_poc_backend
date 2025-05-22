package com.cpl.reconciliation.web.service.impl.instore;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public abstract class AbstractInStoreService {

    @Autowired
    protected NamedParameterJdbcTemplate jdbcTemplate;

    public static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // Set the background color to blue
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Set the text color to white
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    public static CellStyle createTrueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // Set the background color to blue
        style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Set the text color to white
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(false);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    public static CellStyle createFalseStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // Set the background color to blue
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // Set the text color to white
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(false);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
}
