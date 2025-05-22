package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@ToString
public class StoreByCitiesRequest {

    private List<String> cities;
    @NotEmpty(message = "Please select start date!")
    private String startDate;
    @NotEmpty(message = "Please select end date!")
    private String endDate;
}
