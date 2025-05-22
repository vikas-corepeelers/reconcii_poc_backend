package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_GetAuthorization")
public class TRXGetAuthorization {

    @JacksonXmlProperty(localName = "Action")
    private String action;
    @JacksonXmlProperty(localName = "ManagerID")
    private int managerID;
    @JacksonXmlProperty(localName = "ManagerName")
    private String managerName;
    @JacksonXmlProperty(localName = "SecurityLevel")
    private int securityLevel;
    @JacksonXmlProperty(localName = "ExpirationDate")
    private String expirationDate;
    @JacksonXmlProperty(localName = "Password")
    private String password;
    @JacksonXmlProperty(localName = "IsLogged")
    private boolean isLogged;
    @JacksonXmlProperty(localName = "Method")
    private String method;
}
