package com.cpl.reconciliation.core.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ReportingTendersDataResponse {
    private String category;
    private List<Tender> tenders;

    public void addTender(String displayName, String technicalName) {
        if (tenders == null) tenders = new ArrayList<>();
        tenders.add(new Tender(displayName, technicalName));
    }

    @AllArgsConstructor
    @Getter
    @Setter
    class Tender {
        String displayName;
        String technicalName;
    }
}
