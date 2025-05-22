package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.reconciliation.core.enums.PaymentType;
import com.cpl.reconciliation.core.modal.query.MprBankDifference;
import com.cpl.reconciliation.domain.dao.MPRDao;
import com.cpl.reconciliation.domain.entity.MPREntity;
import com.cpl.reconciliation.domain.repository.MPRRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Data
@Slf4j
@Service
public class MPRDaoImpl implements MPRDao {

    private static final Predicate<MprBankDifference> mprBankDifferencePredicate = (mprBankDifference) -> (mprBankDifference.getDifference() - mprBankDifference.getRefundAmount() < 1);
    private static final Predicate<MprBankDifference> mprHDFCBankDifferencePredicate = (mprBankDifference) -> (PaymentType.UPI.name().equalsIgnoreCase(mprBankDifference.getTender()) && "HDFC".equalsIgnoreCase(mprBankDifference.getBank()) && Math.abs(mprBankDifference.getDifference() - mprBankDifference.getRefundAmount()) < 500);
    private final MPRRepository mprRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void saveAll(List<MPREntity> mprEntities) {
        mprRepository.saveAll(mprEntities);
    }

    @Override
    public List<MPREntity> findMPR(String bank, String startDate, String endDate, String tender, int booked) {
        return mprRepository.findMPR(bank, startDate, endDate, tender, booked);
    }

    @Override
    public List<MprBankDifference> getMprBankDifference(String bank, String tender, String startDate, String endDate) {
        List<MprBankDifference> unreconciledMPRs = new ArrayList<>();
        List<MprBankDifference> mprBankDifferences = new ArrayList<>();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("bank", bank);
        parameters.addValue("tender", tender);
        parameters.addValue("startDate", startDate);
        parameters.addValue("endDate", endDate);
        String QUERY = "SELECT\n" +
                "    m.settlement_date AS settlementDate,\n" +
                "    m.bank AS bank,\n" +
                "    m.payment_type AS tender,\n" +
                "    m.mprCount AS mprCount,\n" +
                "    COALESCE(m.mprAmount, 0) AS mprAmount,\n" +
                "    COALESCE(b.bankAmount, 0) AS bankAmount,\n" +
                "    COALESCE((select sum(refund_amount) from mpr_refunds where date(refund_transaction_date_time)=m.settlement_date AND bank=m.bank AND payment_type=m.payment_type AND is_online_deemed=false), 0) AS refundAmount,\n" +
                "    ABS(ROUND(COALESCE(m.mprAmount, 0) - COALESCE(b.bankAmount, 0), 2)) AS diff\n" +
                "FROM\n" +
                "    (SELECT\n" +
                "       DATE(settled_date) AS settlement_date,\n" +
                "       payment_type,\n" +
                "       bank,\n" +
                "       count(*) AS mprCount,\n" +
                "       SUM(settled_amount) AS mprAmount\n" +
                "    FROM\n" +
                "        mpr\n" +
                "    WHERE\n" +
                "        bank = :bank AND payment_type = :tender\n" +
                "    GROUP BY DATE(settled_date)) AS m\n" +
                "LEFT JOIN\n" +
                "    (SELECT\n" +
                "        DATE(expected_actual_transaction_date) AS settlement_date,\n" +
                "        payment_type,\n" +
                "        source_bank as bank,\n" +
                "        SUM(deposit_amt) AS bankAmount\n" +
                "    FROM\n" +
                "        bank_statements\n" +
                "    WHERE\n" +
                "        source_bank = :bank AND payment_type = :tender\n" +
                "    GROUP BY DATE(expected_actual_transaction_date)) AS b\n" +
                "ON (m.settlement_date = b.settlement_date) order by settlementDate asc";
        //log.info("Query: {} with: {}", QUERY, parameters);
        AtomicReference<Double> unreconciledAmount = new AtomicReference<>(0.0);

        jdbcTemplate.query(QUERY, parameters, (rs) -> {
            MprBankDifference mprBankDifference = new MprBankDifference();
            mprBankDifference.setSettlementDate(rs.getString("settlementDate"));
            mprBankDifference.setBank(rs.getString("bank"));
            mprBankDifference.setTender(rs.getString("tender"));
            mprBankDifference.setMprCount(rs.getLong("mprCount"));
            mprBankDifference.setMprAmount(rs.getDouble("mprAmount"));
            mprBankDifference.setBankAmount(rs.getDouble("bankAmount"));
            mprBankDifference.setRefundAmount(rs.getDouble("refundAmount"));
            mprBankDifference.setDifference(rs.getDouble("diff"));
            if ((Math.abs(mprBankDifference.getMprAmount() - mprBankDifference.getBankAmount()) < 1)) {
                 updateBankStatementRecevied((Collections.singletonList(mprBankDifference)));
            } else {
                unreconciledMPRs.add(mprBankDifference);
                double netDeltaAmount = unreconciledAmount.updateAndGet(v -> v + mprBankDifference.getMprAmount()-mprBankDifference.getBankAmount());
                if (Math.abs(netDeltaAmount) < 1) {
                    updateBankStatementRecevied(unreconciledMPRs);
                    unreconciledAmount.set(0.0);
                    unreconciledMPRs.clear();
                } else {
                    log.error("MissMatch: {} ", mprBankDifference);
                }
            }
            mprBankDifferences.add(mprBankDifference);
        });
        return mprBankDifferences;
    }

    @Override
    public void updateStoreTidFromTRMICICIUPI() {
        mprRepository.updateStoreTidFromTRMICICIUPI();
    }

    private void updateBankStatementRecevied(List<MprBankDifference> results) {
        for (MprBankDifference result : results) {
            String QUERY = "UPDATE mpr\n" +
                    "SET bs_matched = TRUE\n" +
                    "WHERE (date(settled_date)) = :settledDate AND bank = :bank AND payment_type= :tender and bs_matched = FALSE";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("bank", result.getBank());
            parameters.addValue("tender", result.getTender());
            parameters.addValue("settledDate", result.getSettlementDate());
            //log.info("QUERY: {} {}", QUERY, parameters);
            int update = jdbcTemplate.update(QUERY, parameters);
            log.info("{} rows updated for Bank: {}, Tender: {}, SettlementDate: {} MPR: {}, BANK: {}, Refund: {}", update, result.getBank(), result.getTender(), result.getSettlementDate(), result.getMprAmount(), result.getBankAmount(), result.getRefundAmount());
        }
    }
}
