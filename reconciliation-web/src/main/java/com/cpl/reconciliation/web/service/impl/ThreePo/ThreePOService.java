package com.cpl.reconciliation.web.service.impl.ThreePo;

import com.cpl.core.api.constant.Formatter;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;
import com.cpl.reconciliation.domain.models.ThreePOReport;
import com.cpl.reconciliation.web.service.ThreePoQueries;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH;
import static com.cpl.core.api.constant.Formatter.YYYYMMDD_HHMMSS_DASH;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import static com.cpl.reconciliation.domain.util.Constants.ORDER_NOT_FOUND_IN_THREE_PO;
import static com.cpl.reconciliation.domain.util.Constants.ROUNDING_OFF;
import com.cpl.reconciliation.domain.util.QueryConfig;
import static com.cpl.reconciliation.domain.util.QueryConfig.TENDER_WISE_UNRECONCILED_REASONS_MAP;
import com.cpl.reconciliation.web.service.util.ThreepoQueryDateRangeRecoLogicAndFEUtil;
import java.sql.ResultSetMetaData;
import java.util.Arrays;

@Slf4j
public abstract class ThreePOService {

    public abstract MissingThreePOMapping getThreePoMissingMapping();

    public abstract void downloadThreePoMissingMapping(Workbook workbook);

    public void setThreePoReportValuesFromSql(ThreePOReport threePOReportPOS, String source, ResultSet resultSet) throws SQLException {
        threePOReportPOS.setThreePO(ThreePO.getEnum(source));

        LocalDate businessDate = null;
        LocalDateTime orderDate = null;
        if (resultSet.getString("businessDate") != null) {
            businessDate = LocalDate.parse(resultSet.getString("businessDate"), YYYYMMDD_DASH);
        }
        if (resultSet.getString("orderDate") != null) {
            if (source.equalsIgnoreCase("MAGICPIN")) {
                orderDate = LocalDate.parse(resultSet.getString("orderDate"), YYYYMMDD_DASH).atStartOfDay();
            } else {
                orderDate = LocalDateTime.parse(resultSet.getString("orderDate"), YYYYMMDD_HHMMSS_DASH);
            }
        }

        threePOReportPOS.setBusinessDate(businessDate);
        threePOReportPOS.setInvoiceNumber(resultSet.getString("invoiceNumber"));
        threePOReportPOS.setReceiptNumber(resultSet.getString("receiptNumber"));
        threePOReportPOS.setPosId(resultSet.getString("posId"));
        threePOReportPOS.setOrderDate(orderDate);
        threePOReportPOS.setStoreCode(resultSet.getString("storeCode"));
        threePOReportPOS.setThreePoOrderId(resultSet.getString("orderID"));
        threePOReportPOS.setOrderStatus(resultSet.getString("orderStatus"));
        threePOReportPOS.setPickupStatus(resultSet.getString("pickupStatus"));
        threePOReportPOS.setCancellationRemark(resultSet.getString("cancellationRemark"));
        threePOReportPOS.setBillSubtotal(resultSet.getDouble("billSubtotal"));
        threePOReportPOS.setRefundForDisputedOrder(resultSet.getDouble("refundForDisputedOrder"));
        threePOReportPOS.setSalt(resultSet.getDouble("salt"));
        threePOReportPOS.setPosTotalAmount(resultSet.getDouble("PosSales"));
        threePOReportPOS.setThreePoTotalAmount(resultSet.getDouble("threePOSales"));
        threePOReportPOS.setPosReceivable(resultSet.getDouble("posReceivables"));
        threePOReportPOS.setThreePoReceivable(resultSet.getDouble("threePOReceivables"));
        threePOReportPOS.setPosPackagingCharge(resultSet.getDouble("posPackagingCharge"));
        threePOReportPOS.setThreePoPackagingCharge(resultSet.getDouble("threePOPackagingCharge"));
        threePOReportPOS.setThreePoTds(resultSet.getDouble("threePOTDS"));
        threePOReportPOS.setPosTds(resultSet.getDouble("posTDS"));
        threePOReportPOS.setPosCommission(resultSet.getDouble("posCommission"));
        threePOReportPOS.setThreePoCommission(resultSet.getDouble("threePOCommission"));
        threePOReportPOS.setPosPgCharge(resultSet.getDouble("posPGCharge"));
        threePOReportPOS.setThreePoPgCharge(resultSet.getDouble("threePOPgCharge"));
        threePOReportPOS.setPosChargesGst(resultSet.getDouble("posChargesGST"));
        threePOReportPOS.setThreePoChargesGst(resultSet.getDouble("threePOChargesGST"));
        threePOReportPOS.setPosConsumerGst(resultSet.getDouble("posConsumerGST"));
        threePOReportPOS.setThreePoConsumerGst(resultSet.getDouble("threePOConsumerGST"));
        threePOReportPOS.setUnreconciledAmount(resultSet.getDouble("unreconciled"));
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            if (columnName.toUpperCase().endsWith("_THREEPO_POS_MISMATCH") || columnName.toUpperCase().endsWith("_THREEPO_SELF_MISMATCH")
                    || columnName.toUpperCase().endsWith("_THREEPO_CUSTOM_MISMATCH")) {
                threePOReportPOS.getUnreconciledMismatchesCases().put(columnName.toUpperCase(), resultSet.getObject(columnName.toUpperCase()) == null ? null : resultSet.getDouble(columnName.toUpperCase()));
            } else if (columnName.toUpperCase().endsWith("_THREEPO_CUSTOM") || columnName.toUpperCase().endsWith("_THREEPO_SELF")
                    || columnName.toUpperCase().endsWith("_THREEPO_POS")) {
                threePOReportPOS.getThreepoFieldsForUnreconciled().put(columnName.toUpperCase(), resultSet.getObject(columnName.toUpperCase()) == null ? null : resultSet.getDouble(columnName.toUpperCase()));
            }
        }
        if (threePOReportPOS.getThreePO().equals(ThreePO.SWIGGY)) {
            threePOReportPOS.setCashPrepayment(resultSet.getDouble("cashPrePaymentAtRestaurant"));
            threePOReportPOS.setMerchantCancellationCharges(resultSet.getDouble("merchantCancellationCharges"));
        }
    }

    List<ThreePOReport> getThreePOVSPOSOrders(ThreePODataRequest request, String source, List<String> storeCodes, NamedParameterJdbcTemplate jdbcTemplate, ThreepoQueryDateRangeRecoLogicAndFEUtil threepoQueryDateRangeRecoLogicAndFEUtil) {
        List<ThreePOReport> threePOReportPOSList = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        parameters.addValue("storeCodes", storeCodes);
        //    if (source.equalsIgnoreCase("SWIGGY")) {
        String sql = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPOvsPOS_QUERY");
        log.info("ThreePOVSPOSOrders {}", sql);
        if (!sql.isEmpty()) {
            jdbcTemplate.query(sql, parameters, (resultSet) -> {
                ThreePOReport threePOReportPOS = new ThreePOReport();
                setThreePoReportValuesFromSql(threePOReportPOS, source, resultSet);
                threePOReportPOSList.add(threePOReportPOS);
            });
        }
        return threePOReportPOSList;
    }

    public Row[] downloadDeltaSheet(List<ThreePOReport> threePOReportPOS, List<Object[]> orderEntityList, Sheet sheet1, Sheet sheet2, Workbook workbook, boolean allThreePoDownload, ThreePO threePO) throws SQLException {
        String baseAmountHeaderName = "Bill Subtotal";
        String debitedAmountHeaderName = "Refund for Disputed Order, if applicable";
        if (threePO.equals(ThreePO.MAGICPIN)) {
            baseAmountHeaderName = "Item Amount";
            debitedAmountHeaderName = "Debited Amount";
        } else if (threePO.equals(ThreePO.SWIGGY)) {
            baseAmountHeaderName = "Item Total";
        }
        String[] headers = {
            "Business Date",
            "Order Date",
            "POS Invoice Number",
            "POS ID",
            "Receipt Number",
            "Store number",
            "Three PO Order ID",
            "POS Net Amount",
            "Three PO Net Amount",
            baseAmountHeaderName,
            "Order Status",
            "Pickup Status",
            "Cancellation Remark",
            debitedAmountHeaderName,
            "Salt/Merchant Discount",
            "POS Receivable",
            "Three PO Receivable",
            "POS Packaging Charge",
            "ThreePO Packaging Charge",
            "pos tds",
            "3po tds",
            "POS Commission",
            "3PO Commission",
            "POS Pg Charge",
            "3PO PG Charge",
            "POS Charges GST",
            "3PO Charges GST",
            "POS Consumer GST",
            "3PO Consumer GST",
            "Delta Net Amount",
            "Delta Receivables",
            "Delta packaging charge",
            "Delta TDS",
            "Delta Commission",
            "Delta PG Charges",
            "Delta GST on Charges (Commission + PG)",
            "Delta Consumer GST",
            "Reconciliation Status",
            "Remarks",
            "Unreconciled Amount",
            "POS % Receivable",
            "ThreePO % Receivable",
            "Cash Prepayment, if any",
            "Delta ThreePO Difference"
        };
        List<String> headerList = new ArrayList<>(Arrays.asList(headers));
        int dynamicFieldSize = 0;
        if (ThreePO.ZOMATO.equals(threePO)) {
            Map<String, String> customLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("ZOMATO_CUSTOM");
            if (customLogicsMap != null) {
                dynamicFieldSize += customLogicsMap.size() * 2;
                for (Map.Entry<String, String> keyValue : customLogicsMap.entrySet()) {
                    String fieldActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_ACTUAL";
                    headerList.add(fieldActual);
                    String fieldCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_CALCULATED";
                    headerList.add(fieldCalculated);
                }
            }
            Map<String, String> selfLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("ZOMATO_SELF");
            if (selfLogicsMap != null) {
                dynamicFieldSize += selfLogicsMap.size() * 2;
                for (Map.Entry<String, String> keyValue : selfLogicsMap.entrySet()) {
                    String fieldActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_ACTUAL";
                    headerList.add(fieldActual);
                    String fieldCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_CALCULATED";
                    headerList.add(fieldCalculated);
                }
            }
            Map<String, String> posLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("ZOMATO_POS");
            if (posLogicsMap != null) {
                dynamicFieldSize += posLogicsMap.size() * 2;
                for (Map.Entry<String, String> keyValue : posLogicsMap.entrySet()) {
                    String fieldActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_ACTUAL";
                    headerList.add(fieldActual);
                    String fieldCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_CALCULATED";
                    headerList.add(fieldCalculated);
                }
            }
        } else if (ThreePO.SWIGGY.equals(threePO)) {
            Map<String, String> customLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("SWIGGY_CUSTOM");
            if (customLogicsMap != null) {
                dynamicFieldSize += customLogicsMap.size() * 2;
                for (Map.Entry<String, String> keyValue : customLogicsMap.entrySet()) {
                    String fieldActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_ACTUAL";
                    headerList.add(fieldActual);
                    String fieldCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_CUSTOM_CALCULATED";
                    headerList.add(fieldCalculated);
                }
            }
            Map<String, String> selfLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("SWIGGY_SELF");
            if (selfLogicsMap != null) {
                dynamicFieldSize += selfLogicsMap.size() * 2;
                for (Map.Entry<String, String> keyValue : selfLogicsMap.entrySet()) {
                    String fieldActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_ACTUAL";
                    headerList.add(fieldActual);
                    String fieldCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_SELF_CALCULATED";
                    headerList.add(fieldCalculated);
                }
            }
            Map<String, String> posLogicsMap = TENDER_WISE_UNRECONCILED_REASONS_MAP.get("SWIGGY_POS");
            if (posLogicsMap != null) {
                dynamicFieldSize += posLogicsMap.size() * 2;
                for (Map.Entry<String, String> keyValue : posLogicsMap.entrySet()) {
                    String fieldActual = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_ACTUAL";
                    headerList.add(fieldActual);
                    String fieldCalculated = keyValue.getKey().substring(2).toUpperCase() + "_THREEPO_POS_CALCULATED";
                    headerList.add(fieldCalculated);
                }
            }
        }
        headers = headerList.toArray(String[]::new);

        CreationHelper createHelper = workbook.getCreationHelper();
        short dateFormat = createHelper.createDataFormat().getFormat("m/d/yy");
        short dateTimeFormat = createHelper.createDataFormat().getFormat("m/d/yy h:mm");

        CellStyle dateFormatStyle = workbook.createCellStyle();
        dateFormatStyle.setDataFormat(dateFormat);
        CellStyle dateTimeFormatStyle = workbook.createCellStyle();
        dateTimeFormatStyle.setDataFormat(dateTimeFormat);

        Row headerRowThreePO = threePOVsPOSSheet(threePOReportPOS, sheet2, headers, workbook, allThreePoDownload, dateFormatStyle, dateTimeFormatStyle, dynamicFieldSize);
        Row headerRowPOS = posVsThreePOSheet(orderEntityList, sheet1, threePOReportPOS.stream().filter(k -> zomatoSaleAdditionOrders(k) && k.getPosTotalAmount() != 0).collect(Collectors.toList()), headers, workbook, allThreePoDownload, dateFormatStyle, dateTimeFormatStyle, dynamicFieldSize);
        return new Row[]{headerRowPOS, headerRowThreePO};
    }

    public boolean zomatoSaleAdditionOrders(ThreePOReport threePOReport) {
        if (threePOReport.getThreePO().equals(ThreePO.ZOMATO)) {
            return "sale".equalsIgnoreCase(threePOReport.getOrderStatus()) || "addition".equalsIgnoreCase(threePOReport.getOrderStatus());
        }
        return true;
    }

    public Row threePOVsPOSSheet(List<ThreePOReport> threePOReportPOSList, Sheet sheet2, String[] headers, Workbook workbook, boolean allThreePoDownload, CellStyle dateFormat, CellStyle dateTimeFormat, int dynamicFieldSize) {
        Row headerRow = sheet2.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        addCommonDatasets(threePOReportPOSList, sheet2, dateFormat, dateTimeFormat, false, false);
        if (!allThreePoDownload) {
            Map<String, List<ThreePOReport>> reasonWiseList = threePOReportPOSList.stream().filter(report -> Strings.isNotBlank(report.getReason())).collect(Collectors.groupingBy(k -> {
                String reason = k.getReason();
                if (reason.contains(ROUNDING_OFF)) {
                    reason = reason.replaceAll(ROUNDING_OFF, "");
                }
                return reason;
            }));
            Map<String, List<ThreePOReport>> statusWiseSheet = threePOReportPOSList.stream().filter(report -> Strings.isNotBlank(report.getOrderStatus())).collect(Collectors.groupingBy(ThreePOReport::getOrderStatus));

            for (Map.Entry<String, List<ThreePOReport>> entry : reasonWiseList.entrySet()) {
                Sheet reasonSheet = workbook.createSheet(entry.getKey());
                Row reasonHeaderRow = reasonSheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    reasonHeaderRow.createCell(i).setCellValue(headers[i]);
                }
                addCommonDatasets(entry.getValue(), reasonSheet, dateFormat, dateTimeFormat, false, false);
            }

            for (Map.Entry<String, List<ThreePOReport>> entry : statusWiseSheet.entrySet()) {
                Sheet reasonSheet = workbook.createSheet(entry.getKey());
                Row reasonHeaderRow = reasonSheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    reasonHeaderRow.createCell(i).setCellValue(headers[i]);
                }
                addCommonDatasets(entry.getValue(), reasonSheet, dateFormat, dateTimeFormat, false, false);
            }

            List<ThreePOReport> merchantCancellationChargesNonZero = threePOReportPOSList.stream().filter(report -> report.getMerchantCancellationCharges() != 0).toList();
            if (!merchantCancellationChargesNonZero.isEmpty()) {
                Sheet merchantCancelledSheet = workbook.createSheet("Non Zero Merch. Cancel. Charges");
                Row reasonHeaderRow = merchantCancelledSheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    reasonHeaderRow.createCell(i).setCellValue(headers[i]);
                }
                reasonHeaderRow.createCell(headers.length).setCellValue("Merchant Cancellation Charges");
                addCommonDatasets(merchantCancellationChargesNonZero, merchantCancelledSheet, dateFormat, dateTimeFormat, true, false);
            }

        }
        return headerRow;
    }

    public Sheet addCommonDatasets(List<ThreePOReport> threePOReportPOSList, Sheet sheet1, CellStyle dateFormat, CellStyle dateTimeFormat, boolean merchantCancelCharges, boolean isPosvsThreePo) {
        for (ThreePOReport threePOReportPOS : threePOReportPOSList) {
            Row row = sheet1.createRow(sheet1.getPhysicalNumberOfRows());
            int col = 0;
            row.createCell(col++).setCellValue(threePOReportPOS.getBusinessDate());
            row.getCell(col - 1).setCellStyle(dateFormat);
            row.createCell(col++).setCellValue(threePOReportPOS.getOrderDate());
            row.getCell(col - 1).setCellStyle(dateTimeFormat);
            row.createCell(col++).setCellValue(threePOReportPOS.getInvoiceNumber());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosId());
            row.createCell(col++).setCellValue(threePOReportPOS.getReceiptNumber());
            row.createCell(col++).setCellValue(threePOReportPOS.getStoreCode());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoOrderId());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosTotalAmount());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoTotalAmount());
            row.createCell(col++).setCellValue(threePOReportPOS.getBillSubtotal());
            row.createCell(col++).setCellValue(threePOReportPOS.getOrderStatus());
            row.createCell(col++).setCellValue(threePOReportPOS.getPickupStatus());
            row.createCell(col++).setCellValue(threePOReportPOS.getCancellationRemark());
            row.createCell(col++).setCellValue(threePOReportPOS.getRefundForDisputedOrder());
            row.createCell(col++).setCellValue(threePOReportPOS.getSalt());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosReceivable());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoReceivable());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosPackagingCharge());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoPackagingCharge());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosTds());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoTds());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosCommission());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoCommission());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosPgCharge());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoPgCharge());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosChargesGst());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoChargesGst());
            row.createCell(col++).setCellValue(threePOReportPOS.getPosConsumerGst());
            row.createCell(col++).setCellValue(threePOReportPOS.getThreePoConsumerGst());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaAmount());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaReceivables());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaPackagingCharge());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaTds());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaCommission());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaPgCharges());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaChargesGST());
            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaConsumerGST());
            if (ThreePO.ZOMATO.equals(threePOReportPOS.getThreePO())
                    && ("cancel".equalsIgnoreCase(threePOReportPOS.getOrderStatus())
                    || "refund".equalsIgnoreCase(threePOReportPOS.getOrderStatus())
                    || "deduction".equalsIgnoreCase(threePOReportPOS.getOrderStatus())
                    || "addition".equalsIgnoreCase(threePOReportPOS.getOrderStatus()))) {/*addition is newly added by Abhishek*/
                row.createCell(col++).setCellValue("");
                row.createCell(col++).setCellValue("");
            } else {
                row.createCell(col++).setCellValue(threePOReportPOS.getUnreconciledAmount() == 0 ? "reconciled" : "unreconciled");
                if (threePOReportPOS.getUnreconciledAmount() != 0) {
                    row.createCell(col++).setCellValue(threePOReportPOS.getReason());
                } else {
                    row.createCell(col++).setCellValue("");
                }
            }

            row.createCell(col++).setCellValue(threePOReportPOS.getUnreconciledAmount());
            int rowNumber = row.getRowNum() + 1;
            row.createCell(col++).setCellFormula("ROUND(" + "P" + rowNumber + "*100/H" + rowNumber + ",2)");
            row.createCell(col++).setCellFormula("ROUND(" + "Q" + rowNumber + "*100/I" + rowNumber + ",2)");
            row.createCell(col++).setCellValue(threePOReportPOS.getCashPrepayment());

            row.createCell(col++).setCellValue(threePOReportPOS.getDeltaThreePODifference());//newly added
            for (Map.Entry<String, Double> keyValue : threePOReportPOS.getThreepoFieldsForUnreconciled().entrySet()) {
                row.createCell(col++).setCellValue(keyValue.getValue() == null ? 0 : keyValue.getValue());
                double rightValue;
                if (keyValue.getValue() == null || threePOReportPOS.getUnreconciledMismatchesCases().get(keyValue.getKey() + "_MISMATCH") == null) {
                    row.createCell(col++).setCellValue("null");
                } else {
                    rightValue = keyValue.getValue() - threePOReportPOS.getUnreconciledMismatchesCases().get(keyValue.getKey() + "_MISMATCH");
                    row.createCell(col++).setCellValue(rightValue);
                }
            }
            if (merchantCancelCharges) {
                row.createCell(col++).setCellValue(threePOReportPOS.getMerchantCancellationCharges());
            }
        }
        return sheet1;
    }

    public Row posVsThreePOSheet(List<Object[]> orderEntityList, Sheet sheet1, List<ThreePOReport> threePOReportPOSList, String[] headers, Workbook workbook, boolean allThreePoDownload, CellStyle dateFormat, CellStyle dateTimeFormat, int dynamicFieldSize) throws SQLException {
        Row headerRow = sheet1.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        addCommonDatasets(threePOReportPOSList, sheet1, dateFormat, dateTimeFormat, false, true);
        Sheet sheet = null;
        if (!allThreePoDownload) {
            sheet = workbook.createSheet(ORDER_NOT_FOUND_IN_THREE_PO);
            Row headerRow1 = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow1.createCell(i).setCellValue(headers[i]);
            }
        }
        try {
            for (Object[] orderEntity : orderEntityList) {
                int col = 0;
                Row row1 = sheet1.createRow(sheet1.getPhysicalNumberOfRows());
                row1.createCell(col++).setCellValue(orderEntity[3] == null ? "" : orderEntity[3].toString());
                row1.getCell(col - 1).setCellStyle(dateFormat);
                row1.createCell(col++).setCellValue(orderEntity[4] == null ? "" : orderEntity[4].toString());
                row1.getCell(col - 1).setCellStyle(dateTimeFormat);
                row1.createCell(col++).setCellValue(orderEntity[8] == null ? "" : orderEntity[8].toString());
                row1.createCell(col++).setCellValue(orderEntity[1] == null ? "" : orderEntity[1].toString());
                row1.createCell(col++).setCellValue(orderEntity[10] == null ? "" : orderEntity[10].toString());
                row1.createCell(col++).setCellValue(orderEntity[2] == null ? "" : orderEntity[2].toString());
                row1.createCell(col++).setCellValue(orderEntity[13].toString());
                row1.createCell(col++).setCellValue(orderEntity[11] == null ? 0.0 : (orderEntity[11] instanceof Double ? (Double) orderEntity[11] : Double.valueOf(orderEntity[11].toString())));
                row1.createCell(headers.length - 7 - dynamicFieldSize).setCellValue("unreconciled");
                row1.createCell(headers.length - 6 - dynamicFieldSize).setCellValue(ORDER_NOT_FOUND_IN_THREE_PO);
                row1.createCell(headers.length - 5 - dynamicFieldSize).setCellValue(orderEntity[11] == null ? 0.0 : (orderEntity[11] instanceof Double ? (Double) orderEntity[11] : Double.valueOf(orderEntity[11].toString())));

                col = 0;
                if (sheet != null) {
                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                    row.createCell(col++).setCellValue(orderEntity[3] == null ? "" : orderEntity[3].toString());
                    row.createCell(col++).setCellValue(orderEntity[4] == null ? "" : orderEntity[4].toString());
                    row.createCell(col++).setCellValue(orderEntity[8] == null ? "" : orderEntity[8].toString());
                    row.createCell(col++).setCellValue(orderEntity[1] == null ? "" : orderEntity[1].toString());
                    row.createCell(col++).setCellValue(orderEntity[10] == null ? "" : orderEntity[10].toString());
                    row.createCell(col++).setCellValue(orderEntity[2] == null ? "" : orderEntity[2].toString());
                    row.createCell(col++).setCellValue(orderEntity[13].toString());
                    row.createCell(col++).setCellValue(orderEntity[11] == null ? 0.0 : (orderEntity[11] instanceof Double ? (Double) orderEntity[11] : Double.valueOf(orderEntity[11].toString())));
                    row.createCell(headers.length - 7 - dynamicFieldSize).setCellValue("unreconciled");
                    row.createCell(headers.length - 6 - dynamicFieldSize).setCellValue(ORDER_NOT_FOUND_IN_THREE_PO);
                    row.createCell(headers.length - 5 - dynamicFieldSize).setCellValue(orderEntity[11] == null ? 0.0 : (orderEntity[11] instanceof Double ? (Double) orderEntity[11] : Double.valueOf(orderEntity[11].toString())));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return headerRow;
    }

    String formatLocalDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(Formatter.DDMMYYYY_HHMMSS_DASH);
    }

    String formatLocalDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(Formatter.DDMMYYYY_DASH);
    }
}
