package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "EV_Custom")
public class EVCustom {

    @JacksonXmlProperty(localName = "Code")
    private String code;
    @JacksonXmlProperty(localName = "Value")
    private String value;
}