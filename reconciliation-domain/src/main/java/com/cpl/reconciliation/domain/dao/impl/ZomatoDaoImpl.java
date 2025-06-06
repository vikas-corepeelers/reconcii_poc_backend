package com.cpl.reconciliation.domain.dao.impl;

import com.cpl.core.api.exception.ApiException;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.domain.dao.ZomatoDao;
import com.cpl.reconciliation.domain.entity.ThreePoDashBoardEntity;
import com.cpl.reconciliation.domain.entity.Zomato;
import com.cpl.reconciliation.domain.repository.RecoLogicRepository;
import com.cpl.reconciliation.domain.repository.ThreePoDashboardRepository;
import com.cpl.reconciliation.domain.repository.ZomatoRepository;
import com.cpl.reconciliation.domain.util.QueryConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Service
public class ZomatoDaoImpl implements ZomatoDao {

    private final ZomatoRepository zomatoRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ThreePoDashboardRepository threePoDashboardRepository;

    @Override
    public void saveAll(List<Zomato> zomatoEntities) {
        zomatoRepository.saveAll(zomatoEntities);
    }

    @Override
    public void save(Zomato entity) {
        zomatoRepository.save(entity);
    }

    @Override
    public void updateDataResponse(LocalDate businessDate) {

        try {
            updateThreePOData(businessDate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error while fetching data");
        }

    }

    private void updateThreePOData(LocalDate businessDate) {
        StringBuilder theepoSqlQuery = new StringBuilder();
        if (QueryConfig.TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.get("zomato") != null) {
            Map<Long, Map<String, Map<String, String>>> dateRangeLogics = QueryConfig.TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.get("zomato");
            for (Map.Entry<Long, Map<String, Map<String, String>>> keyVal : dateRangeLogics.entrySet()) {
                for (Map.Entry<String, Map<String, String>> queries : keyVal.getValue().entrySet()) {
                    theepoSqlQuery.append(queries.getValue().get("THREEPO_SUMMARY_QUERY")).append(" UNION ALL ");
                }
            }
            theepoSqlQuery = new StringBuilder(theepoSqlQuery.substring(0, theepoSqlQuery.lastIndexOf(" UNION ALL ")));
            log.info("zomato dashboard dynamic summary query :: {}", theepoSqlQuery);
        }
        if (!theepoSqlQuery.isEmpty()) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            if (theepoSqlQuery.toString().contains("Date(z.order_date) <= :businessDate")) {
                parameters.addValue("businessDate", businessDate);
            }
            List<ThreePoDashBoardEntity> entities = new ArrayList<>();
            jdbcTemplate.query(theepoSqlQuery.toString(), parameters, (resultSet, rowNum) -> {
                ThreePoDashBoardEntity threePoDashBoardEntity = new ThreePoDashBoardEntity();
                String storeId = resultSet.getString("store_id");
                LocalDate date = LocalDate.parse(resultSet.getString("businessDate"));
                double threePOSales = resultSet.getDouble("threePOSales");
                double posSales = resultSet.getDouble("PosSales");
                double threePOReceivables = resultSet.getDouble("threePOReceivables");
                double threePOCommission = resultSet.getDouble("threePOCommission");
                double threePOCharges = resultSet.getDouble("threePOCharges");
                double salt = resultSet.getDouble("freebies");
                // Ideal scenarios
                double posReceivables = resultSet.getDouble("posReceivables");
                double posCommission = resultSet.getDouble("posCommission");
                double posCharges = resultSet.getDouble("posCharges");

                double reconciled = resultSet.getDouble("reconciled");
                double unreconciled = resultSet.getDouble("unreconciled");
                double receivablesVsReceipts = resultSet.getDouble("receivablesVsReceipts");
                double threePoDiscount = resultSet.getDouble("threePODiscounts");
                double posDiscount = resultSet.getDouble("posDiscounts");

                threePoDashBoardEntity.setTenderName(ThreePO.ZOMATO.name());
                threePoDashBoardEntity.setThreePoSales(threePOSales);
                threePoDashBoardEntity.setThreePoReceivables(threePOReceivables);
                threePoDashBoardEntity.setThreePoCommission(threePOCommission);
                threePoDashBoardEntity.setThreePoCharges(threePOCharges);
                threePoDashBoardEntity.setThreePoFreebies(salt);
                threePoDashBoardEntity.setThreePoDiscounts(threePoDiscount);
                threePoDashBoardEntity.setBusinessDate(date);
                threePoDashBoardEntity.setCategory("ThreePO");
                threePoDashBoardEntity.setStoreCode(storeId);
                threePoDashBoardEntity.setPosSales(posSales);
                threePoDashBoardEntity.setPosReceivables(posReceivables);
                threePoDashBoardEntity.setPosCommission(posCommission);
                threePoDashBoardEntity.setPosCharges(posCharges);
                threePoDashBoardEntity.setPosDiscounts(posDiscount);
                threePoDashBoardEntity.setPosFreebies(salt);
                threePoDashBoardEntity.setReconciled(reconciled);
                threePoDashBoardEntity.setUnReconciled(unreconciled);
                threePoDashBoardEntity.setReceivablesVsReceipts(receivablesVsReceipts);
                /*Id related Code added by Abhishek for ID on 03-dec-2024*/
                threePoDashBoardEntity.setId(threePoDashBoardEntity.getStoreCode() + "|" + threePoDashBoardEntity.getBusinessDate() + "|" + threePoDashBoardEntity.getTenderName());
                entities.add(threePoDashBoardEntity);
                if (entities.size() >= 500) {
                    log.info("going to save threePo DashBoard entities For Zomato of size  : {}", entities.size());
                    threePoDashboardRepository.saveAll(entities);
                    entities.clear();
                }
                return null;
            });
            log.info("going to save threePo DashBoard entities For Zomato of size : {}", entities.size());
            threePoDashboardRepository.saveAll(entities);
        }
    }
}
