package com.cpl.reconciliation.web.api;

import com.cpl.core.api.response.ApiResponse;
import com.cpl.reconciliation.core.response.ManualUploadResponse;
import com.cpl.reconciliation.core.response.ManualUploadTenderResponse;
import com.cpl.reconciliation.core.response.ManualUploadTypeResponse;
import com.cpl.reconciliation.domain.entity.ManualUploads;
import com.cpl.reconciliation.domain.repository.ManualUploadingRepository;
import com.cpl.reconciliation.tasks.service.DataService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cpl.reconciliation.web.service.beans.ColumnsBean;
import com.cpl.reconciliation.web.service.beans.TableAndColumnsBean;
import com.cpl.reconciliation.web.service.beans.TenderAndTablesBean;
import com.cpl.reconciliation.web.service.beans.TenderRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;

import static java.util.stream.Collectors.groupingBy;
import javax.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.validation.annotation.Validated;

@Data
@Slf4j
@RestController
@RequestMapping("/api/v1/")
public class TenderApisController {

    @Autowired
    List<DataService> dataServiceList;
    @Autowired
    ManualUploadingRepository repository;
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    /*Created on 06-12-2024*/
    @GetMapping("/tenderList")
    public List<String> getAllDataSources(HttpServletRequest request) throws JsonProcessingException {
        String tenderQuery = "select distinct(tender_name) from customised_db_fields where tender_name is not null";
        List<String> tenders = jdbcTemplate.queryForList(tenderQuery, new MapSqlParameterSource(), String.class);
        return tenders;
    }

    @PostMapping("/tenderWisetables")
    public ApiResponse<List<TenderAndTablesBean>> getAllDataSourceTables(HttpServletRequest request, @Validated @RequestBody TenderRequest tender) throws JsonProcessingException {
        log.info("Request URL: {}", request.getRequestURI());
        ApiResponse apiResponse = new ApiResponse<>(returnDataSourceAndColumns(tender));
        return apiResponse;
    }

    public List<TenderAndTablesBean> returnDataSourceAndColumns(TenderRequest tender) {
        log.info("tender.getTenders() :: {}", tender.getTenders());
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("tenders", tender.getTenders());
        String dataSourceQuery = "select distinct tender_name,data_source,table_name from customised_db_fields where tender_name is not null AND tender_name IN (:tenders)";
        List<Map<String, Object>> tenderWiseDataSources = jdbcTemplate.queryForList(dataSourceQuery, parameters);
        log.info("tenderWiseDataSources :: {}", tenderWiseDataSources.size());
        Map<String, List<TableAndColumnsBean>> tenderWisetableColumnsList = new HashMap();
        Map<String, List<TenderAndTablesBean>> tenderMap = new HashMap();
        List<TenderAndTablesBean> tenderWiseDatSourceAndTableColumns = new ArrayList();
        for (Map<String, Object> row : tenderWiseDataSources) {
            String tenderName = (String) row.get("tender_name");
            String dataSourceName = (String) row.get("data_source");
            String tableName = (String) row.get("table_name");
            if (!tenderMap.containsKey(tenderName)) {
                tenderMap.put(tenderName, null);
                TenderAndTablesBean tenderTableBean = new TenderAndTablesBean();
                tenderTableBean.setTender(tenderName);
                List<TableAndColumnsBean> tableColumnsList = new ArrayList();
                tenderWisetableColumnsList.put(tenderName, tableColumnsList);
            }
            List<TableAndColumnsBean> TableAndColumnsList = tenderWisetableColumnsList.get(tenderName);
            TableAndColumnsBean tableBean = new TableAndColumnsBean();
            tableBean.setDataSourceName(dataSourceName);
            tableBean.setTableName(tableName);
            List<ColumnsBean> columnsList = getColumnDetails(tenderName, dataSourceName);
            tableBean.setColumns(columnsList);
            TableAndColumnsList.add(tableBean);
            tenderWisetableColumnsList.put(tenderName, TableAndColumnsList);
        }
        for (Map.Entry<String, List<TableAndColumnsBean>> keyVal : tenderWisetableColumnsList.entrySet()) {
            TenderAndTablesBean tenderTableBean = new TenderAndTablesBean();
            tenderTableBean.setTender(keyVal.getKey());
            tenderTableBean.setDataSourceWiseColumns(keyVal.getValue());
            tenderWiseDatSourceAndTableColumns.add(tenderTableBean);
        }
        return tenderWiseDatSourceAndTableColumns;
    }

    // Method to fetch column details
    public List<ColumnsBean> getColumnDetails(String tenderName, String dataSourceName) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("tenderName", tenderName);
        parameters.addValue("dataSource", dataSourceName);

        String query = "SELECT excel_column_name, db_column_name FROM customised_db_fields WHERE tender_name=:tenderName AND data_source=:dataSource";
        return jdbcTemplate.query(query, parameters, (java.sql.ResultSet rs, int rowNum) -> {
            String excelColumnName = rs.getString("excel_column_name");
            String dbColumnName = rs.getString("db_column_name");
            return new ColumnsBean(dbColumnName, excelColumnName);
        });
    }

//    protected Map<DataSource, DataService> dataServiceMap = new HashMap<>();
//
//    @PostConstruct
//    public void init() {
//        dataServiceList.stream().forEach(dataService -> {
//            dataServiceMap.put(dataService.getDataSource(), dataService);
//        });
//    }
//
//    @PostMapping("/upload/data")
//    public ApiResponse<String> uploadDataHandler(@RequestParam("datSource") DataSource dataSource,
//            @RequestParam("businessDate") @DateTimeFormat(pattern = _YYYYMMDD_DASH) LocalDate businessDate,
//            @RequestParam("endDate") @Nullable @DateTimeFormat(pattern = _YYYYMMDD_DASH) LocalDate endDate,
//            @RequestParam("files") List<MultipartFile> files) throws IOException {
//        boolean submitted = dataServiceMap.get(dataSource).uploadManually(businessDate, endDate, files);
//        String message = "Upload Request Successfully Submitted";
//        if (!submitted) {
//            message = "This data set has already been uploaded";
//        }
//        return new ApiResponse<>(message);
//    }
//
    @GetMapping("/datasource")
    public List<ManualUploadResponse> getDataSource() {
        List<ManualUploads> list = repository.findAll();
        List<ManualUploadResponse> responseList = new ArrayList<>();
        Map<String, Map<String, Map<String, List<ManualUploads>>>> map = list.stream().filter(ManualUploads::isActive).collect(groupingBy(ManualUploads::getCategory,
                groupingBy(ManualUploads::getTender, groupingBy(ManualUploads::getType))));

        for (Map.Entry<String, Map<String, Map<String, List<ManualUploads>>>> categoryMap : map.entrySet()) {
            ManualUploadResponse response = new ManualUploadResponse();
            response.setCategory(categoryMap.getKey());
            response.setTenders(new ArrayList<>());
            responseList.add(response);
            for (Map.Entry<String, Map<String, List<ManualUploads>>> tenderMap : categoryMap.getValue().entrySet()) {
                ManualUploadTenderResponse tender = new ManualUploadTenderResponse();
                tender.setTender(tenderMap.getKey());
                tender.setTypes(new ArrayList<>());
                response.getTenders().add(tender);
                for (Map.Entry<String, List<ManualUploads>> typeMap : tenderMap.getValue().entrySet()) {
                    ManualUploadTypeResponse type = new ManualUploadTypeResponse();
                    type.setType(typeMap.getKey());
                    tender.getTypes().add(type);
                    if (!typeMap.getValue().isEmpty()) {
                        type.setDataSource(typeMap.getValue().get(0).getDataSource());
                        type.setActive(typeMap.getValue().get(0).isActive());
                    }
                }
            }
        }

        return responseList;
    }
}
