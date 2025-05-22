package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.request.threepo.ThreePODataRequest;
import com.cpl.reconciliation.core.response.threepo.DataResponse;
import com.cpl.reconciliation.core.response.threepo.GeneratedReportResponse;
import com.cpl.reconciliation.core.response.threepo.MissingThreePOMapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ThreePoApiService {
    DataResponse getDashboardDataResponse(ThreePODataRequest request);

  //  GeneratedReportResponse getPOSvs3POResponse(ThreePODataRequest request);

  //  void getPOSvs3PODownload(ThreePODataRequest request, ByteArrayOutputStream outputStream) throws IOException;

    void reportDownload(ThreePODataRequest request, OutputStream outputStream) throws IOException;

    List<MissingThreePOMapping> getThreePoMissingMapping();

    void downloadThreePoMissingMapping(String threePo, ByteArrayOutputStream outputStream) throws IOException;

    DataResponse getThreePoDashboardDataResponse(ThreePODataRequest request);
}
