package com.cpl.reconciliation.web.service.impl;

import com.cpl.reconciliation.core.request.CustomOrderDataRequest;
import com.cpl.reconciliation.core.response.CustomisedData;
import com.cpl.reconciliation.domain.entity.CustomisedReportFilter;
import com.cpl.reconciliation.web.service.CustomReportApiService;
import com.opencsv.CSVWriter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@Service
public class CustomReportApiServiceImpl implements CustomReportApiService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public void getCustomisedData(CustomOrderDataRequest request, CSVWriter csvWriter) {
        String category = "";
        String sqlFrom = "";
        if (request.getTender().equalsIgnoreCase("SWIGGY")) {
            category = "Swiggy";
            sqlFrom = " FROM swiggy  " +
                    "WHERE  order_date BETWEEN :startDate AND :endDate;";
        } else if(request.getTender().equalsIgnoreCase("ZOMATO")) {
            category = "Zomato";
            sqlFrom = " FROM zomato  " +
                    "WHERE  order_date BETWEEN :startDate AND :endDate;";
        }  else if(request.getTender().equalsIgnoreCase("MAGICPIN")){
            category = "Magicpin";
            sqlFrom = " FROM magicpin  " +
                    "WHERE  date BETWEEN :startDate AND :endDate;";
        }else {
            category = "InStore";
            sqlFrom =
                    " FROM reconcii.orders o " +
                            "JOIN trm t on o.invoice_number=t.order_id and t.transaction_status = 'SUCCESS' " +
                            "JOIN mpr m on t.uid = m.uid " +
                            "WHERE o.order_status = 'Paid' AND o.tender_name IS NOT NULL AND o.invoice_number IS NOT NULL " +
                            "AND o.tender_name LIKE :tender AND o.business_date BETWEEN :startDate AND :endDate;";
        }

        List<CustomisedReportFilter> filters = getSQLFields(CollectionUtils.isEmpty(request.getRequired_fields())?null:request.getRequired_fields() , category);
        List<String> sqlFields = filters.stream().map(CustomisedReportFilter::getSqlField).toList();
        List<String> headers = filters.stream().map(CustomisedReportFilter::getName).toList();

        csvWriter.writeNext(headers.toArray(new String[0]));
        List<String[]> dataList = new ArrayList<>();

        String select = "SELECT " + String.join(",", sqlFields) + " ";
        String sql = select + sqlFrom;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("startDate", request.getStartDate());
        parameters.addValue("endDate", request.getEndDate());
        parameters.addValue("tender", "%" + request.getTender() + "%");
        List<CustomisedData> queryResult = jdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            String[] row = new String[headers.size()];
            int idx = 0;
            for (String field : sqlFields) {
                row[idx++] = rs.getString(field);
            }
            dataList.add(row);
            return null;
        });
        csvWriter.writeAll(dataList);
    }

    @Override
    public List<CustomisedReportFilter> getCustomisedReportFields(String category) {
        String sql = "SELECT name,technical_name " +
                " FROM reconcii.customised_report_filters where category=:category  ";
        if (category.equalsIgnoreCase("CARD") || category.equalsIgnoreCase("UPI")) {
            category = "InStore";
        }
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("category", category);
        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            CustomisedReportFilter filter = new CustomisedReportFilter();

            filter.setName(rs.getString("name"));
            filter.setTechnicalName(rs.getString("technical_name"));
            return filter;
        });
    }

    private List<CustomisedReportFilter> getSQLFields(List<String> fields, String category) {
        String sql = "SELECT name,technical_name,sql_field " +
                " FROM reconcii.customised_report_filters where (COALESCE(:fields,null) is null or technical_name in (:fields)) and category=:category";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("fields", fields);
        parameters.addValue("category", category);
        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            CustomisedReportFilter filter = new CustomisedReportFilter();

            filter.setName(rs.getString("name"));
            filter.setTechnicalName(rs.getString("technical_name"));
            filter.setSqlField(rs.getString("sql_field"));

            return filter;
        });
    }

}
