package com.cpl.reconciliation.core.request;

import com.cpl.core.api.constant.Formatter;
import com.cpl.core.api.util.DateToString;
import com.cpl.core.api.util.StringUtils;
import com.cpl.reconciliation.core.enums.ReportType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
public class DashboardDataRequest extends BaseRequest {

    private List<String> stores;
    private String bank;
    private String tender;
    private ReportType reportType;

    @JsonIgnore
    public String getDownloadFileName() {
        String fineName = reportType.name();
        if (StringUtils.isNotEmpty(tender)) {
            fineName = fineName + "_" + tender;
        }
        if (StringUtils.isNotEmpty(bank)) {
            fineName = fineName + "_" + bank;
        }
        fineName = fineName + "_" + DateToString.currentDateString(Formatter.DDMMYYYY_HHMMSS_DASH) + ".xlsx";
        return fineName;
    }
}
