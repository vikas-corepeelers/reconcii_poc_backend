package com.cpl.reconciliation.web.service.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import static com.cpl.core.api.constant.Formatter.DDMMYYYY_DASH;

@Data
@Slf4j
@Service
public class VoucherCreationUtil {

    private final Environment env;

    public String getNarration(String bank, String paymentType, NarrationType narrationType, LocalDate date) {
        String key = String.join(".", new String[]{"voucher", bank, paymentType, narrationType.name(), "narration"}).toLowerCase();
        String narration = env.getProperty(key, "");
        int n = narration.split("%s", -1).length - 1;
        if (n > 1) {
            return String.format(narration, date.minusDays(1).format(DDMMYYYY_DASH), date.format(DDMMYYYY_DASH));
        }
        return String.format(narration, date.format(DDMMYYYY_DASH));
    }

    public String getTransferNarration(String bank, String paymentType, NarrationType narrationType, LocalDate date,String accountNo) {
        String key = String.join(".", new String[]{"voucher", bank, paymentType, narrationType.name(), "narration"}).toLowerCase();
        String narration = env.getProperty(key, "");
        if (accountNo!=null && accountNo.length()>3){
            narration = narration+accountNo.substring(accountNo.length()-4);
        }
        return narration;
    }
    public String getVersion(String bank, String paymentType, NarrationType narrationType) {
        String key = String.join(".", new String[]{"voucher", bank, paymentType, narrationType.name(), "version"}).toLowerCase();
        return env.getProperty(key, "");
    }

    public String getBankReference(String bank, String paymentType, ResultSet resultSet) throws SQLException {
        switch (bank) {
            case "HDFC" -> {
                if (paymentType.equalsIgnoreCase("CARD")) {
                    String narration = resultSet.getString("narration");
                    String date = narration.substring(narration.length() - 8);
                    String tid = narration.split("TERMINAL")[0];
                    return tid + " " + date;

                } else if (paymentType.equalsIgnoreCase("UPI")) {
                    return resultSet.getString("narration").split("-", 2)[1];
                }
            }
            case "ICICI" -> {
                return resultSet.getString("transaction_id");
            }
            case "AMEX" -> {
                return resultSet.getString("chq_ref_no");
            }
            case "SBI" -> {
                String narration = resultSet.getString("narration");
                String keyword = "BULK POSTING-CR_CONNAUGHT PLAZA RESTAU";

                int startIndex = narration.indexOf(keyword);
                int endIndex = narration.indexOf("--");

                try {
                    if (startIndex != -1 && endIndex != -1) {
                        return narration.substring(startIndex + keyword.length() + 1, endIndex).trim();
                    }
                    throw new RuntimeException();

                } catch (Exception e) {
                    log.error("SBI narration not as per desired format while fetching reference for voucher: {}", narration);
                }
            }
        }
        return "";
    }

    public enum NarrationType {
        REFUND,
        BS,
        MPR,
        CHARGES,
        DEBIT,
        CREDIT

    }

    public static CellStyle getBlackCellStyle(XSSFWorkbook workbook){
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        style.setFont(font);
        return style;
    }

    public static CellStyle getRedCellStyle(XSSFWorkbook workbook){
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
        style.setFont(font);
        return style;
    }
}
