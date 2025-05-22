package com.cpl.reconciliation.core.response;

import com.cpl.core.api.serializer.LocalDateSerialize;
import com.cpl.reconciliation.core.enums.DataSource;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@ToString
public class DataSourceLogResponse {

    @JsonSerialize(using = LocalDateSerialize.class)
    private LocalDate lastReconciled;
    private List<LastSync> lastSyncList = new LinkedList<>();

    @Getter
    @Setter
    public static class LastSync {
        private String tender;
        private String type;
        private DataSource dataSource;
        @JsonSerialize(using = LocalDateSerialize.class)
        private LocalDate lastSynced;
    }

}
