package com.cpl.reconciliation.web.service.impl.ThreePo;

import com.cpl.core.api.constant.Formatter;
import com.cpl.core.api.exception.ApiException;
import com.cpl.core.common.annotations.TrackExecutionTime;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.core.response.threepo.GeneratedReportResponse;
import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;
import com.cpl.reconciliation.core.response.threepo.ThreePOData;
import com.cpl.reconciliation.domain.models.ThreePOReport;
import com.cpl.reconciliation.domain.models.ZomatoPromoReport;
import com.cpl.reconciliation.domain.repository.OrderRepository;
import com.cpl.reconciliation.domain.repository.ZomatoPromoRepository;
import com.cpl.reconciliation.domain.repository.ZomatoRepository;
import com.cpl.reconciliation.web.service.PromoQueries;
import com.cpl.reconciliation.web.service.ThreePoQueries;
import com.cpl.reconciliation.web.service.util.POSThreePoSummarySheetUtil;
import com.cpl.reconciliation.web.service.util.ThreepoQueryDateRangeRecoLogicAndFEUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Data
@Slf4j
@Service
public class ZomatoApiService extends ThreePOService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OrderRepository orderRepository;
    private final ZomatoRepository zomatoRepository;
    private final POSThreePoSummarySheetUtil sheetUtil;
    private final ZomatoPromoRepository zomatoPromoRepository;
    private final ThreepoQueryDateRangeRecoLogicAndFEUtil threepoQueryDateRangeRecoLogicAndFEUtil;

    @TrackExecutionTime
    public ThreePOData getDashboardDataResponse(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        ThreePOData response;
        try {
            CompletableFuture<Double> posSalesFuture = CompletableFuture.supplyAsync(() -> getPOSSales(startDate, endDate, storeCodes));
            CompletableFuture<ThreePOData> threePOsDataFuture = CompletableFuture.supplyAsync(() -> getThreePOData(startDate, endDate, storeCodes));
            CompletableFuture<Double> getPOsTotalNotFoundInZomatoFuture = CompletableFuture.supplyAsync(() -> getPOsTotalNotFoundInZomato(startDate, endDate, storeCodes));
            CompletableFuture<Double> getPromoShare = CompletableFuture.supplyAsync(() -> getPromoShare(startDate, endDate, storeCodes));

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(posSalesFuture, threePOsDataFuture, getPromoShare, getPOsTotalNotFoundInZomatoFuture);
            allFutures.get();

            Double posSales = posSalesFuture.get();
            double possTotalNotFoundInZomato = getPOsTotalNotFoundInZomatoFuture.get();
            ThreePOData threePOsData = threePOsDataFuture.get();

            response = getMergedResponse(posSales, possTotalNotFoundInZomato, threePOsData);
            response.setPromo(getPromoShare.get());
            response.setDeltaPromo(getPromoShare.get());
        } catch (Exception e) {
            log.error("Error while fetching data", e);
            throw new ApiException("Error while fetching data");
        }
        return response;
    }

    private double getPOsTotalNotFoundInZomato(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        double orderSumNotFoundIn3PO = orderRepository.getTotalSalesNotFoundInZomato(startDate, endDate, storeCodes);
        return orderSumNotFoundIn3PO;
    }

    private double getPromoShare(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        return zomatoPromoRepository.getPromoShare(startDate.toLocalDate(), endDate.toLocalDate(), storeCodes);
    }

    private ThreePOData getMergedResponse(Double posSales,
            double posTotalNotFoundInZomato,
            ThreePOData threePOData) {
        threePOData.setPosSales(posSales);
        threePOData.setReconciled(threePOData.getReconciled());
        threePOData.setPosVsThreePO(threePOData.getPosVsThreePO() + posTotalNotFoundInZomato);
        return threePOData;
    }

    private Double getPOSSales(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        return orderRepository.getTotalAmountByDateAndThreePOSource(startDate.toLocalDate(), endDate.toLocalDate(), "zomato", storeCodes);
    }

    private ThreePOData getThreePOData(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        String sql = ThreePoQueries.Zomato_threePOQuery;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("storeCodes", storeCodes);
        parameters.addValue("startDate", startDate.format(Formatter.YYYYMMDD_HHMMSS_DASH));
        parameters.addValue("endDate", endDate.format(Formatter.YYYYMMDD_HHMMSS_DASH));
        ThreePOData threePOData = new ThreePOData();
        threePOData.setTenderName(ThreePO.ZOMATO);

        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
            double discount = 0;
            double threePOSales = resultSet.getDouble("threePOSales");
            double threePOReceivables = resultSet.getDouble("threePOReceivables");
            double threePOCommission = resultSet.getDouble("threePOCommission");
            double threePOCharges = resultSet.getDouble("threePOCharges");
            double salt = resultSet.getDouble("freebies");

            threePOData.setThreePOSales(threePOSales);
            threePOData.setThreePOReceivables(threePOReceivables);
            threePOData.setThreePOCommission(threePOCommission);
            threePOData.setThreePOCharges(threePOCharges);
            threePOData.setThreePODiscounts(discount);
            threePOData.setThreePOFreebies(salt);

            // Ideal scenarios
            double posReceivables = resultSet.getDouble("posReceivables");
            double posCommission = resultSet.getDouble("posCommission");
            double posCharges = resultSet.getDouble("posCharges");

            threePOData.setPosReceivables(posReceivables);
            threePOData.setPosCommission(posCommission);
            threePOData.setPosCharges(posCharges);
            threePOData.setPosDiscounts(discount);
            threePOData.setPosFreebies(salt);

            double reconciled = resultSet.getDouble("reconciled");
            double unreconciled = resultSet.getDouble("unreconciled");
            double receivablesVsReceipts = resultSet.getDouble("receivablesVsReceipts");
            double threePoDiscounts = resultSet.getDouble("threePODiscounts");
            double posDiscounts = resultSet.getDouble("posDiscounts");
            threePOData.setReconciled(reconciled);
            threePOData.setPosVsThreePO(unreconciled);
            threePOData.setReceivablesVsReceipts(receivablesVsReceipts);
            threePOData.setThreePODiscounts(threePoDiscounts);
            threePOData.setPosDiscounts(posDiscounts);

            return null;
        });

        return threePOData;
    }

    public GeneratedReportResponse getPOSvs3POResponse(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        GeneratedReportResponse response = new GeneratedReportResponse();
        double posSales = getPOSSales(startDate, endDate, storeCodes);
        response.setSales(posSales);

        ThreePOData threePO = getThreePOData(startDate, endDate, storeCodes);
//        response.setReceipts(threePO.getReceipts());
        response.setCharges(threePO.getThreePOCharges());
        response.setDifference(response.getSales() - threePO.getThreePOSales());
        response.setReconciled(orderRepository.getTotalSalesReconciledInZomato(startDate, endDate, storeCodes));

        return response;

    }

    @TrackExecutionTime
    public void posVsThreePoDownload(ThreePODataRequest request, Workbook workbook, boolean allThreePoDownload) throws SQLException {
        Sheet sheet3 = workbook.createSheet("Zomato Summary");
        Sheet sheet1 = workbook.createSheet("Zomato POS vs 3PO");
        Sheet sheet2 = workbook.createSheet("Zomato 3PO vs POS");
        Sheet sheet4 = workbook.createSheet("S2");
        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("storeCodes", request.getStores());

        List<Object[]> orderNotFoundList;
        String orderNotFoundQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "ORDER_NOT_FOUND_QUERY");
        log.info("Zomato orderNotFoundQuery {}", orderNotFoundQuery);
        if (!orderNotFoundQuery.isEmpty()) {
            orderNotFoundList = jdbcTemplate.query(orderNotFoundQuery, parameters, (ResultSet resultSet, int rowNum) -> {
                int columnCount = resultSet.getMetaData().getColumnCount();
                Object[] rowValues = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    rowValues[i - 1] = resultSet.getObject(i);
                }
                return rowValues;
            });
        } else {
            orderNotFoundList = new ArrayList();/*empty*/
        }
        log.error("Order not found in zomato count: " + orderNotFoundList.size());
        String posSalesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_SALES_NextDate_QUERY");
        log.info("Zomato POS_SALES_NextDate_QUERY {}", posSalesQuery);
        if (!orderNotFoundQuery.isEmpty()) {
            MapSqlParameterSource posSaleParameters = new MapSqlParameterSource();
            posSaleParameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(posSalesQuery, posSaleParameters, workbook, sheet4, "Zomato Pos Sales", null, false);
        }
        List<ThreePOReport> threePOReportPOS = getThreePOVSPOSOrders(request, "ZOMATO", request.getStores(), jdbcTemplate, threepoQueryDateRangeRecoLogicAndFEUtil);
        log.error("3PO zomato count: " + threePOReportPOS.size());
        Row[] rowHeaders = downloadDeltaSheet(threePOReportPOS, orderNotFoundList, sheet1, sheet2, workbook, allThreePoDownload, ThreePO.ZOMATO);
        sheetUtil.createSummarySheet(workbook, sheet3, sheet1, sheet2, ThreePO.ZOMATO, sheet4, startDate, endDate, rowHeaders);
        //exceptionalReportingNew(workbook, startDate, endDate, request.getStores());
        String exceptionalReportQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "EXCEPTIONAL_REPORT_QUERY");
        log.info("Zomato exceptionalReportQuery {}", exceptionalReportQuery);
        if (!exceptionalReportQuery.isEmpty() && exceptionalReportQuery.contains("<>")) {
            MapSqlParameterSource expParameters = new MapSqlParameterSource();
            expParameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(exceptionalReportQuery, expParameters, workbook, null, "Exceptional Column Reporting", null, true);
        }
    }

    /*Unuse code since it is handled dynamically*/
    public void exceptionalReportingNew(Workbook workbook, LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        Sheet sheet = workbook.createSheet("Exceptional Column Reporting");

        String[] headers = {"Order ID", "Order Date", "Action", "Actual Discount", "Logistics Charge", "Pro Discount Passthrough",
            "Customer Discount", "Rejection Penalty Charge", "User Credits Charge", "Promo Recovery Adj", "Icecream Handling",
            "Icecream Deductions", "Order Support Cost", "Credit Note Amount", "Merchant Delivery Charge"};
        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        Row headerRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", startDate.toLocalDate());
        parameters.addValue("endDate", endDate.toLocalDate());
        parameters.addValue("storeCodes", storeCodes);

        String query = "SELECT order_id,order_date,action,actual_discount,logistics_charge,pro_discount_passthrough,"
                + "customer_discount,rejection_penalty_charge,user_credit_charge,promo_recovery_adj,"
                + "icecream_handling,icecream_deductions,order_support_cost,credit_note_amount,"
                + "merchant_delivery_charge from zomato where (actual_discount<>0"
                + " or logistics_charge<>0"
                + " or pro_discount_passthrough<>0"
                + " or customer_discount<>0"
                + " or rejection_penalty_charge<>0"
                + " or user_credit_charge<>0"
                + " or promo_recovery_adj<>0"
                + " or icecream_handling<>0"
                + " or icecream_deductions<>0"
                + " or order_support_cost<>0"
                + " or credit_note_amount<>0"
                + " or merchant_delivery_charge<>0)"
                + " and (Date(order_date) between :startDate AND :endDate)"
                + " AND (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))";

        AtomicInteger rowNum = new AtomicInteger(1);
        jdbcTemplate.query(query, parameters, (ResultSet rs) -> {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("order_id"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("order_date"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("action"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("actual_discount"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("logistics_charge"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("pro_discount_passthrough"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("customer_discount"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("rejection_penalty_charge"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("user_credit_charge"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("promo_recovery_adj"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("icecream_handling"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("icecream_deductions"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("order_support_cost"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("credit_note_amount"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("merchant_delivery_charge"));
            rowNum.incrementAndGet();
        });
    }

    public void reconciledDownload(ThreePODataRequest request, Workbook workbook) {
        String reconciledQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_RECONCILED_REPORT_QUERY");
        log.info("Zomato reconciledQuery {}", reconciledQuery);
        if (!reconciledQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(reconciledQuery, parameters, workbook, null, "Zomato Reconciled", null, false);
        }
    }
    /*threepo sale and discount queries are same.*/
    public void discountsDownload(ThreePODataRequest request, Workbook workbook) {
        String discountQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_SALES_QUERY");
        log.info("Zomato discountQuery {}", discountQuery);
        if (!discountQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(discountQuery, parameters, workbook, null, "Zomato", "merchant_voucher_discount", false);
        }
    }

    public void posSalesDownload(ThreePODataRequest request, Workbook workbook) {
        String posSalesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_SALES_QUERY");
        log.info("Zomato posSalesQuery {}", posSalesQuery);
        if (!posSalesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(posSalesQuery, parameters, workbook, null, "Zomato Pos Sales", null, false);
        }
    }

    @TrackExecutionTime
    public void threePOSalesDownloadNew(ThreePODataRequest request, Workbook workbook) {
        String threepoSalesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_SALES_QUERY");
        log.info("Zomato threepoSalesQuery {}", threepoSalesQuery);
        if (!threepoSalesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(threepoSalesQuery, parameters, workbook, null, "Zomato", null, false);
        }
    }

    public void posReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato POS Receivable Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Bill Subtotal", "Freebie", "Actual Packaging Charge", "Action", "POS Receivables"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posReceivableQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_RECIEVABLES_QUERY");
        log.info("Zomato posReceivableQuery {}", posReceivableQuery);
        if (!posReceivableQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posReceivableQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("bill_subtotal"));
                row.createCell(9).setCellValue(resultSet.getDouble("freebie"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getString("action"));
                row.createCell(12).setCellValue(resultSet.getDouble("posReceivables"));
                return null;
            });
        }
    }

    public void allPOSChargesDownload(ThreePODataRequest request, Workbook workbook) {
    }

    public void posChargesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato POS Charges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Bill Subtotal", "Freebie", "Actual Packaging Charge", "Action", "POS Charges"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posReceivableQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_CHARGES_QUERY");
        log.info("Zomato POS_CHARGES_QUERY {}", posReceivableQuery);
        if (!posReceivableQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posReceivableQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("bill_subtotal"));
                row.createCell(9).setCellValue(resultSet.getDouble("freebie"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getString("action"));
                row.createCell(12).setCellValue(resultSet.getDouble("posCharges"));
                return null;
            });
        }
    }

    public void posDiscountsDownload(ThreePODataRequest request, Workbook workbook) {
    }

    public void posFreebiesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato POS Freebies Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Bill Subtotal", "Freebie",
            "Actual Packaging Charge", "Action", "POS Freebies"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posFreebiesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_FREEBIE_QUERY");
        log.info("Zomato posFreebiesQuery {}", posFreebiesQuery);
        if (!posFreebiesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posFreebiesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("bill_subtotal"));
                row.createCell(9).setCellValue(resultSet.getDouble("freebie"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getString("action"));
                row.createCell(12).setCellValue(resultSet.getDouble("freebies"));
                return null;
            });
        }
    }

    public void posCommissionDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato Pos Commission Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Bill Subtotal", "Freebie", "Actual Packaging Charge", "Action", "POS Commission"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posCommissionQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_COMMISSION_QUERY");
        log.info("Zomato posCommissionQuery {}", posCommissionQuery);
        if (!posCommissionQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posCommissionQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("bill_subtotal"));
                row.createCell(9).setCellValue(resultSet.getDouble("freebie"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getString("action"));
                row.createCell(12).setCellValue(resultSet.getDouble("posCommission"));
                return null;
            });
        }
    }

    public void threePOReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato ThreeOo Receivables Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Final Amount", "ThreePO Receivables"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOReceivableQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_RECIEVABLES_QUERY");
        log.info("Zomato threePOReceivableQuery {}", threePOReceivableQuery);
        if (!threePOReceivableQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOReceivableQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("final_amount"));
                row.createCell(9).setCellValue(resultSet.getDouble("threePOReceivables"));
                return null;
            });
        } else {
            log.warn("ThreePO Receivables Query is not found. Please verify, zomato recologics are uploaded.");
        }
    }

    public void threePOCommissionDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato ThreePO Commission Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Commission Value", "ThreePO Commission"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOCommissionQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_COMMISSION_QUERY");
        log.info("Zomato threePOCommissionQuery {}", threePOCommissionQuery);
        if (!threePOCommissionQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOCommissionQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("commission_value"));
                row.createCell(9).setCellValue(resultSet.getDouble("threePOCommission"));
                return null;
            });
        } else {
            log.warn("ThreePO Commission Query is not found. Please verify, zomato recologics are uploaded.");
        }
    }

    public void threePOFreebieDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato ThreePO Freebies Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "freebie", "ThreePO Freebies"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOFreebieQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_FREEBIE_QUERY");
        log.info("Zomato threePOFreebieQuery {}", threePOFreebieQuery);
        if (!threePOFreebieQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOFreebieQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("freebie"));
                row.createCell(9).setCellValue(resultSet.getDouble("freebies"));
                return null;
            });
        }
    }

    public void receivablesVsReceiptsDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato ReceivablesVsReceipts Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Payout Date", "Reference Number", "Final Amount", "Bill Subtotal", "Actual Packaging Charge", "Action", "Freebies", "Three PO Receivables", "POS Receivables", "Payout Amount", " POS ReceivablesVsReceipts", "ThreePO ReceivablesVsReceipts"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String receivablesVsReceiptsQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_RECIEVABLE_VS_RECEIPT_QUERY");
        log.info("Zomato receivablesVsReceiptsQuery {}", receivablesVsReceiptsQuery);
        if (!receivablesVsReceiptsQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(receivablesVsReceiptsQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("order_id"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("order_date"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("business_date"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("pos_id"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("store_code"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("payout_date"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("reference_number"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("final_amount"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("bill_subtotal"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("action"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("freebies"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("threePOReceivables"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("posReceivables"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("payout_amount"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("receivablesVsReceipts"));
                double ThreePOReceivablesVsReceipts = resultSet.getDouble("threePOReceivables") - resultSet.getDouble("payout_amount");
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(ThreePOReceivablesVsReceipts);
                return null;
            });
        }
    }

    public void threePOChargesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato ThreePO Charges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "TDS Amount", "Taxes Zomato Fee", "PG Charge", "ThreePO Charges"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_CHARGES_QUERY");
        log.info("Zomato threePOChargesQuery {}", threePOChargesQuery);
        if (!threePOChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("tds_amount"));
                row.createCell(9).setCellValue(resultSet.getDouble("taxes_zomato_fee"));
                row.createCell(10).setCellValue(resultSet.getDouble("pg_charge"));
                row.createCell(11).setCellValue(resultSet.getString("threePOCharges"));
                return null;
            });
        }
    }

    public void allThreePOCharges(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato All ThreePO Charges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Commission Value", "TDS Amount", "Taxes Zomato Fee", "PG Charge", "ThreePO Commission", "Freebies", "ThreePO Discount", "ThreePO Charges"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String allThreePOChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_ALL_CHARGES_QUERY");
        log.info("Zomato allThreePOChargesQuery {}", allThreePOChargesQuery);
        if (!allThreePOChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(allThreePOChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("commission_value"));
                row.createCell(9).setCellValue(resultSet.getDouble("tds_amount"));
                row.createCell(10).setCellValue(resultSet.getDouble("taxes_zomato_fee"));
                row.createCell(11).setCellValue(resultSet.getDouble("pg_charge"));
                row.createCell(12).setCellValue(resultSet.getDouble("threePOCommission"));
                row.createCell(13).setCellValue(resultSet.getDouble("freebies"));
                row.createCell(14).setCellValue(resultSet.getDouble("threePODiscounts"));
                row.createCell(15).setCellValue(resultSet.getDouble("threePOCharges"));
                return null;
            });
        }
    }

    public void allPOSCharges(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Zomato All POS Charges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Bill Subtotal", "Actual Packaging Charge", "Freebies", "POS Commission", "POS Charges"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String allPOSChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_ALL_CHARGES_QUERY");
        log.info("Zomato allPOSChargesQuery {}", allPOSChargesQuery);
        if (!allPOSChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(allPOSChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_id"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("bill_subtotal"));
                row.createCell(9).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(10).setCellValue(resultSet.getDouble("freebies"));
                row.createCell(11).setCellValue(resultSet.getDouble("posCommission"));
                row.createCell(12).setCellValue(resultSet.getDouble("posCharges"));
                return null;
            });
        }
    }

    @Override
    public MissingThreePOMapping getThreePoMissingMapping() {
        MissingThreePOMapping mapping = new MissingThreePOMapping();
        mapping.setMissing(zomatoRepository.getRestaurantIdCountWherestoreCodeIsNull());
        mapping.setTotalStores(zomatoRepository.getRestaurantIdCount());
        mapping.setThreePO(ThreePO.ZOMATO);
        return mapping;
    }

    @Override
    public void downloadThreePoMissingMapping(Workbook workbook) {
        List<String> getMissingRestaurantIds = zomatoRepository.getRestaurantIdsCountWherestoreCodeIsNull();
        Sheet sheet = workbook.createSheet("Zomato Mappings");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Store Code", "Zomato Store Code", "Packaging Charge"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (String missingRestId : getMissingRestaurantIds) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(1).setCellValue(missingRestId);
        }

    }

    public void promo(ThreePODataRequest request, Workbook workbook) {
        LocalDateTime start = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        LocalDateTime end = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        List<ZomatoPromoReport> promoReports = new ArrayList<>();
        String promoQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_PROMO_REPORT_QUERY");
        log.info("Zomato POS_PROMO_REPORT_QUERY {}", promoQuery);
        if (!promoQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            jdbcTemplate.query(promoQuery, parameters, (resultSet, rowNumber) -> {
                ZomatoPromoReport promoReport = new ZomatoPromoReport();

                promoReport.setAggregation(resultSet.getDate("Aggregation").toLocalDate());
                promoReport.setTabId(resultSet.getString("tab_id"));
                promoReport.setResId(resultSet.getString("res_id"));
                promoReport.setEntityName(resultSet.getString("entity_name"));
                promoReport.setBrandName(resultSet.getString("brand_name"));
                promoReport.setAccountType(resultSet.getString("account_type"));
                promoReport.setPromoCode(resultSet.getString("promo_code"));
                promoReport.setPsSegment(resultSet.getString("ps_segment"));
                promoReport.setZvdFinal(resultSet.getDouble("zvd_final"));
                promoReport.setZvd(resultSet.getDouble("zvd"));
                promoReport.setMvd(resultSet.getDouble("mvd"));
                promoReport.setOfflineRecon(resultSet.getDouble("offline_recon"));
                promoReport.setControl(resultSet.getDouble("control"));
                promoReport.setBurn(resultSet.getDouble("burn"));
                promoReport.setSalt(resultSet.getDouble("salt"));
                promoReport.setTotal(resultSet.getDouble("total"));
                promoReport.setNet(resultSet.getDouble("net"));
                promoReport.setConstruct(resultSet.getDouble("construct"));
                promoReport.setCommission(resultSet.getDouble("commission"));
                promoReport.setPg(resultSet.getDouble("pg"));
                promoReport.setGstOnCommission(resultSet.getDouble("gst_on_commission"));
                promoReport.setGstOnPg(resultSet.getDouble("gst_on_pg"));
                promoReport.setFinalAmount(resultSet.getDouble("final_amount"));
                promoReport.setStoreCode(resultSet.getString("store_code"));
                promoReport.setDayOfWeek(promoReport.getAggregation().getDayOfWeek());

                setThreePoReportValuesFromSql(promoReport, "zomato", resultSet);
                promoReports.add(promoReport);

                return null;
            });
            addZomatoSalt(promoReports, request);
            Sheet sheet1 = workbook.createSheet("Zomato Order Sheet");
            Sheet sheet2 = workbook.createSheet("Zomato Summary");
            Sheet sheet3 = workbook.createSheet("Zomato Food cost");
            createPromoOrderSheet(promoReports, sheet1);
            createPromoSummarySheet(sheet1, sheet2);
            createFoodSummarySheet(sheet1, sheet3, promoReports, start.toLocalDate(), end.toLocalDate());
        }
    }

    private void addZomatoSalt(List<ZomatoPromoReport> promoReports, ThreePODataRequest request) {
        String saltQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_SALT_REPORT_QUERY");
        log.info("Zomato SALT_REPORT_QUERY {}", saltQuery);
        if (!saltQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            jdbcTemplate.query(saltQuery, parameters, (resultSet, rowNumber) -> {
                ZomatoPromoReport promoReport = new ZomatoPromoReport();

                promoReport.setAggregation(resultSet.getDate("created_at").toLocalDate());
                promoReport.setTabId(resultSet.getString("tab_id"));
                promoReport.setResId(resultSet.getString("res_id"));
                promoReport.setPromoCode(resultSet.getString("promo_code"));
                promoReport.setZvd(resultSet.getDouble("zomato_voucher_discount"));
                promoReport.setStoreCode(resultSet.getString("store_code"));
                promoReport.setDayOfWeek(promoReport.getAggregation().getDayOfWeek());

                setThreePoReportValuesFromSql(promoReport, "zomato", resultSet);

                promoReport.setSalt(resultSet.getDouble("salt_discount"));
                double billSubtotal = resultSet.getDouble("bill_subtotal");
                double packagingCharge = resultSet.getDouble("packaging_charges");
                promoReport.setTotal(billSubtotal + packagingCharge - promoReport.getSalt());

                String freebieItem = resultSet.getString("freebieItem");
                if (freebieItem == null) {
                    freebieItem = "";
                }
                promoReport.setFreebieItem(freebieItem);
                promoReport.setFreebieCost(resultSet.getDouble("freebieCost"));
                promoReport.setFreebieSalePrice(resultSet.getDouble("freebieSalePrice"));

                promoReports.add(promoReport);

                return null;
            });
        }
    }

    private void createPromoOrderSheet(List<ZomatoPromoReport> promoReports, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "Aggregation", "tab_id", "res_id", "Date", "Day", "Store Code", "entity_name", "brand_name", "account_type", "promo_code", "ps_segment", "Salt",
            "zvd_final", "zvd", "mvd", "offline_recon", "control", "burn", "Total", "net", "construct", "commission",
            "Pg", "GST on commission", "GST on Pg", "Final Amount", "CPRL Share",
            "Dis %", "Construct", "Promo Remarks", "POS Amount", "Delta Amount", "Reco Status", "Reco remarks", "POS Bill amount (Excluding GST 5%)",
            "POS Commission", "POS TDS ", "POS Receivable",
            "Zomato Receivable", "Delta Receivable", "UTR Amount", "Refund for disputed order",
            "Freebies",
            "Order Status as per Zomato", "Freebie Item", "Freebie cost price", "Freebie sale price"

        };

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        for (ZomatoPromoReport report : promoReports) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            double totalDiscount = report.getZvd();
            boolean isMaxDiscountCapped = totalDiscount > 80.0;

            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getAggregation().format(Formatter.YYYYMMDD));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getTabId());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getResId());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getAggregation().format(Formatter.DDMMYYYY_DASH));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getDayOfWeek().name());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getStoreCode());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getEntityName());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getBrandName());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getAccountType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPromoCode());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPsSegment());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getSalt());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getZvdFinal());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getZvd());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getMvd());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getOfflineRecon());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getControl());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getBurn());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getTotal());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getNet());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getConstruct());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getCommission());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPg());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getGstOnCommission());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getGstOnPg());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getFinalAmount());

            // CPRL Share
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");

            boolean isFreebie = report.isFreebieOrder();

            // Dis %
            double discountPercentage = 0;
            if (!isFreebie) {

                Cell cell = row.createCell(row.getPhysicalNumberOfCells());
                discountPercentage = (totalDiscount * 100 / report.getNet());
                String formattedValue = decimalFormat.format(discountPercentage);
                if (report.getNet() != 0) {
                    cell.setCellValue(Double.parseDouble(formattedValue));
                } else {
                    cell.setCellValue(formattedValue);
                }
            } else {
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
            }

            // Construct
            String construct = "";
            if (isFreebie) {
                construct = "Freebie Orders";
            } else {
                construct = "Maximum 40% upto 80";
            }
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(construct);

            // Promo Remarks
            String remark = "";
            if (isFreebie) {
                remark = "No Promo Share asked (Freebie Orders)";
                if (!DayOfWeek.TUESDAY.equals(report.getDayOfWeek()) && !DayOfWeek.FRIDAY.equals(report.getDayOfWeek())) {
                    remark = "Freebies given other than offer day";
                }
            } else {
                remark = "As per Approval Mail";
                if (isMaxDiscountCapped) {
                    remark = "Disallowed due to Dis. Value more than 80/-";
                } else if (discountPercentage > 40 || report.getNet() == 0) {
                    remark = "Discount % is more than 40%, however Discount amount is less than Rs. 80/-";
                }
            }
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(remark);

            // POS Amount
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPosTotalAmount() + report.getPosConsumerGst());

            // Diff.
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("AE" + (row.getRowNum() + 1) + "-S" + (row.getRowNum() + 1) + "*105%");

            String reason = report.getReason();
            String status = report.getUnreconciledAmount() == 0 ? "reconciled" : "unreconciled";
            if (report.getThreePoOrderId() == null) {
                reason = "Not found in Zomato Order level data";
                status = "unreconciled";
            }
            // Reco status
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(status);
            // CPRL Reco remarks
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reason);

            // Bill amount (Excluding GST 5%)
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPosTotalAmount());

            // POS Commission
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPosCommission());

            // POS TDS
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPosTds());

            // POS Receivable
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPosReceivable());

            // Amount received as per swiggy
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getThreePoReceivable());

            // Diff.
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getDeltaReceivables());

            // UTR Amount
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPayout_amount());

            // Refund for disputed order (As per Swiggy's Order Level Data)
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getRefundForDisputedOrder());

            // Freebies value as per zomato
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getSalt());

            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getOrderStatus());

            if (isFreebie) {
                // Freebie Item
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getFreebieItem());
                // Freebie Cost
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getFreebieCost());
                // Freebie Sale Price
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getFreebieSalePrice());
            }

        }
    }

    private void getFreebieRow(Sheet sheet, int currentRow, String cellValue, String construct, Sheet orderSheet, String reason, String cprlRemark, String discountTotal, String cprlShare) {
        Row row = sheet.createRow(currentRow);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cellValue);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(construct);

        String formula = "SUMIF(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), "N")
        }, ",") + ")";
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);

    }

    private void createPromoSummarySheet(Sheet orderSheet, Sheet sheet) {
        int currentRow = 0;
        Row header = sheet.createRow(currentRow++);
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Particulars");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Construct");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Zomato Voucher Discount");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("CPRPL Share");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Commission Charges");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total PG Charge");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total GST On Commission");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total GST On PG");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Amount Payable");
        Row row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("Earlier claimed amount as per Zomato");
        String formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "N"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(2).setCellFormula(formula);
        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "R"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(3).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "V"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(4).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "W"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(5).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "X"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(6).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "Y"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(7).setCellFormula(formula);
        row.createCell(8).setCellFormula("D2-E2-F2-G2-H2");

        getRemarksRow(sheet, currentRow++, "Amount Payable As per Approval email (Construct up to Rs.80/-)", "Maximum 40% upto 80", orderSheet, "As per Approval Mail");
        getRemarksRow(sheet, currentRow++, "Disallowed due to Dis. Value more than 80/-", "Maximum 40% upto 80", orderSheet, "Disallowed due to Dis. Value more than 80/-");
        getFreebieRow(sheet, currentRow++, "Freebie Orders", "No Promo share was asked", orderSheet, "No Promo Share asked (Freebie Orders)", "U", "N", "R");
        getRemarksRow(sheet, currentRow++, "Discount % is more than 40%, however Discount amount is less than Rs. 80/-", "Maximum 40% upto 80", orderSheet, "Discount % is more than 40%, however Discount amount is less than Rs. 80/-");
        row = sheet.createRow(currentRow++);
        row.createCell(2).setCellFormula("SUM(" + "C3" + ":C" + (currentRow - 1) + ")");
        row.createCell(3).setCellFormula("SUM(" + "D3" + ":D" + (currentRow - 1) + ")");
        row.createCell(4).setCellFormula("SUM(" + "E3" + ":E" + (currentRow - 1) + ")");
        row.createCell(5).setCellFormula("SUM(" + "F3" + ":F" + (currentRow - 1) + ")");
        row.createCell(6).setCellFormula("SUM(" + "G3" + ":G" + (currentRow - 1) + ")");
        row.createCell(7).setCellFormula("SUM(" + "H3" + ":H" + (currentRow - 1) + ")");
        row.createCell(8).setCellFormula("SUM(" + "I3" + ":I" + (currentRow - 1) + ")");
//
        sheet.createRow(currentRow++).createCell(0).setCellValue("To be excluded:");
//
        row = sheet.createRow(currentRow++);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("Disallowed due to Dis. Value more than 80/-");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("C4");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-D4");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-E4");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-F4");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-G4");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-H4");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-I4");
//

        row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("Discount % is more than 40%, however Discount amount is less than Rs. 80/-");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("C6");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-D6");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-E6");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-F6");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-G6");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-H6");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("-I6");

        row = sheet.createRow(currentRow++);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("CPRL Share");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");

        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("SUM(D7:D" + (currentRow - 1) + ")");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("SUM(E7:E" + (currentRow - 1) + ")");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("SUM(F7:F" + (currentRow - 1) + ")");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("SUM(G7:G" + (currentRow - 1) + ")");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("SUM(H7:H" + (currentRow - 1) + ")");
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("SUM(I7:I" + (currentRow - 1) + ")");

    }

    private void getRemarksRow(Sheet sheet, int currentRow, String cellValue, String construct, Sheet orderSheet, String reason) {
        Row row = sheet.createRow(currentRow);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cellValue);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(construct);

        String formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "N"),
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\"", getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\""
        }, ",") + ")";
        row.createCell(2).setCellFormula(formula);
        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "R"), getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(3).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "V"), getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(4).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "W"), getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(5).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "X"), getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(6).setCellFormula(formula);

        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "Y"), getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), "G"), "\"<>\""
        }, ",") + ")";
        row.createCell(7).setCellFormula(formula);
        formula = String.format("D%s-E%s-F%s-G%s-H%s", (currentRow + 1), (currentRow + 1), (currentRow + 1), (currentRow + 1), (currentRow + 1));
        row.createCell(8).setCellFormula(formula);

    }

    private void createFoodSummarySheet(Sheet orderSheet, Sheet sheet, List<ZomatoPromoReport> reports,
            LocalDate startDate, LocalDate endDate) {
        Map<String, Map<Double, Map<Double, Map<DayOfWeek, List<ZomatoPromoReport>>>>> map = reports.stream().filter(k -> StringUtils.isNotBlank(k.getFreebieItem())).collect(groupingBy(ZomatoPromoReport::getFreebieItem,
                Collectors.groupingBy(ZomatoPromoReport::getFreebieCost, Collectors.groupingBy(ZomatoPromoReport::getFreebieSalePrice, Collectors.groupingBy(ZomatoPromoReport::getDayOfWeek)))));
        int rowNum = 0;
        Row header = sheet.createRow(rowNum++);
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Item Name");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Cost Price");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Sale Price");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Day");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Items");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Cost");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Sale");
        Set<DayOfWeek> daysSet = new HashSet<>();

        int mapWiseRows = 0;

        for (Map.Entry<String, Map<Double, Map<Double, Map<DayOfWeek, List<ZomatoPromoReport>>>>> entry1 : map.entrySet()) {
            String itemName = entry1.getKey();
            for (Map.Entry<Double, Map<Double, Map<DayOfWeek, List<ZomatoPromoReport>>>> entry2 : entry1.getValue().entrySet()) {
                double costPrice = entry2.getKey();
                for (Map.Entry<Double, Map<DayOfWeek, List<ZomatoPromoReport>>> entry3 : entry2.getValue().entrySet()) {
                    double salePrice = entry3.getKey();
                    for (Map.Entry<DayOfWeek, List<ZomatoPromoReport>> entry4 : entry3.getValue().entrySet()) {
                        DayOfWeek day = entry4.getKey();
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(itemName);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(costPrice);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(salePrice);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(day.name());
                        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "AC"),
                            "\"Freebie Orders\"",
                            getFormulaWithRef(orderSheet.getSheetName(), "AU"), "A" + rowNum,
                            getFormulaWithRef(orderSheet.getSheetName(), "E"), "\"" + day.name() + "\"",
                            getFormulaWithRef(orderSheet.getSheetName(), "AV"), "\"=" + costPrice + "\"",
                            getFormulaWithRef(orderSheet.getSheetName(), "AW"), "\"=" + salePrice + "\""
                        }, ",") + ")";
                        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("E" + rowNum + "*B" + rowNum);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("E" + rowNum + "*C" + rowNum);
                        daysSet.add(day);
                        ++mapWiseRows;
                    }
                }
            }
        }
        Row row = sheet.createRow(rowNum++);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("Freebies given other than offer day");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "AC"),
            "\"Freebie Orders\"",
            getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"Freebies given other than offer day\""
        }, ",") + ")";
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("");
        formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "L"), getFormulaWithRef(orderSheet.getSheetName(), "AC"),
            "\"Freebie Orders\"",
            getFormulaWithRef(orderSheet.getSheetName(), "AD"),
            "\"Freebies given other than offer day\""
        }, ",") + ")";
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);

        rowNum++;

        header = sheet.createRow(rowNum++);
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Day");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Sale");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Cost");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Approved Cost");

        Map<DayOfWeek, Double> approvedCostMap = getApprovedCostDayWiseForMonth(startDate, endDate);

        int dayCount = daysSet.size();

        for (DayOfWeek day : daysSet) {
            row = sheet.createRow(rowNum++);
            double costBudget = approvedCostMap.getOrDefault(day, 0.0);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(day.name());
            formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(sheet.getSheetName(), "G", 2, (mapWiseRows + 1)),
                getFormulaWithRef(sheet.getSheetName(), "D", 2, (mapWiseRows + 1)),
                "A" + rowNum,}, ",") + ")";
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);
            formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(sheet.getSheetName(), "F", 2, (mapWiseRows + 1)),
                getFormulaWithRef(sheet.getSheetName(), "D", 2, (mapWiseRows + 1)),
                "A" + rowNum,}, ",") + ")";
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(costBudget);
        }
        if (dayCount > 0) {
            row = sheet.createRow(rowNum++);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");

            String totalSale = "SUM(B" + (rowNum - dayCount) + ":B" + (rowNum - 1) + ")";
            String totalCost = "SUM(C" + (rowNum - dayCount) + ":C" + (rowNum - 1) + ")";
            String totalApproved = "SUM(D" + (rowNum - dayCount) + ":D" + (rowNum - 1) + ")";

            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(totalSale);
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(totalCost);
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(totalApproved);

        }

    }

    public int getMaximumCappedDiscount() {
        return 80;
    }

    private String getFormulaWithRef(String sheetName, String column) {
        return "'" + sheetName + "'" + "!" + column + ":" + column;
    }

    private String getFormulaWithRef(String sheetName, String column, int start, int end) {
        end = Math.max(end, start);
        return "'" + sheetName + "'" + "!" + column + start + ":" + column + end;
    }

    private Map<DayOfWeek, Double> getApprovedCostDayWiseForMonth(LocalDate startDate, LocalDate endDate) {
        String sql = PromoQueries.DATEWISE_FREEBIE_BUDGET;
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        parameters.addValue("startDate", startDate);
        parameters.addValue("endDate", endDate);
        parameters.addValue("tender", "ZOMATO");

        HashMap<DayOfWeek, Double> map = new HashMap<>();

        jdbcTemplate.query(sql, parameters, (resultSet, rowNumber) -> {
            String day = resultSet.getString("day");
            double budget = resultSet.getDouble("budget");
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(day.toUpperCase(Locale.ROOT));
            map.put(dayOfWeek, budget);
            return null;
        });
        return map;
    }

    public ThreePOData getThreePODashboardDataResponse(LocalDate startDate, LocalDate endDate, List<String> stores) {
        ThreePOData response;
        try {
            CompletableFuture<ThreePOData> threePOsDataFuture = CompletableFuture.supplyAsync(() -> getThreePODashboardData(startDate, endDate, stores));
            response = threePOsDataFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new ApiException("Error while fetching zomato dashboard data");
        }
        return response;
    }

    private ThreePOData getThreePODashboardData(LocalDate startDate, LocalDate endDate, List<String> stores) {
        String sql = ThreePoQueries.Zomato_threePODashboardQuery;
        log.info("threepo zomato dahsboard  query {}", sql);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", startDate.format(Formatter.YYYYMMDD_DASH));
        parameters.addValue("endDate", endDate.format(Formatter.YYYYMMDD_DASH));
        parameters.addValue("storeCodes", stores);
        ThreePOData threePOData = new ThreePOData();
        threePOData.setTenderName(ThreePO.ZOMATO);

        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
            double discount = 0;
            double threePOSales = resultSet.getDouble("threePOSales");
            double threePOReceivables = resultSet.getDouble("threePOReceivables");
            double threePOCommission = resultSet.getDouble("threePOCommission");
            double threePOCharges = resultSet.getDouble("threePOCharges");
            double salt = resultSet.getDouble("freebies");

            threePOData.setThreePOSales(threePOSales);
            threePOData.setThreePOReceivables(threePOReceivables);
            threePOData.setThreePOCommission(threePOCommission);
            threePOData.setThreePOCharges(threePOCharges);
            threePOData.setThreePODiscounts(discount);
            threePOData.setThreePOFreebies(salt);

            // Ideal scenarios
            double posReceivables = resultSet.getDouble("posReceivables");
            double posCommission = resultSet.getDouble("posCommission");
            double posCharges = resultSet.getDouble("posCharges");

            threePOData.setPosReceivables(posReceivables);
            threePOData.setPosCommission(posCommission);
            threePOData.setPosCharges(posCharges);
            threePOData.setPosDiscounts(discount);
            threePOData.setPosFreebies(salt);

            double reconciled = resultSet.getDouble("reconciled");
            double unreconciled = resultSet.getDouble("unreconciled");
            double receivablesVsReceipts = resultSet.getDouble("receivablesVsReceipts");
            double threePoDiscount = resultSet.getDouble("threePODiscounts");
            double posDiscount = resultSet.getDouble("posDiscounts");

            threePOData.setReceivablesVsReceipts(receivablesVsReceipts);
            threePOData.setReconciled(reconciled);
            threePOData.setPosVsThreePO(unreconciled);
            threePOData.setThreePODiscounts(threePoDiscount);
            threePOData.setPosDiscounts(posDiscount);

            return null;
        });

        return threePOData;
    }

    public void createWorkBookDynamically(String query, MapSqlParameterSource parameters, Workbook workbook, Sheet sheet, String sheetName, String highlightedColumn, Boolean exceptionalReport) {
        AtomicInteger rowNo = new AtomicInteger(1);
        AtomicBoolean headers = new AtomicBoolean(true);
        AtomicReference<Sheet> sheetRef = new AtomicReference<>(sheet);
        if (sheetRef.get() == null) {
            sheetRef.set(workbook.createSheet(sheetName));
        }
        Row headerRow = sheetRef.get().createRow(0);
        jdbcTemplate.query(query, parameters, (ResultSet resultSet, int rowNum) -> {
            int columnCount = resultSet.getMetaData().getColumnCount();
            if (headers.get()) {
                for (int index = 1; index <= columnCount; index++) {
                    String columnName = resultSet.getMetaData().getColumnName(index);
                    headerRow.createCell(index - 1).setCellValue(columnName);
                    if (highlightedColumn != null && columnName.equalsIgnoreCase(highlightedColumn)) {
                        CellStyle boldStyle = workbook.createCellStyle();
                        Font boldFont = workbook.createFont();
                        boldFont.setBold(true);
                        boldStyle.setFont(boldFont);
                        headerRow.getCell(index - 1).setCellStyle(boldStyle);
                    }
                }
                headers.set(false);
            }
            Row row = sheetRef.get().createRow(rowNo.getAndIncrement());
            for (int index = 1; index <= columnCount; index++) {
                Object obj = resultSet.getObject(index);
                if (obj == null) {
                    row.createCell(index - 1).setCellValue("");
                } else if (obj instanceof String string) {
                    row.createCell(index - 1).setCellValue(string);
                } else if (obj instanceof Double double1) {
                    row.createCell(index - 1).setCellValue(double1);
                } else if (obj instanceof LocalDateTime localDateTime) {
                    row.createCell(index - 1).setCellValue(localDateTime.toString());
                } else if (obj instanceof LocalDate localDate) {
                    row.createCell(index - 1).setCellValue(localDate.toString());
                } else {
                    row.createCell(index - 1).setCellValue(obj.toString());
                }
            }
            return null;
        });
        if (exceptionalReport && headers.get()) {
            List<String> columns = extractColumns(query);
            int index = 0;
            for (String column : columns) {
                headerRow.createCell(index).setCellValue(column);
                index++;
            }
        }
    }

    public static List<String> extractColumns(String sqlQuery) {
        List<String> columns = new ArrayList<>();
        // Regular expression pattern to match columns between SELECT and FROM clauses
        String regex = "(?i)SELECT\\s+(.*?)\\s+FROM";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sqlQuery);
        if (matcher.find()) {
            String columnsPart = matcher.group(1); // Get the part between SELECT and FROM
            // Split columns by commas and trim spaces
            String[] columnArray = columnsPart.split(",");
            for (String column : columnArray) {
                // Remove the alias 'z.' if it exists and add to the list
                columns.add(column.trim().replaceFirst("^z\\.", ""));
            }
        }
        return columns;
    }

}
