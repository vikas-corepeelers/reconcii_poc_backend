package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_ModifyExchangeRate")
public class TRXModifyExchangeRate {

    @JacksonXmlProperty(localName = "tenderId", isAttribute = true)
    private int tenderId;
    @JacksonXmlProperty(localName = "newRate", isAttribute = true)
    private double newRate;
    @JacksonXmlProperty(localName = "oldRate", isAttribute = true)
    private double oldRate;
}
