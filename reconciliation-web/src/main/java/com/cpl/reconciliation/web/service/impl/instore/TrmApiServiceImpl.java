package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.core.common.annotations.TrackExecutionTime;
import com.cpl.reconciliation.core.constant.ReportHeaders;
import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.BankWiseData;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.TenderWiseData;
import com.cpl.reconciliation.core.response.TrmData;
import com.cpl.reconciliation.core.response.instore.MissingTID;
import com.cpl.reconciliation.core.response.instore.MprVsTrmData;
import com.cpl.reconciliation.core.response.instore.TrmVsMprData;
import com.cpl.reconciliation.domain.repository.TRMRepository;
import com.cpl.reconciliation.web.service.util.TRMMPRSummarySheetUtil;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static com.cpl.reconciliation.domain.util.Constants.*;

@Data
@Slf4j
@Service
public class TrmApiServiceImpl extends AbstractInStoreService implements TrmApiService {

    private final TRMRepository trmRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TRMMPRSummarySheetUtil trmmprSummarySheetUtil;

    @Override
    public List<TrmData> getTrmData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n"
                + "    trm.*\n"
                + "FROM\n"
                + "    trm\n"
                + "WHERE\n"
                + "    trm.transaction_date BETWEEN :startDate AND :endDate\n"
                + "        AND trm.transaction_status = 'SUCCESS'";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND trm.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += " AND trm.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        if (!StringUtils.isEmpty(request.getBank())) {
            QUERY += " AND trm.acquirer_bank = :bank";
            parameters.addValue("bank", request.getBank());
        }
        log.info("TRM Sale: {} , Parameters: {}", QUERY, parameters);
        List<TrmData> saleDataList = new LinkedList<>();
        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            TrmData trmData = new TrmData();
            trmData.setPaymentType(rs.getString("payment_type"));
            trmData.setPosId(rs.getString("pos_id"));
            trmData.setStoreId(rs.getString("store_id"));
            trmData.setSaleType(rs.getString("transaction_type"));
            trmData.setInvoiceNumber(rs.getString("order_id"));
            trmData.setRrnNumber(rs.getString("rrn"));
            trmData.setSaleAmount(rs.getDouble("trm_amount"));
            //
            trmData.setSource(rs.getString("source"));
            trmData.setAcquirerBank(rs.getString("acquirer_bank"));
            trmData.setTid(rs.getString("tid"));
            trmData.setMid(rs.getString("mid"));
            trmData.setTransactionId(rs.getString("transaction_id"));
            trmData.setTransactionDate(rs.getString("transaction_date"));
            trmData.setSettlementDate(rs.getString("settlement_date"));
            trmData.setMprCommonKey(rs.getString("uid"));
            //UPI
            trmData.setCustomerVPA(rs.getString("customervpa"));
            //CARD
            trmData.setCardNo(rs.getString("card_number"));
            trmData.setCardType(rs.getString("card_type"));
            trmData.setCardNetwork(rs.getString("network_type"));
            trmData.setApprovalCode(rs.getString("auth_code"));
            saleDataList.add(trmData);
        });
        return saleDataList;
    }

    @Override
    @SneakyThrows
    public void trmDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<TrmData> trmDataList = getTrmData(request);
        String[] headers = ReportHeaders.TrmSale.TRM_Header;
        String sheetname = "TRMSales";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            if (Strings.isNotBlank(request.getTender())) {
                if (PaymentType.CARD.name().equalsIgnoreCase(request.getTender())) {
                    headers = ReportHeaders.TrmSale.CARD_Header;
                    Sheet currentSheet = allocateNewSheet(workbook, sheetname, headers);
                    Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                    int sheetCounter = 1;//Start at 1 because we already created Initial sheet
                    int rowCounter = 2;//1st row in sheet
                    double totalSaleAmt = 0.0;
                    for (TrmData trmData : trmDataList) {
                        if (rowCounter == 1048572) {//Max row reached, build new sheet
                            //Increase sheetCounter
                            sheetCounter++;
                            String new_sheetName = sheetname + "_" + sheetCounter;//Name of sheet
                            log.info("Creating new sheet: {} ", new_sheetName);
                            currentSheet = allocateNewSheet(workbook, new_sheetName, headers);//Point currentSheet to new sheet
                            //Reset rowCounter to 0
                            rowCounter = 0;
                        }
                        Row row = getTrmDataExcelRow(trmData, currentSheet);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCardNo());
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCardType());
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCardNetwork());
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getApprovalCode());
                        totalSaleAmt = totalSaleAmt + trmData.getSaleAmount();
                        rowCounter++;
                    }
                    totalRow.createCell(0).setCellValue("Total");
                    totalRow.createCell(8).setCellValue(totalSaleAmt);
                } else if (PaymentType.UPI.name().equalsIgnoreCase(request.getTender())) {
                    headers = ReportHeaders.TrmSale.UPI_Header;
                    Sheet currentSheet = allocateNewSheet(workbook, sheetname, headers);
                    Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                    int sheetCounter = 1;//Start at 1 because we already created Initial sheet
                    int rowCounter = 2;//1st row in sheet
                    double totalSaleAmt = 0.0;
                    for (TrmData trmData : trmDataList) {
                        if (rowCounter == 1048572) {//Max row reached, build new sheet
                            //Increase sheetCounter
                            sheetCounter++;
                            String new_sheetName = sheetname + "_" + sheetCounter;//Name of sheet
                            log.info("Creating new sheet: {} ", new_sheetName);
                            currentSheet = allocateNewSheet(workbook, new_sheetName, headers);//Point currentSheet to new sheet
                            //Reset rowCounter to 0
                            rowCounter = 0;
                        }
                        Row row = getTrmDataExcelRow(trmData, currentSheet);
                        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCustomerVPA());
                        totalSaleAmt = totalSaleAmt + trmData.getSaleAmount();
                        rowCounter++;
                    }
                    totalRow.createCell(0).setCellValue("Total");
                    totalRow.createCell(8).setCellValue(totalSaleAmt);
                }
            } else {
                Sheet currentSheet = allocateNewSheet(workbook, sheetname, headers);
                Row totalRow = currentSheet.createRow(currentSheet.getPhysicalNumberOfRows());
                int sheetCounter = 1;//Start at 1 because we already created Initial sheet
                int rowCounter = 2;//1st row in sheet
                double totalSaleAmt = 0.0;
                for (TrmData trmData : trmDataList) {
                    if (rowCounter == 1048572) {//Max row reached, build new sheet
                        //Increase sheetCounter
                        sheetCounter++;
                        String new_sheetName = sheetname + "_" + sheetCounter;//Name of sheet
                        log.info("Creating new sheet: {} ", new_sheetName);
                        currentSheet = allocateNewSheet(workbook, new_sheetName, headers);//Point currentSheet to new sheet
                        //Reset rowCounter to 0
                        rowCounter = 0;
                    }
                    Row row = getTrmDataExcelRow(trmData, currentSheet);
                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCustomerVPA());
                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCardNo());
                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCardType());
                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getCardNetwork());
                    row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getApprovalCode());
                    totalSaleAmt = totalSaleAmt + trmData.getSaleAmount();
                    rowCounter++;
                }
                totalRow.createCell(0).setCellValue("Total");
                totalRow.createCell(8).setCellValue(totalSaleAmt);
            }
            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    private static Sheet allocateNewSheet(SXSSFWorkbook workbook, String sheetName, String[] headers) {
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

    private Row getTrmDataExcelRow(TrmData trmData, Sheet sheet) {
        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getSource());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getPaymentType());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getAcquirerBank());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getTid());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getMid());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getPosId());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getStoreId());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getSaleType());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getSaleAmount());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getInvoiceNumber());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getRrnNumber());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getTransactionId());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getTransactionDate());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getSettlementDate());
        row.createCell(row.getPhysicalNumberOfCells()).setCellValue(trmData.getMprCommonKey());
        return row;
    }

    @Override
    public DashboardDataResponse getTrmVsMprData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n"
                + "    trm.payment_type,\n"
                + "    trm.acquirer_bank,\n"
                + "    COALESCE(SUM(trm.trm_amount), 0) AS sales_amount,\n"
                + "    (SELECT \n"
                + "            COALESCE(SUM(mpr_amount),0)\n"
                + "        FROM\n"
                + "            mpr\n"
                + "        WHERE\n"
                + "            payment_type = trm.payment_type\n"
                + "                AND bank = trm.acquirer_bank AND transaction_date BETWEEN :startDate AND :endDate) AS receipts_amount,\n"
                + "     COALESCE(SUM(m.mpr_amount), 0) AS reconciled_amount,\n"
                + "    COALESCE(SUM(m.commission), 0) AS charges,\n"
                + "    (COALESCE(SUM(trm.trm_amount), 0) - (COALESCE(SUM(m.mpr_amount), 0))) AS difference\n"
                + "FROM\n"
                + "    subway.trm trm\n"
                + "        LEFT JOIN\n"
                + "    subway.mpr m ON (m.uid = trm.uid)\n"
                + "WHERE\n"
                + "    trm.transaction_date BETWEEN :startDate AND :endDate\n"
                + "        AND trm.transaction_status = 'SUCCESS'";
        String groupClause = "GROUP BY trm.payment_type , trm.acquirer_bank\n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND trm.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += " AND trm.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        if (!StringUtils.isEmpty(request.getBank())) {
            QUERY += " AND trm.acquirer_bank = :bank";
            parameters.addValue("bank", request.getBank());
        }
        QUERY += groupClause;
        HashMap<String, TenderWiseData> map = new HashMap<>();
        jdbcTemplate.query(QUERY, parameters, (resultSet) -> {
            if (!map.containsKey(resultSet.getString("payment_type"))) {
                TenderWiseData tenderWiseData = new TenderWiseData();
                tenderWiseData.setTenderName(resultSet.getString("payment_type"));
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(resultSet.getString("payment_type"), tenderWiseData);
            }
            TenderWiseData tender = map.get(resultSet.getString("payment_type"));
            BankWiseData bankWiseData = new BankWiseData();
            bankWiseData.setBankName(resultSet.getString("acquirer_bank"));
            bankWiseData.setSales(resultSet.getDouble("sales_amount"));
            bankWiseData.setReceipts(resultSet.getDouble("receipts_amount"));
            bankWiseData.setReconciled(resultSet.getDouble("reconciled_amount"));
            bankWiseData.setDifference(resultSet.getDouble("difference"));
            bankWiseData.setCharges(resultSet.getDouble("charges"));
            tender.getBankWiseDataList().add(bankWiseData);
            tender.setSales(tender.getSales() + bankWiseData.getSales());
            tender.setReceipts(tender.getReceipts() + bankWiseData.getReceipts());
            tender.setReconciled(tender.getReconciled() + bankWiseData.getReconciled());
            tender.setDifference(tender.getDifference() + bankWiseData.getDifference());
            tender.setCharges(tender.getCharges() + bankWiseData.getCharges());
            map.put(resultSet.getString("payment_type"), tender);
        });
        DashboardDataResponse response = new DashboardDataResponse();
        response.setTenderWiseDataList(new ArrayList<>());
        for (TenderWiseData tenderWiseData : map.values()) {
            response.setSales(response.getSales() + tenderWiseData.getSales());
            response.setReceipts(response.getReceipts() + tenderWiseData.getReceipts());
            response.setReconciled(response.getReconciled() + tenderWiseData.getReconciled());
            response.setDifference(response.getDifference() + tenderWiseData.getDifference());
            response.setCharges(response.getCharges() + tenderWiseData.getCharges());
            response.getTenderWiseDataList().add(tenderWiseData);
        }
        return response;
    }

    @TrackExecutionTime
    public List<TrmVsMprData> getTrmVsMprDataDownload(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
//        String QUERY = "SELECT \n"
//                + "    trm.*,\n"
//                + " mpr.id as mpr_id,\n"
//                + " mpr.payment_type as mpr_payment_type,\n"
//                + " mpr.bank as mpr_bank,\n"
//                + " mpr.tid as mpr_tid,\n"
//                + " mpr.mid as mpr_mid,\n"
//                + " mpr.store_id as mpr_store_id,\n"
//                + " mpr.mpr_amount as mpr_amount,\n"
//                + " mpr.commission as mpr_commission,\n"
//                + " mpr.settled_amount as mpr_settled_amount,\n"
//                + " mpr.rrn as mpr_bank_rrn,\n"
//                + " mpr.transaction_date as mpr_transaction_date,\n"
//                + " mpr.settled_date as mpr_settlement_date,\n"
//                + " mpr.payerva as mpr_customervpa\n"
//                + "FROM\n"
//                + "    trm trm\n"
//                + "        LEFT JOIN\n"
//                + "    mpr mpr ON (((trm.acquirer_bank='PHONEPE' AND (trm.uid = mpr.merchant_ref_id OR (trm.uid = mpr.transaction_utr))) \n"
//                + "OR (trm.acquirer_bank = 'YES' AND trm.uid = mpr.uid) OR (trm.acquirer_bank = 'AMEX')))\n"
//                + "WHERE\n"
//                + "    trm.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "        AND trm.transaction_status = 'SUCCESS'";
        String QUERY = " WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "     m.id,\n"
                + " 	m.payment_type,\n"
                + "     m.bank,\n"
                + "     m.tid,\n"
                + "     m.mid,\n"
                + "     m.store_id,\n"
                + "     m.mpr_amount,\n"
                + "     m.commission,\n"
                + "     m.settled_amount,\n"
                + "     m.rrn,\n"
                + "     m.transaction_date,\n"
                + "     m.settled_date,\n"
                + "     m.payerva\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "     m.id,\n"
                + " 	m.payment_type,\n"
                + "     m.bank,\n"
                + "     m.tid,\n"
                + "     m.mid,\n"
                + "     m.store_id,\n"
                + "     m.mpr_amount,\n"
                + "     m.commission,\n"
                + "     m.settled_amount,\n"
                + "     m.rrn,\n"
                + "     m.transaction_date,\n"
                + "     m.settled_date,\n"
                + "     m.payerva\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "     m.id,\n"
                + " 	m.payment_type,\n"
                + "     m.bank,\n"
                + "     m.tid,\n"
                + "     m.mid,\n"
                + "     m.store_id,\n"
                + "     m.mpr_amount,\n"
                + "     m.commission,\n"
                + "     m.settled_amount,\n"
                + "     m.rrn,\n"
                + "     m.transaction_date,\n"
                + "     m.settled_date,\n"
                + "     m.payerva\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + ") \n"
                + "SELECT \n"
                + "    trm.*,\n"
                + " c.id as mpr_id,\n"
                + " c.payment_type as mpr_payment_type,\n"
                + " c.bank as mpr_bank,\n"
                + " c.tid as mpr_tid,\n"
                + " c.mid as mpr_mid,\n"
                + " c.store_id as mpr_store_id,\n"
                + " c.mpr_amount as mpr_amount,\n"
                + " c.commission as mpr_commission,\n"
                + " c.settled_amount as mpr_settled_amount,\n"
                + " c.rrn as mpr_bank_rrn,\n"
                + " c.transaction_date as mpr_transaction_date,\n"
                + " c.settled_date as mpr_settlement_date,\n"
                + " c.payerva as mpr_customervpa\n"
                + "FROM\n"
                + "    trm trm LEFT JOIN Combined c ON trm.uid = c.uid\n"
                + "WHERE\n"
                + "    trm.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND trm.transaction_status = 'SUCCESS'";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND trm.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += " AND trm.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        if (!StringUtils.isEmpty(request.getBank())) {
            QUERY += " AND trm.acquirer_bank = :bank";
            parameters.addValue("bank", request.getBank());
        }
        log.info("TRMvsMPR Sale: {} , Parameters: {}", QUERY, parameters);
        List<TrmVsMprData> result = new ArrayList<>();
        jdbcTemplate.query(QUERY, parameters, (rs -> {
            TrmVsMprData trmData = new TrmVsMprData();
            trmData.setPaymentType(rs.getString("payment_type"));
            trmData.setPosId(rs.getString("pos_id"));
            trmData.setStoreId(rs.getString("store_id"));
            trmData.setSaleType(rs.getString("transaction_type"));
            trmData.setInvoiceNumber(rs.getString("order_id"));
            trmData.setRrnNumber(rs.getString("rrn"));
            trmData.setSaleAmount(rs.getDouble("trm_amount"));
            //
            trmData.setSource(rs.getString("source"));
            trmData.setAcquirerBank(rs.getString("acquirer_bank"));
            trmData.setTid(rs.getString("tid"));
            trmData.setMid(rs.getString("mid"));
            trmData.setTransactionId(rs.getString("transaction_id"));
            trmData.setTransactionDate(rs.getString("transaction_date"));
            trmData.setSettlementDate(rs.getString("settlement_date"));
            trmData.setMprCommonKey(rs.getString("uid"));
            //UPI
            trmData.setCustomerVPA(rs.getString("customervpa"));
            //CARD
            trmData.setCardNo(rs.getString("card_number"));
            trmData.setCardType(rs.getString("card_type"));
            trmData.setCardNetwork(rs.getString("network_type"));
            trmData.setApprovalCode(rs.getString("auth_code"));
            //MPR data
            trmData.setMprId(rs.getString("mpr_id"));
            trmData.setMprPaymentType(rs.getString("mpr_payment_type"));
            trmData.setMprBank(rs.getString("mpr_bank"));
            trmData.setMprTid(rs.getString("mpr_tid"));
            trmData.setMprMid(rs.getString("mpr_mid"));
            trmData.setMprStoreId(rs.getString("mpr_store_id"));
            trmData.setMprAmount(rs.getDouble("mpr_amount"));
            trmData.setMprCommission(rs.getDouble("mpr_commission"));
            trmData.setMprSettleAmount(rs.getDouble("mpr_settled_amount"));
            trmData.setMprBankRRN(rs.getString("mpr_bank_rrn"));
            trmData.setMprTransactionDate(rs.getString("mpr_transaction_date"));
            trmData.setMprSettlementDate(rs.getString("mpr_settlement_date"));
            trmData.setMprCustomerVPA(rs.getString("mpr_customervpa"));
            result.add(trmData);
        }));
        return result;
    }

    @TrackExecutionTime
    public List<MprVsTrmData> getMprVsTrmDataDownload(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
//        String QUERY = "SELECT \n"
//                + "    trm.*,\n"
//                + " mpr.id as mpr_id,\n"
//                + " mpr.payment_type as mpr_payment_type,\n"
//                + " mpr.bank as mpr_bank,\n"
//                + " mpr.tid as mpr_tid,\n"
//                + " mpr.mid as mpr_mid,\n"
//                + " mpr.store_id as mpr_store_id,\n"
//                + " mpr.mpr_amount as mpr_amount,\n"
//                + " mpr.commission as mpr_commission,\n"
//                + " mpr.settled_amount as mpr_settled_amount,\n"
//                + " mpr.rrn as mpr_bank_rrn,\n"
//                + " mpr.transaction_date as mpr_transaction_date,\n"
//                + " mpr.settled_date as mpr_settlement_date,\n"
//                + " mpr.uid as mpr_uid,\n"
//                + " mpr.payerva as mpr_customervpa\n"
//                + "FROM\n"
//                + "    mpr mpr\n"
//                + "        LEFT JOIN\n"
//                + "    trm trm ON ((((trm.acquirer_bank='PHONEPE' AND (trm.uid = mpr.merchant_ref_id OR (trm.uid = mpr.transaction_utr))) \n"
//                + "OR (trm.acquirer_bank = 'YES' AND trm.uid = mpr.uid) OR (trm.acquirer_bank = 'AMEX'))) and trm.transaction_status = 'SUCCESS')\n"
//                + "WHERE\n"
//                + "    mpr.transaction_date BETWEEN :startDate AND :endDate";
        String QUERY = " WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "     m.id,\n"
                + " 	m.payment_type,\n"
                + "     m.bank,\n"
                + "     m.tid,\n"
                + "     m.mid,\n"
                + "     m.store_id,\n"
                + "     m.mpr_amount,\n"
                + "     m.commission,\n"
                + "     m.settled_amount,\n"
                + "     m.rrn,\n"
                + "     m.transaction_date,\n"
                + "     m.settled_date,\n"
                + "     m.payerva\n"
                + "    FROM mpr m "
                + "    LEFT JOIN trm t ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND m.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND m.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "     m.id,\n"
                + " 	m.payment_type,\n"
                + "     m.bank,\n"
                + "     m.tid,\n"
                + "     m.mid,\n"
                + "     m.store_id,\n"
                + "     m.mpr_amount,\n"
                + "     m.commission,\n"
                + "     m.settled_amount,\n"
                + "     m.rrn,\n"
                + "     m.transaction_date,\n"
                + "     m.settled_date,\n"
                + "     m.payerva\n"
                + "    FROM mpr m "
                + "    LEFT JOIN trm t ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND m.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND m.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "     m.id,\n"
                + " 	m.payment_type,\n"
                + "     m.bank,\n"
                + "     m.tid,\n"
                + "     m.mid,\n"
                + "     m.store_id,\n"
                + "     m.mpr_amount,\n"
                + "     m.commission,\n"
                + "     m.settled_amount,\n"
                + "     m.rrn,\n"
                + "     m.transaction_date,\n"
                + "     m.settled_date,\n"
                + "     m.payerva\n"
                + "    FROM mpr m "
                + "    LEFT JOIN trm t ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND m.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND m.store_id IN (:storeList)\n"
                + ")    \n"
                + " SELECT \n"
                + "    trm.*,\n"
                + " c.id as mpr_id,\n"
                + " c.payment_type as mpr_payment_type,\n"
                + " c.bank as mpr_bank,\n"
                + " c.tid as mpr_tid,\n"
                + " c.mid as mpr_mid,\n"
                + " c.store_id as mpr_store_id,\n"
                + " c.mpr_amount as mpr_amount,\n"
                + " c.commission as mpr_commission,\n"
                + " c.settled_amount as mpr_settled_amount,\n"
                + " c.rrn as mpr_bank_rrn,\n"
                + " c.transaction_date as mpr_transaction_date,\n"
                + " c.settled_date as mpr_settlement_date,\n"
                + " c.uid as mpr_uid,\n"
                + " c.payerva as mpr_customervpa\n"
                + "FROM Combined c LEFT JOIN \n"
                + "trm trm ON c.uid=trm.uid and trm.transaction_status = 'SUCCESS'\n"
                + "WHERE c.transaction_date BETWEEN :startDate AND :endDate\n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND c.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += " AND c.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        if (!StringUtils.isEmpty(request.getBank())) {
            QUERY += " AND c.bank = :bank";
            parameters.addValue("bank", request.getBank());
        }
        log.info("MPRvsTRm Sale: {} , Parameters: {}", QUERY, parameters);
        List<MprVsTrmData> result = new ArrayList<>();
        jdbcTemplate.query(QUERY, parameters, (rs -> {
            MprVsTrmData trmData = new MprVsTrmData();
            trmData.setPaymentType(rs.getString("payment_type"));
            trmData.setPosId(rs.getString("pos_id"));
            trmData.setStoreId(rs.getString("store_id"));
            trmData.setSaleType(rs.getString("transaction_type"));
            trmData.setInvoiceNumber(rs.getString("order_id"));
            trmData.setRrnNumber(rs.getString("rrn"));
            trmData.setSaleAmount(rs.getDouble("trm_amount"));
            //
            trmData.setSource(rs.getString("source"));
            trmData.setAcquirerBank(rs.getString("acquirer_bank"));
            trmData.setTid(rs.getString("tid"));
            trmData.setMid(rs.getString("mid"));
            trmData.setTransactionId(rs.getString("transaction_id"));
            trmData.setTransactionDate(rs.getString("transaction_date"));
            trmData.setSettlementDate(rs.getString("settlement_date"));
            //UPI
            trmData.setCustomerVPA(rs.getString("customervpa"));
            //CARD
            trmData.setCardNo(rs.getString("card_number"));
            trmData.setCardType(rs.getString("card_type"));
            trmData.setCardNetwork(rs.getString("network_type"));
            trmData.setApprovalCode(rs.getString("auth_code"));
            //MPR data
            trmData.setMprId(rs.getString("mpr_id"));
            trmData.setMprPaymentType(rs.getString("mpr_payment_type"));
            trmData.setMprBank(rs.getString("mpr_bank"));
            trmData.setMprTid(rs.getString("mpr_tid"));
            trmData.setMprMid(rs.getString("mpr_mid"));
            trmData.setMprStoreId(rs.getString("mpr_store_id"));
            trmData.setMprAmount(rs.getDouble("mpr_amount"));
            trmData.setMprCommission(rs.getDouble("mpr_commission"));
            trmData.setMprSettleAmount(rs.getDouble("mpr_settled_amount"));
            trmData.setMprBankRRN(rs.getString("mpr_bank_rrn"));
            trmData.setMprTransactionDate(rs.getString("mpr_transaction_date"));
            trmData.setMprSettlementDate(rs.getString("mpr_settlement_date"));
            trmData.setMprCustomerVPA(rs.getString("mpr_customervpa"));
            trmData.setMprCommonKey(rs.getString("mpr_uid"));
            result.add(trmData);
        }));
        return result;
    }

    @Override
    @SneakyThrows
    @TrackExecutionTime
    public void getTrmVsMprDataDownload(DashboardDataRequest request, OutputStream outputStream) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            String sheet1Name = "TRM vs MPR";
            String sheet2Name = "MPR vs TRM";
            Sheet sheet = workbook.createSheet("Summary");
            getTRMVsMPRSheet(request, workbook, sheet1Name);
            getMPRVsTRMSheet(request, workbook, sheet2Name);
            trmmprSummarySheetUtil.createSummarySheet(workbook, sheet, getAllSheetsBySheetName(workbook, sheet1Name), getAllSheetsBySheetName(workbook, sheet2Name));
            workbook.write(outputStream);
            workbook.dispose();
        }
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

    public void getTRMVsMPRSheet(DashboardDataRequest request, Workbook workbook, String sheetName) {
        String[] headers = ReportHeaders.TrmSale.TRMVSMPR_Header;
        Sheet currentSheet = allocateNewSheet((SXSSFWorkbook) workbook, sheetName, headers);
        int sheetCounter = 1;//Start at 1 because we already created Initial sheet
        int rowCounter = 2;//1st row in sheet
        List<TrmVsMprData> dataList = getTrmVsMprDataDownload(request);
        log.info("TRMVsMPR Data Size: {}", dataList.size());
        for (TrmVsMprData data : dataList) {
            if (rowCounter == 1048572) {//Max row reached, build new sheet
                //Increase sheetCounter
                sheetCounter++;
                String new_sheetName = sheetName + "_" + sheetCounter;//Name of sheet
                log.info("Creating new sheet: {} ", new_sheetName);
                currentSheet = allocateNewSheet((SXSSFWorkbook) workbook, new_sheetName, headers);//Point currentSheet to new sheet
                //Reset rowCounter to 0
                rowCounter = 0;
            }
            Row row = getTrmDataExcelRow(data, currentSheet);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardNo());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardNetwork());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getApprovalCode());
            //MPR Rows
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprPaymentType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprTid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprMid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprStoreId());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprCommission());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprSettleAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprBankRRN());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprTransactionDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprSettlementDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(getReason(data.getMprId(), data.getTransactionId(), data.getMprAmount(), data.getSaleAmount()));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(Math.abs(data.getMprAmount() - data.getSaleAmount()));
            rowCounter++;
        }
    }

    private void getMPRVsTRMSheet(DashboardDataRequest request, Workbook workbook, String sheetName) {
        String[] headers = ReportHeaders.TrmSale.MPRVSTRM_Header;
        Sheet currentSheet = allocateNewSheet((SXSSFWorkbook) workbook, sheetName, headers);
        int sheetCounter = 1;//Start at 1 because we already created Initial sheet
        int rowCounter = 2;//1st row in sheet
        //
        List<MprVsTrmData> dataList = getMprVsTrmDataDownload(request);
        log.info("MPRVsTRM Data Size: {}", dataList.size());
        for (MprVsTrmData data : dataList) {
            if (rowCounter == 1048752) {//Max row reached, build new sheet
                //Increase sheetCounter
                sheetCounter++;
                String new_sheetName = sheetName + "_" + sheetCounter;//Name of sheet
                log.info("Creating new sheet: {} ", new_sheetName);
                currentSheet = allocateNewSheet((SXSSFWorkbook) workbook, new_sheetName, headers);//Point currentSheet to new sheet
                //Reset rowCounter to 0
                rowCounter = 0;
            }
            Row row = getTrmDataExcelRow(data, currentSheet);
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardNo());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getCardNetwork());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getApprovalCode());
            //MPR Rows
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprPaymentType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprTid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprMid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprStoreId());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprCommission());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprSettleAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprBankRRN());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprTransactionDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprSettlementDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(data.getMprCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(getReason(data.getMprId(), data.getTransactionId(), data.getMprAmount(), data.getSaleAmount()));
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(Math.abs(data.getMprAmount() - data.getSaleAmount()));
            rowCounter++;
        }
    }

    private String getReason(String mprId, String trmId, double mprAmount, double trmAmount) {
        if (ObjectUtils.isEmpty(mprId)) {
            return TXN_NOT_FOUND_IN_MPR;
        } else if (ObjectUtils.isEmpty(trmId)) {
            return TXN_NOT_FOUND_IN_TRM;
        } else if (mprAmount > trmAmount) {
            return MPR_AMT_GREATER;
        } else if (trmAmount > mprAmount) {
            return TRM_AMT_GREATER;
        }
        return "Matched";
    }

    @Override
    public HashMap<String, HashMap<String, String>> getMissingTidMapping() {
        HashMap<String, HashMap<String, String>> finalmap = new HashMap<>();
        HashMap<String, String> upiMap = new HashMap<>();
        HashMap<String, String> cardMap = new HashMap<>();
        List<MissingTID> missingTidValues = trmRepository.getMissingTIDCountTenderAndBank();
        List<MissingTID> totalTidValues = trmRepository.getTIDCountTenderAndBank();
        for (MissingTID missingTID : missingTidValues) {
            if (!missingTID.getBank().equalsIgnoreCase("AMEX")) {
                String value = missingTID.getValue() == 0 ? String.valueOf(0) : String.valueOf(missingTID.getValue());
                if (missingTID.getTender().name().equals("UPI")) {
                    upiMap.put(missingTID.getBank(), value);
                } else if (missingTID.getTender().name().equals("CARD")) {
                    cardMap.put(missingTID.getBank(), value);
                }
            }
        }
        for (MissingTID missingTID : totalTidValues) {
            if (!missingTID.getBank().equalsIgnoreCase("AMEX")) {
                String totalvalue = missingTID.getValue() == 0 ? String.valueOf(0) : String.valueOf(missingTID.getValue());
                if (missingTID.getTender().name().equals("UPI")) {
                    upiMap.put(missingTID.getBank(), upiMap.getOrDefault(missingTID.getBank(), "0") + "/" + totalvalue);
                } else if (missingTID.getTender().name().equals("CARD")) {
                    cardMap.put(missingTID.getBank(), cardMap.getOrDefault(missingTID.getBank(), "0") + "/" + totalvalue);
                }
            }
        }
        finalmap.put("UPI", upiMap);
        finalmap.put("CARD", cardMap);
        return finalmap;
//        mappings = missingTidValues.stream().filter(missingTID -> !missingTID.getBank().name().equalsIgnoreCase("AMEX"))
//                .map(missingTID ->
//                    new MissingTIDMapping(missingTID.getTender().name(),missingTID.getBank().name(),(int) missingTID.getValue(),0)
//                ).collect(Collectors.toList());
//        for (MissingTID totalTidValue:totalTidValues){
//            mappings.stream().filter(missingTID -> missingTID.getTender().equalsIgnoreCase(totalTidValue.getTender().name())
//            && missingTID.getBank().equalsIgnoreCase(totalTidValue.getBank().name())).forEach(missingTID -> missingTID.setTotal((int) totalTidValue.getValue()));
//        }
//        return mappings;
    }

    @Override
    public void downloadMissingTIDMapping(MissingTIDReportRequest request, Workbook workbook) {
        List<String> missingTidList = trmRepository.getMissingTIDTenderAndBank(PaymentType.valueOf(request.getTender()), request.getBank());
        Sheet sheet = workbook.createSheet("Tid Mappings " + request.getTender() + "_" + request.getBank());
        Row headerRow = sheet.createRow(0);
        String[] headers = {"TID", "STORE CODE"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (String missingTId : missingTidList) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(0).setCellValue(missingTId);
        }
    }
}
