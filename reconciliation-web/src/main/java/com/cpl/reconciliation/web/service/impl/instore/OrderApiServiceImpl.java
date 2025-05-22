package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.reconciliation.core.constant.ReportHeaders;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.SaleData;
import com.cpl.reconciliation.core.response.SaleDataWrapper;
import com.cpl.reconciliation.core.response.TenderWiseData;
import com.cpl.reconciliation.domain.entity.DashBoardEntity;
import com.cpl.reconciliation.domain.entity.StoreEntity;
import com.cpl.reconciliation.domain.models.CashReco;
import com.cpl.reconciliation.domain.repository.OrderRepository;
import com.cpl.reconciliation.domain.repository.OrderTrmQuery;
import com.cpl.reconciliation.domain.repository.StoreRepository;
import com.cpl.reconciliation.web.service.util.POSTRMSummarySheetUtil;
import com.cpl.reconciliation.web.service.util.PosTrmData;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.cpl.reconciliation.domain.util.Constants.*;

@Data
@Slf4j
@Service
public class OrderApiServiceImpl extends AbstractInStoreService implements OrderApiService {

    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final POSTRMSummarySheetUtil postrmSummarySheetUtil;

    @Override
    public SaleDataWrapper getSaleData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n"
                + "   *\n"
                + "FROM\n"
                + "    orders o\n"
                + //                "          JOIN\n" +
                //                "    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n" +
                "WHERE\n"
                + "    order_date BETWEEN :startDate AND :endDate\n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += "AND o.store_id IN (:storeList)\n";
            parameters.addValue("storeList", request.getStores());
        }
        QUERY += "AND o.tender_name IS NOT NULL\n"
                + "AND o.invoice_number IS NOT NULL\n"
                + "AND o.order_status = 'Paid'\n";
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += "AND o.tender_name = :tender";
            parameters.addValue("tender", request.getTender());
        }
        log.info("POS Sale: {} , Parameters: {}", QUERY, parameters);
        SaleDataWrapper saleDataWrapper = new SaleDataWrapper();
        AtomicReference<Double> totalSaleAmt = new AtomicReference<>(0.0);
        AtomicReference<Double> totalTaxAmt = new AtomicReference<>(0.0);
        AtomicReference<Double> totalTenderAmt = new AtomicReference<>(0.0);
        List<SaleData> saleDataList = new LinkedList<>();
        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            SaleData saleData = new SaleData();
            saleData.setPaymentType(rs.getString("tender_name"));
            saleData.setBusinessDate(rs.getString("business_date"));
            saleData.setOrderDate(rs.getString("order_date"));
            saleData.setPosId(rs.getString("pos_id"));
            saleData.setStoreId(rs.getString("store_id"));
            saleData.setSaleType(rs.getString("sale_type"));
            saleData.setInvoiceNumber(rs.getString("invoice_number"));
            // saleData.setRrnNumber(rs.getString("rrn"));
            saleData.setSaleAmount(rs.getDouble("total_amount"));
            saleData.setSaleTax(rs.getDouble("total_tax"));
            saleData.setTenderAmount(rs.getDouble("total_amount"));
            totalSaleAmt.set(totalSaleAmt.get() + saleData.getSaleAmount());
            totalTaxAmt.set(totalTaxAmt.get() + saleData.getSaleTax());
            totalTenderAmt.set(totalTenderAmt.get() + saleData.getTenderAmount());
            saleDataList.add(saleData);
        });
        saleDataWrapper.setTotalSaleAmt(totalSaleAmt.get());
        saleDataWrapper.setTotalTaxAmt(totalTaxAmt.get());
        saleDataWrapper.setTotalTenderAmt(totalTenderAmt.get());
        saleDataWrapper.setSaleDataList(saleDataList);
        return saleDataWrapper;
    }

    @Override
    @SneakyThrows
    public void saleDownload(DashboardDataRequest request, OutputStream outputStream) {
        SaleDataWrapper saleDataWrapper = getSaleData(request);
        String sheetname = "POSSales";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            //Sheet sheet = workbook.createSheet("");
            Sheet currentSheet = allocateNewSheet(workbook, sheetname);
            Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
            totalRow.createCell(0).setCellValue("Total");
            totalRow.createCell(8).setCellValue(saleDataWrapper.getTotalSaleAmt());
            totalRow.createCell(9).setCellValue(saleDataWrapper.getTotalTaxAmt());
            totalRow.createCell(10).setCellValue(saleDataWrapper.getTotalTenderAmt());
            int sheetCounter = 1;//Start at 1 because we already created Initial sheet
            int rowCounter = 2;//1st row in sheet
            for (SaleData saleData : saleDataWrapper.getSaleDataList()) {
                if (rowCounter == 1048572) {//Max row reached, build new sheet
                    //Increase sheetCounter
                    sheetCounter++;
                    String new_sheetName = "POSSales" + "_" + sheetCounter;//Name of sheet
                    log.info("Creating new sheet: {} ", new_sheetName);
                    currentSheet = allocateNewSheet(workbook, new_sheetName);//Point currentSheet to new sheet
                    //Reset rowCounter to 0
                    rowCounter = 0;
                }

                Row row = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getPaymentType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getOrderDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getPosId());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getStoreId());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getSaleType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getInvoiceNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getRrnNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getSaleAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getSaleTax());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(saleData.getTenderAmount());
                rowCounter++;
            }
            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private static Sheet allocateNewSheet(SXSSFWorkbook workbook, String sheetName) {
        String[] headers = ReportHeaders.PosSale.POS_HEADER;
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        /*You can add style here too or write header here*/
        return sheet;
    }

    @Override
    public DashboardDataResponse getOrderVsTrmData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        DashboardDataResponse ddr = new DashboardDataResponse();
        String sql1 = OrderTrmQuery.sql1;
        if (Strings.isNotBlank(request.getTender())) {
            sql1 += "AND t.name LIKE '" + "%" + request.getTender() + "%'\n";
        }
        if (!CollectionUtils.isEmpty(request.getStores())) {
            sql1 += "AND o.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        sql1 += OrderTrmQuery.sql1GroupClause;
        List<TenderWiseData> tenderWiseDataList = jdbcTemplate.query(sql1, parameters, (rs, rowNum) -> {
            TenderWiseData tenderWiseData = new TenderWiseData();
            tenderWiseData.setTenderName(rs.getString("name"));
            //
            tenderWiseData.setSales(rs.getDouble("sales_amount"));
            ddr.setSales(ddr.getSales() + tenderWiseData.getSales());
            tenderWiseData.setSalesCount(rs.getLong("sales_count"));
            ddr.setSalesCount(ddr.getSalesCount() + tenderWiseData.getSalesCount());
            //
            tenderWiseData.setReconciled(rs.getDouble("match_amount"));
            ddr.setReconciled(ddr.getReconciled() + tenderWiseData.getReconciled());
            tenderWiseData.setReconciledCount(rs.getLong("match_count"));
            ddr.setReconciledCount(ddr.getReconciledCount() + tenderWiseData.getReconciledCount());
            //
            tenderWiseData.setDifference(rs.getDouble("amount_difference"));
            ddr.setDifference(ddr.getDifference() + tenderWiseData.getDifference());
            tenderWiseData.setDifferenceCount(rs.getLong("count_difference"));
            ddr.setDifferenceCount(ddr.getDifferenceCount() + tenderWiseData.getDifferenceCount());
            //
            return tenderWiseData;
        });
        String sql2 = OrderTrmQuery.sql2;
        if (Strings.isNotBlank(request.getTender())) {
            sql2 += "AND t.payment_type LIKE '" + "%" + request.getTender() + "%'\n";
        }
        sql2 += OrderTrmQuery.sql2GroupClause;
        ddr.setTenderWiseDataList(tenderWiseDataList);
        jdbcTemplate.query(sql2, parameters, (rs) -> {
            String tenderName = rs.getString("payment_type");
            for (TenderWiseData twd : tenderWiseDataList) {
                if (twd.getTenderName().equalsIgnoreCase(tenderName)) {
                    double receipts = rs.getDouble("receipts");
                    long receiptsCount = rs.getLong("receipts_count");
                    twd.setReceipts(receipts);
                    twd.setReceiptsCount(receiptsCount);
                    ddr.setReceipts(ddr.getReceipts() + receipts);
                    ddr.setReceiptsCount(ddr.getReceiptsCount() + receiptsCount);
                }
            }
        });
        return ddr;
    }

    @Override
    @SneakyThrows
    public void getOrderVsTrmDownload(DashboardDataRequest request, OutputStream outputStream) {
        String[] headers = ReportHeaders.PosSale.POSVSTRM_HEADERS;
        try (Workbook workbook = new SXSSFWorkbook(50)) {
            String sheet1Name = "POS vs TRM";
            String sheet2Name = "TRM vs POS";
            Sheet sheet = workbook.createSheet("Summary");
//            Sheet sheet1 = workbook.createSheet("POS vs TRM");
//            Sheet sheet2 = workbook.createSheet("TRM vs POS");
//            addPOSvsTRMSheet(workbook, headers, request, sheet1);
//            addTRMvsPOSSheet(workbook, headers, request, sheet2);
//            postrmSummarySheetUtil.createSummarySheet(workbook, sheet, sheet1, sheet2);
            addPOSvsTRMSheet(workbook, headers, request, sheet1Name);
            addTRMvsPOSSheet(workbook, headers, request, sheet2Name);

            postrmSummarySheetUtil.createSummarySheet(workbook, sheet, getAllSheetsBySheetName(workbook, sheet1Name), getAllSheetsBySheetName(workbook, sheet2Name));
            workbook.write(outputStream);
        }
    }

    @Override
    public void cashRecoDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<CashReco> recoList = getCashRecoDataList(request);
        Map<LocalDate, Double> cashCollectedByBusinessDate = recoList.stream()
                .collect(Collectors.groupingBy(
                        reco -> Optional.ofNullable(reco.getBusinessDate()).orElse(LocalDate.MIN), // Use default date if null
                        Collectors.summingDouble(reco -> Optional.ofNullable(reco.getCashPickUp()).orElse(0.0))
                ));
        List<CashReco> updatedCashRecolist = recoList.stream().map(x -> {
            CashReco cashReco = new CashReco();
            cashReco.setBusinessDate(x.getBusinessDate());
            cashReco.setStoreCode(x.getStoreCode());
            cashReco.setCharges(x.getCharges());
            cashReco.setSales(x.getSales());
            cashReco.setCashPickUp(x.getCashPickUp());
            cashReco.setDepositAmount(x.getDepositAmount());
            cashReco.setReconciled(x.getReconciled());
            cashReco.setUnReconciled(x.getUnReconciled());
            cashReco.setSalesVsPickup(x.getSalesVsPickup());
            List<StoreEntity> allStores = storeRepository.findAll();
            if (request.getStores().size() >= allStores.size()) {
                cashReco.setTotalCashPickUp(cashCollectedByBusinessDate.get(x.getBusinessDate()));
                cashReco.setPickupVsDeposit(cashReco.getTotalCashPickUp() - cashReco.getDepositAmount());
                if (cashReco.getPickupVsDeposit() != 0) {
                    if (cashReco.getPickupVsDeposit() < 0) {
                        cashReco.setReason("Excess Amount in Bank");
                    } else {
                        cashReco.setReason("Shortage Amount in Bank");
                    }
                    cashReco.setUnReconciled(cashReco.getSales());
                    cashReco.setReconciled(0.0);
                } else {
                    cashReco.setReconciled(cashReco.getSales());
                    cashReco.setUnReconciled(0.0);
                    cashReco.setReason("N/A");
                }
            }
            return cashReco;
        }).toList();


        String sheetname = "CashReco";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet currentSheet = allocateCashRecoSheet(workbook, sheetname);
//            Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
            int sheetCounter = 1;//Start at 1 because we already created Initial sheet
            int rowCounter = 1;//1st row in sheet
            for (CashReco cashReco : updatedCashRecolist) {
                if (rowCounter == 1048572) {//Max row reached, build new sheet
                    //Increase sheetCounter
                    sheetCounter++;
                    String new_sheetName = "CashReco" + "_" + sheetCounter;//Name of sheet
                    log.info("Creating new sheet: {} ", new_sheetName);
                    currentSheet = allocateCashRecoSheet(workbook, new_sheetName);//Point currentSheet to new sheet
                    //Reset rowCounter to 0
                    rowCounter = 0;
                }

                Row row = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getStoreCode() + "-" + cashReco.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSales());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getCharges());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getCashPickUp());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getBusinessDate().plusDays(1));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue("cms");
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getDepositAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getReconciled());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getUnReconciled());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getPickupVsDeposit());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getReason());
                rowCounter++;
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void salesVsPickUpDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<CashReco> recoList = getCashRecoDataList(request);
        List<CashReco> updatedCashRecolist = recoList.stream().map(x -> {
            CashReco cashReco = new CashReco();
            cashReco.setBusinessDate(x.getBusinessDate());
            cashReco.setStoreCode(x.getStoreCode());
            cashReco.setCharges(x.getCharges());
            cashReco.setSales(x.getSales());
            cashReco.setCashPickUp(x.getCashPickUp());
            cashReco.setDepositAmount(x.getDepositAmount());
            cashReco.setReconciled(x.getReconciled());
            cashReco.setUnReconciled(x.getUnReconciled());
            cashReco.setSalesVsPickup(x.getSalesVsPickup());
            if (cashReco.getSalesVsPickup() != 0) {
                if (cashReco.getSalesVsPickup() < 0) {
                    cashReco.setReason("Shortfall");
                } else {
                    cashReco.setReason("Excess");
                }
                cashReco.setUnReconciled(cashReco.getSales());
                cashReco.setReconciled(0.0);
            } else {
                cashReco.setReconciled(cashReco.getSales());
                cashReco.setUnReconciled(0.0);
                cashReco.setReason("N/A");
            }
            return cashReco;
        }).toList();


        String sheetname = "SalesVsPickup";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet currentSheet = allocateSalesVsPickupSheet(workbook, sheetname);
//            Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
            int sheetCounter = 1;//Start at 1 because we already created Initial sheet
            int rowCounter = 1;//1st row in sheet
            for (CashReco cashReco : updatedCashRecolist) {
                if (rowCounter == 1048572) {//Max row reached, build new sheet
                    //Increase sheetCounter
                    sheetCounter++;
                    String new_sheetName = "SalesVsPickup" + "_" + sheetCounter;//Name of sheet
                    log.info("Creating new sheet: {} ", new_sheetName);
                    currentSheet = allocateSalesVsPickupSheet(workbook, new_sheetName);//Point currentSheet to new sheet
                    //Reset rowCounter to 0
                    rowCounter = 0;
                }

                Row row = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getStoreCode() + "-" + cashReco.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSales());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getCharges());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getCashPickUp());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getBusinessDate().plusDays(1));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue("cms");
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getReconciled());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getUnReconciled());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getReason());
                rowCounter++;
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void pickupVsReceiptsDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<CashReco> recoList = getCashRecoDataList(request);
        Map<LocalDate, Double> cashCollectedByBusinessDate = recoList.stream()
                .collect(Collectors.groupingBy(
                        reco -> Optional.ofNullable(reco.getBusinessDate()).orElse(LocalDate.MIN), // Use default date if null
                        Collectors.summingDouble(reco -> Optional.ofNullable(reco.getCashPickUp()).orElse(0.0))
                ));

        List<CashReco> updatedCashRecolist = recoList.stream().map(x -> {
            CashReco cashReco = new CashReco();
            cashReco.setBusinessDate(x.getBusinessDate());
            cashReco.setStoreCode(x.getStoreCode());
            cashReco.setCharges(x.getCharges());
            cashReco.setSales(x.getSales());
            cashReco.setCashPickUp(x.getCashPickUp());
            cashReco.setDepositAmount(x.getDepositAmount());
            cashReco.setReconciled(x.getReconciled());
            cashReco.setUnReconciled(x.getUnReconciled());
            cashReco.setSalesVsPickup(x.getSalesVsPickup());
            List<StoreEntity> allStores = storeRepository.findAll();
            if (request.getStores().size() >= allStores.size()) {
                cashReco.setTotalCashPickUp(cashCollectedByBusinessDate.get(x.getBusinessDate()));
                cashReco.setPickupVsDeposit(cashReco.getTotalCashPickUp() - cashReco.getDepositAmount());
                if (cashReco.getPickupVsDeposit() != 0) {
                    if (cashReco.getPickupVsDeposit() < 0) {
                        cashReco.setReason("Excess Amount in Bank");
                    } else {
                        cashReco.setReason("Shortage Amount in Bank");
                    }
                    cashReco.setUnReconciled(cashReco.getSales());
                    cashReco.setReconciled(0.0);
                } else {
                    cashReco.setReconciled(cashReco.getSales());
                    cashReco.setUnReconciled(0.0);
                    cashReco.setReason("N/A");
                }
            }
            return cashReco;
        }).toList();


        String sheetname = "PickupVsReceipts";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet currentSheet = allocatePickupVsReceiptsSheet(workbook, sheetname);
//            Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
            int sheetCounter = 1;//Start at 1 because we already created Initial sheet
            int rowCounter = 1;//1st row in sheet
            for (CashReco cashReco : updatedCashRecolist) {
                if (rowCounter == 1048572) {//Max row reached, build new sheet
                    //Increase sheetCounter
                    sheetCounter++;
                    String new_sheetName = "PickupVsReceipts" + "_" + sheetCounter;//Name of sheet
                    log.info("Creating new sheet: {} ", new_sheetName);
                    currentSheet = allocatePickupVsReceiptsSheet(workbook, new_sheetName);//Point currentSheet to new sheet
                    //Reset rowCounter to 0
                    rowCounter = 0;
                }

                Row row = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getStoreCode() + "-" + cashReco.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSales());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getCharges());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getCashPickUp());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getBusinessDate().plusDays(1));
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue("cms");
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getSalesVsPickup());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getReconciled());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getUnReconciled());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getTotalCashPickUp());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getDepositAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getPickupVsDeposit());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cashReco.getReason());
                rowCounter++;
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    private Sheet allocatePickupVsReceiptsSheet(SXSSFWorkbook workbook, String sheetName) {

        String[] headers = ReportHeaders.PosSale.PickupVsReceipts_HEADER;
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        /*You can add style here too or write header here*/
        return sheet;
    }

    private Sheet allocateCashRecoSheet(SXSSFWorkbook workbook, String sheetName) {
        String[] headers = ReportHeaders.PosSale.CASH_RECO_HEADER;
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        /*You can add style here too or write header here*/
        return sheet;
    }

    private Sheet allocateSalesVsPickupSheet(SXSSFWorkbook workbook, String sheetName) {
        String[] headers = ReportHeaders.PosSale.SalesVsPickup_HEADER;
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        /*You can add style here too or write header here*/
        return sheet;
    }

    private List<CashReco> getCashRecoDataList(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        parameters.addValue("storeList", request.getStores());

        String QUERY = "WITH pos_sale_cte AS (\n" +
                "    SELECT \n" +
                "        SUM(o.gross_amount) AS t_amount,\n" +
                "        o.business_date,\n" +
                "        o.store_id\n" +
                "    FROM \n" +
                "        orders o \n" +
                "    WHERE \n" +
                "        o.tender_name = 'CASH' \n" +
                "        AND o.business_date between :startDate and :endDate \n" +
                "    GROUP BY \n" +
                "        o.business_date, o.store_id\n" +
                "),\n" +
                "cash_pickup_cte AS (\n" +
                "    SELECT \n" +
                "        SUM(c.pickup_amount) AS p_amount,\n" +
                "        c.pick_up_date,\n" +
                "        c.store_id\n" +
                "    FROM \n" +
                "        cms_data c \n" +
                "    WHERE \n" +
                "        c.pick_up_date between :startDate and :endDate \n" +
                "    GROUP BY \n" +
                "        c.pick_up_date, c.store_id\n" +
                "),\n" +
                "bank_statements_cte AS (\n" +
                "    SELECT \n" +
                "        SUM(b.deposit_amt) AS deposited_amount,\n" +
                "        b.value_date\n" +
                "    FROM \n" +
                "        bank_statements b \n" +
                "    WHERE \n" +
                "        b.narration LIKE '%CMS INFO SYSTEMS%'\n" +
                "        AND b.value_date between :startDate and :endDate \n" +
                "    GROUP BY \n" +
                "        b.value_date\n" +
                ")\n" +
                "\n" +
                "SELECT \n" +
                "    ps.business_date,\n" +
                "    ps.store_id,\n" +
                "    ps.t_amount AS total_sales,\n" +
                "    cp.p_amount AS total_cash_picked_up,\n" +
                "    CASE \n" +
                "        WHEN ps.t_amount = cp.p_amount THEN COALESCE(ps.t_amount,0)\n" +
                "        ELSE NULL\n" +
                "    END AS reconciled,\n" +
                "    CASE \n" +
                "        WHEN COALESCE(ps.t_amount, 0) <> COALESCE(cp.p_amount, 0) THEN COALESCE(ps.t_amount, 0) \n" +
                "        ELSE NULL\n" +
                "    END AS unreconciled,\n" +
                "    ps.t_amount - COALESCE(cp.p_amount, 0) AS sales_vs_pickup,\n" +
                "    bs.deposited_amount AS deposited_amount,\n" +
                "    cp.p_amount - bs.deposited_amount AS pickup_vs_deposit_amt \n" +
                "FROM \n" +
                "    pos_sale_cte ps\n" +
                "LEFT JOIN \n" +
                "    cash_pickup_cte cp \n" +
                "    ON LEFT(ps.store_id, 5) = cp.store_id \n" +
                "    AND cp.pick_up_date = DATE_ADD(ps.business_date, INTERVAL 1 DAY) \n" +
                "LEFT JOIN \n" +
                "    bank_statements_cte bs\n" +
                "    ON bs.value_date = DATE_ADD(ps.business_date, INTERVAL 2 DAY) where ps.store_id in (:storeList) \n" +
                "ORDER BY \n" +
                "    ps.business_date, ps.store_id;";
        List<CashReco> cashRecoList = new ArrayList<>();
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = "CASH";
            DashBoardEntity dashBoardEntity = new DashBoardEntity();
            CashReco cashReco = new CashReco();
            cashReco.setStoreCode(resultSet.getString("store_id"));
            cashReco.setBusinessDate(LocalDate.parse(resultSet.getString("business_date")));
            cashReco.setTenderName(paymentType);
            cashReco.setSales(resultSet.getDouble("total_sales"));
            cashReco.setDepositAmount(resultSet.getDouble("deposited_amount"));
            cashReco.setCashPickUp(resultSet.getDouble("total_cash_picked_up"));
            cashReco.setCharges(0.0);
            cashReco.setReconciled(resultSet.getDouble("reconciled"));
            cashReco.setUnReconciled(resultSet.getDouble("unreconciled"));
            cashReco.setSalesVsPickup(resultSet.getDouble("sales_vs_pickup"));
            cashReco.setPickupVsDeposit(resultSet.getDouble("pickup_vs_deposit_amt"));
            cashRecoList.add(cashReco);
            return null;
        });
        return cashRecoList;
    }

    private List<Sheet> getAllSheetsBySheetName(Workbook workbook, String sheetName) {
        List<Sheet> sheets = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            if (workbook.getSheetName(i).contains(sheetName)) {
                sheets.add(workbook.getSheetAt(i));
            }
        }
        return sheets;
    }

    private void getSheet(Workbook workbook, String[] headers, DashboardDataRequest request, Sheet sheet, String sql, MapSqlParameterSource parameters, boolean reasonWiseSheet) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        List<PosTrmData> transactions = jdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            PosTrmData transaction = new PosTrmData();

            // Set properties of the Transaction object based on the result set
            transaction.setBusinessDate(rs.getString("businessDate"));
            transaction.setOrderDate(rs.getString("orderDate"));
            transaction.setInvoiceNumber(rs.getString("invoiceNumber"));
            transaction.setStoreID(rs.getString("storeID"));
            transaction.setPosID(rs.getString("posID"));
            transaction.setTenderName(rs.getString("tenderName"));
            transaction.setTotalAmount(rs.getDouble("totalAmount"));
            transaction.setSaleType(rs.getString("saleType"));
            transaction.setTenderAmount(rs.getDouble("tenderAmount"));
            transaction.setTenderRRN(rs.getString("tenderRRN"));
            transaction.setTransactionID(rs.getString("transactionID"));
            transaction.setAcquirerBank(rs.getString("acquirerBank"));
            transaction.setAuthCode(rs.getString("authCode"));
            transaction.setCardNumber(rs.getString("cardNumber"));
            transaction.setCardType(rs.getString("cardType"));
            transaction.setNetworkType(rs.getString("networkType"));
            transaction.setCustomerVPA(rs.getString("customerVPA"));
            transaction.setMID(rs.getString("mid"));
            transaction.setPaymentType(rs.getString("paymentType"));
            transaction.setTrmRRN(rs.getString("trmRRN"));
            transaction.setSource(rs.getString("source"));
            transaction.setTID(rs.getString("tID"));
            transaction.setTransactionStatus(rs.getString("transactionStatus"));
            transaction.setSettlementDate(rs.getString("settlementDate"));
            transaction.setTrmAmount(rs.getDouble("trmAmount"));
            transaction.setAmountDifference(rs.getDouble("remainingAmount"));

            return transaction;
        });
        createSheet(sheet, headerStyle, headers, transactions);
        if (reasonWiseSheet) {
            Map<String, List<PosTrmData>> map = transactions.stream().filter(k -> !ObjectUtils.isEmpty(k.getRemarks())).collect(Collectors.groupingBy(PosTrmData::getRemarks));
            for (Map.Entry<String, List<PosTrmData>> mapEntry : map.entrySet()) {
                Sheet sheetReason = workbook.createSheet(mapEntry.getKey());
                createSheet(sheetReason, headerStyle, headers, mapEntry.getValue());
            }
        } else {
            List<PosTrmData> notFoundInPos = transactions.stream().filter(k -> TXN_NOT_FOUND_IN_POS.equalsIgnoreCase(k.getRemarks())).collect(Collectors.toList());
            Sheet sheetReason = workbook.createSheet(TXN_NOT_FOUND_IN_POS);
            createSheet(sheetReason, headerStyle, headers, notFoundInPos);
        }

    }

    private void getSheet(Workbook workbook, String[] headers, DashboardDataRequest request, String sql, MapSqlParameterSource parameters, boolean reasonWiseSheet, String sheetName) {
        List<PosTrmData> transactions = jdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            PosTrmData transaction = new PosTrmData();

            // Set properties of the Transaction object based on the result set
            transaction.setBusinessDate(rs.getString("businessDate"));
            transaction.setOrderDate(rs.getString("orderDate"));
            transaction.setInvoiceNumber(rs.getString("invoiceNumber"));
            transaction.setStoreID(rs.getString("storeID"));
            transaction.setPosID(rs.getString("posID"));
            transaction.setTenderName(rs.getString("tenderName"));
            transaction.setTotalAmount(rs.getDouble("totalAmount"));
            transaction.setSaleType(rs.getString("saleType"));
            transaction.setTenderAmount(rs.getDouble("tenderAmount"));
            transaction.setTenderRRN(rs.getString("tenderRRN"));
            transaction.setTransactionID(rs.getString("transactionID"));
            transaction.setAcquirerBank(rs.getString("acquirerBank"));
            transaction.setAuthCode(rs.getString("authCode"));
            transaction.setCardNumber(rs.getString("cardNumber"));
            transaction.setCardType(rs.getString("cardType"));
            transaction.setNetworkType(rs.getString("networkType"));
            transaction.setCustomerVPA(rs.getString("customerVPA"));
            transaction.setMID(rs.getString("mid"));
            transaction.setPaymentType(rs.getString("paymentType"));
            transaction.setTrmRRN(rs.getString("trmRRN"));
            transaction.setSource(rs.getString("source"));
            transaction.setTID(rs.getString("tID"));
            transaction.setTransactionStatus(rs.getString("transactionStatus"));
            transaction.setSettlementDate(rs.getString("settlementDate"));
            transaction.setTrmAmount(rs.getDouble("trmAmount"));
            transaction.setAmountDifference(rs.getDouble("remainingAmount"));

            return transaction;
        });
        createSheet(workbook, headers, transactions, sheetName);
        if (reasonWiseSheet) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            Map<String, List<PosTrmData>> map = transactions.stream().filter(k -> !ObjectUtils.isEmpty(k.getRemarks())).collect(Collectors.groupingBy(PosTrmData::getRemarks));
            for (Map.Entry<String, List<PosTrmData>> mapEntry : map.entrySet()) {
                Sheet sheetReason = workbook.createSheet(mapEntry.getKey());
                createSheet(sheetReason, headerStyle, headers, mapEntry.getValue());
            }
        } else {
            CellStyle headerStyle = createHeaderStyle(workbook);
            List<PosTrmData> notFoundInPos = transactions.stream().filter(k -> TXN_NOT_FOUND_IN_POS.equalsIgnoreCase(k.getRemarks())).collect(Collectors.toList());
            Sheet sheetReason = workbook.createSheet(TXN_NOT_FOUND_IN_POS);
            createSheet(sheetReason, headerStyle, headers, notFoundInPos);
        }

    }

    private void createSheet(Sheet sheet, CellStyle headerStyle, String[] headers, List<PosTrmData> transactions) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        for (PosTrmData data : transactions) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getBusinessDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getOrderDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getInvoiceNumber());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getStoreID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getPosID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTenderName());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTotalAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getSaleType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTenderAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTenderRRN());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTransactionID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getAcquirerBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getAuthCode());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardNumber());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getNetworkType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getPaymentType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTrmRRN());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getSource());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTransactionStatus());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getSettlementDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTrmAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getAmountDifference());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getRemarks());

        }

    }

    private void createSheet(Workbook workbook, String[] headers, List<PosTrmData> transactions, String sheetName) {
        CellStyle headerStyle = createHeaderStyle(workbook);
        Sheet currentSheet = allocateNewSheet((SXSSFWorkbook) workbook, sheetName);
        Row headerRow = currentSheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        int sheetCounter = 1;//Start at 1 because we already created Initial sheet
        int rowCounter = 1;//1st row in sheet
        for (PosTrmData data : transactions) {
            if (rowCounter == 1048572) {//Max row reached, build new sheet
                //Increase sheetCounter
                sheetCounter++;
                String new_sheetName = sheetName + "_" + sheetCounter;//Name of sheet
                log.info("Creating new sheet: {} ", new_sheetName);
                currentSheet = allocateNewSheet((SXSSFWorkbook) workbook, new_sheetName);//Point currentSheet to new sheet
                headerRow = currentSheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                //Reset rowCounter to 0
                rowCounter = 0;
            }
            Row row = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getBusinessDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getOrderDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getInvoiceNumber());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getStoreID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getPosID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTenderName());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTotalAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getSaleType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTenderAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTenderRRN());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTransactionID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getAcquirerBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getAuthCode());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardNumber());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getNetworkType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getPaymentType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTrmRRN());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getSource());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTID());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTransactionStatus());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getSettlementDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getTrmAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getAmountDifference());
            String remarks = "";
            if (data.getRemarks() == null || data.getRemarks().equals("")) {
                remarks = "Matched";
            } else {
                remarks = data.getRemarks();
            }
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(remarks);
            rowCounter++;
        }

    }

    //    private void getSheet(Workbook workbook, String[] headers, DashboardDataRequest request, Sheet sheet,String sql,MapSqlParameterSource parameters ){
//        Row headerRow = sheet.createRow(0);
//        CellStyle headerStyle = createHeaderStyle(workbook);
//        for (int i = 0; i < headers.length; i++) {
//            Cell cell = headerRow.createCell(i);
//            cell.setCellValue(headers[i]);
//            cell.setCellStyle(headerStyle);
//        }
//
//        jdbcTemplate.query(sql, parameters, (rs) -> {
//            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
//            Double trmAmount = rs.getDouble("trmAmount");
//            Double posAmount = rs.getDouble("tenderAmount");
//            String invoiceNumber = rs.getString("invoiceNumber");
//            String transactionID = rs.getString("transactionID");
//            for (int i = 0; i < headers.length - 1; i++) {
//                Object cellValue = rs.getObject(1 + i);
//                if (cellValue instanceof Double) {
//                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue((Double) cellValue);
//                } else {
//                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue(cellValue == null ? "" : cellValue.toString());
//                }
//            }
//            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(getReason(trmAmount, posAmount,invoiceNumber,transactionID));
//
//        });
//    }
    private void addPOSvsTRMSheet(Workbook workbook, String[] headers, DashboardDataRequest request, Sheet sheet) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String sql1 = OrderTrmQuery.posVsTRMDownload;
//        if (Strings.isNotBlank(request.getTender())) {
//            sql1 += "AND t.name LIKE '" + "%" + request.getTender() + "%'\n";
//        }
        if (!CollectionUtils.isEmpty(request.getStores())) {
            sql1 += "AND o.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        getSheet(workbook, headers, request, sheet, sql1, parameters, true);

    }

    private void addPOSvsTRMSheet(Workbook workbook, String[] headers, DashboardDataRequest request, String sheetName) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String sql1 = OrderTrmQuery.posVsTRMDownload;
        if (Strings.isNotBlank(request.getTender())) {
            sql1 += "AND o.tender_name LIKE '" + "%" + request.getTender() + "%'\n";
        }
        if (!CollectionUtils.isEmpty(request.getStores())) {
            sql1 += "AND o.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        getSheet(workbook, headers, request, sql1, parameters, true, sheetName);

    }

    private void addTRMvsPOSSheet(Workbook workbook, String[] headers, DashboardDataRequest request, Sheet sheet) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String sql1 = OrderTrmQuery.trmVsPosDownload;
        if (Strings.isNotBlank(request.getTender())) {
            sql1 += "AND trm.payment_type LIKE '" + "%" + request.getTender() + "%'\n";
        }
        if (!CollectionUtils.isEmpty(request.getStores())) {
            sql1 += "AND trm.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        getSheet(workbook, headers, request, sheet, sql1, parameters, false);

    }

    private void addTRMvsPOSSheet(Workbook workbook, String[] headers, DashboardDataRequest request, String sheetName) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String trmvspos = OrderTrmQuery.trmVsPosDownload;
        if (Strings.isNotBlank(request.getTender())) {
            trmvspos += "AND trm.payment_type LIKE '" + "%" + request.getTender() + "%'\n";
        }
        if (!CollectionUtils.isEmpty(request.getStores())) {
            trmvspos += "AND trm.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        log.info("trmvspos {}", trmvspos);
        getSheet(workbook, headers, request, trmvspos, parameters, false, sheetName);

    }

    private String getReason(Double trmAmount, Double posAmount, String invoiceNumber, String transactionId) {
        if (transactionId == null) {
            return TXN_NOT_FOUND_IN_TRM;
        } else if (invoiceNumber == null) {
            return TXN_NOT_FOUND_IN_POS;

        } else if (trmAmount > posAmount) {
            return TRM_AMT_GREATER_THAN_POS;

        } else if (posAmount > trmAmount) {
            return POS_AMT_GREATER_THAN_TRM;
        }
        return "";
    }
}
