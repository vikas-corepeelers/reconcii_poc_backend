package com.cpl.reconciliation.web.service.impl;

import com.cpl.reconciliation.domain.entity.AuditLogEntity;
import com.cpl.reconciliation.domain.repository.AuditLogRepository;
import com.cpl.reconciliation.web.service.AuditLogService;
import com.cpl.reconciliation.web.service.beans.AuditLogRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/*
 * @author Abhishek N
 */
@Data
@Slf4j
@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    AuditLogRepository repository;
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public String saveAuditData(AuditLogRequest request) {
        try {
            AuditLogEntity entity = new AuditLogEntity();
            entity.setUserName(request.getUserName());
            entity.setUserEmail(request.getUserEmail());
            entity.setSystemIp(request.getSystemIp());
            entity.setRole(request.getRole());
            entity.setAction(request.getAction());
            entity.setRequest(request.getReqData());
            entity.setResponse(request.getResData());
            entity.setRemarks(request.getRemarks());
            entity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            repository.save(entity);
            return "success";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "failure";
        }
    }

    @Override
    public List<Map<String, String>> getAuditLogByUserName(String userName) {
        return repository.getAuditLogByUserName(userName);
    }

    @Override
    public List<Map<String, Object>> getAuditLogByUserNameAndDateRange(String userName, String startDate, String endDate) {
        String auditQuery = "SELECT id, username, user_email, role , system_ip, action, request, response, DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at\n"
                + "FROM reco_sso.audit_log_1 \n"
                + "WHERE username = :userName\n"
                + " AND created_at BETWEEN :startDate AND :endDate\n"
                + "\n"
                + "UNION ALL\n"
                + "\n"
                + "SELECT id, username, user_email, role , system_ip, action, request, response, DATE_FORMAT(created_at, '%Y-%m-%d %H:%i:%s') AS created_at\n"
                + "FROM reconcii.audit_log_1 \n"
                + "WHERE username = :userName\n"
                + " AND created_at BETWEEN :startDate AND :endDate\n"
                + "\n"
                + "ORDER BY created_at ASC;";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userName", userName);
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        List<Map<String, Object>> auditLogs = jdbcTemplate.queryForList(auditQuery, params);
        return auditLogs;
    }
}
