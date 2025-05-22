package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeneratedReportResponse {
    private Long id;
    private String startDate;
    private String endDate;
    private List<String> stores;
    private String status;
    private String reportType;
    private String bank;
    private String tender;
    private String fileName;
    private double fileSize;
    private String createdAt;
}
