package com.cpl.reconciliation.core.response;

import com.cpl.reconciliation.core.request.StoreRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class StoreResponse extends StoreRequest {

    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
