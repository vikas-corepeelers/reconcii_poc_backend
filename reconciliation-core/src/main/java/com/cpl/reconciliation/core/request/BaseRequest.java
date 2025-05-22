package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

import static com.cpl.core.api.constant.Formatter.YYYYMMDD_HHMMSS_DASH;

@Getter
@Setter
@ToString
public class BaseRequest {

    @NotEmpty(message = "Please select start date")
    protected String startDate;
    @NotEmpty(message = "Please select end date")
    protected String endDate;

    public LocalDateTime getStartLocalDate() {
        return LocalDateTime.parse(startDate, YYYYMMDD_HHMMSS_DASH);
    }

    public LocalDateTime getEndLocalDate() {
        return LocalDateTime.parse(endDate, YYYYMMDD_HHMMSS_DASH);
    }
}
