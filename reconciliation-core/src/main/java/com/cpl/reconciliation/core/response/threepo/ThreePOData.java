package com.cpl.reconciliation.core.response.threepo;

import com.cpl.reconciliation.core.enums.ThreePO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ThreePOData extends Data implements Comparable<ThreePOData> {
    private ThreePO tenderName;

    @Override
    public int compareTo(ThreePOData o) {
        if (o.getPosSales() > this.posSales) return 1;
        else if (o.getPosSales() == this.posSales) return 0;
        return -1;
    }

}
