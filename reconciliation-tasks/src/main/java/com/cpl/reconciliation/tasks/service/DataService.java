package com.cpl.reconciliation.tasks.service;

import com.cpl.reconciliation.core.enums.DataSource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface DataService {

    DataSource getDataSource();

    void executeTask() throws Exception;

    default boolean uploadManually(LocalDate businessDate, LocalDate endDate, List<MultipartFile> file) throws IOException {
        return true;
    };
}
