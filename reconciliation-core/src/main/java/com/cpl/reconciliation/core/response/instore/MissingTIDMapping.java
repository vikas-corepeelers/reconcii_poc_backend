package com.cpl.reconciliation.core.response.instore;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MissingTIDMapping {
    protected String tender;
    protected String bank;
    protected int missing;
    protected int total;

}
