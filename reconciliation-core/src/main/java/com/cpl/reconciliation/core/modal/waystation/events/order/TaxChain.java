package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TaxChain")
public class TaxChain {

    @JacksonXmlProperty(localName = "BPAmount", isAttribute = true)
    private double bpAmount;
    @JacksonXmlProperty(localName = "BPBaseAmount", isAttribute = true)
    private double bpBaseAmount;
    @JacksonXmlProperty(localName = "BDAmount", isAttribute = true)
    private double bdAmount;
    @JacksonXmlProperty(localName = "BDBaseAmount", isAttribute = true)
    private double bdBaseAmount;
    @JacksonXmlProperty(localName = "amount", isAttribute = true)
    private String amount;
    @JacksonXmlProperty(localName = "baseAmount", isAttribute = true)
    private String baseAmount;
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private int id;
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
    @JacksonXmlProperty(localName = "rate", isAttribute = true)
    private double rate;
    @JacksonXmlProperty(localName = "calculationSequence", isAttribute = true)
    private int calculationSequence;
    @JacksonXmlProperty(localName = "taxChainId", isAttribute = true)
    private int taxChainId;
}