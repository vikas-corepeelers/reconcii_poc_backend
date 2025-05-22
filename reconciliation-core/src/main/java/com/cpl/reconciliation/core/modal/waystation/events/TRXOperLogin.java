package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_OperLogin")
public class TRXOperLogin {

    @JacksonXmlProperty(localName = "CrewID")
    private int crewID;
    @JacksonXmlProperty(localName = "CrewName")
    private String crewName;
    @JacksonXmlProperty(localName = "CrewSecurityLevel")
    private int crewSecurityLevel;
    @JacksonXmlProperty(localName = "POD")
    private String pos;
    @JacksonXmlProperty(localName = "RemotePOD")
    private String remotePOD;
    @JacksonXmlProperty(localName = "AutoLogin")
    private boolean autoLogin;
}
