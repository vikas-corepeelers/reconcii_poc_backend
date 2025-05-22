//package com.cpl.reconciliation.web.service.impl.ThreePo;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.common.annotations.TrackExecutionTime;
//import com.cpl.reconciliation.core.enums.ThreePO;
//import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
//import com.cpl.reconciliation.core.response.threepo.GeneratedReportResponse;
//import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;
//import com.cpl.reconciliation.core.response.threepo.ThreePOData;
//import com.cpl.reconciliation.domain.entity.Magicpin;
//import com.cpl.reconciliation.domain.entity.OrderEntity;
//import com.cpl.reconciliation.domain.models.ThreePOReport;
//import com.cpl.reconciliation.domain.repository.MagicpinRepository;
//import com.cpl.reconciliation.domain.repository.OrderRepository;
//import com.cpl.reconciliation.web.service.ThreePoQueries;
//import com.cpl.reconciliation.web.service.util.POSThreePoSummarySheetUtil;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.stereotype.Service;
//
//import java.sql.Date;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.atomic.AtomicInteger;
//
//@Data
//@Slf4j
//@Service
//public class MagicPinApiService extends ThreePOService {
//    private final NamedParameterJdbcTemplate jdbcTemplate;
//    private final OrderRepository orderRepository;
//    private final MagicpinRepository magicpinRepository;
//    private final POSThreePoSummarySheetUtil sheetUtil;
//
//
//    @TrackExecutionTime
//    public ThreePOData getDashboardDataResponse(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
//        ThreePOData response;
//        try {
//            CompletableFuture<Double> posSalesFuture = CompletableFuture.supplyAsync(() -> getPOSSales(startDate, endDate, storeCodes));
//            CompletableFuture<ThreePOData> threePOsDataFuture = CompletableFuture.supplyAsync(() -> getThreePOData(startDate, endDate, storeCodes));
//            CompletableFuture<Double> getPOsTotalNotFoundInMagicpinFuture = CompletableFuture.supplyAsync(() -> getPOsTotalNotFoundInMagicpin(startDate, endDate, storeCodes));
//
//
//            CompletableFuture<Void> allFutures = CompletableFuture.allOf(posSalesFuture, threePOsDataFuture);
//            allFutures.get();
//
//            Double posSales = posSalesFuture.get();
//            ThreePOData threePOsData = threePOsDataFuture.get();
//            double possTotalNotFoundInZomato = getPOsTotalNotFoundInMagicpinFuture.get();
//
//
//            response = getMergedResponse(posSales, possTotalNotFoundInZomato, threePOsData);
//        } catch (Exception e) {
//            throw new ApiException("Error while fetching data");
//        }
//
//        return response;
//    }
//
//    private double getPOsTotalNotFoundInMagicpin(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
//        return orderRepository.getTotalSalesNotFoundInMagicpin(startDate, endDate, storeCodes);
//
//    }
//
//    private ThreePOData getMergedResponse(Double posSales,
//                                          double possTotalNotFoundInZomato,
//                                          ThreePOData threePOData) {
//        threePOData.setPosSales(posSales);
//        threePOData.setPosVsThreePO(threePOData.getPosVsThreePO() + possTotalNotFoundInZomato);
//        return threePOData;
//    }
//
//
//    private Double getPOSSales(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
//        return orderRepository.getTotalAmountByDateAndThreePOSource(startDate.toLocalDate(), endDate.toLocalDate(), "magicpin", storeCodes);
//    }
//
//
//
//    private ThreePOData getThreePOData(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
//        String sql = ThreePoQueries.Magicpin_threePOQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("storeCodes", storeCodes);
//        parameters.addValue("startDate", startDate.toLocalDate().format(Formatter.YYYYMMDD_DASH));
//        parameters.addValue("endDate", endDate.toLocalDate().format(Formatter.YYYYMMDD_DASH));
//        ThreePOData threePOData = new ThreePOData();
//        threePOData.setTenderName(ThreePO.MAGICPIN);
//
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//
//            // TODO: PROMO SHEET
//            double discount = 0;
//
//            double threePOSales = resultSet.getDouble("threePOSales");
//            double threePOReceivables = resultSet.getDouble("threePOReceivables");
//            double threePOCommission = resultSet.getDouble("threePOCommission");
//            double threePOCharges = resultSet.getDouble("threePOCharges");
//            double salt = resultSet.getDouble("freebies");
//
//
//            threePOData.setThreePOSales(threePOSales);
//            threePOData.setThreePOReceivables(threePOReceivables);
//            threePOData.setThreePOCommission(threePOCommission);
//            threePOData.setThreePOCharges(threePOCharges);
//            threePOData.setThreePODiscounts(discount);
//            threePOData.setThreePOFreebies(salt);
//
//
//            // Ideal scenarios
//            double posReceivables = resultSet.getDouble("posReceivables");
//            double posCommission = resultSet.getDouble("posCommission");
//            double posCharges = resultSet.getDouble("posCharges");
//
//            threePOData.setPosReceivables(posReceivables);
//            threePOData.setPosCommission(posCommission);
//            threePOData.setPosCharges(posCharges);
//            threePOData.setPosDiscounts(discount);
//            threePOData.setPosFreebies(salt);
//
//            double reconciled = resultSet.getDouble("reconciled");
//            double unreconciled = resultSet.getDouble("unreconciled");
//            double receivablesVsReceipts = resultSet.getDouble("receivablesVsReceipts");
//
//            threePOData.setReconciled(reconciled);
//            threePOData.setPosVsThreePO(unreconciled);
//            threePOData.setReceivablesVsReceipts(receivablesVsReceipts);
//
//
//            return null;
//        });
//
//        return threePOData;
//    }
//
//
//
//
//
//
//    public GeneratedReportResponse getPOSvs3POResponse(LocalDateTime startDate, LocalDateTime endDate, List<String> storeCodes) {
//        GeneratedReportResponse response = new GeneratedReportResponse();
//        double posSales = getPOSSales(startDate, endDate, storeCodes);
//        response.setSales(posSales);
//
//        ThreePOData threePO = getThreePOData(startDate, endDate, storeCodes);
////        response.setReceipts(threePO.getReceipts());
//        response.setCharges(threePO.getThreePOCharges());
//        response.setDifference(response.getSales() - threePO.getThreePOSales());
//        response.setReconciled(orderRepository.getTotalSalesReconciledInMagicPin(startDate, endDate, storeCodes));
//
//        return response;
//
//    }
//
//
//
//
//
//    @TrackExecutionTime
//    public void posVsThreePoDownload(ThreePODataRequest request, Workbook workbook, boolean allThreePoDownload) {
//        Sheet sheet3 = workbook.createSheet("Magicpin Summary");
//        Sheet sheet1 = workbook.createSheet("Magicpin POS vs 3PO");
//        Sheet sheet2 = workbook.createSheet("Magicpin 3PO vs POS");
//        Sheet sheet4 = workbook.createSheet("S2");
//        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        List<OrderEntity> orderEntityList = orderRepository.getAllOrdersNotFoundInMagicpin(startDate, endDate, request.getStores());
//        List<OrderEntity> subsequentDateOrders = orderRepository.getSubsequentDateOrders(endDate.toLocalDate(), Date.valueOf(endDate.toLocalDate().plusDays(1)), "MAGICPIN", request.getStores());
//        List<ThreePOReport> threePOReportPOS = getThreePOVSPOSOrders("MAGICPIN", startDate, endDate, request.getStores(), jdbcTemplate);
//        downloadDeltaSheet(threePOReportPOS, orderEntityList, sheet1, sheet2, workbook, allThreePoDownload, ThreePO.MAGICPIN);
//        posDownload(subsequentDateOrders, sheet4);
//        sheetUtil.createSummarySheet(workbook, sheet3, sheet1, sheet2, ThreePO.MAGICPIN, sheet4, startDate, endDate);
//        List<Magicpin> unknownColumnsEntries = magicpinRepository.getUnknownColumnIfPresent(startDate.toLocalDate(), endDate.toLocalDate(), request.getStores());
//        exceptionalReporting(workbook, unknownColumnsEntries);
//
//    }
//
//    public void exceptionalReporting(Workbook workbook, List<Magicpin> unknownColumnsEntries) {
//        Sheet sheet = workbook.createSheet("Exceptional Column Reporting");
//        String[] headers = {"Order ID", "Date", "Order status", "MFP"};
//        CellStyle boldStyle = workbook.createCellStyle();
//        Font boldFont = workbook.createFont();
//        boldFont.setBold(true);
//        boldStyle.setFont(boldFont);
//        Row headerRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        for (Magicpin magicpin : unknownColumnsEntries) {
//            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//            row.createCell(0).setCellValue(magicpin.getOrderId());
//            row.createCell(1).setCellValue(formatLocalDate(magicpin.getDate()));
//            row.createCell(2).setCellValue(magicpin.getOrderStatus());
//            row.createCell(3).setCellValue(magicpin.getMfp());
//        }
//    }
//
//    public void reconciledDownload(ThreePODataRequest request, Workbook workbook) {
//        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        Sheet sheet1 = workbook.createSheet("Magicpin Reconciled");
//        List<Magicpin> magicpins = magicpinRepository.findAllReconciledByDateAndStoreCodes(startDate.toLocalDate(), endDate.toLocalDate(), request.getStores());
//        completeThreePODownload(magicpins, sheet1);
//
//
//    }
//
//    public void chargesDownload(ThreePODataRequest request, Workbook workbook) {
//        Sheet sheet = workbook.createSheet("Magicpin");
//        LocalDate startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
//        LocalDate endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
//        List<Magicpin> magicpins = magicpinRepository.findAllByDateAndStoreCodes(startDate, endDate, request.getStores());
//        threePOChargesDownload(magicpins, sheet, workbook);
//    }
//
//    public void threePOChargesDownload(List<Magicpin> magicpins, Sheet sheet, Workbook workbook) {
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Date", "Order ID", "City", "Locality", "PID", "MID", "Merchant Name",
//                "Rejection ID", "Rejection Reason", "Order Status", "Merchant GMV", "Item Amount",
//                "Packaging Charge", "GST", "MFP", "Commission", "GST on Commission", "Debited Amount",
//                "TDS", "Net Payable", "Store Code", "Total Charges"};
//        CellStyle boldStyle = workbook.createCellStyle();
//        Font boldFont = workbook.createFont();
//        boldFont.setBold(true);
//        boldStyle.setFont(boldFont);
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//            if (headers[i].equalsIgnoreCase("Commission")
//                    || headers[i].equalsIgnoreCase("GST on Commission")
//                    || headers[i].equalsIgnoreCase("TDS") || headers[i].equalsIgnoreCase("Total Charges")) {
//                headerRow.getCell(i).setCellStyle(boldStyle);
//            }
//        }
//
//        for (Magicpin value : magicpins) {
//            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//
//            row.createCell(0).setCellValue(formatLocalDate(value.getDate()));
//            row.createCell(1).setCellValue(value.getOrderId());
//            row.createCell(2).setCellValue(value.getCity());
//            row.createCell(3).setCellValue(value.getLocality());
//            row.createCell(4).setCellValue(value.getPid());
//            row.createCell(5).setCellValue(value.getMid());
//            row.createCell(6).setCellValue(value.getMerchantName());
//            row.createCell(7).setCellValue(value.getRejectionId());
//            row.createCell(8).setCellValue(value.getRejectionReason());
//            row.createCell(9).setCellValue(value.getOrderStatus());
//            row.createCell(10).setCellValue(value.getMerchantGmv());
//            row.createCell(11).setCellValue(value.getItemAmount());
//            row.createCell(12).setCellValue(value.getPackagingCharge());
//            row.createCell(13).setCellValue(value.getGst());
//            row.createCell(14).setCellValue(value.getMfp());
//            row.createCell(15).setCellValue(value.getCommission());
//            row.getCell(15).setCellStyle(boldStyle);
//
//            row.createCell(16).setCellValue(value.getGstOnCommission());
//            row.getCell(16).setCellStyle(boldStyle);
//
//            row.createCell(17).setCellValue(value.getDebitedAmount());
//            row.createCell(18).setCellValue(value.getTds());
//            row.getCell(18).setCellStyle(boldStyle);
//
//            row.createCell(19).setCellValue(value.getNetPayable());
//            row.createCell(20).setCellValue(value.getMcdStoreCode());
//            row.createCell(21).setCellValue(value.getCommission() + value.getGstOnCommission() + value.getTds());
//            headerRow.getCell(21).setCellStyle(boldStyle);
//        }
//    }
//
//    public void discountsDownload(ThreePODataRequest request, Workbook workbook) {
//        Sheet sheet = workbook.createSheet("Magicpin");
//        LocalDate startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
//        LocalDate endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
//        List<Magicpin> magicpins = magicpinRepository.findAllByDateAndStoreCodes(startDate, endDate, request.getStores());
//
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Date", "Order ID", "City", "Locality", "PID", "MID", "Merchant Name",
//                "Rejection ID", "Rejection Reason", "Order Status", "Merchant GMV", "Item Amount",
//                "Packaging Charge", "GST", "MFP", "Commission", "GST on Commission", "Debited Amount",
//                "TDS", "Net Payable", "Store Code"};
//
//        CellStyle boldStyle = workbook.createCellStyle();
//        Font boldFont = workbook.createFont();
//        boldFont.setBold(true);
//        boldStyle.setFont(boldFont);
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//            if (headers[i].equalsIgnoreCase("MFP")) {
//                headerRow.getCell(i).setCellStyle(boldStyle);
//            }
//        }
//
//        for (Magicpin value : magicpins) {
//            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//
//            row.createCell(0).setCellValue(formatLocalDate(value.getDate()));
//            row.createCell(1).setCellValue(value.getOrderId());
//            row.createCell(2).setCellValue(value.getCity());
//            row.createCell(3).setCellValue(value.getLocality());
//            row.createCell(4).setCellValue(value.getPid());
//            row.createCell(5).setCellValue(value.getMid());
//            row.createCell(6).setCellValue(value.getMerchantName());
//            row.createCell(7).setCellValue(value.getRejectionId());
//            row.createCell(8).setCellValue(value.getRejectionReason());
//            row.createCell(9).setCellValue(value.getOrderStatus());
//            row.createCell(10).setCellValue(value.getMerchantGmv());
//            row.createCell(11).setCellValue(value.getItemAmount());
//            row.createCell(12).setCellValue(value.getPackagingCharge());
//            row.createCell(13).setCellValue(value.getGst());
//            row.createCell(14).setCellValue(value.getMfp());
//            row.getCell(14).setCellStyle(boldStyle);
//
//            row.createCell(15).setCellValue(value.getCommission());
//            row.createCell(16).setCellValue(value.getGstOnCommission());
//            row.createCell(17).setCellValue(value.getDebitedAmount());
//            row.createCell(18).setCellValue(value.getTds());
//            row.createCell(19).setCellValue(value.getNetPayable());
//            row.createCell(20).setCellValue(value.getMcdStoreCode());
//        }
//    }
//
//    public void posSalesDownload(ThreePODataRequest request, Workbook workbook) {
//        Sheet sheet = workbook.createSheet("Magicpin");
//        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        List<OrderEntity> orderEntityList = orderRepository.getAllOrdersByDateAndThreePoAndStores(startDate.toLocalDate(), endDate.toLocalDate(), "MAGICPIN", request.getStores());
//        posDownload(orderEntityList, sheet);
//    }
//
//    public void completeThreePODownload(List<Magicpin> magicpins, Sheet sheet) {
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Date", "Order ID", "City", "Locality", "PID", "MID", "Merchant Name",
//                "Rejection ID", "Rejection Reason", "Order Status", "Merchant GMV", "Item Amount",
//                "Packaging Charge", "GST", "MFP", "Commission", "GST on Commission", "Debited Amount",
//                "TDS", "Net Payable", "Store Code"};
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//
//        for (Magicpin value : magicpins) {
//            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//
//            row.createCell(0).setCellValue(formatLocalDate(value.getDate()));
//            row.createCell(1).setCellValue(value.getOrderId());
//            row.createCell(2).setCellValue(value.getCity());
//            row.createCell(3).setCellValue(value.getLocality());
//            row.createCell(4).setCellValue(value.getPid());
//            row.createCell(5).setCellValue(value.getMid());
//            row.createCell(6).setCellValue(value.getMerchantName());
//            row.createCell(7).setCellValue(value.getRejectionId());
//            row.createCell(8).setCellValue(value.getRejectionReason());
//            row.createCell(9).setCellValue(value.getOrderStatus());
//            row.createCell(10).setCellValue(value.getMerchantGmv());
//            row.createCell(11).setCellValue(value.getItemAmount());
//            row.createCell(12).setCellValue(value.getPackagingCharge());
//            row.createCell(13).setCellValue(value.getGst());
//            row.createCell(14).setCellValue(value.getMfp());
//            row.createCell(15).setCellValue(value.getCommission());
//            row.createCell(16).setCellValue(value.getGstOnCommission());
//            row.createCell(17).setCellValue(value.getDebitedAmount());
//            row.createCell(18).setCellValue(value.getTds());
//            row.createCell(19).setCellValue(value.getNetPayable());
//            row.createCell(20).setCellValue(value.getMcdStoreCode());
//
//        }
//    }
//
//    @TrackExecutionTime
//    public void threePOSalesDownload(ThreePODataRequest request, Workbook workbook) {
//        Sheet sheet = workbook.createSheet("Magicpin");
//        LocalDate startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
//        LocalDate endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
//        List<Magicpin> magicpins = magicpinRepository.findAllByDateAndStoreCodes(startDate, endDate, request.getStores());
//        completeThreePODownload(magicpins, sheet);
//    }
//
//
//    public void getPOSvs3PODownload(ThreePODataRequest request, Workbook workbook) {
//        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
//        switch (request.getReportType()) {
//            case POSSales -> {
//                Sheet sheet = workbook.createSheet("POSSales");
//                List<OrderEntity> orderEntityList = orderRepository.getAllOrdersByDateAndThreePoAndStores(startDate.toLocalDate(), endDate.toLocalDate(), "MAGICPIN", request.getStores());
//                posDownload(orderEntityList, sheet);
//            }
//            case Receipts -> {
//                Sheet sheet = workbook.createSheet("Receipts");
//                List<Magicpin> magicpins = magicpinRepository.findAllByDateAndStoreCodes(startDate.toLocalDate(), endDate.toLocalDate(), request.getStores());
//                completeThreePODownload(magicpins, sheet);
//            }
//            case Charges -> {
//                Sheet sheet = workbook.createSheet("Charges");
//                List<Magicpin> magicpins = magicpinRepository.findAllByDateAndStoreCodes(startDate.toLocalDate(), endDate.toLocalDate(), request.getStores());
//                threePOChargesDownload(magicpins, sheet, workbook);
//            }
//            case Reconciled -> {
//                Sheet sheet = workbook.createSheet("POS 3PO Magicpin Reconciled Orders");
//                List<Magicpin> magicpins = magicpinRepository.findAllOrdersByDateAndStoreCodesFoundInPOS(startDate.toLocalDate(), endDate.toLocalDate(), request.getStores());
//                completeThreePODownload(magicpins, sheet);
//
//            }
//            case Difference -> {
//                Sheet sheet1 = workbook.createSheet("Magicpin POS");
//                Sheet sheet2 = workbook.createSheet("Magicpin 3PO");
//                List<OrderEntity> orderEntityList = orderRepository.getAllOrdersByDateAndThreePoAndStores(startDate.toLocalDate(), endDate.toLocalDate(), "MAGICPIN", request.getStores());
//                posDownload(orderEntityList, sheet1);
//                List<Magicpin> magicpins = magicpinRepository.findAllByDateAndStoreCodes(startDate.toLocalDate(), endDate.toLocalDate(), request.getStores());
//                completeThreePODownload(magicpins, sheet2);
//            }
//        }
//    }
//
//    public void posReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_posRecievableQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin POS Receivables Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Amount", "Actual Packaging Charge", "Debited Amount", "Order Status", "POS Receivables"};
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("item_amount"));
//            row.createCell(9).setCellValue(resultSet.getDouble("actual_packaging_charge"));
//            row.createCell(10).setCellValue(resultSet.getDouble("debited_amount"));
//            row.createCell(11).setCellValue(resultSet.getString("order_status"));
//            row.createCell(12).setCellValue(resultSet.getDouble("posReceivables"));
//            return null;
//        });
//
//    }
//
//    public void allPOSChargesDownload(ThreePODataRequest request, Workbook workbook) {
//    }
//
//    public void posChargesDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_posChargesQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin POS Charges Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Amount", "Actual Packaging Charge", "Debited Amount", "Order Status", "POS Charges"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("item_amount"));
//            row.createCell(9).setCellValue(resultSet.getDouble("actual_packaging_charge"));
//            row.createCell(10).setCellValue(resultSet.getDouble("debited_amount"));
//            row.createCell(11).setCellValue(resultSet.getString("order_status"));
//            row.createCell(12).setCellValue(resultSet.getDouble("posCharges"));
//            return null;
//        });
//    }
//
//    public void posDiscountsDownload(ThreePODataRequest request, Workbook workbook) {
////        String sql = ThreePoQueries.Magicpin_discountsQuery;
////        MapSqlParameterSource parameters = new MapSqlParameterSource();
////        parameters.addValue("startDate", request.getStartDate());
////        parameters.addValue("endDate", request.getEndDate());
////        parameters.addValue("storeCodes", request.getStores());
////        Sheet sheet = workbook.createSheet("MagicpinPosDiscountsReport");
////        Row headerRow = sheet.createRow(0);
////        String[] headers = {"Business Date", "Order Id","Item Amount","POS Discounts"};
////
////        for (int i = 0; i < headers.length; i++) {
////            headerRow.createCell(i).setCellValue(headers[i]);
////        }
////        AtomicInteger rowNo = new AtomicInteger(1);
////        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
////            Row row = sheet.createRow(rowNo.getAndIncrement());
////            row.createCell(0).setCellValue(resultSet.getDate("date"));
////            row.createCell(1).setCellValue(resultSet.getString("order_id"));
////            row.createCell(2).setCellValue(resultSet.getString("item_amount"));
////            row.createCell(3).setCellValue(resultSet.getDouble("discounts"));
////            return null;
////        });
//    }
//
//    public void posFreebiesDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_freebiesQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("MagicpinPosCommissionReport");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Amount", "Actual Packaging Charge", "Debited Amount", "Order Status", "POS Freebies"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("item_amount"));
//            row.createCell(9).setCellValue(resultSet.getDouble("actual_packaging_charge"));
//            row.createCell(10).setCellValue(resultSet.getDouble("debited_amount"));
//            row.createCell(11).setCellValue(resultSet.getString("order_status"));
//            row.createCell(12).setCellValue(resultSet.getDouble("freebies"));
//            return null;
//        });
//    }
//
//    public void posCommissionDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_posCommissionQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin POS Commission Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Item Amount", "Actual Packaging Charge", "Debited Amount", "Order Status", "POS Commission"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("item_amount"));
//            row.createCell(9).setCellValue(resultSet.getDouble("actual_packaging_charge"));
//            row.createCell(10).setCellValue(resultSet.getDouble("debited_amount"));
//            row.createCell(11).setCellValue(resultSet.getString("order_status"));
//            row.createCell(12).setCellValue(resultSet.getDouble("posCommission"));
//            return null;
//        });
//    }
//
//    public void threePOReceivablesDownload(ThreePODataRequest request, Workbook workbook) {
//
//        String sql = ThreePoQueries.Magicpin_threePOReceivablesQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin ThreePO Receivables Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Net Payable", "ThreePO Receivables"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("net_payable"));
//            row.createCell(9).setCellValue(resultSet.getDouble("threePOReceivables"));
//            return null;
//        });
//    }
//
//    public void threePOCommissionDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_threePOCommissionQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin ThreePO Commission Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "Commission", "ThreePO Commission"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("commission"));
//            row.createCell(9).setCellValue(resultSet.getDouble("threePOCommission"));
//            return null;
//        });
//    }
//
//    public void threePOFreebieDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_threePOFreebieQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin ThreePO Freebie Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "ThreePO freebies"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("freebies"));
//            return null;
//        });
//    }
//
//    public void receivablesVsReceiptsDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_receivablesVsReceiptsQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin ReceivablesVsReceipts Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code","Payout Date","Reference Number", "Item Amount", "Actual Packaging Charge", "Debited Amount", "Order Status","Three PO Receivable","POS Receivable" ,"Payout Amount", "POS ReceivablesVsReceipts","ThreePO ReceivablesVsReceipts"};
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("order_id"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDate("date"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("store_code"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDate("payout_date"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("reference_number"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("item_amount"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("actual_packaging_charge"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("debited_amount"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getString("order_status"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("threePOReceivable"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("posReceivable"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("payout_amount"));
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(resultSet.getDouble("receivablesVsReceipts"));
//            double ThreePOReceivablesVsReceipts=resultSet.getDouble("threePOReceivable")-resultSet.getDouble("payout_amount");
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(ThreePOReceivablesVsReceipts);
//
//            return null;
//        });
//    }
//
//    public void threePOChargesDownload(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_threePOChargesQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin ThreePO Charges Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code", "TDS", "GST On Commission", "ThreePO Charges"};
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("tds"));
//            row.createCell(9).setCellValue(resultSet.getDouble("gst_on_commission"));
//            row.createCell(10).setCellValue(resultSet.getDouble("threePOCharges"));
//            return null;
//        });
//    }
//
//    public void allThreePOCharges(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_allThreePOChargesQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin AllThreePOCharges Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code","Commission", "TDS", "GST On Commission","ThreePO Commission","freebies","ThreePO Charges"};
//
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("commission"));
//            row.createCell(9).setCellValue(resultSet.getDouble("tds"));
//            row.createCell(10).setCellValue(resultSet.getDouble("gst_on_commission"));
//            row.createCell(11).setCellValue(resultSet.getDouble("threePOCommission"));
//            row.createCell(12).setCellValue(resultSet.getDouble("freebies"));
//            row.createCell(13).setCellValue(resultSet.getDouble("threePOCharges"));
//            return null;
//        });
//    }
//
//    public void allPOSCharges(ThreePODataRequest request, Workbook workbook) {
//        String sql = ThreePoQueries.Magicpin_allPOSChargeQuery;
//        MapSqlParameterSource parameters = new MapSqlParameterSource();
//        parameters.addValue("startDate", request.getStartDate());
//        parameters.addValue("endDate", request.getEndDate());
//        parameters.addValue("storeCodes", request.getStores());
//        Sheet sheet = workbook.createSheet("Magicpin allPOSCharges Report");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Order Id", "Order Date", "Invoice Number", "POS Net Amount", "Business Date", "Receipt Number", "POS Id", "Store Code","Item Amount", "Actual Packaging Charge", "Debited Amount","freebies","POS Commission","POS Charges"};
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//        AtomicInteger rowNo = new AtomicInteger(1);
//        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
//            Row row = sheet.createRow(rowNo.getAndIncrement());
//            row.createCell(0).setCellValue(resultSet.getString("order_id"));
//            row.createCell(1).setCellValue(resultSet.getDate("date"));
//            row.createCell(2).setCellValue(resultSet.getString("invoice_number"));
//            row.createCell(3).setCellValue(resultSet.getDouble("pos_total_amount-pos_total_tax"));
//            row.createCell(4).setCellValue(resultSet.getDate("business_date"));
//            row.createCell(5).setCellValue(resultSet.getString("receipt_number"));
//            row.createCell(6).setCellValue(resultSet.getString("pos_id"));
//            row.createCell(7).setCellValue(resultSet.getString("store_code"));
//            row.createCell(8).setCellValue(resultSet.getDouble("item_amount"));
//            row.createCell(9).setCellValue(resultSet.getDouble("actual_packaging_charge"));
//            row.createCell(10).setCellValue(resultSet.getDouble("debited_amount"));
//            row.createCell(11).setCellValue(resultSet.getDouble("freebies"));
//            row.createCell(12).setCellValue(resultSet.getDouble("posCommission"));
//            row.createCell(13).setCellValue(resultSet.getDouble("posCharges"));
//            return null;
//        });
//    }
//
//    @Override
//    public MissingThreePOMapping getThreePoMissingMapping() {
//        MissingThreePOMapping mapping = new MissingThreePOMapping();
//        mapping.setMissing(magicpinRepository.getRestaurantIdCountWhereMcdStoreCodeIsNull());
//        mapping.setTotalStores(magicpinRepository.getRestaurantIdCount());
//        mapping.setThreePO(ThreePO.MAGICPIN);
//        return mapping;
//    }
//
//    @Override
//    public void downloadThreePoMissingMapping(Workbook workbook) {
//        List<String> getMissingRestaurantIds = magicpinRepository.getRestaurantIdWhereMcdStoreCodeIsNull();
//        Sheet sheet = workbook.createSheet("Magicpin Mappings");
//        Row headerRow = sheet.createRow(0);
//        String[] headers = {"Mid", "Store Code", "State Code", "Packaging Charge"};
//
//        for (int i = 0; i < headers.length; i++) {
//            headerRow.createCell(i).setCellValue(headers[i]);
//        }
//
//        for (String missingRestId : getMissingRestaurantIds) {
//            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//            row.createCell(0).setCellValue(missingRestId);
//        }
//
//    }
//}
