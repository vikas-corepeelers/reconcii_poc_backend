package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.core.common.annotations.TrackExecutionTime;
import com.cpl.reconciliation.core.constant.ReportHeaders;
import com.cpl.reconciliation.core.modal.query.MprBankDifference;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.BankWiseData;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.MprData;
import com.cpl.reconciliation.core.response.TenderWiseData;
import com.cpl.reconciliation.core.response.instore.BankStatementData;
import com.cpl.reconciliation.domain.dao.MPRDao;
import com.cpl.reconciliation.domain.repository.MPRRepository;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@Slf4j
@Service
public class MprApiServiceImpl extends AbstractInStoreService implements MprApiService {

    private final MPRDao mprDao;
    private final BankStatementService bankStatementService;
    @Autowired
    private final MPRRepository mprRepository;

    @Override
    public DashboardDataResponse getMprVsBankData(DashboardDataRequest request) {
        String sql = "SELECT \n" +
                "    payment_type,\n" +
                "    bank,\n" +
                "    COALESCE(SUM(sales_amount), 0) AS sales_amount,\n" +
                "    COALESCE(SUM(receipt_amount), 0) AS receipt_amount,\n" +
                "    COALESCE(SUM(difference), 0) AS difference,\n" +
                "    COALESCE(SUM(charges), 0) AS charges,\n" +
                "     COALESCE(SUM(reconciled_amount), 0) AS reconciled\n" +
                "FROM\n" +
                "    (SELECT \n" +
                "        m.bank AS bank,\n" +
                "            m.payment_type AS payment_type,\n" +
                "            DATE(m.settled_date) AS settlementDate,\n" +
                "            COALESCE(SUM(m.mpr_amount), 0) AS sales_amount,\n" +
                "            COALESCE(SUM(m.commission), 0) AS charges,\n" +
                "            CASE\n" +
                "                WHEN\n" +
                "                    COALESCE(SUM(m.settled_amount), 0) <= (SELECT \n" +
                "                            SUM(deposit_amt)\n" +
                "                        FROM\n" +
                "                            bank_statements\n" +
                "                        WHERE\n" +
                "                             DATE(expected_actual_transaction_date) = settlementDate\n" +
                "                        AND payment_type = m.payment_type\n" +
                "                        AND source_bank = m.bank)\n" +
                "                THEN\n" +
                "                    COALESCE(SUM(m.mpr_amount), 0)\n" +
                "                ELSE ((SELECT \n" +
                "                        coalesce(SUM(deposit_amt),0)\n" +
                "                    FROM\n" +
                "                        bank_statements\n" +
                "                    WHERE\n" +
                "                         DATE(expected_actual_transaction_date) = settlementDate\n" +
                "                        AND payment_type = m.payment_type\n" +
                "                        AND source_bank = m.bank) + COALESCE(SUM(m.commission), 0))\n" +
                "            END AS reconciled_amount,\n" +
                "            (SELECT \n" +
                "                    COALESCE(SUM(deposit_amt), 0)\n" +
                "                FROM\n" +
                "                    bank_statements\n" +
                "                WHERE\n" +
                "                    DATE(expected_actual_transaction_date) = settlementDate\n" +
                "                        AND payment_type = m.payment_type\n" +
                "                        AND source_bank = m.bank) AS receipt_amount,\n" +
                "            (COALESCE(SUM(m.settled_amount)) - (SELECT \n" +
                "                    COALESCE(SUM(deposit_amt), 0)\n" +
                "                FROM\n" +
                "                    bank_statements\n" +
                "                WHERE\n" +
                "                    DATE(expected_actual_transaction_date) = settlementDate\n" +
                "                        AND payment_type = m.payment_type\n" +
                "                        AND source_bank = m.bank)) AS difference\n" +
                "    FROM\n" +
                "        mpr m\n" +
                "    WHERE\n" +
                "        m.transaction_date BETWEEN :startDate AND :endDate ";
        String groupClause = "GROUP BY m.bank , m.payment_type , DATE(m.settled_date)) derived\n" +
                "GROUP BY bank,payment_type; ";
        if (Strings.isNotBlank(request.getTender())) {
            sql += "AND m.payment_type LIKE '" + "%" + request.getTender() + "%'\n";
        }
        if (Strings.isNotBlank(request.getBank())) {
            sql += "AND m.bank LIKE '" + "%" + request.getBank() + "%'\n";
        }
        sql += groupClause;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        HashMap<String, TenderWiseData> map = new HashMap<>();
        jdbcTemplate.query(sql, parameters, (resultSet, rowNum) -> {
            if (!map.containsKey(resultSet.getString("payment_type"))) {
                TenderWiseData tenderWiseData = new TenderWiseData();
                tenderWiseData.setTenderName(resultSet.getString("payment_type"));
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(resultSet.getString("payment_type"), tenderWiseData);
            }
            TenderWiseData tender = map.get(resultSet.getString("payment_type"));
            BankWiseData bankWiseData = new BankWiseData();
            bankWiseData.setBankName(resultSet.getString("bank"));
            bankWiseData.setSales(resultSet.getDouble("sales_amount"));
            bankWiseData.setReceipts(resultSet.getDouble("receipt_amount"));
            bankWiseData.setDifference(resultSet.getDouble("difference"));
            bankWiseData.setCharges(resultSet.getDouble("charges"));
            bankWiseData.setReconciled(resultSet.getDouble("reconciled"));
            tender.getBankWiseDataList().add(bankWiseData);
            //
            tender.setSales(tender.getSales() + bankWiseData.getSales());
            tender.setReceipts(tender.getReceipts() + bankWiseData.getReceipts());
            tender.setDifference(tender.getDifference() + bankWiseData.getDifference());
            tender.setCharges(tender.getCharges() + bankWiseData.getCharges());
            tender.setReconciled(tender.getReconciled() + bankWiseData.getReconciled());
            map.put(resultSet.getString("payment_type"), tender);
            return null;
        });
        DashboardDataResponse response = new DashboardDataResponse();
        response.setTenderWiseDataList(new ArrayList<>());
        for (TenderWiseData tenderWiseData : map.values()) {
            response.setSales(response.getSales() + tenderWiseData.getSales());
            response.setReceipts(response.getReceipts() + tenderWiseData.getReceipts());
            response.setDifference(response.getDifference() + tenderWiseData.getDifference());
            response.setCharges(response.getCharges() + tenderWiseData.getCharges());
            response.setReconciled(response.getReconciled() + tenderWiseData.getReconciled());
            response.getTenderWiseDataList().add(tenderWiseData);
        }
        return response;
    }

    @Override
    @TrackExecutionTime
    public List<MprData> getMprDownload(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n" +
                " mpr.*\n" +
                "FROM\n" +
                "    mpr mpr\n" +
                "WHERE\n" +
                "    mpr.settled_date BETWEEN :startDate AND :endDate";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND mpr.store_id IN (:storeList)";
            parameters.addValue("storeList", request.getStores());
        }
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += " AND mpr.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        if (!StringUtils.isEmpty(request.getBank())) {
            QUERY += " AND mpr.bank = :bank";
            parameters.addValue("bank", request.getBank());
        }
        log.info("MPRvsTRm Sale: {} , Parameters: {}", QUERY, parameters);
        List<MprData> result = new ArrayList<>();
        jdbcTemplate.query(QUERY, parameters, (rs -> {
            MprData mprData = new MprData();
            mprData.setId(rs.getString("id"));
            mprData.setPaymentType(rs.getString("payment_type"));
            mprData.setBank(rs.getString("bank"));
            mprData.setTid(rs.getString("tid"));
            mprData.setMid(rs.getString("mid"));
            mprData.setStoreId(rs.getString("store_id"));
            mprData.setMprAmount(rs.getDouble("mpr_amount"));
            mprData.setCommission(rs.getDouble("commission"));
            mprData.setSettledAmount(rs.getDouble("settled_amount"));
            mprData.setRrnNumber(rs.getString("rrn"));
            mprData.setTransactionDate(rs.getString("transaction_date"));
            mprData.setSettlementDate(rs.getString("settled_date"));
            mprData.setCustomerVPA(rs.getString("payerva"));
            mprData.setCardNo(rs.getString("card_number"));
            mprData.setCardType(rs.getString("card_type"));
            //mprData.setCardIssuer(rs.getString("card_type"));
            //mprData.setCardNetwork(rs.getString("card_type"));
            mprData.setApprovalCode(rs.getString("auth_code"));
            mprData.setBsMatched(rs.getBoolean("bs_matched"));
            result.add(mprData);
        }));
        return result;
    }

    @Override
    @SneakyThrows
    public void getMprVsBankDataDownload(DashboardDataRequest request, OutputStream outputStream) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            getMPRSheet(request, workbook);
            getBankSheet(request, workbook);
            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    @Override
    @SneakyThrows
    public void downloadMpr(DashboardDataRequest request, OutputStream outputStream) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            getMPRSheet(request, workbook);
            workbook.write(outputStream);
            workbook.dispose();
        }
    }

    @Override
    public List<MprBankDifference> getMprBankDifference(DashboardDataRequest request) {
        return mprDao.getMprBankDifference(request.getBank(), request.getTender(), request.getStartDate(), request.getEndDate());
    }

    private void getBankSheet(DashboardDataRequest request, Workbook workbook) {
        List<BankStatementData> bankStatementDataList = bankStatementService.getBankStatementDownload(request);
        String[] headers = ReportHeaders.BankStatement.Bank_Header;
        Sheet sheet = workbook.createSheet("BankStatement");
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        for (BankStatementData bankStatementData : bankStatementDataList) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getPaymentType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getMprBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getDepositDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getSettlementDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getDepositAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getClosingBalance());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(bankStatementData.getNarration());
        }
    }

    public void getMPRSheet(DashboardDataRequest request, Workbook workbook) {
        List<MprData> mprDataList = getMprDownload(request);
        String[] headers = ReportHeaders.MPR.MPR_Header;
        Sheet sheet = workbook.createSheet("MPR");
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        for (MprData mprData : mprDataList) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getPaymentType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getBank());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getTid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getMid());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getStoreId());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getMprAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getCommission());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getSettledAmount());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getRrnNumber());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getTransactionDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getSettlementDate());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getCustomerVPA());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getCardNo());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getCardType());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.getApprovalCode());
            row.createCell(row.getPhysicalNumberOfCells()).setCellValue(mprData.isBsMatched() ? "Amount recevied in bank account" : "Amount did not credited in bank account");
        }


    }
    public String getAmexMissingTidMapping(){
        String value = String.valueOf(mprRepository.getAmexMissingTidCount())  + "/" + String.valueOf(mprRepository.getAmexTotalTidCount()) ;
        //MissingTIDMapping amexMapping = new MissingTIDMapping("CARD","AMEX",mprRepository.getAmexMissingTidCount(),mprRepository.getAmexTotalTidCount());
        return value;
    }

    public void downloadAmexMissingTidMapping(MissingTIDReportRequest request, SXSSFWorkbook workbook){
        List<String> missingTidList = mprRepository.getAmexMissingTid();
        Sheet sheet = workbook.createSheet("Tid Mappings "+request.getTender() + "_" +request.getBank());
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Store Name", "Store Code"};

        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        for (String missingTId : missingTidList) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            row.createCell(0).setCellValue(missingTId);
        }
    }

    }
