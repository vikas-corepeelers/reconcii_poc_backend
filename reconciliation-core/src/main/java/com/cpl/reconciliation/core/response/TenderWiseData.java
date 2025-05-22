package com.cpl.reconciliation.core.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class TenderWiseData extends DashboardData {

    private String tenderName;
    private List<BankWiseData> bankWiseDataList;

    @JsonProperty(value = "trmSalesData")
    public DashboardData getTrmSalesData() {
        DashboardData trm = new DashboardData();
        if (bankWiseDataList != null) {
            bankWiseDataList.forEach(bankWiseData -> {
                trm.setSales(trm.getSales() + bankWiseData.getSales());
                trm.setReceipts(trm.getReceipts() + bankWiseData.getReceipts());
                trm.setCharges(trm.getCharges() + bankWiseData.getCharges());
                trm.setReconciled(trm.getReconciled() + bankWiseData.getReconciled());
                trm.setPosVsTrm(trm.getPosVsTrm() + bankWiseData.getPosVsTrm());
                trm.setTrmVsMpr(trm.getTrmVsMpr() + bankWiseData.getTrmVsMpr());
                trm.setMprVsBank(trm.getMprVsBank() + bankWiseData.getMprVsBank());
            });
        }
        return trm;
    }
}
