package com.cpl.reconciliation.core.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class CustomOrderDataRequest extends BaseRequest {

    @NotBlank(message = "Tender is required")
    private String tender;
    private List<String> required_fields;

}
