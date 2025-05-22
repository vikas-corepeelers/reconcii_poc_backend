package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Ev_Custom")
public class EVvCustom {

    @JacksonXmlProperty(localName = "Info")
    private Info info;
}

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Info")
class Info{

    @JacksonXmlProperty(localName = "code", isAttribute = true)
    private String code;
    @JacksonXmlProperty(localName = "data", isAttribute = true)
    private String data;
}
