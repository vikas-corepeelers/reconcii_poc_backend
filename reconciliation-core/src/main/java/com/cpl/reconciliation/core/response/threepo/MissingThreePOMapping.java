package com.cpl.reconciliation.core.response.threepo;

import com.cpl.reconciliation.core.enums.ThreePO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MissingThreePOMapping {
    protected double missing;
    protected double totalStores;
    protected ThreePO threePO;


}
