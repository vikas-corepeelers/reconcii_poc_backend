/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.cpl.reconciliation.web.service.beans;

import java.time.LocalDateTime;
import lombok.Data;

/**
 *
 * @author Abhishek N
 */
@Data
public class AuditLogRequest {

    private String userName;

    private String userEmail;

    private String systemIp;

    private String role;

    private String action;

    private String reqData;

    private String resData;
    
    private String remarks;
}
