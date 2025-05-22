package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_LogToggleRunnerBox")
public class TRXLogToggleRunnerBox {

    @JacksonXmlProperty(localName = "ManagerID")
    private String managerID;
    @JacksonXmlProperty(localName = "ManagerName")
    private String managerName;
    @JacksonXmlProperty(localName = "SecurityLevel")
    private String securityLevel;
}
