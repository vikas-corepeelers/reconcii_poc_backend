package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_OperLogout")
public class TRXOperLogout {

    @JacksonXmlProperty(localName = "CrewID")
    private int crewID;
    @JacksonXmlProperty(localName = "CrewName")
    private String crewName;
    @JacksonXmlProperty(localName = "AutoLogout")
    private boolean autoLogout;
}

