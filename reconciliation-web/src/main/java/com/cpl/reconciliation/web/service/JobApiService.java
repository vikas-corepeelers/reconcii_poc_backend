package com.cpl.reconciliation.web.service;

import com.cpl.reconciliation.core.enums.DataSource;

public interface JobApiService {

    void runJob(DataSource jobName);
}
