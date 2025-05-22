package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.reconciliation.core.modal.query.MprBankDifference;
import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.MprData;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.OutputStream;
import java.util.List;

public interface MprApiService {

    DashboardDataResponse getMprVsBankData(DashboardDataRequest request);

    List<MprData> getMprDownload(DashboardDataRequest request);

    void getMprVsBankDataDownload(DashboardDataRequest request, OutputStream outputStream);

    void downloadMpr(DashboardDataRequest request, OutputStream outputStream);

    List<MprBankDifference> getMprBankDifference(DashboardDataRequest request);

    String getAmexMissingTidMapping();

    void downloadAmexMissingTidMapping(MissingTIDReportRequest request, SXSSFWorkbook workbook);
}
