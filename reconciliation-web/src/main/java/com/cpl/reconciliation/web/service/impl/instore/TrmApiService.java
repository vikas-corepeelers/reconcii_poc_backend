package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.request.MissingTIDReportRequest;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.TrmData;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public interface TrmApiService {

    List<TrmData> getTrmData(DashboardDataRequest request);

    void trmDownload(DashboardDataRequest request, OutputStream outputStream);

    DashboardDataResponse getTrmVsMprData(DashboardDataRequest request);

    void getTrmVsMprDataDownload(DashboardDataRequest request, OutputStream outputStream);

    HashMap<String, HashMap<String, String>> getMissingTidMapping();

    void downloadMissingTIDMapping(MissingTIDReportRequest request , Workbook workbook);
}
