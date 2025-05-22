package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.core.common.annotations.TrackExecutionTime;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.response.instore.BankStatementData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Service
public class BankStatementServiceImpl extends AbstractInStoreService implements BankStatementService {

    @Override
    @TrackExecutionTime
    public List<BankStatementData> getBankStatementDownload(DashboardDataRequest request) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        String QUERY = "SELECT \n" +
                "    payment_type,\n" +
                "    bank,\n" +
                "    source_bank AS mpr_bank,\n" +
                "    date AS deposit_date,\n" +
                "    expected_actual_transaction_date AS settlement_date,\n" +
                "    deposit_amt AS deposit_amount,\n" +
                "    closing_balance,\n" +
                "    narration\n" +
                "FROM\n" +
                "    bank_statements bs\n" +
                "WHERE\n" +
                "    bs.expected_actual_transaction_date BETWEEN :startDate AND :endDate";
        if (!StringUtils.isEmpty(request.getTender())) {
            QUERY += " AND bs.payment_type = :tender";
            parameters.addValue("tender", request.getTender());
        }
        if (!StringUtils.isEmpty(request.getBank())) {
            QUERY += " AND bs.source_bank = :bank";
            parameters.addValue("bank", request.getBank());
        }
        log.info("BankStatement Data: {} , Parameters: {}", QUERY, parameters);
        List<BankStatementData> result = new ArrayList<>();
        jdbcTemplate.query(QUERY, parameters, (rs -> {
            BankStatementData bankStatementData = new BankStatementData();
            bankStatementData.setPaymentType(rs.getString("payment_type"));
            bankStatementData.setBank(rs.getString("bank"));
            bankStatementData.setMprBank(rs.getString("mpr_bank"));
            bankStatementData.setDepositDate(rs.getString("deposit_date"));
            bankStatementData.setSettlementDate(rs.getString("settlement_date"));
            bankStatementData.setDepositAmount(rs.getDouble("deposit_amount"));
            bankStatementData.setClosingBalance(rs.getDouble("closing_balance"));
            bankStatementData.setNarration(rs.getString("narration"));
            result.add(bankStatementData);
        }));
        return result;
    }
}
