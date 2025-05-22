package com.cpl.reconciliation.web.api;

import com.cpl.reconciliation.web.service.AuditLogService;
import com.cpl.reconciliation.web.service.beans.AuditLogRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Abhishek N
 */
@Slf4j
@Data
@RestController
@RequestMapping("/api/v2/auditlog")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @PostMapping("/save")
    public String saveAuditLog(HttpServletRequest httpRequest, @RequestBody AuditLogRequest request) {
        String clientIp = getClientIpAddr(httpRequest);
        log.info(clientIp);
        request.setSystemIp(clientIp);
        return auditLogService.saveAuditData(request);
    }

    @GetMapping("/getByUser")
    public List<Map<String, String>> getByUser(@RequestParam("userName") String userName) {
        List<Map<String, String>> auditLog = auditLogService.getAuditLogByUserName(userName);
        return auditLog;
    }

    @GetMapping("/getByUserAndDateRange")
    public List<Map<String, Object>> getByUserAndDateRange(@RequestParam("userName") String userName, @RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {
        List<Map<String, Object>> auditLog = auditLogService.getAuditLogByUserNameAndDateRange(userName, startDate, endDate);
        return auditLog;
    }

    private String getClientIpAddr(HttpServletRequest request) {
        logHeaderNames(request);
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public void logHeaderNames(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("Header: {} = {}", headerName, headerValue);
        }
    }
}
