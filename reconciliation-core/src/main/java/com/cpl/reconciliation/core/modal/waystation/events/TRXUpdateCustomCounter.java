package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_UpdateCustomCounter")
public class TRXUpdateCustomCounter {

    @JacksonXmlProperty(localName = "counterName", isAttribute = true)
    private String counterName;
    @JacksonXmlProperty(localName = "counterValue", isAttribute = true)
    private double counterValue;
    @JacksonXmlProperty(localName = "operation", isAttribute = true)
    private String operation;
    @JacksonXmlProperty(localName = "operationValue", isAttribute = true)
    private double operationValue;
}
