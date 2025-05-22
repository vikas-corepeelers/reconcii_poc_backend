package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "POSTimings")
public class POSTimings {

    @JacksonXmlProperty(isAttribute = true)
    private int itemsCount;
    @JacksonXmlProperty(isAttribute = true)
    private String untilPay;
    @JacksonXmlProperty(isAttribute = true)
    private String untilStore;
    @JacksonXmlProperty(isAttribute = true)
    private String untilTotal;
}
