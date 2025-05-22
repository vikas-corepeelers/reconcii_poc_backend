package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ManualUploadTenderResponse {

    String tender;
    List<ManualUploadTypeResponse> types;
}
