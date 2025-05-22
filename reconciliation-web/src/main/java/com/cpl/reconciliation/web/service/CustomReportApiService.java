package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.request.CustomOrderDataRequest;
import com.cpl.reconciliation.domain.entity.CustomisedReportFilter;
import com.opencsv.CSVWriter;

import java.util.List;

public interface CustomReportApiService {
    void getCustomisedData(CustomOrderDataRequest request, CSVWriter csvWriter);

    List<CustomisedReportFilter> getCustomisedReportFields(String category);

}
