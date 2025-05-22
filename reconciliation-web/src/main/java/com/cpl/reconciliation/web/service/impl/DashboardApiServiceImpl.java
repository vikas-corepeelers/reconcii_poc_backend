package com.cpl.reconciliation.web.service.impl;

import com.cpl.core.api.exception.ApiException;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.*;
import com.cpl.reconciliation.core.response.instore.DeltaDTO;
import com.cpl.reconciliation.web.service.DashboardApiService;
import com.cpl.reconciliation.web.service.impl.instore.MprApiService;
import com.cpl.reconciliation.web.service.impl.instore.OrderApiService;
import com.cpl.reconciliation.web.service.impl.instore.TrmApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.cpl.reconciliation.web.service.impl.instore.AbstractInStoreService.createHeaderStyle;

@Data
@Slf4j
@Service
public class DashboardApiServiceImpl implements DashboardApiService {

    private final MprApiService mprApiService;
    private final TrmApiService trmApiService;
    private final OrderApiService orderApiService;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public DashboardDataResponse getDashboardDataResponse(DashboardDataRequest request) {
        HashMap<String, TenderWiseData> map = new HashMap<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        getFirstLevelMap(request, map);
        String QUERY = "WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "           t.payment_type, \n"
                + "           t.acquirer_bank, \n"
                + "           t.trm_amount AS trm_amount,\n"
                + "           m.bs_matched,\n"
                + "           m.settled_amount,\n"
                + "           m.bank_charges,\n"
                + "           CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "      AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "      AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "           t.payment_type, \n"
                + "           t.acquirer_bank, \n"
                + "           t.trm_amount AS trm_amount,\n"
                + "           m.bs_matched,\n"
                + "           m.settled_amount,\n"
                + "           m.bank_charges,\n"
                + "           CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount \n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "       AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "      AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "           t.payment_type, \n"
                + "           t.acquirer_bank, \n"
                + "           t.trm_amount AS trm_amount,\n"
                + "           m.bs_matched,\n"
                + "           m.settled_amount,\n"
                + "           m.bank_charges,\n"
                + "           CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "     AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "      AND t.store_id IN (:storeList)\n"
                + ")\n"
                + "\n"
                + "SELECT \n"
                + "    t.payment_type, \n"
                + "    t.acquirer_bank,\n"
                + "    COALESCE(SUM(t.trm_amount), 0) AS sales_amount,\n"
                + "    COALESCE(SUM(CASE WHEN c.bs_matched = true THEN c.settled_amount ELSE 0 END), 0) AS receipts_amount,\n"
                + "    COALESCE(SUM(c.bank_charges), 0) AS charges,\n"
                + "    COALESCE(SUM(c.mpr_amount), 0) as reconciled_amount,\n"
                + "    (COALESCE(SUM(t.trm_amount), 0) - COALESCE(SUM(c.mpr_amount), 0)) AS trmvsmpr,\n"
                + "    COALESCE(SUM(CASE WHEN c.bs_matched = false THEN c.settled_amount ELSE 0 END), 0) AS mprvsbank \n"
                + "FROM \n"
                + "    trm t \n"
                + "LEFT JOIN \n"
                + "    Combined c ON (c.uid = t.uid)\n"
                + "WHERE \n"
                + "    t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "    AND t.store_id IN (:storeList)\n";

        if (!CollectionUtils.isEmpty(request.getStores())) {
            parameters.addValue("storeList", request.getStores());
        }
        if (Strings.isNotBlank(request.getTender())) {
            QUERY += "AND t.payment_type = :tender\n";
            parameters.addValue("tender", request.getTender());
        }
        if (Strings.isNotBlank(request.getBank())) {
            QUERY += "AND t.acquirer_bank = :bank\n";
            parameters.addValue("bank", request.getBank());
        }
        QUERY += "GROUP BY t.payment_type , t.acquirer_bank";
        log.info("IN-Store SecondLevel:: {} , Parameters: {}", QUERY, parameters);
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            if (!map.containsKey(paymentType)) {
                TenderWiseData tenderWiseData = new TenderWiseData();
                tenderWiseData.setTenderName(paymentType);
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(paymentType, tenderWiseData);
            }
            TenderWiseData tender = map.get(paymentType);
            BankWiseData bankWiseData = new BankWiseData();
            bankWiseData.setBankName(resultSet.getString("acquirer_bank"));
            bankWiseData.setSales(resultSet.getDouble("sales_amount"));
            bankWiseData.setReceipts(resultSet.getDouble("receipts_amount"));
            bankWiseData.setCharges(resultSet.getDouble("charges"));
            bankWiseData.setReconciled(resultSet.getDouble("reconciled_amount"));
            bankWiseData.setTrmVsMpr(resultSet.getDouble("trmvsmpr"));
            bankWiseData.setMprVsBank(resultSet.getDouble("mprvsbank"));
            tender.getBankWiseDataList().add(bankWiseData);
            map.put(paymentType, tender);
            return null;
        });
        DashboardDataResponse response = new DashboardDataResponse();
        response.setTenderWiseDataList(new ArrayList<>());
        for (TenderWiseData tenderWiseData : map.values()) {
            response.setSales(response.getSales() + tenderWiseData.getSales());
            response.setReceipts(response.getReceipts() + tenderWiseData.getReceipts());
            response.setCharges(response.getCharges() + tenderWiseData.getCharges());
            response.setReconciled(response.getReconciled() + tenderWiseData.getReconciled());
            response.setPosVsTrm(response.getPosVsTrm() + tenderWiseData.getPosVsTrm());
            response.setTrmVsMpr(response.getTrmVsMpr() + tenderWiseData.getTrmVsMpr());
            response.setMprVsBank(response.getMprVsBank() + tenderWiseData.getMprVsBank());
            response.getTenderWiseDataList().add(tenderWiseData);
        }
        return response;
    }

    @Override
    public DashboardInStoreDataResponse getDashboardInStoreDataResponse(DashboardDataRequest request) {
        DashboardInStoreDataResponse response = new DashboardInStoreDataResponse();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        SourceWiseDashboardData posData = getPOSData(request);
        SourceWiseDashboardData trmData = getTRMData(request);
        response.setPosData(posData);
        response.setTrmData(trmData);
        return response;
    }

    public SourceWiseDashboardData getPOSData(DashboardDataRequest request) {
        HashMap<String, NewTenderWiseData> map = new HashMap<>();
        getFirstLevelMap1(request, map);
        SourceWiseDashboardData posData = new SourceWiseDashboardData();
        posData.setTenderWiseDataList(new ArrayList<>());
        for (NewTenderWiseData tenderWiseData : map.values()) {
            posData.setSales(posData.getSales() + tenderWiseData.getSales());
            posData.setReceipts(posData.getReceipts() + tenderWiseData.getReceipts());
            posData.setCharges(posData.getCharges() + tenderWiseData.getCharges());
            posData.setReconciled(posData.getReconciled() + tenderWiseData.getReconciled());
            posData.setPosVsTrm(posData.getPosVsTrm() + tenderWiseData.getPosVsTrm());
            posData.setTrmVsMpr(posData.getTrmVsMpr() + tenderWiseData.getTrmVsMpr());
            posData.setMprVsBank(posData.getMprVsBank() + tenderWiseData.getMprVsBank());
            posData.getTenderWiseDataList().add(tenderWiseData);
        }
        return posData;
    }

    public SourceWiseDashboardData getTRMData(DashboardDataRequest request) {
        SourceWiseDashboardData trmData = new SourceWiseDashboardData();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        HashMap<String, NewTenderWiseData> map = new HashMap<>();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n"
                + "    t.payment_type,\n"
                + "    t.acquirer_bank,\n"
                + "    COALESCE(SUM(t.trm_amount), 0) AS sales_amount,\n"
                + "    (SELECT \n"
                + "            COALESCE(SUM(settled_amount),0)\n"
                + "        FROM\n"
                + "            mpr\n"
                + "        WHERE\n"
                + "            transaction_date BETWEEN :startDate AND :endDate\n"
                + "                AND store_id IN (:storeList)\n"
                + "                AND bs_matched=true\n"
                + "                AND payment_type = t.payment_type\n"
                + "                AND bank = t.acquirer_bank) AS receipts_amount,\n"
                + "   CASE\n"
                + "        WHEN\n"
                + "            t.acquirer_bank = 'AMEX'\n"
                + "        THEN\n"
                + "           (SELECT \n"
                + "                    COALESCE(SUM(commission+service_tax), 0)\n"
                + "                FROM\n"
                + "                    mpr\n"
                + "                WHERE\n"
                + "                   transaction_date BETWEEN :startDate AND :endDate\n"
                + "                        AND store_id IN (:storeList)\n"
                + "                        AND bs_matched=true\n"
                + "                        AND payment_type = t.payment_type\n"
                + "                        AND bank = t.acquirer_bank)\n"
                + "        ELSE \n"
                + "           COALESCE(SUM(CASE WHEN m.bs_matched = TRUE THEN m.commission ELSE 0 END), 0)\n"
                + "    END AS charges,\n"
                + "    CASE\n"
                + "        WHEN\n"
                + "            t.acquirer_bank = 'AMEX'\n"
                + "        THEN\n"
                + "           (SELECT \n"
                + "                    COALESCE(SUM(mpr_amount), 0)\n"
                + "                FROM\n"
                + "                    mpr\n"
                + "                WHERE\n"
                + "                   transaction_date BETWEEN :startDate AND :endDate\n"
                + "                        AND store_id IN (:storeList)\n"
                + "                        AND bs_matched=true\n"
                + "                        AND payment_type = t.payment_type\n"
                + "                        AND bank = t.acquirer_bank)\n"
                + "        ELSE \n"
                + "           COALESCE(SUM(CASE WHEN m.bs_matched = TRUE THEN m.mpr_amount ELSE 0 END), 0)\n"
                + "    END AS reconciled_amount,\n"
                + "    (COALESCE(SUM(t.trm_amount), 0) - COALESCE(SUM(m.mpr_amount), 0)) AS trmvsmpr,\n"
                + "    (COALESCE(SUM(CASE WHEN m.bs_matched = false THEN m.settled_amount ELSE 0 END), 0)) AS mprvsbank\n"
                + "FROM\n"
                + "    subway.trm t\n"
                + "        LEFT JOIN\n"
                + "    subway.mpr m ON (m.uid = t.uid)\n"
                + "WHERE\n"
                + "    t.transaction_status = 'SUCCESS'\n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += "AND t.store_id IN (:storeList)\n";
            parameters.addValue("storeList", request.getStores());
        }
        if (Strings.isNotBlank(request.getTender())) {
            QUERY += "AND t.payment_type = :tender\n";
            parameters.addValue("tender", request.getTender());
        }
        if (Strings.isNotBlank(request.getBank())) {
            QUERY += "AND t.acquirer_bank :bank\n";
            parameters.addValue("bank", request.getBank());
        }
        QUERY += "GROUP BY t.payment_type , t.acquirer_bank";
        log.info("IN-Store SecondLevel: {} , Parameters: {}", QUERY, parameters);
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            if (!map.containsKey(paymentType)) {
                NewTenderWiseData tenderWiseData = new NewTenderWiseData();
                tenderWiseData.setTenderName(paymentType);
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(paymentType, tenderWiseData);
            }
            NewTenderWiseData tender = map.get(paymentType);

            BankWiseData bankWiseData = new BankWiseData();
            bankWiseData.setBankName(resultSet.getString("acquirer_bank"));
            bankWiseData.setSales(resultSet.getDouble("sales_amount"));
            bankWiseData.setReceipts(resultSet.getDouble("receipts_amount"));
            bankWiseData.setCharges(resultSet.getDouble("charges"));
            bankWiseData.setReconciled(resultSet.getDouble("reconciled_amount"));
            bankWiseData.setTrmVsMpr(resultSet.getDouble("trmvsmpr"));
            bankWiseData.setMprVsBank(resultSet.getDouble("mprvsbank"));
            tender.getBankWiseDataList().add(bankWiseData);
            if (bankWiseData != null) {
                tender.setSales(tender.getSales() + bankWiseData.getSales());
                tender.setReceipts(tender.getReceipts() + bankWiseData.getReceipts());
                tender.setCharges(tender.getCharges() + bankWiseData.getCharges());
                tender.setReconciled(tender.getReconciled() + bankWiseData.getReconciled());
                tender.setPosVsTrm(tender.getPosVsTrm() + bankWiseData.getPosVsTrm());
                tender.setTrmVsMpr(tender.getTrmVsMpr() + bankWiseData.getTrmVsMpr());
                tender.setMprVsBank(tender.getMprVsBank() + bankWiseData.getMprVsBank());

            }
            map.put(paymentType, tender);
            return null;
        });
        trmData.setTenderWiseDataList(new ArrayList<>());
        for (NewTenderWiseData tenderWiseData : map.values()) {
            trmData.setSales(trmData.getSales() + tenderWiseData.getSales());
            trmData.setReceipts(trmData.getReceipts() + tenderWiseData.getReceipts());
            trmData.setCharges(trmData.getCharges() + tenderWiseData.getCharges());
            trmData.setReconciled(trmData.getReconciled() + tenderWiseData.getReconciled());
            trmData.setPosVsTrm(trmData.getPosVsTrm() + tenderWiseData.getPosVsTrm());
            trmData.setTrmVsMpr(trmData.getTrmVsMpr() + tenderWiseData.getTrmVsMpr());
            trmData.setMprVsBank(trmData.getMprVsBank() + tenderWiseData.getMprVsBank());
            trmData.getTenderWiseDataList().add(tenderWiseData);
        }
        return trmData;
    }

    private ArrayList<DeltaDTO> getMPRvsBankData(DashboardDataRequest request) {
        ArrayList<DeltaDTO> deltaDTOS = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT m.payment_type, m.bank as acquirer_bank, CASE WHEN bs_matched=false THEN sum(m.settled_amount) else 0  end as MprvsBank FROM mpr m\n"
                + "WHERE m.settled_date BETWEEN :startDate AND :endDate AND m.store_id IN (:storeList) GROUP BY m.payment_type, m.bank";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            parameters.addValue("storeList", request.getStores());
        }
        System.out.println("deltaDTOS Query " + QUERY);
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            String bank = resultSet.getString("acquirer_bank").toUpperCase();
            DeltaDTO deltaDTO = new DeltaDTO(paymentType, bank, resultSet.getDouble("mprvsbank"));
            deltaDTOS.add(deltaDTO);
            return null;
        });
        return deltaDTOS;
    }

    private ArrayList<DeltaDTO> getTRMvsMPRData(DashboardDataRequest request) {
        ArrayList<DeltaDTO> deltaDTOS = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "WITH Combined AS (\n"
                + "    SELECT t.payment_type, t.acquirer_bank, \n"
                + "    m.uid as uid,\n"
                + "    m.mpr_amount AS mpr_amount\n"
                + "    FROM trm t LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "      AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN ('73036-1-0', '68891-2-0', '72467-1-0', '72518-1-0') \n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT t.payment_type, t.acquirer_bank, \n"
                + "    m.merchant_ref_id as uid, \n"
                + "    m.mpr_amount AS mpr_amount\n"
                + "    FROM trm t LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "      AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "\n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT t.payment_type, t.acquirer_bank, \n"
                + "    m.transaction_utr as uid,\n"
                + "    m.mpr_amount AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + ")\n"
                + "SELECT tm.payment_type,tm.acquirer_bank,(COALESCE(SUM(tm.trm_amount), 0) - COALESCE(SUM(c.mpr_amount), 0)) \n"
                + "AS trmvsmpr\n"
                + "FROM trm tm LEFT JOIN Combined c ON tm.uid = c.uid \n"
                + "WHERE tm.transaction_status = 'SUCCESS' \n"
                + "AND tm.transaction_date BETWEEN :startDate AND :endDate\n"
                + "AND tm.store_id IN (:storeList)\n"
                + "GROUP BY tm.payment_type, tm.acquirer_bank\n";

        if (!CollectionUtils.isEmpty(request.getStores())) {
            parameters.addValue("storeList", request.getStores());
        }
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            String bank = resultSet.getString("acquirer_bank").toUpperCase();
            DeltaDTO deltaDTO = new DeltaDTO(paymentType, bank, resultSet.getDouble("trmvsmpr"));
            deltaDTOS.add(deltaDTO);
            return null;
        });
        return deltaDTOS;
    }

    private void getFirstLevelMap1(DashboardDataRequest request, HashMap<String, NewTenderWiseData> map) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n"
                + "    t.name as payment_type,\n"
                + "    COALESCE(SUM(t.amount), 0) AS sales,\n"
                + "    (SELECT \n"
                + "            SUM(settled_amount)\n"
                + "        FROM\n"
                + "            mpr\n"
                + "        WHERE\n"
                + "            transaction_date BETWEEN :startDate AND :endDate\n"
                + "                AND store_id IN (:storeList)\n"
                + "                AND bs_matched=true\n"
                + "                AND payment_type=t.name) AS receipts,\n"
                + "    COALESCE(SUM(m.commission), 0) AS charges,\n"
                + "    (COALESCE(SUM(CASE WHEN m.bs_matched = true THEN m.mpr_amount ELSE 0 END), 0)) AS reconciled_amount,\n"
                + "    (COALESCE(SUM(t.amount), 0) - COALESCE(SUM(trm.trm_amount), 0)) AS posvstrm\n"
                + "FROM\n"
                + "    orders o\n"
                + "        JOIN\n"
                + "    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n"
                + "        LEFT JOIN\n"
                + "    trm trm ON (o.transaction_number = trm.cloud_ref_id AND trm.transaction_status = 'SUCCESS' AND (t.rrn = trm.rrn || (t.name='Card' AND trm.acquirer_bank = 'AMEX')))\n"
                + "        LEFT JOIN\n"
                + "    mpr m ON (m.uid = trm.uid AND m.bs_matched=true)\n"
                + "WHERE\n"
                + "    o.order_date BETWEEN :startDate AND :endDate\n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += "AND o.store_id IN (:storeList)\n";
            parameters.addValue("storeList", request.getStores());
        }
        QUERY += "AND o.tender_name IS NOT NULL\n"
                + "AND o.invoice_number IS NOT NULL\n"
                + "AND o.order_status = 'Paid'\n";
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += "AND t.name = :tender\n";
            parameters.addValue("tender", request.getTender());
        }
        QUERY += "GROUP BY t.name";
        log.info("IN-Store FirstLevel: {} , Parameters: {}", QUERY, parameters);
        ArrayList<DeltaDTO> deltaDTOS = getMPRvsBankData(request);
        ArrayList<DeltaDTO> trMvsMPRDataDTOS = getTRMvsMPRData(request);
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            if (!map.containsKey(paymentType)) {
                NewTenderWiseData tenderWiseData = new NewTenderWiseData();
                tenderWiseData.setTenderName(paymentType);
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(paymentType, tenderWiseData);
            }
            NewTenderWiseData tender = map.get(paymentType);
            tender.setSales(resultSet.getDouble("sales"));
            tender.setReceipts(resultSet.getDouble("receipts"));
            tender.setCharges(resultSet.getDouble("charges"));
            tender.setReconciled(resultSet.getDouble("reconciled_amount"));
            tender.setPosVsTrm(resultSet.getDouble("posvstrm"));
            double trmvsMpr = trMvsMPRDataDTOS.stream().filter(deltaDTO -> deltaDTO.getPaymentType().equals(paymentType)).mapToDouble(DeltaDTO::getDelta).sum();
            tender.setTrmVsMpr(trmvsMpr);
            //tender.setTrmVsMpr(resultSet.getDouble("trmvsmpr"));
            //tender.setMprVsBank(resultSet.getDouble("mprvsbank"));
            double mprvsBank = deltaDTOS.stream().filter(deltaDTO -> deltaDTO.getPaymentType().equals(paymentType)).mapToDouble(DeltaDTO::getDelta).sum();
            map.put(paymentType, tender);
            tender.setMprVsBank(mprvsBank);
            return null;
        });
    }

    private void getFirstLevelMap(DashboardDataRequest request, HashMap<String, TenderWiseData> map) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "\t\t   m.bank_charges AS bank_charges,\n"
                + "\t\t   m.bs_matched AS bs_matched,\n"
                + "           t.payment_type, \n"
                + "           t.acquirer_bank, \n"
                + "           CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "      AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "      AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "           m.bank_charges AS bank_charges,\n"
                + "\t\t   m.bs_matched AS bs_matched,\n"
                + "           t.payment_type, \n"
                + "           t.acquirer_bank, \n"
                + "           CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "       AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "      AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "\t\t   m.bank_charges AS bank_charges,\n"
                + "\t\t\tm.bs_matched AS bs_matched,\n"
                + "           t.payment_type, \n"
                + "           t.acquirer_bank, \n"
                + "           CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "     AND t.transaction_date BETWEEN :startDate AND :endDate \n"
                + "      AND t.store_id IN (:storeList)\n"
                + ")\n"
                + "\n"
                + "SELECT \n"
                + "    t.name as payment_type, \n"
                + "    COALESCE(SUM(t.amount), 0) AS sales,\n"
                + "    (SELECT SUM(settled_amount)\n"
                + "     FROM mpr\n"
                + "     WHERE transaction_date BETWEEN :startDate AND :endDate  \n"
                + "       AND store_id IN (:storeList)\n"
                + "       AND bs_matched=true\n"
                + "       AND payment_type=t.name) AS receipts,\n"
                + "    COALESCE(SUM(c.bank_charges), 0) AS charges,\n"
                + "    (COALESCE(SUM(CASE WHEN c.bs_matched = true THEN c.mpr_amount ELSE 0 END), 0)) AS reconciled_amount,\n"
                + "    (COALESCE(SUM(t.amount), 0) - COALESCE(SUM(tm.trm_amount), 0)) AS posvstrm\n"
                + "FROM\n"
                + "    orders o\n"
                + "    JOIN tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n"
                + "    LEFT JOIN trm tm ON (\n"
                + "        o.transaction_number = tm.cloud_ref_id \n"
                + "        AND tm.transaction_status = 'SUCCESS'\n"
                + "    )\n"
                + "    LEFT JOIN Combined c ON (\n"
                + "        c.uid = tm.uid\n"
                + "    )\n"
                + "WHERE \n"
                + "    o.order_date BETWEEN :startDate AND :endDate \n"
                + "    AND o.store_id IN (:storeList)\n"
                + "    AND o.tender_name IS NOT NULL \n"
                + "    AND o.invoice_number IS NOT NULL \n"
                + "    AND o.order_status = 'Paid' \n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            parameters.addValue("storeList", request.getStores());
        }
        if (request.getTender() != null && !request.getTender().isEmpty()) {
            QUERY += "AND t.name = :tender\n";
            parameters.addValue("tender", request.getTender());
        }
        QUERY += "GROUP BY t.name";
        log.info("IN-Store FirstLevel: {} , Parameters: {}", QUERY, parameters);
        ArrayList<DeltaDTO> deltaDTOS = getMPRvsBankData(request);
        ArrayList<DeltaDTO> trMvsMPRDataDTOS = getTRMvsMPRData(request);
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            if (!map.containsKey(paymentType)) {
                TenderWiseData tenderWiseData = new TenderWiseData();
                tenderWiseData.setTenderName(paymentType);
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(paymentType, tenderWiseData);
            }
            TenderWiseData tender = map.get(paymentType);
            tender.setSales(resultSet.getDouble("sales"));
            tender.setReceipts(resultSet.getDouble("receipts"));
            tender.setCharges(resultSet.getDouble("charges"));
            tender.setReconciled(resultSet.getDouble("reconciled_amount"));
            tender.setPosVsTrm(resultSet.getDouble("posvstrm"));
            double trmvsMpr = trMvsMPRDataDTOS.stream().filter(deltaDTO -> deltaDTO.getPaymentType().equals(paymentType)).mapToDouble(DeltaDTO::getDelta).sum();
            tender.setTrmVsMpr(trmvsMpr);
            //tender.setTrmVsMpr(resultSet.getDouble("trmvsmpr"));
            //tender.setMprVsBank(resultSet.getDouble("mprvsbank"));
            double mprvsBank = deltaDTOS.stream().filter(deltaDTO -> deltaDTO.getPaymentType().equals(paymentType)).mapToDouble(DeltaDTO::getDelta).sum();
            tender.setMprVsBank(mprvsBank);
            map.put(paymentType, tender);
            return null;
        });
    }

    private void getFirstLevelMapSummaryTablesBased(DashboardDataRequest request, HashMap<String, TenderWiseData> map) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        String QUERY = "SELECT tender at payment_type,SUM(sales),SUM(receipts),SUM(charges),SUM(reconciled_amount),"
                + "SUM(posvstrm) FROM \n"
                + "instore_posvstrm_firstlevel_dashboard_data WHERE (date BETWEEN :startDate AND :endDate) AND "
                + "store_code IN (:storeList) GROUP BY tender";

        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());

        if (!CollectionUtils.isEmpty(request.getStores())) {
            parameters.addValue("storeList", request.getStores());
        }

        log.info("IN-Store FirstLevel: {} , Parameters: {}", QUERY, parameters);
        ArrayList<DeltaDTO> deltaDTOS = getMPRvsBankData(request);
        ArrayList<DeltaDTO> trMvsMPRDataDTOS = getTRMvsMPRData(request);

        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            if (!map.containsKey(paymentType)) {
                TenderWiseData tenderWiseData = new TenderWiseData();
                tenderWiseData.setTenderName(paymentType);
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(paymentType, tenderWiseData);
            }
            TenderWiseData tender = map.get(paymentType);
            tender.setSales(resultSet.getDouble("sales"));
            tender.setReceipts(resultSet.getDouble("receipts"));
            tender.setCharges(resultSet.getDouble("charges"));
            tender.setReconciled(resultSet.getDouble("reconciled_amount"));
            tender.setPosVsTrm(resultSet.getDouble("posvstrm"));
            double trmvsMpr = trMvsMPRDataDTOS.stream().filter(deltaDTO -> deltaDTO.getPaymentType().equals(paymentType)).mapToDouble(DeltaDTO::getDelta).sum();
            tender.setTrmVsMpr(trmvsMpr);
            //tender.setTrmVsMpr(resultSet.getDouble("trmvsmpr"));
            //tender.setMprVsBank(resultSet.getDouble("mprvsbank"));
            double mprvsBank = deltaDTOS.stream().filter(deltaDTO -> deltaDTO.getPaymentType().equals(paymentType)).mapToDouble(DeltaDTO::getDelta).sum();
            tender.setMprVsBank(mprvsBank);
            map.put(paymentType, tender);
            return null;
        });
    }

    @Override
    public List<ReportingTendersDataResponse> getReportingTenders() {
        String sql = "SELECT category,display_name,technical_name FROM reconcii.reporting_tenders;";
        Map<String, Object> params = new HashMap<>();
        HashMap<String, ReportingTendersDataResponse> map = new HashMap<>();
        jdbcTemplate.query(sql, params, (resultSet, rowNum) -> {
            String category = resultSet.getString("category");
            ReportingTendersDataResponse response;
            if (!map.containsKey(category)) {
                response = new ReportingTendersDataResponse();
                response.setCategory(category);
            } else {
                response = map.get(category);
            }
            response.addTender(resultSet.getString("display_name"), resultSet.getString("technical_name"));
            map.put(category, response);
            return null;
        });
        return map.values().stream().toList();
    }

    @Override
    public List<ReceiptData> receiptsData(DashboardDataRequest request) {
        String QUERY = "SELECT \n"
                + "    *\n"
                + "FROM\n"
                + "    bank_statements\n"
                + "WHERE\n"
                + "    expected_actual_transaction_date BETWEEN :startDate AND :endDate\n";

        if (Strings.isNotBlank(request.getTender())) {
            QUERY += "AND payment_type='" + request.getTender() + "'\n";
        }
        if (Strings.isNotBlank(request.getBank())) {
            QUERY += "AND source_bank='" + request.getBank() + "'\n";
        }
        System.out.println(QUERY);
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        List<ReceiptData> receiptDataList = new LinkedList<>();
        ReceiptData receiptDataTotal = new ReceiptData();
        receiptDataTotal.setPaymentType("Total");
        receiptDataList.add(receiptDataTotal);
        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            ReceiptData receiptData = new ReceiptData();
            receiptData.setPaymentType(rs.getString("Payment_Type"));
            receiptData.setAcquirerBank(rs.getString("source_bank"));
            receiptData.setDepositBank(rs.getString("bank"));
            receiptData.setDepositAmount(rs.getDouble("deposit_amt"));
            receiptData.setValueDate(rs.getString("date"));
            receiptData.setNarration(rs.getString("narration"));
            receiptDataTotal.setDepositAmount(receiptDataTotal.getDepositAmount() + receiptData.getDepositAmount());
            receiptDataList.add(receiptData);
        });
        return receiptDataList;
    }

    @Override
    public void receiptsDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<ReceiptData> receiptDataList = receiptsData(request);
        String header = "Tender,Acquirer Bank,Deposit Bank,Deposit Amount,Value Date,Narration";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet sheet = workbook.createSheet("Receipts");
            Row headerRow = sheet.createRow(0);
            String[] headers = header.split(",");
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            for (ReceiptData receiptData : receiptDataList) {
                Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(receiptData.getPaymentType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(receiptData.getAcquirerBank());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(receiptData.getDepositBank());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(receiptData.getDepositAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(receiptData.getValueDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(receiptData.getNarration());
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (Exception e) {
            throw new ApiException("Error Occurred while downloading report");
        }
    }

    public List<ChargesData> getChargesData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
//        String QUERY = "WITH Combined AS (\n"
//                + "    SELECT m.uid AS uid,\n"
//                + "    m.card_category,\n"
//                + "    m.card_type,\n"
//                + "    m.settled_amount,\n"
//                + "    m.commission,\n"
//                + "    m.bs_matched,\n"
//                + "    CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
//                + "    FROM trm t \n"
//                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
//                + "    WHERE t.transaction_status = 'SUCCESS' \n"
//                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "    AND t.store_id IN (:storeList)\n"
//                + "    \n"
//                + "    UNION ALL\n"
//                + "    \n"
//                + "    SELECT m.merchant_ref_id AS uid,\n"
//                + "    m.card_category,\n"
//                + "    m.card_type,\n"
//                + "    m.settled_amount,\n"
//                + "    m.commission,\n"
//                + "    m.bs_matched,\n"
//                + "    CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
//                + "    FROM trm t \n"
//                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
//                + "    WHERE t.transaction_status = 'SUCCESS' \n"
//                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "    AND t.store_id IN (:storeList)\n"
//                + "    \n"
//                + "    UNION ALL\n"
//                + "    \n"
//                + "    SELECT m.transaction_utr AS uid,\n"
//                + "    m.card_category,\n"
//                + "    m.card_type,\n"
//                + "    m.settled_amount,\n"
//                + "    m.commission,\n"
//                + "    m.bs_matched,\n"
//                + "    CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
//                + "    FROM trm t \n"
//                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
//                + "    WHERE t.transaction_status = 'SUCCESS' \n"
//                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "    AND t.store_id IN (:storeList)\n"
//                + ") \n"
//                + "SELECT \n"
//                + "    trm.acquirer_bank AS Bank,\n"
//                + "    o.tender_name AS Payment_Type,\n" /*t.payment_type is changed to o.tender_name*/
//                + "    o.business_date AS Business_date,\n"
//                + "    o.store_id AS Store_Id,\n"
//                + "    o.sale_type AS Sale_Type,\n"
//                + "    o.invoice_number AS Invoice_Number,\n"
//                + "    0 AS RRN_Number,\n"                    /*t.rrn is changed to 0*/
//                + "    o.total_amount AS Sale_Amount,\n"
//                + "    o.total_tax AS Sale_Tax,\n"
//                + "    o.total_amount AS Tender_Amount,\n"    /*t.amount is changed to o.total_amount*/
//                + "    trm.trm_amount AS TRM_Amount,\n"
//                + "    c.card_category AS Card_Type,\n"
//                + "    c.card_type AS Card_Network,\n"
//                + "    c.mpr_amount AS MPR_Amount,\n"
//                + "    c.settled_amount AS Settled_Amount,\n"
//                + "    c.commission AS Charges\n"
//                + "FROM\n"
//                + "    orders o\n"
////                + "        JOIN\n"
////                + "    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n"
//                + "    LEFT JOIN\n"
//                + "    trm trm ON (o.transaction_number = trm.cloud_ref_id AND trm.transaction_status = 'SUCCESS')\n"
//                + "        LEFT JOIN Combined c ON trm.uid = c.uid\n"
//                + "        WHERE  o.order_date BETWEEN :startDate AND :endDate\n";
        String QUERY = "WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "    m.card_category,\n"
                + "    m.card_type,\n"
                + "    m.settled_amount,\n"
                + "    m.bank_charges,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "    m.card_category,\n"
                + "    m.card_type,\n"
                + "    m.settled_amount,\n"
                + "    m.bank_charges,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "    m.card_category,\n"
                + "    m.card_type,\n"
                + "    m.settled_amount,\n"
                + "    m.bank_charges,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + ") \n"
                + "SELECT \n"
                + "    trm.acquirer_bank AS Bank,\n"
                + "    trm.payment_type AS Payment_Type,\n" /*t.payment_type is changed to o.tender_name*/
                + "    Date(trm.transaction_date) AS Business_date,\n"
                + "    trm.store_id AS Store_Id,\n"
                + "    o.sale_type AS Sale_Type,\n"
                + "    o.invoice_number AS Invoice_Number,\n"
                + "    trm.rrn AS RRN_Number,\n" /*t.rrn is changed to 0*/
                + "    o.total_amount AS Sale_Amount,\n"
                + "    o.total_tax AS Sale_Tax,\n"
                + "    o.total_amount AS Tender_Amount,\n" /*t.amount is changed to o.total_amount*/
                + "    trm.trm_amount AS TRM_Amount,\n"
                + "    c.card_category AS Card_Type,\n"
                + "    c.card_type AS Card_Network,\n"
                + "    c.mpr_amount AS MPR_Amount,\n"
                + "    c.settled_amount AS Settled_Amount,\n"
                + "    c.bank_charges AS Charges\n"
                + "FROM\n"
                + " trm trm LEFT JOIN Combined c ON (trm.uid = c.uid) LEFT JOIN orders o ON (o.transaction_number = trm.cloud_ref_id)\n"
                + " WHERE trm.transaction_status = 'SUCCESS' AND (trm.transaction_date BETWEEN :startDate AND :endDate)\n";
        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND trm.store_id IN (:storeList)\n";
            parameters.addValue("storeList", request.getStores());
        }
        // QUERY += "AND o.tender_name IS NOT NULL\n"
        //         + "AND o.invoice_number IS NOT NULL\n"
        //         + "AND o.order_status = 'Paid'\n"
        //         + "AND trm.trm_amount IS NOT NULL\n"
        //         + "AND c.mpr_amount IS NOT NULL\n"
        //         + "AND c.bs_matched=true\n";
        if (request.getTender() != null && !request.getTender().isEmpty()) {
            //QUERY += "AND t.name = :tender";
            QUERY += " AND trm.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        log.info("Charges Data: {} , Parameters: {}", QUERY, parameters);
        List<ChargesData> chargesDataList = new LinkedList<>();
        ChargesData chargesDataTotal = new ChargesData();
        chargesDataTotal.setPaymentType("Total");
        chargesDataList.add(chargesDataTotal);
        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            ChargesData chargesData = new ChargesData();
            chargesData.setBank(rs.getString("Bank"));
            chargesData.setPaymentType(rs.getString("Payment_Type"));
            chargesData.setBusinessDate(rs.getString("Business_date"));
            chargesData.setStoreId(rs.getString("Store_Id"));
            chargesData.setSaleType(rs.getString("Sale_Type"));
            chargesData.setInvoiceNumber(rs.getString("Invoice_Number"));
            chargesData.setRrnNumber(rs.getString("RRN_Number"));
            //
            chargesData.setSaleAmount(rs.getDouble("Sale_Amount"));
            chargesData.setSaleTax(rs.getDouble("Sale_Tax"));
            chargesData.setTenderAmount(rs.getDouble("Tender_Amount"));
            chargesData.setTrmAmount(rs.getDouble("TRM_Amount"));
            chargesData.setMprAmount(rs.getDouble("MPR_Amount"));
            chargesData.setSettledAmount(rs.getDouble("Settled_Amount"));
            chargesData.setCharges(rs.getDouble("Charges"));
            chargesData.setCardType(rs.getString("Card_Type"));
            chargesData.setCardNetwork(rs.getString("Card_Network"));
            //
            chargesDataTotal.setSaleAmount(chargesDataTotal.getSaleAmount() + chargesData.getSaleAmount());
            chargesDataTotal.setSaleTax(chargesDataTotal.getSaleTax() + chargesData.getSaleTax());
            chargesDataTotal.setTenderAmount(chargesDataTotal.getTenderAmount() + chargesData.getTenderAmount());
            chargesDataTotal.setTrmAmount(chargesDataTotal.getTrmAmount() + chargesData.getTrmAmount());
            chargesDataTotal.setMprAmount(chargesDataTotal.getMprAmount() + chargesData.getMprAmount());
            chargesDataTotal.setSettledAmount(chargesDataTotal.getSettledAmount() + chargesData.getSettledAmount());
            chargesDataTotal.setCharges(chargesDataTotal.getCharges() + chargesData.getCharges());
            chargesDataList.add(chargesData);
        });
        return chargesDataList;
    }

    @Override
    public void chargesDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<ChargesData> chargesDataList = getChargesData(request);
        String header = "Tender,Bank,BusinessDate,Store,SaleType,Invoice Number,RRN Number,Sales Amount,Sales Tax,Tender Amount,TRM Amount,MPR Amount,Settled_Amount,Charges,Card_Type,Card_Network";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet sheet = workbook.createSheet("Charges");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = header.split(",");
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            for (ChargesData chargesData : chargesDataList) {
                Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getPaymentType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getBank());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getStoreId());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getSaleType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getInvoiceNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getRrnNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getSaleAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getSaleTax());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getTenderAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getTrmAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getMprAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getSettledAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getCharges());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getCardType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(chargesData.getCardNetwork());
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (Exception e) {
            throw new ApiException("Error Occurred while downloading Charges Report!");
        }
    }

    @Override
    public List<ReconciledData> getReconciledData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
//        String QUERY = "WITH Combined AS (\n"
//                + "    SELECT m.uid AS uid,\n"
//                + "    m.settled_amount,\n"
//                + "    m.commission,\n"
//                + "    m.bs_matched,\n"
//                + "    CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
//                + "    FROM trm t \n"
//                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
//                + "    WHERE t.transaction_status = 'SUCCESS' \n"
//                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "    AND t.store_id IN (:storeList)\n"
//                + "    \n"
//                + "    UNION ALL\n"
//                + "    \n"
//                + "    SELECT m.merchant_ref_id AS uid,\n"
//                + "    m.settled_amount,\n"
//                + "    m.commission,\n"
//                + "    m.bs_matched,\n"
//                + "    CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
//                + "    FROM trm t \n"
//                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
//                + "    WHERE t.transaction_status = 'SUCCESS' \n"
//                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "    AND t.store_id IN (:storeList)\n"
//                + "    \n"
//                + "    UNION ALL\n"
//                + "    \n"
//                + "    SELECT m.transaction_utr AS uid,\n"
//                + "    m.settled_amount,\n"
//                + "    m.commission,\n"
//                + "    m.bs_matched,\n"
//                + "    CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
//                + "    FROM trm t \n"
//                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
//                + "    WHERE t.transaction_status = 'SUCCESS' \n"
//                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
//                + "    AND t.store_id IN (:storeList)\n"
//                + ") \n"
//                + "SELECT trm.acquirer_bank AS Bank,\n"
//                + "    t.name AS Payment_Type,\n"
//                + "    o.business_date AS Business_date,\n"
//                + "    o.store_id AS Store_Id,\n"
//                + "    o.sale_type AS Sale_Type,\n"
//                + "    o.invoice_number AS Invoice_Number,\n"
//                + "    t.rrn AS RRN_Number,\n"
//                + "    o.total_amount AS Sale_Amount,\n"
//                + "    o.total_tax AS Sale_Tax,\n"
//                + "    t.amount AS Tender_Amount,\n"
//                + "    trm.trm_amount AS TRM_Amount,\n"
//                + "    c.mpr_amount AS MPR_Amount,\n"
//                + "    c.settled_amount AS Settled_Amount,\n"
//                + "    c.commission AS Charges\n"
//                + "FROM\n"
//                + "    orders o\n"
//                + "    JOIN\n"
//                + "    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n"
//                + "    LEFT JOIN\n"
//                + "    trm trm ON (o.transaction_number = trm.cloud_ref_id  AND trm.transaction_status = 'SUCCESS') \n"
//                + "    LEFT JOIN \n"
//                + "    Combined c ON (trm.uid = c.uid) "
//                + "WHERE  o.order_date BETWEEN :startDate AND :endDate\n";

        String QUERY = "WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "    m.settled_amount,\n"
                + "    m.bank_charges,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "    m.settled_amount,\n"
                + "    m.bank_charges,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "    m.settled_amount,\n"
                + "    m.bank_charges,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + ") \n"
                + "SELECT trm.acquirer_bank AS Bank,\n"
                + "    trm.payment_type AS Payment_Type,\n"
                + "    Date(trm.transaction_date) AS Business_date,\n"
                + "    trm.store_id AS Store_Id,\n"
                + "    o.sale_type AS Sale_Type,\n"
                + "    o.invoice_number AS Invoice_Number,\n"
                + "    trm.rrn AS RRN_Number,\n"
                + "    o.total_amount AS Sale_Amount,\n"
                + "    o.total_tax AS Sale_Tax,\n"
                + "    o.total_amount AS Tender_Amount,\n"
                + "    trm.trm_amount AS TRM_Amount,\n"
                + "    c.mpr_amount AS MPR_Amount,\n"
                + "    c.settled_amount AS Settled_Amount,\n"
                + "    c.bank_charges AS Charges\n"
                + "FROM\n"
                + " trm trm LEFT JOIN Combined c ON (trm.uid = c.uid) LEFT JOIN orders o ON (o.transaction_number = trm.cloud_ref_id)\n"
                + " WHERE trm.transaction_status = 'SUCCESS' AND (trm.transaction_date BETWEEN :startDate AND :endDate)\n";

        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += " AND trm.store_id IN (:storeList)\n";
            parameters.addValue("storeList", request.getStores());
        }
        QUERY += " AND c.bs_matched=true\n";
//        QUERY += "AND o.tender_name IS NOT NULL\n"
//                + "AND o.invoice_number IS NOT NULL\n"
//                + "AND o.order_status = 'Paid'\n"
//                + "AND trm.trm_amount IS NOT NULL\n"
//                + "AND c.mpr_amount IS NOT NULL\n"
//                + "AND c.bs_matched=true\n";

        if (!StringUtils.isEmpty(request.getTender())) {
//            QUERY += "AND t.name = :tender";
            QUERY += " AND trm.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        log.info("Reconciled Data: {} , Parameters: {}", QUERY, parameters);
        List<ReconciledData> reconciledDataList = new LinkedList<>();
        ReconciledData reconciledDataTotal = new ReconciledData();
        reconciledDataTotal.setPaymentType("Total");
        reconciledDataList.add(reconciledDataTotal);
        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            ReconciledData reconciledData = new ReconciledData();
            reconciledData.setBank(rs.getString("Bank"));
            reconciledData.setPaymentType(rs.getString("Payment_Type"));
            reconciledData.setBusinessDate(rs.getString("Business_date"));
            reconciledData.setStoreId(rs.getString("Store_Id"));
            reconciledData.setSaleType(rs.getString("Sale_Type"));
            reconciledData.setInvoiceNumber(rs.getString("Invoice_Number"));
            reconciledData.setRrnNumber(rs.getString("RRN_Number"));
            //
            reconciledData.setSaleAmount(rs.getDouble("Sale_Amount"));
            reconciledData.setSaleTax(rs.getDouble("Sale_Tax"));
            reconciledData.setTenderAmount(rs.getDouble("Tender_Amount"));
            reconciledData.setTrmAmount(rs.getDouble("TRM_Amount"));
            reconciledData.setMprAmount(rs.getDouble("MPR_Amount"));
            reconciledData.setSettledAmount(rs.getDouble("Settled_Amount"));
            reconciledData.setCharges(rs.getDouble("Charges"));
            //
            reconciledDataTotal.setSaleAmount(reconciledDataTotal.getSaleAmount() + reconciledData.getSaleAmount());
            reconciledDataTotal.setSaleTax(reconciledDataTotal.getSaleTax() + reconciledData.getSaleTax());
            reconciledDataTotal.setTenderAmount(reconciledDataTotal.getTenderAmount() + reconciledData.getTenderAmount());
            reconciledDataTotal.setTrmAmount(reconciledDataTotal.getTrmAmount() + reconciledData.getTrmAmount());
            reconciledDataTotal.setMprAmount(reconciledDataTotal.getMprAmount() + reconciledData.getMprAmount());
            reconciledDataTotal.setSettledAmount(reconciledDataTotal.getSettledAmount() + reconciledData.getSettledAmount());
            reconciledDataTotal.setCharges(reconciledDataTotal.getCharges() + reconciledData.getCharges());
            reconciledDataList.add(reconciledData);
        });
        return reconciledDataList;
    }

    @Override
    public void reconciledDownload(DashboardDataRequest request, OutputStream outputStream) {
        List<ReconciledData> reconciledDataList = getReconciledData(request);
        String header = "Tender,Bank,BusinessDate,Store,SaleType,Invoice Number,RRN Number,Sales Amount,Sales Tax,Tender Amount,TRM Amount,MPR Amount,Settled_Amount,Charges";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet sheet = workbook.createSheet("Reconciled");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = header.split(",");
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            for (ReconciledData reconciledData : reconciledDataList) {
                Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getPaymentType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getBank());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getStoreId());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getSaleType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getInvoiceNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getRrnNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getSaleAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getSaleTax());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getTenderAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getTrmAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getMprAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getSettledAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reconciledData.getCharges());
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (Exception e) {
            throw new ApiException("Error Occurred while downloading Reconciled Report!");
        }
    }

    @Override
    public UnreconciledDataWrapper getUnreconciledData(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
//        String QUERY = "SELECT\n"
//                + "    t.name AS Payment_Type,\n"
//                + "    trm.acquirer_bank AS Bank,\n"
//                + "    o.business_date AS Business_date,\n"
//                + "    o.store_id AS Store_Id,\n"
//                + "    o.sale_type AS Sale_Type,\n"
//                + "    o.invoice_number AS Invoice_Number,\n"
//                + "    t.rrn AS RRN_Number,\n"
//                + "    o.total_amount AS Sale_Amount,\n"
//                + "    o.total_tax AS Sale_Tax,\n"
//                + "    t.amount AS Tender_Amount,\n"
//                + "    trm.trm_amount AS TRM_Amount,\n"
//                + "    m.mpr_amount AS MPR_Amount,\n"
//                + "    m.bs_matched AS BS_Matched\n"
//                + "FROM\n"
//                + "    orders o\n"
//                + "        JOIN\n"
//                + "    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n"
//                + "        LEFT JOIN\n"
//                + "    trm trm ON (o.transaction_number = trm.cloud_ref_id  AND trm.transaction_status = 'SUCCESS' AND (t.rrn = trm.rrn || (t.name='Card' AND trm.acquirer_bank = 'AMEX')))\n"
//                + "        LEFT JOIN\n"
//                + "    mpr m ON (((trm.acquirer_bank='PHONEPE' AND (trm.uid = m.merchant_ref_id OR (trm.uid = m.transaction_utr))) \n"
//                + "OR (trm.acquirer_bank = 'YES' AND trm.uid = m.uid) OR (trm.acquirer_bank = 'AMEX')))\n"
//                + "WHERE\n"
//                + "    o.order_date BETWEEN :startDate AND :endDate\n";
        String QUERY = "WITH Combined AS (\n"
                + "    SELECT m.uid AS uid,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.uid IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.merchant_ref_id AS uid,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.merchant_ref_id IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT m.transaction_utr AS uid,\n"
                + "    m.bs_matched,\n"
                + "    CASE WHEN m.transaction_utr IS NOT NULL THEN m.mpr_amount ELSE 0 END AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date BETWEEN :startDate AND :endDate\n"
                + "    AND t.store_id IN (:storeList)\n"
                + ") \n"
                + "SELECT\n"
                //+ "   t.name AS Payment_Type,\n"
                + "   o.tender_name AS Payment_Type,\n"
                + "    trm.acquirer_bank AS Bank,\n"
                + "    o.business_date AS Business_date,\n"
                + "    o.store_id AS Store_Id,\n"
                + "    o.sale_type AS Sale_Type,\n"
                + "    o.invoice_number AS Invoice_Number,\n"
                //     + "  t.rrn AS RRN_Number,\n"
                + "  trm.rrn AS RRN_Number,\n"
                + "    o.total_amount AS Sale_Amount,\n"
                + "    o.total_tax AS Sale_Tax,\n"
                //  + "    t.amount AS Tender_Amount,\n"
                + "    o.total_amount AS Tender_Amount,\n"
                + "    trm.trm_amount AS TRM_Amount,\n"
                + "    c.mpr_amount AS MPR_Amount,\n"
                + "    c.bs_matched AS BS_Matched\n"
                + "FROM\n"
                + "    orders o\n"
                //+ "    JOIN\n"
                //+ "    tender t ON (o.id = t.order_id AND t.name IN ('Card' , 'UPI'))\n"
                + "    LEFT JOIN\n"
                + "    trm trm ON (o.transaction_number = trm.cloud_ref_id  AND trm.transaction_status = 'SUCCESS')\n"
                + "    LEFT JOIN\n"
                + "    Combined c ON trm.uid = c.uid\n"
                + "WHERE o.order_date BETWEEN :startDate AND :endDate\n";

        if (!CollectionUtils.isEmpty(request.getStores())) {
            QUERY += "AND o.store_id IN (:storeList)\n";
            parameters.addValue("storeList", request.getStores());
        }

        QUERY += "AND o.tender_name IS NOT NULL\n"
                + "AND o.invoice_number IS NOT NULL\n"
                + "AND o.order_status = 'Paid'\n"
                + "AND (trm.trm_amount IS NULL OR c.mpr_amount IS NULL OR c.bs_matched=false)\n";

        if (!StringUtils.isEmpty(request.getTender())) {
            //  QUERY += "AND t.name = :tender";
            QUERY += "AND o.tender_name = :tender";
            parameters.addValue("tender", request.getTender());
        }
        log.info("Unreconciled Data: {} , Parameters: {}", QUERY, parameters);
        UnreconciledDataWrapper unreconciledDataWrapper = new UnreconciledDataWrapper();
        AtomicReference<Double> totalSaleAmt = new AtomicReference<>(0.0);
        AtomicReference<Double> totalTaxAmt = new AtomicReference<>(0.0);
        AtomicReference<Double> totalTenderAmt = new AtomicReference<>(0.0);
        AtomicReference<Double> totalTRMAmt = new AtomicReference<>(0.0);
        AtomicReference<Double> totalMPRAmt = new AtomicReference<>(0.0);
        List<UnreconciledData> unreconciledDataList = new LinkedList<>();
        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            UnreconciledData unreconciledData = new UnreconciledData();
            unreconciledData.setPaymentType(rs.getString("Payment_Type"));
            unreconciledData.setBusinessDate(rs.getString("Business_date"));
            unreconciledData.setStoreId(rs.getString("Store_Id"));
            unreconciledData.setSaleType(rs.getString("Sale_Type"));
            unreconciledData.setInvoiceNumber(rs.getString("Invoice_Number"));
            unreconciledData.setRrnNumber(rs.getString("RRN_Number"));
            unreconciledData.setSaleAmount(rs.getDouble("Sale_Amount"));
            unreconciledData.setSaleTax(rs.getDouble("Sale_Tax"));
            unreconciledData.setTenderAmount(rs.getDouble("Tender_Amount"));
            unreconciledData.setTrmAmount(rs.getDouble("TRM_Amount"));
            unreconciledData.setMprAmount(rs.getDouble("MPR_Amount"));
            unreconciledData.setBsMatched(rs.getBoolean("BS_Matched"));
            //
            totalSaleAmt.set(totalSaleAmt.get() + unreconciledData.getSaleAmount());
            totalTaxAmt.set(totalTaxAmt.get() + unreconciledData.getSaleTax());
            totalTenderAmt.set(totalTenderAmt.get() + unreconciledData.getTenderAmount());
            totalTRMAmt.set(totalTRMAmt.get() + unreconciledData.getTrmAmount());
            totalMPRAmt.set(totalMPRAmt.get() + unreconciledData.getMprAmount());
            unreconciledDataList.add(unreconciledData);
        });
        unreconciledDataWrapper.setTotalSaleAmt(totalSaleAmt.get());
        unreconciledDataWrapper.setTotalTaxAmt(totalTaxAmt.get());
        unreconciledDataWrapper.setTotalTenderAmt(totalTenderAmt.get());
        unreconciledDataWrapper.setTotalTRMAmt(totalTRMAmt.get());
        unreconciledDataWrapper.setTotalMPRAmt(totalMPRAmt.get());
        unreconciledDataWrapper.setUnreconciledDataList(unreconciledDataList);
        return unreconciledDataWrapper;
    }

    @Override
    public void unreconciledDownload(DashboardDataRequest request, OutputStream outputStream) {
        UnreconciledDataWrapper unreconciledDataWrapper = getUnreconciledData(request);
        String header = "Tender,Bank,BusinessDate,Store,SaleType,Invoice Number,RRN Number,Sales Amount,Sales Tax,Tender Amount,TRM Amount,MPR Amount,Reason";
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(50)) {
            Sheet sheet = workbook.createSheet("Unreconciled");
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            String[] headers = header.split(",");
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            Row totalRow = sheet.createRow(sheet.getPhysicalNumberOfRows());
            totalRow.createCell(totalRow.getPhysicalNumberOfCells()).setCellValue("Total");
            totalRow.createCell(7).setCellValue(unreconciledDataWrapper.getTotalSaleAmt());
            totalRow.createCell(8).setCellValue(unreconciledDataWrapper.getTotalTaxAmt());
            totalRow.createCell(9).setCellValue(unreconciledDataWrapper.getTotalTenderAmt());
            totalRow.createCell(10).setCellValue(unreconciledDataWrapper.getTotalTRMAmt());
            totalRow.createCell(11).setCellValue(unreconciledDataWrapper.getTotalMPRAmt());
            for (UnreconciledData unreconciledData : unreconciledDataWrapper.getUnreconciledDataList()) {
                Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getPaymentType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getBank());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getBusinessDate());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getStoreId());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getSaleType());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getInvoiceNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getRrnNumber());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getSaleAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getSaleTax());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getTenderAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getTrmAmount());
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(unreconciledData.getMprAmount());
                String reason = unreconciledData.getTrmAmount() == 0 ? "TRM transaction missing " : (unreconciledData.getMprAmount() == 0 ? "MPR transaction missing" : "Bank transaction missing");
                row.createCell(row.getPhysicalNumberOfCells()).setCellValue(reason);
            }
            workbook.write(outputStream);
            workbook.dispose();
        } catch (Exception e) {
            throw new ApiException("Error Occurred while downloading Unreconciled Report!");
        }
    }

    @Override
    public void reportDownload(DashboardDataRequest request, OutputStream outputStream) {
        switch (request.getReportType()) {
            case POSSales -> {
                if(request.getTender().equalsIgnoreCase("CASH")){
                    orderApiService.cashRecoDownload(request, outputStream);
                }
                else{
                    orderApiService.saleDownload(request, outputStream);
                }
            }
            case SalesVsPickup->{
                orderApiService.salesVsPickUpDownload(request, outputStream);
            }
            case PickupVsReceipts->{
                orderApiService.pickupVsReceiptsDownload(request, outputStream);
            }

            case TRMSales -> {
                trmApiService.trmDownload(request, outputStream);
            }
            case Receipts -> {
                receiptsDownload(request, outputStream);
            }
            case Charges -> {
                chargesDownload(request, outputStream);
            }
            case Reconciled -> {
                reconciledDownload(request, outputStream);
            }
            case UnReconciled -> {
                unreconciledDownload(request, outputStream);
            }
            case POSVsTRM -> {
                orderApiService.getOrderVsTrmDownload(request, outputStream);
            }
            case TRMVsMPR -> {
                trmApiService.getTrmVsMprDataDownload(request, outputStream);
            }
            case MPRVsBank -> {
                mprApiService.getMprVsBankDataDownload(request, outputStream);
            }
            case MPR -> {
                mprApiService.downloadMpr(request, outputStream);
            }
            default -> {

            }
        }
    }

    @Override
    public HashMap<String, HashMap<String, String>> missingTIDMapping() {
        HashMap<String, HashMap<String, String>> trmmapping = trmApiService.getMissingTidMapping();
        String amexCard = mprApiService.getAmexMissingTidMapping();
        HashMap<String, String> cardMap = trmmapping.get("CARD");
        cardMap.put("AMEX", amexCard);
        trmmapping.put("CARD", cardMap);
        return trmmapping;
    }

    @Override
    public void downloadMissingTIDReport(MissingTIDReportRequest request, ByteArrayOutputStream outputStream) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(50);
        if (!request.getBank().equalsIgnoreCase("AMEX")) {
            trmApiService.downloadMissingTIDMapping(request, workbook);
        } else {
            mprApiService.downloadAmexMissingTidMapping(request, workbook);
        }
        workbook.write(outputStream);
        workbook.dispose();
        workbook.close();
    }

    @Override
    public DashboardDataResponse getInstoreDashboardResponse(DashboardDataRequest request) {
        HashMap<String, TenderWiseData> map = new HashMap<>();
        LocalDate startDate = LocalDateTime.parse(request.getStartDate(), com.cpl.core.api.constant.Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
        LocalDate endDate = LocalDateTime.parse(request.getEndDate(), com.cpl.core.api.constant.Formatter.YYYYMMDD_HHMMSS_DASH).toLocalDate();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", startDate);
        parameters.addValue("endDate", endDate);

        String QUERY = "SELECT tender_name,bank,"
                + "COALESCE(SUM(charges),0) AS charges,"
                + "COALESCE(SUM(receipts),0) AS receipts,"
                + "COALESCE(SUM(sales),0) AS sales,"
                + "COALESCE(SUM(reconciled),0) AS reconciled,"
                + "COALESCE(SUM(mpr_vs_bank),0) AS mpr_vs_bank,"
                + "COALESCE(SUM(trm_vs_mpr),0) AS trm_vs_mpr,"
                + "COALESCE(SUM(pos_vs_trm),0) AS pos_vs_trm,"
                + "COALESCE(SUM(pickup_vs_receipts),0) AS pickup_vs_receipts,\n"
                + "COALESCE(SUM(sales_vs_pickup),0) AS sales_vs_pickup \n"
                + " FROM dashboard_data "
                + "WHERE \n"
                + " store_code IN (:storeList) \n"
                + " AND "
                + "business_date BETWEEN :startDate AND :endDate\n"
                + " group by tender_name, bank";

        if (!CollectionUtils.isEmpty(request.getStores())) {
            parameters.addValue("storeList", request.getStores());
        }
        log.info("in-store dahsboard  query {}", QUERY);
        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String tender_name = resultSet.getString("tender_name").toUpperCase();
            if (!map.containsKey(tender_name)) {
                TenderWiseData tenderWiseData = new TenderWiseData();
                tenderWiseData.setTenderName(tender_name);
                tenderWiseData.setBankWiseDataList(new ArrayList<>());
                map.put(tender_name, tenderWiseData);
            }
            TenderWiseData tender = map.get(tender_name);
            BankWiseData bankWiseData = new BankWiseData();
            bankWiseData.setBankName(resultSet.getString("bank"));
            bankWiseData.setSales(resultSet.getDouble("sales"));
            bankWiseData.setReceipts(resultSet.getDouble("receipts"));
            bankWiseData.setCharges(resultSet.getDouble("charges"));
            bankWiseData.setReconciled(resultSet.getDouble("reconciled"));
            bankWiseData.setTrmVsMpr(resultSet.getDouble("trm_vs_mpr"));
            bankWiseData.setMprVsBank(resultSet.getDouble("mpr_vs_bank"));
            bankWiseData.setPosVsTrm(resultSet.getDouble("pos_vs_trm"));
            bankWiseData.setSalesVsPickup(resultSet.getDouble("sales_vs_pickup"));
            bankWiseData.setPickupVsReceipts(resultSet.getDouble("pickup_vs_receipts"));
            tender.getBankWiseDataList().add(bankWiseData);
            tender.setSales(tender.getSales() + bankWiseData.getSales());
            tender.setReceipts(tender.getReceipts() + bankWiseData.getReceipts());
            tender.setReconciled(tender.getReconciled() + bankWiseData.getReconciled());
            tender.setCharges(tender.getCharges() + bankWiseData.getCharges());
            tender.setTrmVsMpr(tender.getTrmVsMpr() + bankWiseData.getTrmVsMpr());
            tender.setMprVsBank(tender.getMprVsBank() + bankWiseData.getMprVsBank());
            tender.setPosVsTrm(tender.getPosVsTrm() + bankWiseData.getPosVsTrm());
            tender.setSalesVsPickup(tender.getSalesVsPickup() + bankWiseData.getSalesVsPickup());
            tender.setPickupVsReceipts(tender.getPickupVsReceipts() + bankWiseData.getPickupVsReceipts());
            map.put(tender_name, tender);
            return null;
        });
        log.info("Issue before instore dashboard query");

        DashboardDataResponse response = new DashboardDataResponse();
        response.setTenderWiseDataList(new ArrayList<>());
        for (TenderWiseData tenderWiseData : map.values()) {
            response.setSales(response.getSales() + tenderWiseData.getSales());
            response.setReceipts(response.getReceipts() + tenderWiseData.getReceipts());
            response.setCharges(response.getCharges() + tenderWiseData.getCharges());
            response.setReconciled(response.getReconciled() + tenderWiseData.getReconciled());
            response.setPosVsTrm(response.getPosVsTrm() + tenderWiseData.getPosVsTrm());
            response.setTrmVsMpr(response.getTrmVsMpr() + tenderWiseData.getTrmVsMpr());
            response.setMprVsBank(response.getMprVsBank() + tenderWiseData.getMprVsBank());
            response.setSalesVsPickup(response.getSalesVsPickup() + tenderWiseData.getSalesVsPickup());
            response.setPickupVsReceipts(response.getPickupVsReceipts() + tenderWiseData.getPickupVsReceipts());
            response.getTenderWiseDataList().add(tenderWiseData);
        }
        log.info("Issue after instore dashboard query");
        return response;
    }
}
