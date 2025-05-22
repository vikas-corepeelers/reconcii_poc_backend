package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.TenderWiseData;
import com.cpl.reconciliation.core.response.instore.DeltaDTO;
import com.cpl.reconciliation.domain.dao.DashboardDao;
import com.cpl.reconciliation.domain.entity.DashBoardEntity;
import com.cpl.reconciliation.domain.repository.DashboardRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Data
@Slf4j
@Service
public class DashboardDaoImpl implements DashboardDao {

    private final DashboardRepository dashboardRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<DashBoardEntity> getAll() {
        return dashboardRepository.findAll();
    }

    @Override
    public DashboardDataResponse getDashboardDataResponse(LocalDate businessDate) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("businessDate", businessDate);
        List<DashBoardEntity> dashBoardEntities = new ArrayList<>();

        String QUERY = "SELECT result.businessDate, \n"
                + "       result.payment_type ,\n"
                + "       result.acquirer_bank,\n"
                + "       result.store_id,"
                + "       SUM(result.sales_amount) as sales_amount,\n"
                + "       SUM(result.receipts_amount) as receipts_amount,\n"
                + "       SUM(result.charges) as charges,\n"
                + "       SUM(result.reconciled_amount) as reconciled_amount,\n"
                + "       SUM(result.trmvsmpr) as trmvsmpr,\n"
                + "       SUM(result.mprvsbank) as mprvsbank,\n"
                + "       (COALESCE(SUM(o.total_amount), 0) - COALESCE(SUM(result.trm_amount), 0)) AS posvstrm from  (\n"
                + "WITH Combined AS (\n"
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
                + "    WHERE t.transaction_status = 'SUCCESS' and Date(t.transaction_date) <= :businessDate\n"
                + "    UNION ALL\n"
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
                + "    WHERE t.transaction_status = 'SUCCESS' and Date(t.transaction_date) <= :businessDate\n"
                + "    UNION ALL\n"
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
                + "    WHERE t.transaction_status = 'SUCCESS' and Date(t.transaction_date) <= :businessDate\n"
                + ")\n"
                + "SELECT \n"
                + "    t.cloud_ref_id,t.store_id,t.payment_type, Date(t.transaction_date) as businessDate,t.acquirer_bank,\n"
                + "    COALESCE(SUM(t.trm_amount), 0) as trm_amount,\n"
                + "    COALESCE(SUM(t.trm_amount), 0) AS sales_amount,\n"
                + "    COALESCE(SUM(CASE WHEN c.bs_matched = true THEN c.settled_amount ELSE 0 END), 0) AS receipts_amount,\n"
                + "    COALESCE(SUM(c.bank_charges), 0) AS charges,\n"
                + "    COALESCE(SUM(CASE WHEN c.bs_matched = TRUE THEN c.mpr_amount ELSE 0 END), 0)  as reconciled_amount,\n"
                + "    (COALESCE(SUM(t.trm_amount), 0) - COALESCE(SUM(c.mpr_amount), 0)) AS trmvsmpr,\n"
                + "    COALESCE(SUM(CASE WHEN c.bs_matched = false THEN c.settled_amount ELSE 0 END), 0) AS mprvsbank \n"
                + "FROM \n"
                + "    trm t \n"
                + "LEFT JOIN \n"
                + "    Combined c ON (c.uid = t.uid)\n"
                + "WHERE \n"
                + "t.transaction_status = 'SUCCESS' and Date(t.transaction_date) <= :businessDate\n"
                + "GROUP BY t.payment_type, Date(t.transaction_date),t.acquirer_bank,t.store_id, t.cloud_ref_id) result left join orders o on result.cloud_ref_id=o.transaction_number "
                + "GROUP BY result.businessDate,result.payment_type, result.acquirer_bank,result.store_id";

        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            DashBoardEntity dashBoardEntity = new DashBoardEntity();
            dashBoardEntity.setStoreCode(resultSet.getString("store_id"));
            dashBoardEntity.setBusinessDate(LocalDate.parse(resultSet.getString("businessDate")));
            dashBoardEntity.setTenderName(paymentType);
            dashBoardEntity.setBank(resultSet.getString("acquirer_bank"));
            dashBoardEntity.setSales(resultSet.getDouble("sales_amount"));
            dashBoardEntity.setReceipts(resultSet.getDouble("receipts_amount"));
            dashBoardEntity.setCharges(resultSet.getDouble("charges"));
            dashBoardEntity.setReconciled(resultSet.getDouble("reconciled_amount"));
            dashBoardEntity.setTrmVsMpr(resultSet.getDouble("trmvsmpr"));
            dashBoardEntity.setMprVsBank(resultSet.getDouble("mprvsbank"));
            dashBoardEntity.setMprVsBank(resultSet.getDouble("posvstrm"));
            dashBoardEntity.setId(dashBoardEntity.getTenderName() + "|" + dashBoardEntity.getBusinessDate() + "|" + dashBoardEntity.getStoreCode() + "|" + dashBoardEntity.getBank());
            dashBoardEntities.add(dashBoardEntity);
            if (dashBoardEntities.size() >= 500) {
                log.info("going to save dashboard Data : {}", dashBoardEntities.size());
                dashboardRepository.saveAll(dashBoardEntities);
                dashBoardEntities.clear();
            }
            return null;
        });
        dashboardRepository.saveAll(dashBoardEntities);
        log.info("going to save dashboard Data : {}", dashBoardEntities.size());
        return null;

    }

    @Override
    public void getCashDashboardDataResponse(LocalDate businessDate) {

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("businessDate", businessDate);
        List<DashBoardEntity> dashBoardEntities = new ArrayList<>();

        String QUERY = "WITH pos_sale_cte AS (\n" +
                "    SELECT \n" +
                "        SUM(o.gross_amount) AS t_amount,\n" +
                "        o.business_date,\n" +
                "        o.store_id\n" +
                "    FROM \n" +
                "        orders o \n" +
                "    WHERE \n" +
                "        o.tender_name = 'CASH' \n" +
                "        AND o.business_date <= :businessDate \n" +
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
                "        c.pick_up_date <= :businessDate \n" +
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
                "        AND b.value_date <= :businessDate \n" +
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
                "    ON bs.value_date = DATE_ADD(ps.business_date, INTERVAL 2 DAY) \n" +
                "ORDER BY \n" +
                "    ps.business_date, ps.store_id;";

        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = "CASH";
            DashBoardEntity dashBoardEntity = new DashBoardEntity();
            dashBoardEntity.setStoreCode(resultSet.getString("store_id"));
            dashBoardEntity.setBusinessDate(LocalDate.parse(resultSet.getString("business_date")));
            dashBoardEntity.setTenderName(paymentType);
//            dashBoardEntity.setBank(resultSet.getString("acquirer_bank"));
            dashBoardEntity.setBank("HSBC");
            dashBoardEntity.setSales(resultSet.getDouble("total_sales"));
            dashBoardEntity.setReceipts(resultSet.getDouble("deposited_amount"));
            dashBoardEntity.setCharges(0.0);
            dashBoardEntity.setReconciled(resultSet.getDouble("reconciled"));
            dashBoardEntity.setUnReconciled(resultSet.getDouble("unreconciled"));
            dashBoardEntity.setSalesVsPickup(resultSet.getDouble("sales_vs_pickup"));
            dashBoardEntity.setPickupVsReceipts(resultSet.getDouble("pickup_vs_deposit_amt"));
            dashBoardEntity.setId(dashBoardEntity.getTenderName() + "|" + dashBoardEntity.getBusinessDate() + "|" + dashBoardEntity.getStoreCode() + "|" + dashBoardEntity.getBank());
            dashBoardEntities.add(dashBoardEntity);
            if (dashBoardEntities.size() >= 500) {
                log.info("going to save dashboard Data For Cash : {}", dashBoardEntities.size());
                dashboardRepository.saveAll(dashBoardEntities);
                dashBoardEntities.clear();
            }
            return null;
        });
        dashboardRepository.saveAll(dashBoardEntities);
        log.info("going to save dashboard Data for Cash : {}", dashBoardEntities.size());
        return ;

    }

    private void getFirstLevelMap(LocalDate businessDate, HashMap<String, TenderWiseData> map) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("businessDate", businessDate);
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
                + "      AND t.transaction_date =:businessDate  \n"
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
                + "       AND t.transaction_date =:businessDate \n"
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
                + "     AND t.transaction_date =:businessDate \n"
                + ")\n"
                + "\n"
                + "SELECT \n"
                + "    t.name as payment_type,o.store_id, \n"
                + "    COALESCE(SUM(t.amount), 0) AS sales,\n"
                + "    (SELECT SUM(settled_amount)\n"
                + "     FROM mpr\n"
                + "     WHERE transaction_date =:businessDate  \n"
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
                + "    o.order_date =:businessDate \n"
                + "    AND o.tender_name IS NOT NULL \n"
                + "    AND o.invoice_number IS NOT NULL \n"
                + "    AND o.order_status = 'Paid' \n";

        QUERY += "GROUP BY t.name";
        log.info("IN-Store FirstLevel: {} , Parameters: {}", QUERY, parameters);
        ArrayList<DeltaDTO> deltaDTOS = getMPRvsBankData(businessDate);
        ArrayList<DeltaDTO> trMvsMPRDataDTOS = getTRMvsMPRData(businessDate);
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

    private ArrayList<DeltaDTO> getMPRvsBankData(LocalDate businessDate) {
        ArrayList<DeltaDTO> deltaDTOS = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("businessDate", businessDate);
        String QUERY = "SELECT m.payment_type, m.bank as acquirer_bank, CASE WHEN bs_matched=false THEN sum(m.settled_amount) else 0  end as MprvsBank FROM mpr m\n"
                + "WHERE m.settled_date =:businessDate GROUP BY m.payment_type, m.bank";

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

    private ArrayList<DeltaDTO> getTRMvsMPRData(LocalDate businessDate) {
        ArrayList<DeltaDTO> deltaDTOS = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("businessDate", businessDate);
        String QUERY = "WITH Combined AS (\n"
                + "    SELECT t.payment_type, t.acquirer_bank, \n"
                + "    m.uid as uid,\n"
                + "    m.mpr_amount AS mpr_amount\n"
                + "    FROM trm t LEFT JOIN mpr m ON (m.uid = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "      AND t.transaction_date =:businessDate\n"
                + "    \n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT t.payment_type, t.acquirer_bank, \n"
                + "    m.merchant_ref_id as uid, \n"
                + "    m.mpr_amount AS mpr_amount\n"
                + "    FROM trm t LEFT JOIN mpr m ON (m.merchant_ref_id = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "      AND t.transaction_date =: businessDate\n"
                + "\n"
                + "    UNION ALL\n"
                + "    \n"
                + "    SELECT t.payment_type, t.acquirer_bank, \n"
                + "    m.transaction_utr as uid,\n"
                + "    m.mpr_amount AS mpr_amount\n"
                + "    FROM trm t \n"
                + "    LEFT JOIN mpr m ON (m.transaction_utr = t.uid)\n"
                + "    WHERE t.transaction_status = 'SUCCESS' \n"
                + "    AND t.transaction_date =: businessDate\n"
                + ")\n"
                + "SELECT tm.payment_type,tm.acquirer_bank,(COALESCE(SUM(tm.trm_amount), 0) - COALESCE(SUM(c.mpr_amount), 0)) \n"
                + "AS trmvsmpr\n"
                + "FROM trm tm LEFT JOIN Combined c ON tm.uid = c.uid \n"
                + "WHERE tm.transaction_status = 'SUCCESS' \n"
                + "AND tm.transaction_date =:businessDate\n"
                + "GROUP BY tm.payment_type, tm.acquirer_bank\n";

        jdbcTemplate.query(QUERY, parameters, (resultSet, rowNum) -> {
            String paymentType = resultSet.getString("payment_type").toUpperCase();
            String bank = resultSet.getString("acquirer_bank").toUpperCase();
            DeltaDTO deltaDTO = new DeltaDTO(paymentType, bank, resultSet.getDouble("trmvsmpr"));
            deltaDTOS.add(deltaDTO);
            return null;
        });
        return deltaDTOS;
    }

}
