package com.cpl.reconciliation.core.response;

import com.cpl.reconciliation.core.enums.DataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ManualUploadTypeResponse {

    String type;
    boolean isActive;
    DataSource dataSource;
}
