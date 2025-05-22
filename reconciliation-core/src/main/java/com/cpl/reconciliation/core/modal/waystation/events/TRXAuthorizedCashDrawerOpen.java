package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_Authorized_Cash_Drawer_Open")
public class TRXAuthorizedCashDrawerOpen {

    @JacksonXmlProperty(localName = "Type")
    private String type;
    @JacksonXmlProperty(localName = "Reason")
    private String reason;
    @JacksonXmlProperty(localName = "Amount")
    private String amount;
    @JacksonXmlProperty(localName = "Time")
    private String time;
}