package com.cpl.reconciliation.web.service.util;

import com.cpl.core.api.constant.Formatter;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.domain.util.QueryConfig;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 *
 * @author Abhishek N
 */
@Log4j2
@Component
public class ThreepoQueryDateRangeRecoLogicAndFEUtil {

    public String reportQueryToExecuteBasedOnRecoLogicsDateRange(ThreePODataRequest request, String reportType) {
        StringBuilder reportQuery = new StringBuilder("");
        String tender = request.getTender().name().toLowerCase();
        log.info("request Tender {}", tender);
        if (QueryConfig.TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.containsKey(tender)) {
            Map<Long, Map<String, Map<String, String>>> dateRangeLogics = QueryConfig.TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.get(tender);
            for (Map.Entry<Long, Map<String, Map<String, String>>> idWiseQuery : dateRangeLogics.entrySet()) {
                for (Map.Entry<String, Map<String, String>> queries : idWiseQuery.getValue().entrySet()) {
                    LocalDate startDate = request.getStartLocalDate().toLocalDate();
                    LocalDate endDate = request.getEndLocalDate().toLocalDate();
                    String[] dateRangeAndType = queries.getKey().split(",");
                    String fromDate = dateRangeAndType[0];
                    String toDate = dateRangeAndType[1];
                    LocalDate startDateReco, endDateReco = null;
                    if (!fromDate.isEmpty()) {
                        LocalDate stdate = LocalDate.parse(fromDate);
                        startDateReco = stdate;
                        if (!toDate.isEmpty()) {
                            LocalDate todate = LocalDate.parse(toDate);
                            endDateReco = todate;
                        }
                        if (startDate.isBefore(startDateReco)) {
                            if (endDate.isAfter(startDateReco)) {
                                startDate = startDateReco;
                                if (endDateReco != null && endDate.isAfter(endDateReco)) {
                                    endDate = endDateReco;
                                }
                            } else {
                                continue;
                            }
                        } else if (endDateReco != null) {
                            if (startDate.isAfter(endDateReco)) {
                                continue;
                            } else if (endDate.isAfter(endDateReco)) {
                                endDate = endDateReco;
                            }
                        }
                        String effectiveType = dateRangeAndType[2];
                        if (effectiveType.toLowerCase().contains("transaction_date")) {
                            effectiveType = "order_date";
                        }
                        if (reportType.equals("POS_SALES_QUERY") || reportType.equals("ORDER_NOT_FOUND_QUERY")) {
                            effectiveType = "Date(o." + effectiveType + ")";
                        } else if (reportType.equals("POS_PROMO_REPORT_QUERY")) {
                            if (tender.equalsIgnoreCase("zomato")) {
                                effectiveType = "Date(p.aggregation)";
                            } else if (tender.equalsIgnoreCase("swiggy")) {
                                effectiveType = "Date(p.date)";
                            }
                        } else if (reportType.equals("POS_SALT_REPORT_QUERY")) {
                            if (tender.equalsIgnoreCase("zomato")) {
                                effectiveType = "Date(p.created_at)";
                            }
                        } else {
                            if (tender.equalsIgnoreCase("zomato")) {
                                effectiveType = "Date(z." + effectiveType + ")";
                            } else if (tender.equalsIgnoreCase("swiggy")) {
                                effectiveType = "Date(s." + effectiveType + ")";
                            } else if (tender.equalsIgnoreCase("magicpin")) {
                                effectiveType = "Date(m." + effectiveType + ")";
                            }
                        }
                        String dateConditionToReplace;
                        if (reportType.equals("POS_SALES_NextDate_QUERY")) {
                            dateConditionToReplace = "(DATE(o.order_date)='" + endDate.plusDays(1) + "' AND o.business_date='" + endDate + "')";
                        } else {
                            dateConditionToReplace = effectiveType + ">='" + startDate + "' AND " + effectiveType + "<='" + endDate + "'";
                        }
                        reportQuery.append(queries.getValue().get(reportType).replace("<DATE_RANGE_REPORT_CLAUSE_CONDITION>", dateConditionToReplace)).append(" UNION ALL ");
                    }
                }
            }
            if (!reportQuery.isEmpty()) {
                reportQuery = new StringBuilder(reportQuery.substring(0, reportQuery.lastIndexOf(" UNION ALL ")));
            }
        } else {
            log.error("RecoLogics are not available for " + tender + ". Please verify it");
        }
        return reportQuery.toString();
    }
}
