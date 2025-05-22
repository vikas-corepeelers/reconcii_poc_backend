package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.web.service.beans.AuditLogRequest;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Abhishek N
 */
public interface AuditLogService {

    String saveAuditData(AuditLogRequest request);

    List<Map<String, String>> getAuditLogByUserName(String userName);
    
    List<Map<String, Object>> getAuditLogByUserNameAndDateRange(String userName, String startDate, String endDate);
}
