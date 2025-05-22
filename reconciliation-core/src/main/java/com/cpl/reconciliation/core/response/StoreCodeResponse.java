package com.cpl.reconciliation.core.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StoreCodeResponse {

    private String name;
    private String code;
    private String state;
    private String city;
    private boolean posDataSync;
}
