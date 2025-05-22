package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "COD")
public class Cod {

    @JacksonXmlProperty(localName = "number", isAttribute = true)
    private String number;
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
}
