package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ToString
public class StoreByStatesRequest {

    private List<String> states;
    @NotEmpty(message = "Please select start date!")
    private String startDate;
    @NotEmpty(message = "Please select end date!")
    private String endDate;
}
