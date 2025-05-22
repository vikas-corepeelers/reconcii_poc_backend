package com.cpl.reconciliation.core.modal.waystation.events;

import com.cpl.reconciliation.core.modal.waystation.events.order.Order;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "TRX_Sale")
public class TRXSale {

    @JacksonXmlProperty(localName = "POD", isAttribute = true)
    private String pod;
    @JacksonXmlProperty(localName = "RemPOD", isAttribute = true)
    private String remPod;
    @JacksonXmlProperty(localName = "status", isAttribute = true)
    private String status;
    @JacksonXmlProperty(localName = "Order")
    private Order order;

    @Override
    public String toString() {
        return "TRXSale{" +
                "pod='" + pod + '\'' +
                ", remPod='" + remPod + '\'' +
                ", status='" + status + '\'' +
                ", order=" + order +
                '}';
    }
}
