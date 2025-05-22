package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Ev_SetCOD")
public class EvSetCOD {

    @JacksonXmlProperty(localName = "COD", isAttribute = true)
    private String cod;
    @JacksonXmlProperty(localName = "POD", isAttribute = true)
    private String pod;
    @JacksonXmlProperty(localName = "WorkingMode", isAttribute = true)
    private String workingMode;
}
