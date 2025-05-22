package com.cpl.reconciliation.web.service.impl;

import com.cpl.core.api.constant.Formatter;
import com.cpl.core.api.util.AuthUtils;
import com.cpl.core.api.util.DateToString;
import com.cpl.core.api.util.StringUtils;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.GeneratedReportRequest;
import com.cpl.reconciliation.core.request.GeneratedReportResponse;
import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.domain.entity.GeneratedReports;
import com.cpl.reconciliation.domain.entity.enums.GeneratedReportStatus;
import com.cpl.reconciliation.domain.repository.GeneratedReportsRepository;
import com.cpl.reconciliation.web.service.DashboardApiService;
import com.cpl.reconciliation.web.service.ThreePoApiService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@Data
public class ReportGenerationServiceImpl {

    private final static String FILE_SEPARATOR = System.getProperty("file.separator");

    @Autowired
    @Qualifier(value = "asyncExecutor")
    private final Executor asyncExecutor;
    private final GeneratedReportsRepository generatedReportsRepository;
    private final ThreePoApiService threePoApiService;
    private final DashboardApiService dashboardApiService;

    public boolean downloadThreePOReport(ThreePODataRequest request) throws IOException {
        GeneratedReports report = new GeneratedReports();
        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        report.setReportType(request.getReportType().name());
        Collections.sort(request.getStores());
        report.setStores(String.join(",", request.getStores()));
        report.setTender(request.getTender() == null ? "ALL" : request.getTender().name());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setStatus(GeneratedReportStatus.IN_PROGRESS);
        report = generatedReportsRepository.save(report);
        GeneratedReports finalReport = report;
        String fileName = finalReport.getId() + ".xlsx";
        String date = DateToString.currentDateString(Formatter.YYYYMMDD);
        String outputPath = FILE_SEPARATOR + "home" + FILE_SEPARATOR + "ubuntu" + FILE_SEPARATOR + "generated_reports" + FILE_SEPARATOR + "three_po" + FILE_SEPARATOR + date;
        // String outputPath = "C:\\Users\\Abhishek N\\Documents\\Work\\Subway\\downloads\\" + fileName;
        Files.createDirectories(Path.of(outputPath));
        String filePath = outputPath + FILE_SEPARATOR + fileName;

        CompletableFuture.runAsync(() -> {
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                threePoApiService.reportDownload(request, outputStream);
                long fileSizeInBytes = new File(filePath).length();
                double sizeInMB = (double) fileSizeInBytes / (1024 * 1024);
                sizeInMB = Math.floor(sizeInMB * 100) / 100;
                finalReport.setFileSize(sizeInMB);
                finalReport.setStatus(GeneratedReportStatus.SUCCESS);
                finalReport.setPath(filePath);
            } catch (Exception e) {
                finalReport.setStatus(GeneratedReportStatus.FAILED);
                log.error("Error generated report for id: {}", finalReport.getId());
                e.printStackTrace();
            } finally {
                generatedReportsRepository.save(finalReport);
            }
        }, asyncExecutor);
        return true;
    }

    public boolean downloadInStoreReport(DashboardDataRequest request) throws IOException {
        GeneratedReports report = new GeneratedReports();
        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate(), Formatter.YYYYMMDD_HHMMSS_DASH);
        report.setReportType(request.getReportType().name());
        Collections.sort(request.getStores());
        if (StringUtils.isNotEmpty(request.getBank())) {
            report.setBank(request.getBank());
        } else {
            report.setBank("ALL");
        }
        report.setStores(String.join(",", request.getStores()));
        report.setTender(request.getTender() == null ? "ALL" : request.getTender());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setStatus(GeneratedReportStatus.IN_PROGRESS);
        report = generatedReportsRepository.save(report);
        GeneratedReports finalReport = report;
        String fileName = finalReport.getId() + ".xlsx";
        String date = DateToString.currentDateString(Formatter.YYYYMMDD);

        String outputPath = FILE_SEPARATOR + "home" + FILE_SEPARATOR + "ubuntu" + FILE_SEPARATOR + "generated_reports" + FILE_SEPARATOR + "in_store" + FILE_SEPARATOR + date;
        System.out.println("outputPath : "+outputPath);
        // String outputPath = "C:\\Users\\Abhishek N\\Documents\\Work\\Subway\\downloads\\instore\\" + fileName;
        Files.createDirectories(Path.of(outputPath));
        String filePath = outputPath + FILE_SEPARATOR + fileName;

        CompletableFuture.runAsync(() -> {
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                dashboardApiService.reportDownload(request, outputStream);
                long fileSizeInBytes = new File(filePath).length();
                double sizeInMB = (double) fileSizeInBytes / (1024 * 1024);
                sizeInMB = Math.floor(sizeInMB * 100) / 100;
                finalReport.setFileSize(sizeInMB);
                finalReport.setStatus(GeneratedReportStatus.SUCCESS);
                finalReport.setPath(filePath);
            } catch (Exception e) {
                finalReport.setStatus(GeneratedReportStatus.FAILED);
                log.error("Error generated report for id: {}", finalReport.getId());
                e.printStackTrace();
            } finally {
                generatedReportsRepository.save(finalReport);
            }
        }, asyncExecutor);
        return true;
    }

    @Transactional
    public String[] getGeneratedReportPathAndName(long id) {
        GeneratedReports report = generatedReportsRepository.getOne(id);
        return new String[]{report.getPath(), report.getFileName()};
    }

    @Transactional
    public List<GeneratedReportResponse> getAllGeneratedReports(GeneratedReportRequest request) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(8);
        LocalDateTime endDate = LocalDateTime.now();
        String reportType = request.getReportType() == null ? null : request.getReportType().name();
        String username = AuthUtils.principal().getUsername();
        List<GeneratedReports> reports = generatedReportsRepository.getReports(startDate, endDate, request.getTender(), reportType, request.getBank(), username);
        List<GeneratedReportResponse> responses = new ArrayList<>();
        for (GeneratedReports report : reports) {
            responses.add(mapEntityToResponse(report));
        }
        return responses;
    }

    @Transactional
    public GeneratedReportResponse getGeneratedReport(long id) {
        GeneratedReports report = generatedReportsRepository.getOne(id);
        return mapEntityToResponse(report);
    }

    GeneratedReportResponse mapEntityToResponse(GeneratedReports report) {
        GeneratedReportResponse response = new GeneratedReportResponse();
        response.setId(report.getId());
        response.setBank(report.getBank());
        response.setStatus(report.getStatus().name());
        response.setReportType(report.getReportType());
        response.setTender(report.getTender());
        response.setStores(Arrays.stream(report.getStores().split(",")).toList());
        response.setStartDate(report.getStartDate().toLocalDate().format(Formatter.MMMdyyyy_SOD));
        response.setEndDate(report.getEndDate().toLocalDate().format(Formatter.MMMdyyyy_SOD));
        response.setFileName(GeneratedReportStatus.SUCCESS.equals(report.getStatus()) ? report.getFileName() : null);
        response.setFileSize(report.getFileSize());
        response.setCreatedAt(report.getAdded().format(Formatter.DDMMMYYHHMMSS));
        return response;
    }

}
