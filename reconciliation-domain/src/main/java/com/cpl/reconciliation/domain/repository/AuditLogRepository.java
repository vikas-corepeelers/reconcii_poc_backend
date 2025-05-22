package com.cpl.reconciliation.domain.repository;

import com.cpl.reconciliation.domain.entity.AuditLogEntity;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
/*
 * @author Abhishek N
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    @Query(value = "select id,service_name,user_name,systemip,url,action,req_data,entry_date from audit_log WHERE user_name=:userName", nativeQuery = true)
    public List<Map<String, String>> getAuditLogByUserName(String userName);
    
    @Query(value = "select id,service_name,user_name,systemip,url,action,req_data,entry_date from audit_log WHERE user_name=:userName AND (entry_date between :startDate AND :endDate)", nativeQuery = true)
    public List<Map<String, String>> getAuditLogByUserNameAndDateRange(String userName, String startDate, String endDate);
}
