package com.cpl.reconciliation.core.request.threepo;

import com.cpl.reconciliation.core.enums.ReportType;
import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.core.request.BaseRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class ThreePODataRequest extends BaseRequest {

    private List<String> stores;
    private ThreePO tender;
    private ReportType reportType;

    public String getFileName() {
        if (tender != null) {
            return getReportType() + "_" + tender.displayName + "_" + getStartLocalDate().toLocalDate() + "_" + getEndLocalDate().toLocalDate() + ".xlsx";
        }
        return getReportType() + "_" + getStartLocalDate().toLocalDate() + "_" + getEndLocalDate().toLocalDate() + ".xlsx";
    }
}
