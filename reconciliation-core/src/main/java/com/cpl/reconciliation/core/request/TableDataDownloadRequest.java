package com.cpl.reconciliation.core.request;

import com.cpl.reconciliation.core.enums.DataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TableDataDownloadRequest extends BaseRequest{
    private DataSource dataSource;
}
