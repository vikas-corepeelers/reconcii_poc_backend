package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TRXSetSMState {

    @JacksonXmlProperty(localName = "PODId", isAttribute = true)
    private String podId;
    @JacksonXmlProperty(localName = "POSState")
    private String posState;
    @JacksonXmlProperty(localName = "CrewId")
    private int crewId;
    @JacksonXmlProperty(localName = "CrewName")
    private String crewName;
    @JacksonXmlProperty(localName = "CrewSecurityLevel")
    private int crewSecurityLevel;
    @JacksonXmlProperty(localName = "LoginTime")
    private String loginTime;
    @JacksonXmlProperty(localName = "LogoutTime")
    private String logoutTime;
    @JacksonXmlProperty(localName = "InitialFloat")
    private double initialFloat;
}
