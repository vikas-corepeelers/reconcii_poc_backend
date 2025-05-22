package com.cpl.reconciliation.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class DashboardDataResponse extends DashboardData {

    private List<TenderWiseData> tenderWiseDataList;

    @JsonProperty(value = "trmSalesData")
    public DashboardData getTrmSalesData() {
        DashboardData trm = new DashboardData();
        if (tenderWiseDataList != null) {
            tenderWiseDataList.forEach(tenderWiseData -> {
                trm.setSales(trm.getSales() + tenderWiseData.getTrmSalesData().getSales());
                trm.setReceipts(trm.getReceipts() + tenderWiseData.getTrmSalesData().getReceipts());
                trm.setCharges(trm.getCharges() + tenderWiseData.getTrmSalesData().getCharges());
                trm.setReconciled(trm.getReconciled() + tenderWiseData.getTrmSalesData().getReconciled());
                trm.setPosVsTrm(trm.getPosVsTrm() + tenderWiseData.getTrmSalesData().getPosVsTrm());
                trm.setTrmVsMpr(trm.getTrmVsMpr() + tenderWiseData.getTrmSalesData().getTrmVsMpr());
                trm.setMprVsBank(trm.getMprVsBank() + tenderWiseData.getTrmSalesData().getMprVsBank());
            });
        }
        return trm;
    }
}
