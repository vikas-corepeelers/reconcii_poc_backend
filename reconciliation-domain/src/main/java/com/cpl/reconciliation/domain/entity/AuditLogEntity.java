package com.cpl.reconciliation.domain.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Abhishek N
 */
@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLogEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "username", nullable = false, length = 45)
    private String userName;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "role", nullable = false, length = 100)
    private String role;

    @Column(name = "system_ip", nullable = false, length = 45)
    private String systemIp;

    @Column(name = "action", nullable = false, length = 255)
    private String action;

    @Column(name = "request", columnDefinition = "TEXT")
    private String request;

    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

}
