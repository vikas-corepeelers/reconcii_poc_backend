package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Fiscal_Information")
public class FiscalInformation {

    @JacksonXmlProperty(localName = "TIN", isAttribute = true)
    private String tin;
    @JacksonXmlProperty(localName = "ZIP", isAttribute = true)
    private String zip;
    @JacksonXmlProperty(localName = "address", isAttribute = true)
    private String address;
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
}
