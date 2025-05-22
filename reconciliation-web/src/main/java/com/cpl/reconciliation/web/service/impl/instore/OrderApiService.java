package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.response.DashboardDataResponse;
import com.cpl.reconciliation.core.response.SaleDataWrapper;

import java.io.OutputStream;

public interface OrderApiService {

    SaleDataWrapper getSaleData(DashboardDataRequest request);

    void saleDownload(DashboardDataRequest request, OutputStream outputStream);

    DashboardDataResponse getOrderVsTrmData(DashboardDataRequest request);

    void getOrderVsTrmDownload(DashboardDataRequest request, OutputStream outputStream);

    void cashRecoDownload(DashboardDataRequest request, OutputStream outputStream);

    void salesVsPickUpDownload(DashboardDataRequest request, OutputStream outputStream);

    void pickupVsReceiptsDownload(DashboardDataRequest request, OutputStream outputStream);
}
