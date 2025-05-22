package com.cpl.reconciliation.domain.entity;

import com.cpl.reconciliation.domain.converter.LocalDateTimeConverter;
import com.cpl.reconciliation.domain.entity.enums.GeneratedReportStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "generated_reports")
public class GeneratedReports extends BaseMetaEntity {

    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime startDate;
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime endDate;
    @Column(columnDefinition = "longtext")
    private String stores;
    @Enumerated(value = EnumType.STRING)
    private GeneratedReportStatus status;
    private String reportType;
    private String tender;
    private String bank;
    private String path;
    private double fileSize;

    public String getFileName() {
        if (tender != null) {
            if (bank==null || bank.isEmpty())
                return getReportType() + "_" + tender + "_" + getStartDate().toLocalDate() + "_" + getEndDate().toLocalDate() + ".xlsx";
            else
                return getReportType() + "_" + tender + "_"+ bank + "_" + getStartDate().toLocalDate() + "_" + getEndDate().toLocalDate() + ".xlsx";
        }
        return getReportType() + "_" + getStartDate().toLocalDate() + "_" + getEndDate().toLocalDate() + ".xlsx";
    }
}
