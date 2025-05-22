package com.cpl.reconciliation.web.service.impl.ThreePo;

import com.cpl.core.api.constant.Formatter;
import com.cpl.core.api.exception.ApiException;
import com.cpl.core.common.annotations.TrackExecutionTime;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.core.response.threepo.GeneratedReportResponse;
import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;
import com.cpl.reconciliation.core.response.threepo.ThreePOData;
import com.cpl.reconciliation.domain.models.SwiggyPromoReport;
import com.cpl.reconciliation.domain.models.ThreePOReport;
import com.cpl.reconciliation.domain.repository.*;
import com.cpl.reconciliation.domain.util.QueryConfig;
import com.cpl.reconciliation.web.service.PromoQueries;
import com.cpl.reconciliation.web.service.ThreePoQueries;
import static com.cpl.reconciliation.web.service.impl.ThreePo.ZomatoApiService.extractColumns;
import com.cpl.reconciliation.web.service.util.POSThreePoSummarySheetUtil;
import com.cpl.reconciliation.web.service.util.ThreepoQueryDateRangeRecoLogicAndFEUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class SwiggyApiService extends ThreePOService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final OrderRepository orderRepository;
    private final SwiggyRepository swiggyRepository;
    private final POSThreePoSummarySheetUtil sheetUtil;
    private final SwiggyPromoRepository swiggyPromoRepository;
    private final BudgetMasterRepository budgetMasterRepository;
    private final ThreepoQueryDateRangeRecoLogicAndFEUtil threepoQueryDateRangeRecoLogicAndFEUtil;

    @TrackExecutionTime
    public ThreePOData getDashboardDataResponse(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        ThreePOData response;
        try {
            CompletableFuture<Double> posSalesFuture = CompletableFuture.supplyAsync(() -> getPOSSales(startDate, endDate, storeCodes));
            CompletableFuture<ThreePOData> threePOsDataFuture = CompletableFuture.supplyAsync(() -> getThreePOData(startDate, endDate, storeCodes));
            CompletableFuture<Double> getPOsTotalNotFoundInSwiggyFuture = CompletableFuture.supplyAsync(() -> getPOsTotalNotFoundInSwiggy(startDate, endDate, storeCodes));
            CompletableFuture<Double> getPromoShare = CompletableFuture.supplyAsync(() -> getPromoShare(startDate, endDate, storeCodes));

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(posSalesFuture, threePOsDataFuture, getPOsTotalNotFoundInSwiggyFuture, getPromoShare);
            allFutures.get();

            Double posSales = posSalesFuture.get();
            ThreePOData threePOsData = threePOsDataFuture.get();
            double possTotalNotFoundInSwiggy = getPOsTotalNotFoundInSwiggyFuture.get();

            response = getMergedResponse(posSales, possTotalNotFoundInSwiggy, threePOsData);
            response.setPromo(getPromoShare.get());
            response.setDeltaPromo(getPromoShare.get());
        } catch (Exception e) {
            throw new ApiException("Error while fetching data");
        }

        return response;
    }

    private double getPromoShare(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        return swiggyPromoRepository.getPromoShare(startDate.toLocalDate(), endDate.toLocalDate(), storeCodes);
    }

    private double getPOsTotalNotFoundInSwiggy(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        return orderRepository.getTotalSalesNotFoundInSwiggy(startDate, endDate, storeCodes);
    }

    private ThreePOData getMergedResponse(Double posSales,
            double posTotalNotFoundInSwiggy,
            ThreePOData threePOData) {
        threePOData.setPosSales(posSales);
        threePOData.setReconciled(threePOData.getReconciled());
        threePOData.setPosVsThreePO(threePOData.getPosVsThreePO() + posTotalNotFoundInSwiggy);
        return threePOData;
    }

    private Double getPOSSales(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        return orderRepository.getTotalAmountByDateAndThreePOSource(startDate.toLocalDate(), endDate.toLocalDate(), "swiggy", storeCodes);
    }

    private ThreePOData getThreePOData(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        String sql = ThreePoQueries.Swiggy_threePOQuery;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", startDate.format(Formatter.YYYYMMDD_HHMMSS_DASH));
        parameters.addValue("endDate", endDate.format(Formatter.YYYYMMDD_HHMMSS_DASH));
        parameters.addValue("storeCodes", storeCodes);
        ThreePOData threePOData = new ThreePOData();
        threePOData.setTenderName(ThreePO.SWIGGY);

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

    public GeneratedReportResponse getPOSvs3POResponse(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        GeneratedReportResponse response = new GeneratedReportResponse();
        double posSales = getPOSSales(startDate, endDate, storeCodes);
        response.setSales(posSales);

        ThreePOData threePO = getThreePOData(startDate, endDate, storeCodes);
//       response.setReceipts(threePO.getReceipts());
        response.setCharges(threePO.getThreePOCharges());
        response.setDifference(response.getSales() - threePO.getThreePOSales());
        response.setReconciled(orderRepository.getTotalSalesReconciledInSwiggy(startDate, endDate, storeCodes));

        return response;

    }

    @TrackExecutionTime
    public void posVsThreePoDownload(ThreePODataRequest request, Workbook workbook, boolean allThreePoDownload) throws SQLException {
        Sheet sheet3 = workbook.createSheet("Swiggy Summary");
        Sheet sheet1 = workbook.createSheet("Swiggy POS vs 3PO");
        Sheet sheet2 = workbook.createSheet("Swiggy 3PO vs POS");
        Sheet sheet4 = workbook.createSheet("S2");

        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("storeCodes", request.getStores());

        List<Object[]> orderNotFoundList;
        String orderNotFoundQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "ORDER_NOT_FOUND_QUERY");
        log.info("Swiggy orderNotFoundQuery {}", orderNotFoundQuery);
        if (!orderNotFoundQuery.isEmpty()) {
            log.info("ORDER_NOT_FOUND_QUERY query :: {}", orderNotFoundQuery);
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
        log.error("Order not found in swiggy count: " + orderNotFoundList.size());
        String posSalesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_SALES_NextDate_QUERY");
        log.info("Swiggy POS_SALES_NextDate_QUERY {}", posSalesQuery);
        if (!orderNotFoundQuery.isEmpty()) {
            MapSqlParameterSource posSaleParameters = new MapSqlParameterSource();
            posSaleParameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(posSalesQuery, posSaleParameters, workbook, sheet4, "Swiggy Pos Sales", null, false);
        }
        List<ThreePOReport> threePOReportPOS = getThreePOVSPOSOrders(request, "SWIGGY", request.getStores(), jdbcTemplate, threepoQueryDateRangeRecoLogicAndFEUtil);
        Row[] rowHeaders = downloadDeltaSheet(threePOReportPOS, orderNotFoundList, sheet1, sheet2, workbook, allThreePoDownload, ThreePO.SWIGGY);
        sheetUtil.createSummarySheet(workbook, sheet3, sheet1, sheet2, ThreePO.SWIGGY, sheet4, startDate, endDate, rowHeaders);
        //  exceptionalReportingNew(workbook, startDate, endDate, request.getStores());
        String exceptionalReportQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "EXCEPTIONAL_REPORT_QUERY");
        log.info("Swiggy exceptionalReportQuery {}", exceptionalReportQuery);
        if (!exceptionalReportQuery.isEmpty() && exceptionalReportQuery.contains("<>")) {
            MapSqlParameterSource expParameters = new MapSqlParameterSource();
            expParameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(exceptionalReportQuery, expParameters, workbook, null, "Exceptional Column Reporting", null, true);
        }
    }

    public void exceptionalReportingNew(Workbook workbook, LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
        Sheet sheet = workbook.createSheet("Exceptional Column Reporting");
        String[] headers = {"Order ID", "Date", "Order status", "Discount On Swiggy Platform Service Fee", "Long Distance Fee", "Discount on Long Distance Fee", "Collection Fee", "Access Fee", "Merchant Cancellation Charges", "Call Center Service Fees", "Delivery fee (sponsored by merchant w/o tax)", "TCS"};
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

        String query = "SELECT order_no,order_date,order_status,discount_on_swiggy_platform_service_fee,"
                + "long_distance_fee,discount_on_long_distance_fee,collection_fee,access_fee,"
                + "merchant_cancellation_charges,call_center_servicefees,delivery_fee,tcs from swiggy where "
                + " (discount_on_swiggy_platform_service_fee <> 0"
                + " OR long_distance_fee <> 0"
                + " OR discount_on_long_distance_fee <> 0"
                + " OR collection_fee <> 0"
                + " OR access_fee <> 0"
                + " OR merchant_cancellation_charges <> 0"
                + " OR call_center_servicefees <> 0"
                + " OR delivery_fee <> 0"
                + " OR tcs <> 0) AND Date(order_date) between :startDate AND :endDate AND (COALESCE(:storeCodes,NULL) is null or store_code in (:storeCodes))";

        AtomicInteger rowNum = new AtomicInteger(1);
        jdbcTemplate.query(query, parameters, (ResultSet rs) -> {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("order_no"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("order_date"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("order_status"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("discount_on_swiggy_platform_service_fee"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("long_distance_fee"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("discount_on_long_distance_fee"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("collection_fee"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("access_fee"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("merchant_cancellation_charges"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("call_center_servicefees"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("delivery_fee"));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(rs.getString("tcs"));
            rowNum.incrementAndGet();
        });
    }

    public void reconciledDownload(ThreePODataRequest request, Workbook workbook) {
        String reconciledQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_RECONCILED_REPORT_QUERY");
        log.info("Swiggy reconciledQuery {}", reconciledQuery);
        if (!reconciledQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(reconciledQuery, parameters, workbook, null, "Swiggy Reconciled", null, false);
        }
    }

    public void discountsDownload(ThreePODataRequest request, Workbook workbook) {
        String discountQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_SALES_QUERY");
        log.info("Zomato discountQuery {}", discountQuery);
        if (!discountQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(discountQuery, parameters, workbook, null, "Swiggy", "merchant_discount", false);
        }
    }

    public void posSalesDownload(ThreePODataRequest request, Workbook workbook) {
        String posSalesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_SALES_QUERY");
        log.info("Swiggy posSalesQuery {}", posSalesQuery);
        if (!posSalesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(posSalesQuery, parameters, workbook, null, "Swiggy Pos Sales", null, false);
        }
    }

    @TrackExecutionTime
    public void threePOSalesDownloadNew(ThreePODataRequest request, Workbook workbook) {
        String threepoSalesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_SALES_QUERY");
        log.info("Swiggy threepoSalesQuery {}", threepoSalesQuery);
        if (!threepoSalesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            createWorkBookDynamically(threepoSalesQuery, parameters, workbook, null, "Swiggy", null, false);
        }
    }

    public void posReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("SwiggyPosReceivableReport");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Total", "Merchant Discount", "Actual Packaging Charge", "Refund For Disputed Order", "Order Status", "Pick UP Status", "POS Receivables"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posReceivableQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_RECIEVABLES_QUERY");
        log.info("Swiggy posReceivableQuery {}", posReceivableQuery);
        if (!posReceivableQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posReceivableQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("item_total"));
                row.createCell(9).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getDouble("refund_for_disputed_order"));
                row.createCell(12).setCellValue(resultSet.getString("order_status"));
                row.createCell(13).setCellValue(resultSet.getString("pick_up_status"));
                row.createCell(14).setCellValue(resultSet.getDouble("posReceivables"));
                return null;
            });
        }
    }

    public void allPOSChargesDownload(ThreePODataRequest request, Workbook workbook) {
    }

    public void posChargesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy POS Charges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Total", "Merchant Discount", "Actual Packaging Charge", "Refund For Disputed Order", "Order Status", "Pick UP Status", "POS Charges"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_CHARGES_QUERY");
        log.info("Swiggy posChargesQuery {}", posChargesQuery);
        if (!posChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("item_total"));
                row.createCell(9).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getDouble("refund_for_disputed_order"));
                row.createCell(12).setCellValue(resultSet.getString("order_status"));
                row.createCell(13).setCellValue(resultSet.getString("pick_up_status"));
                row.createCell(14).setCellValue(resultSet.getDouble("posCharges"));
                return null;
            });
        }
    }

    public void posDiscountsDownload(ThreePODataRequest request, Workbook workbook) {
    }

    public void posFreebiesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy POS Freebies Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Total", "Merchant Discount", "Actual Packaging Charge", "Refund For Disputed Order", "Order Status", "Pick UP Status", "POS freebies"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posFreebiesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_FREEBIE_QUERY");
        log.info("Swiggy posFreebiesQuery {}", posFreebiesQuery);
        if (!posFreebiesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posFreebiesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("item_total"));
                row.createCell(9).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getDouble("refund_for_disputed_order"));
                row.createCell(12).setCellValue(resultSet.getString("order_status"));
                row.createCell(13).setCellValue(resultSet.getString("pick_up_status"));
                row.createCell(14).setCellValue(resultSet.getDouble("freebies"));
                return null;
            });
        }
    }

    public void posCommissionDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy POS Commission Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Total", "Merchant Discount", "Actual Packaging Charge", "Refund For Disputed Order", "Order Status", "Pick UP Status", "POS Commission"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String posCommissionQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_COMMISSION_QUERY");
        log.info("Swiggy posCommissionQuery {}", posCommissionQuery);
        if (!posCommissionQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(posCommissionQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("item_total"));
                row.createCell(9).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getDouble("refund_for_disputed_order"));
                row.createCell(12).setCellValue(resultSet.getString("order_status"));
                row.createCell(13).setCellValue(resultSet.getString("pick_up_status"));
                row.createCell(14).setCellValue(resultSet.getDouble("posCommission"));
                return null;
            });
        }
    }

    public void threePOReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy POS Commission Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Net Payable Amount After TCS and TDS", "ThreePO Receivables"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOReceivableQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_RECIEVABLES_QUERY");
        log.info("Swiggy threePOReceivableQuery {}", threePOReceivableQuery);
        if (!threePOReceivableQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOReceivableQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("net_payable_amount_after_tcs_and_tds"));
                row.createCell(9).setCellValue(resultSet.getDouble("threePOReceivables"));
                return null;
            });
        } else {
            log.warn("ThreePO Receivables Query is not found. Please verify, swiggy recologics are uploaded.");
        }
    }

    public void threePOCommissionDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy ThreePO Commission Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Total Swiggy Service Fee", "ThreePO Commission"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOCommissionQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_COMMISSION_QUERY");
        log.info("Swiggy threePOCommission Query {}", threePOCommissionQuery);
        if (!threePOCommissionQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOCommissionQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("total_swiggy_service_fee"));
                row.createCell(9).setCellValue(resultSet.getDouble("threePOCommission"));
                return null;
            });
        } else {
            log.warn("ThreePO Commission Query is not found. Please verify, swiggy recologics are uploaded.");
        }
    }

    public void threePOFreebieDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy ThreePO Freebie Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Merchant Discount", "ThreePO Freebies"};
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
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(9).setCellValue(resultSet.getDouble("freebies"));
                return null;
            });
        }
    }

    public void receivablesVsReceiptsDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy ReceivablesVsReceipts Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Payout Date", "Reference Number", "Net Payable Amount After TCS And TDS", "Item Total", "Merchant Discount", "Actual Packaging Charge", "Refund For Disputed Order", "Order Status", "Pick Up Status", "Freebies", "Three PO Receivable", "POS Receivables", "Payout Amount", "POS ReceivablesVsReceipts", "ThreePO ReceivablesVsReceipts"};

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
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("order_no"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDate("order_date"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDate("business_date"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("pos_id"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("store_code"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDate("payout_date"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("reference_number"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("net_payable_amount_after_tcs_and_tds"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("item_total"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("refund_for_disputed_order"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("order_status"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("pick_up_status"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("freebies"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("threePOReceivables"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("posReceivables"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("payout_amount"));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("receivablesVsReceipts"));
                double ThreePoReceivablesVsReceipts = resultSet.getDouble("threePOReceivables") - resultSet.getDouble("payout_amount");
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(ThreePoReceivablesVsReceipts);
                return null;
            });
        }
    }

    public void threePOChargesDownload(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy ThreePO Charge Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "TDS", "Total GST", "ThreePO Charges"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String threePOChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_CHARGES_QUERY");
        log.info("Swiggy threePOChargesQuery {}", threePOChargesQuery);
        if (!threePOChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(threePOChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("tds"));
                row.createCell(9).setCellValue(resultSet.getDouble("total_gst"));
                row.createCell(10).setCellValue(resultSet.getDouble("threePOCharges"));
                return null;
            });
        }
    }

    public void allThreePOCharges(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy allThreePOCharges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Total Swiggy Service Fee", "Merchant Discount", "TDS", "Total GST", "ThreePO Commission", "Freebies", "ThreePO Discount", "ThreePO Charges"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String allThreePOChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "THREEPO_ALL_CHARGES_QUERY");
        log.info("Swiggy threePOChargesQuery {}", allThreePOChargesQuery);
        if (!allThreePOChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(allThreePOChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("total_swiggy_service_fee"));
                row.createCell(9).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(10).setCellValue(resultSet.getDouble("tds"));
                row.createCell(11).setCellValue(resultSet.getDouble("total_gst"));
                row.createCell(12).setCellValue(resultSet.getDouble("threePOCommission"));
                row.createCell(13).setCellValue(resultSet.getDouble("freebies"));
                row.createCell(14).setCellValue(resultSet.getDouble("threePODiscounts"));
                row.createCell(15).setCellValue(resultSet.getDouble("threePOCharges"));
                return null;
            });
        }
    }

    public void allPOSCharges(ThreePODataRequest request, Workbook workbook) {
        Sheet sheet = workbook.createSheet("Swiggy allPOSCharges Report");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Order Id", "Order Date", "invoice_number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Merchant Discount", "Item Total", "Actual Packaging Charge", "Refund For Disputed Order", "Freebies", "POS Commission", "POS Charges"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        String allPOSChargesQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_ALL_CHARGES_QUERY");
        log.info("Swiggy allPOSChargesQuery {}", allPOSChargesQuery);
        if (!allPOSChargesQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            AtomicInteger rowNo = new AtomicInteger(1);
            jdbcTemplate.query(allPOSChargesQuery, parameters, (resultSet, rowNum) -> {
                Row row = sheet.createRow(rowNo.getAndIncrement());
                row.createCell(0).setCellValue(resultSet.getString("order_no"));
                row.createCell(1).setCellValue(resultSet.getDate("order_date"));
                row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
                row.createCell(3).setCellValue(resultSet.getDouble("PosSales"));
                row.createCell(4).setCellValue(resultSet.getDate("business_date"));
                row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
                row.createCell(6).setCellValue(resultSet.getString("pos_id"));
                row.createCell(7).setCellValue(resultSet.getString("store_code"));
                row.createCell(8).setCellValue(resultSet.getDouble("merchant_discount"));
                row.createCell(9).setCellValue(resultSet.getDouble("item_total"));
                row.createCell(10).setCellValue(resultSet.getDouble("actual_packaging_charge"));
                row.createCell(11).setCellValue(resultSet.getDouble("refund_for_disputed_order"));
                row.createCell(12).setCellValue(resultSet.getDouble("freebies"));
                row.createCell(13).setCellValue(resultSet.getDouble("posCommission"));
                row.createCell(14).setCellValue(resultSet.getDouble("posCharges"));
                return null;
            });
        }
    }

    @Override
    public MissingThreePOMapping getThreePoMissingMapping() {
        MissingThreePOMapping mapping = new MissingThreePOMapping();
        mapping.setMissing(swiggyRepository.getRestaurantIdCountWherestoreCodeIsNull());
        mapping.setTotalStores(swiggyRepository.getRestaurantIdCount());
        mapping.setThreePO(ThreePO.SWIGGY);
        return mapping;
    }

    @Override
    public void downloadThreePoMissingMapping(Workbook workbook) {
        List<String> getMissingRestaurantIds = swiggyRepository.getRestaurantIdWherestoreCodeIsNull();
        Sheet sheet = workbook.createSheet("Swiggy Mappings");
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Swiggy Store Code", "Store Code", "State Code", "Customer Code", "Vendor Code", "Packaging Charge"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (String missingRestId : getMissingRestaurantIds) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(0).setCellValue(missingRestId);
        }

    }

    public void promo(ThreePODataRequest request, Workbook workbook) {
        LocalDate startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
        LocalDate endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
        List<SwiggyPromoReport> promoReports = new ArrayList<>();
        int welcomeBackTransactionCount = swiggyPromoRepository.getWelcomeBackTransactionCount(startDate, endDate, request.getStores());
        int totalTransactionCount = swiggyRepository.totalTransactionCount(startDate, endDate, request.getStores());
        String promoQuery = threepoQueryDateRangeRecoLogicAndFEUtil.reportQueryToExecuteBasedOnRecoLogicsDateRange(request, "POS_PROMO_REPORT_QUERY");
        log.info("Swiggy POS_PROMO_REPORT_QUERY {}", promoQuery);
        if (!promoQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("storeCodes", request.getStores());
            jdbcTemplate.query(promoQuery, parameters, (resultSet, rowNumber) -> {
                SwiggyPromoReport promoReport = new SwiggyPromoReport();

                promoReport.setDiscountTotal(resultSet.getDouble("discount_total"));
                promoReport.setGmv(resultSet.getDouble("gmv"));
                promoReport.setCouponCode(resultSet.getString("coupon_code"));
                promoReport.setDow(resultSet.getInt("dow"));
                promoReport.setRid(resultSet.getString("rid"));
                promoReport.setMonth(resultSet.getString("month"));
                promoReport.setOrderId(resultSet.getString("order_id"));
                promoReport.setHourOfDay(resultSet.getInt("hour_of_day"));
                promoReport.setDate(resultSet.getDate("date").toLocalDate());
                promoReport.setBrandName(resultSet.getString("brand_name"));
                promoReport.setCouponCode(resultSet.getString("coupon_code"));
                promoReport.setUserType(resultSet.getString("user_type"));
                promoReport.setUserCohort(resultSet.getString("user_cohort"));
                promoReport.setFreebieDiscount(resultSet.getDouble("freebie_discount"));
                promoReport.setDiscountTotal(resultSet.getDouble("discount_total"));
                promoReport.setGmv(resultSet.getDouble("gmv"));
                promoReport.setRemarks(resultSet.getString("remarks"));
                promoReport.setDay(DayOfWeek.valueOf(resultSet.getString("day")));
                promoReport.setStoreCode(resultSet.getString("store_code"));
                String freebieItem = resultSet.getString("freebieItem");
                if (freebieItem == null) {
                    freebieItem = "";
                }
                promoReport.setFreebieItem(freebieItem);
                promoReport.setFreebieCost(resultSet.getDouble("freebieCost"));
                promoReport.setFreebieSalePrice(resultSet.getDouble("freebieSalePrice"));
                promoReport.setImages(resultSet.getString("images"));
                promoReport.setReason(resultSet.getString("reason"));
                setThreePoReportValuesFromSql(promoReport, "swiggy", resultSet);
                promoReports.add(promoReport);

                return null;
            });
        }
        Sheet sheet1 = workbook.createSheet("Swiggy Order Sheet");
        Sheet sheet2 = workbook.createSheet("Swiggy Summary");
        Sheet sheet3 = workbook.createSheet("Swiggy Food cost");
        createPromoOrderSheet(promoReports, workbook, welcomeBackTransactionCount, totalTransactionCount, sheet1);
        createPromoSummarySheet(workbook, welcomeBackTransactionCount, totalTransactionCount, sheet1, sheet2);
        createFoodSummarySheet(sheet1, sheet3, promoReports, startDate, endDate);

    }

    private void createFoodSummarySheet(Sheet orderSheet, Sheet sheet, List<SwiggyPromoReport> reports,
            LocalDate startDate, LocalDate endDate) {
        Map<String, Map<Double, Map<Double, Map<DayOfWeek, List<SwiggyPromoReport>>>>> map = reports.stream().filter(SwiggyPromoReport::isFreebie).collect(groupingBy(SwiggyPromoReport::getFreebieItem,
                Collectors.groupingBy(SwiggyPromoReport::getFreebieCost, Collectors.groupingBy(SwiggyPromoReport::getFreebieSalePrice, Collectors.groupingBy(SwiggyPromoReport::getDay)))));
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

        for (Map.Entry<String, Map<Double, Map<Double, Map<DayOfWeek, List<SwiggyPromoReport>>>>> entry1 : map.entrySet()) {
            String itemName = entry1.getKey();
            for (Map.Entry<Double, Map<Double, Map<DayOfWeek, List<SwiggyPromoReport>>>> entry2 : entry1.getValue().entrySet()) {
                double costPrice = entry2.getKey();
                for (Map.Entry<Double, Map<DayOfWeek, List<SwiggyPromoReport>>> entry3 : entry2.getValue().entrySet()) {
                    double salePrice = entry3.getKey();
                    for (Map.Entry<DayOfWeek, List<SwiggyPromoReport>> entry4 : entry3.getValue().entrySet()) {
                        DayOfWeek day = entry4.getKey();
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(itemName);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(costPrice);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(salePrice);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(day.name());
                        String formula = "COUNTIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "T"),
                            "\"Freebie Orders\"",
                            getFormulaWithRef(orderSheet.getSheetName(), "AO"), "A" + rowNum,
                            getFormulaWithRef(orderSheet.getSheetName(), "H"), "\"" + day.name() + "\"",
                            getFormulaWithRef(orderSheet.getSheetName(), "AP"), "\"=" + costPrice + "\"",
                            getFormulaWithRef(orderSheet.getSheetName(), "AQ"), "\"=" + salePrice + "\""
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

        rowNum++;

        header = sheet.createRow(rowNum++);
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Day");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Sale");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Cost");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Approved Cost");

        Map<DayOfWeek, Double> approvedCostMap = getApprovedCostDayWiseForMonth(startDate, endDate);

        int dayCount = daysSet.size();

        for (DayOfWeek day : daysSet) {
            Row row = sheet.createRow(rowNum++);
            double costBudget = approvedCostMap.getOrDefault(day, 0.0);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(day.name());
            String formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(sheet.getSheetName(), "G", 2, (mapWiseRows + 1)),
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
            Row row = sheet.createRow(rowNum++);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");

            String totalSale = "SUM(B" + (rowNum - dayCount) + ":B" + (rowNum - 1) + ")";
            String totalCost = "SUM(C" + (rowNum - dayCount) + ":C" + (rowNum - 1) + ")";
            String totalApproved = "SUM(D" + (rowNum - dayCount) + ":D" + (rowNum - 1) + ")";

            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(totalSale);
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(totalCost);
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(totalApproved);

        }

    }

    private Map<DayOfWeek, Double> getApprovedCostDayWiseForMonth(LocalDate startDate, LocalDate endDate) {
        String sql = PromoQueries.DATEWISE_FREEBIE_BUDGET;
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        parameters.addValue("startDate", startDate);
        parameters.addValue("endDate", endDate);
        parameters.addValue("tender", "SWIGGY");
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

    private void createPromoSummarySheet(Workbook workbook, int welcomeBackTransactionCount, int totalTransactionCount, Sheet orderSheet, Sheet sheet) {
        int currentRow = 0;
        Row header = sheet.createRow(currentRow++);
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Particulars");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Construct");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Discount Burn");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("CPRPL Share");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Less:-  Comm. Inclusive GST");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Net Of Commision");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("GST On Remaining");
        header.createCell(header.getPhysicalNumberOfCells()).setCellValue("Total Amount Payable");
        Row row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("Earlier claimed amount as per Swiggy");
        row.createCell(2).setCellFormula("SUM(" + getFormulaWithRef(orderSheet.getSheetName(), "N", 2, orderSheet.getLastRowNum() + 1) + ")");
        String formula = "SUMIFS(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), "N"), getFormulaWithRef(orderSheet.getSheetName(), "Q"),
            "\"\"",
            getFormulaWithRef(orderSheet.getSheetName(), "B"), "\"<>\""
        }, ",") + ")";
        row.createCell(3).setCellFormula("45%*" + formula);
        row.createCell(4).setCellFormula("D2*20.65%");
        row.createCell(5).setCellFormula("D2-E2");
        row.createCell(6).setCellFormula("F2*18%");
        row.createCell(7).setCellFormula("F2+G2");

        getRemarksRow(sheet, currentRow++, "Amount Payable As per Approval email (Construct up to Rs.80/-)", "Maximum 40% upto 80", orderSheet, "As per Approval Mail", "U", "N", "R");
        getRemarksRow(sheet, currentRow++, "Disallowed due to Dis. Value more than 80/- (other Than welcome back)", "Maximum 40% upto 80", orderSheet, "Disallowed due to Dis. Value more than 80/- (other Than welcome back)", "U", "N", "R");
        getRemarksRow(sheet, currentRow++, getWelcomeBackTransactionCapRemark(welcomeBackTransactionCount, totalTransactionCount), "Maximum 40% upto 80", orderSheet, getWelcomeBackTransactionCapRemark(welcomeBackTransactionCount, totalTransactionCount), "U", "N", "R");
        getFreebieRow(sheet, currentRow++, "Freebie Orders", "No Promo share was asked", orderSheet, "No Promo Share asked (Freebie Orders)", "U", "N", "R");
        getRemarksRow(sheet, currentRow++, "Discount % is more than 40%, however Discount amount is less than Rs. 80/-", "Maximum 40% upto 80", orderSheet, "Discount % is more than 40%, however Discount amount is less than Rs. 80/-", "U", "N", "R");
        getFreebieRow(sheet, currentRow++, "On Welcomeback coupons, Freebies & Discount both have run. However our discount Share is Zero", "", orderSheet, "On Welcomeback coupons, Freebies also run. However our discount Share is Zero", "U", "N", "R");
        getRemarksRow(sheet, currentRow++, getWelcomeBackTransactionCapRemark(welcomeBackTransactionCount, totalTransactionCount) + " (Dis value more than 100/- so exluded from CPRL Share)", "Maximum 40% upto 80", orderSheet, getWelcomeBackTransactionCapRemark(welcomeBackTransactionCount, totalTransactionCount) + " (Dis value more than 100/- so exluded from CPRL Share)", "U", "N", "R");
        row = sheet.createRow(currentRow++);
        row.createCell(2).setCellFormula("SUM(" + "C3" + ":C" + (currentRow - 1) + ")");
        row.createCell(3).setCellFormula("SUM(" + "D3" + ":D" + (currentRow - 1) + ")");

        sheet.createRow(currentRow++).createCell(0).setCellValue("To be excluded:");

        row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("Disallowed due to Dis. Value more than 80/- (other Than welcome back)");
        row.createCell(2).setCellFormula("C4");
        row.createCell(3).setCellFormula("-D4");

        row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("Discount % is more than 40%, however Discount amount is less than Rs. 80/-");
        row.createCell(3).setCellFormula("-D7");

        row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue(getWelcomeBackTransactionCapRemark(welcomeBackTransactionCount, totalTransactionCount) + " (Dis value more than 100/- so exluded from CPRL Share)");
        row.createCell(1).setCellValue("Maximum 40% upto 80");
        row.createCell(2).setCellFormula("C9");
        row.createCell(3).setCellFormula("-D9");

        row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("Negative Settlement");
        row.createCell(3).setCellFormula("SUMIF(" + getFormulaWithRef(orderSheet.getSheetName(), "AD", 2, (orderSheet.getLastRowNum() + 1)) + ",\"<0\")");

        row = sheet.createRow(currentRow++);
        row.createCell(0).setCellValue("CPRL Share");

        row.createCell(3).setCellFormula("SUM(D10:D" + (currentRow - 1) + ")");
        row.createCell(4).setCellFormula("D" + currentRow + "*20.65%");
        row.createCell(5).setCellFormula(("D" + currentRow) + "-E" + (currentRow));
        row.createCell(6).setCellFormula("F" + currentRow + "*18%");
        row.createCell(7).setCellFormula(("F" + currentRow) + "+G" + (currentRow));

    }

    private String getFormulaWithRef(String sheetName, String column) {
        return "'" + sheetName + "'" + "!" + column + ":" + column;
    }

    private String getFormulaWithRef(String sheetName, String column, int start, int end) {
        end = Math.max(end, start);
        return "'" + sheetName + "'" + "!" + column + start + ":" + column + end;
    }

    private void getFreebieRow(Sheet sheet, int currentRow, String cellValue, String construct, Sheet orderSheet, String reason, String cprlRemark, String discountTotal, String cprlShare) {
        Row row = sheet.createRow(currentRow);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cellValue);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(construct);

        String formula = "SUMIF(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), cprlRemark),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), discountTotal)
        }, ",") + ")";
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);

    }

    private void getRemarksRow(Sheet sheet, int currentRow, String cellValue, String construct, Sheet orderSheet, String reason, String cprlRemark, String discountTotal, String cprlShare) {
        Row row = sheet.createRow(currentRow);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cellValue);
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(construct);
        int orderSheetRowCount = orderSheet.getLastRowNum() + 1;

        String formula = "SUMIF(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), cprlRemark),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), discountTotal)
        }, ",") + ")";
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);
        formula = "SUMIF(" + StringUtils.join(new String[]{getFormulaWithRef(orderSheet.getSheetName(), cprlRemark),
            "\"" + reason + "\"",
            getFormulaWithRef(orderSheet.getSheetName(), cprlShare)
        }, ",") + ")";
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula(formula);
        row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("(D" + (currentRow + 1) + "/C" + (currentRow + 1) + ")*100");

    }

    private void createPromoOrderSheet(List<SwiggyPromoReport> promoReports, Workbook workbook, int welcomeBackTransactionCount, int totalTransactionCount, Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {
            "DOW", "RID", "Store Code", "Month", "ORDER_ID", "Hour of the day",
            "Date", "Day", "BRAND_NAME", "COUPON_CODE", "User Type", "User Cohort",
            "Freebie discount", "DISCOUNT_TOTAL", "GMV", "Dis. Capped upto 80",
            "Remarks", "CPRL Share", "Dis %", "Construct", "Promo Remarks", "POS Amount", "Delta Amount", "Reco Status", "Reco remarks", "POS Bill amount (Excluding GST 5%)",
            "POS Commission", "POS TDS ", "POS Receivable",
            "Swiggy Receivable", "Delta Receivable", "Refund for disputed order",
            "Amount Receivable %", "Receipt %", "Freebies", "Images", "Reasons", "Order Status as per Swiggy", "Freebie Item", "Freebie cost price", "Freebie sale price"

        };

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        for (SwiggyPromoReport report : promoReports) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            boolean isFreebie = false;
            double totalDiscount = report.getDiscountTotal();
            double gmv = report.getGmv();
            String couponCode = report.getCouponCode();
            boolean isMaxDiscountCapped = totalDiscount > getMaximumCappedDiscount();
            int rowNum = row.getRowNum() + 1;

            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getDow());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getRid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getStoreCode());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getMonth());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getOrderId());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getHourOfDay());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getDate().format(Formatter.DDMMYYYY_DASH));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getDay().name());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getBrandName());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(couponCode);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getUserType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getUserCohort());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getFreebieDiscount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(totalDiscount);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(gmv);

            String remarks = report.getRemarks();
            if (remarks != null && remarks.contains("Freebie")) {
                isFreebie = true;
            }

            // Manual fields
            // Discount capped upto 80
            int discountCapped = 0;
            if (!isFreebie && !couponCode.equalsIgnoreCase("WELCOMEBACK") && isMaxDiscountCapped) {
                discountCapped = getMaximumCappedDiscount();
            }
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(discountCapped);

            // Remarks
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(remarks);

            // CPRL Share
            if (!isFreebie) {
                double cprlShare = 0.45 * totalDiscount;
                BigDecimal bd = new BigDecimal(cprlShare);
                BigDecimal roundedNumber = bd.setScale(2, RoundingMode.HALF_UP);
                cprlShare = roundedNumber.doubleValue();
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cprlShare);
            } else {
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(0);
            }

            // Dis %
            double discountPercentage = 0;
            if (!isFreebie) {
                Cell cell = row.createCell(row.getPhysicalNumberOfCells());
                discountPercentage = (totalDiscount * 100 / ((gmv / 1.05) - report.getPosPackagingCharge()));
                String formattedValue = decimalFormat.format(discountPercentage);
                cell.setCellValue(Double.parseDouble(formattedValue));
            } else {
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");
            }

            // Construct
            String construct = "";
            if (isFreebie) {
                construct = remarks;
            } else {
                if (couponCode.equalsIgnoreCase("WELCOMEBACK")) {
                    construct = "Welcome Back Coupons dis upto 100/-";
                    if (totalDiscount > 100) {
                        construct = "Welcome Back Coupons dis upto More than 100/-";
                    }
                } else {
                    construct = "Maximum 40% upto 80";
                }
            }
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(construct);

            // CPRL Remarks
            String remark = "";
            if (isFreebie) {
                remark = "No Promo Share asked (Freebie Orders)";
                if (couponCode != null && couponCode.equalsIgnoreCase("WELCOMEBACK")) {
                    remark = "On Welcomeback coupons, Freebies also run. However our discount Share is Zero";
                }
            } else {
                if (couponCode.equalsIgnoreCase("WELCOMEBACK")) {
                    remark = getWelcomeBackTransactionCapRemark(welcomeBackTransactionCount, totalTransactionCount);
                    if (totalDiscount > 100) {
                        remark += " (Dis value more than 100/- so exluded from CPRL Share)";
                    }
                } else {
                    remark = "As per Approval Mail";
                    if (isMaxDiscountCapped) {
                        remark = "Disallowed due to Dis. Value more than 80/- (other Than welcome back)";
                    } else if (discountPercentage > 40) {
                        remark = "Discount % is more than 40%, however Discount amount is less than Rs. 80/-";
                    }
                }
            }
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(remark);

            // POS Amount
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getPosTotalAmount() + report.getPosConsumerGst());

            // Diff.
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("V" + (row.getRowNum() + 1) + "-O" + (row.getRowNum() + 1));

            String reason = report.getReason();
            String status = report.getUnreconciledAmount() == 0 ? "reconciled" : "unreconciled";
            if (report.getThreePoOrderId() == null) {
                reason = "Not found in Swiggy Order level data";
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

            // Refund for disputed order (As per Swiggy's Order Level Data)
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getRefundForDisputedOrder());

            // Amount Receivable %
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("AC" + (row.getRowNum() + 1) + "/Z" + (row.getRowNum() + 1));

            // Receipt %
            row.createCell(row.getPhysicalNumberOfCells()).setCellFormula("AD" + (row.getRowNum() + 1) + "/Z" + (row.getRowNum() + 1));

            // Freebies value as per swiggy
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(report.getFreebieDiscount());

            // Images
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");

            // Reasons
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue("");

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

    public int getMaximumCappedDiscount() {
        return 80;
    }

    public String getWelcomeBackTransactionCapRemark(int welcomeBackTransactionCount, int totalTransactionCount) {
        double percentage = (double) (welcomeBackTransactionCount * 100) / totalTransactionCount;
        return "Welcome Back Coupon code - Total Transaction counts are " + welcomeBackTransactionCount + " , and these are" + (percentage > 2 ? " not" : "") + " within the limit of 2% of Total Transactions count";
    }

    public ThreePOData getThreePODashboardDataResponse(LocalDate startDate, LocalDate endDate, List<String> stores) {
        ThreePOData response;
        try {
            CompletableFuture<ThreePOData> threePOsDataFuture = CompletableFuture.supplyAsync(() -> getThreePODashboardData(startDate, endDate, stores));
            response = threePOsDataFuture.get();
            log.info("response {}", response);
        } catch (InterruptedException | ExecutionException e) {
            throw new ApiException("Error while fetching swiggy dashboard data");
        }
        return response;
    }

    private ThreePOData getThreePODashboardData(LocalDate startDate, LocalDate endDate, List<String> storeCodes) {
        String sql = ThreePoQueries.Swiggy_threePODashboardQuery;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", startDate.format(Formatter.YYYYMMDD_DASH));
        parameters.addValue("endDate", endDate.format(Formatter.YYYYMMDD_DASH));
        parameters.addValue("storeCodes", storeCodes);
        ThreePOData threePOData = new ThreePOData();
        threePOData.setTenderName(ThreePO.SWIGGY);
        log.info("threepo swiggy dahsboard  query {}", sql);
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

//    public void createWorkBookDynamically(String query, MapSqlParameterSource parameters, Workbook workbook, String sheetName, String highlightedColumn) {
//        AtomicInteger rowNo = new AtomicInteger(1);
//        AtomicBoolean headers = new AtomicBoolean(true);
//        Sheet sheet = workbook.createSheet(sheetName);
//        Row headerRow = sheet.createRow(0);
//
//        jdbcTemplate.query(query, parameters, (ResultSet resultSet, int rowNum) -> {
//            int columnCount = resultSet.getMetaData().getColumnCount();
//            if (headers.get()) {
//                for (int index = 1; index <= columnCount; index++) {
//                    String columnName = resultSet.getMetaData().getColumnName(index);
//                    headerRow.createCell(index - 1).setCellValue(columnName);
//                    if (highlightedColumn != null && columnName.equalsIgnoreCase(highlightedColumn)) {
//                        CellStyle boldStyle = workbook.createCellStyle();
//                        Font boldFont = workbook.createFont();
//                        boldFont.setBold(true);
//                        boldStyle.setFont(boldFont);
//                        headerRow.getCell(index - 1).setCellStyle(boldStyle);
//                    }
//                }
//                headers.set(false);
//            }
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            for (int index = 1; index <= columnCount; index++) {
//                Object obj = resultSet.getObject(index);
//                if (obj == null) {
//                    row.createCell(index - 1).setCellValue("");
//                } else if (obj instanceof String string) {
//                    row.createCell(index - 1).setCellValue(string);
//                } else if (obj instanceof Double double1) {
//                    row.createCell(index - 1).setCellValue(double1);
//                } else if (obj instanceof LocalDateTime localDateTime) {
//                    row.createCell(index - 1).setCellValue(localDateTime.toString());
//                } else if (obj instanceof LocalDate localDate) {
//                    row.createCell(index - 1).setCellValue(localDate.toString());
//                } else {
//                    row.createCell(index - 1).setCellValue(obj.toString());
//                }
//            }
//            return null;
//        });
//    }
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
