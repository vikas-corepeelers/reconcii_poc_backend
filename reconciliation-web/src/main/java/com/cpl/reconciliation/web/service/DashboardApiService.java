package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public interface DashboardApiService {

    DashboardDataResponse getDashboardDataResponse(DashboardDataRequest request);

    DashboardInStoreDataResponse getDashboardInStoreDataResponse(DashboardDataRequest request);

    List<ReportingTendersDataResponse> getReportingTenders();

    List<ReceiptData> receiptsData(DashboardDataRequest request);

    void receiptsDownload(DashboardDataRequest request, OutputStream outputStream);

    void chargesDownload(DashboardDataRequest request, OutputStream outputStream);

    List<ReconciledData> getReconciledData(DashboardDataRequest request);

    void reconciledDownload(DashboardDataRequest request, OutputStream outputStream);

    UnreconciledDataWrapper getUnreconciledData(DashboardDataRequest request);

    void unreconciledDownload(DashboardDataRequest request, OutputStream outputStream);

    void reportDownload(DashboardDataRequest request, OutputStream writer);

    HashMap<String, HashMap<String, String>> missingTIDMapping();

    void downloadMissingTIDReport(MissingTIDReportRequest request, ByteArrayOutputStream outputStream) throws IOException;

    DashboardDataResponse getInstoreDashboardResponse(DashboardDataRequest request);
}
