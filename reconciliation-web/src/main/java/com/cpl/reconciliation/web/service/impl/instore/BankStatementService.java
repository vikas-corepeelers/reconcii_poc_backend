package com.cpl.reconciliation.web.service.impl.instore;

import com.cpl.reconciliation.core.request.DashboardDataRequest;
import com.cpl.reconciliation.core.response.instore.BankStatementData;

import java.util.List;

public interface BankStatementService {

    List<BankStatementData> getBankStatementDownload(DashboardDataRequest request);
}
