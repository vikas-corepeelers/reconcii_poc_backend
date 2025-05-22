package com.cpl.reconciliation.core.modal.waystation.events.order;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Reduction")
public class Reduction {

    @JacksonXmlProperty(localName = "Qty")
    private int qty;
    @JacksonXmlProperty(localName = "AfterTotal")
    private int afterTotal;
    @JacksonXmlProperty(localName = "BeforeTotal")
    private int beforeTotal;
    @JacksonXmlProperty(localName = "Amount")
    private double amount;
    @JacksonXmlProperty(localName = "AmountAfterTotal")
    private double amountAfterTotal;
    @JacksonXmlProperty(localName = "AmountBeforeTotal")
    private double amountBeforeTotal;
}

