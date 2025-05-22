package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Ev_DrawerClose")
public class EvDrawerClose {

    @JacksonXmlProperty(localName = "TotalOpenTime", isAttribute = true)
    private int totalOpenTime;
    @JacksonXmlProperty(localName = "UnauthorizedOpen", isAttribute = true)
    private boolean unauthorizedOpen;
}
