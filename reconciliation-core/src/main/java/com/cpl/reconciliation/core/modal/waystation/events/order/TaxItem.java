package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "taxItem")
public class TaxItem {

    @JacksonXmlProperty(isAttribute = true)
    private double amount;
    @JacksonXmlProperty(isAttribute = true)
    private double baseAmt;
    @JacksonXmlProperty(isAttribute = true)
    private String desc;
    @JacksonXmlProperty(isAttribute = true)
    private int id;
    @JacksonXmlProperty(isAttribute = true)
    private double rate;
}
