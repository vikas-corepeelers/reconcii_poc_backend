package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TaxType")
public class TaxType {

    @JacksonXmlProperty(localName = "TaxId")
    private int taxId;
    @JacksonXmlProperty(localName = "TaxDescription")
    private String taxDescription;
    @JacksonXmlProperty(localName = "TaxRate")
    private double taxRate;
    @JacksonXmlProperty(localName = "TaxBasis")
    private String taxBasis;
    @JacksonXmlProperty(localName = "TaxCalcType")
    private String taxCalcType;
    @JacksonXmlProperty(localName = "Rounding")
    private String rounding;
    @JacksonXmlProperty(localName = "Precision")
    private int precision;
}
