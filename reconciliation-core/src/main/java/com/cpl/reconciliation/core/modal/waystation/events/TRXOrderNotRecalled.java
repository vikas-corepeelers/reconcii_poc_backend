package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_OrderNotRecalled")
public class TRXOrderNotRecalled {

    @JacksonXmlElementWrapper(localName = "OrderNotRecalled", useWrapping = false)
    @JacksonXmlProperty(localName = "OrderNotRecalled")
    private List<OrderNotRecalled> orderNotRecalledList;
}

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "OrderNotRecalled")
class OrderNotRecalled {

    @JacksonXmlProperty(localName = "key", isAttribute = true)
    private String key;
    @JacksonXmlProperty(localName = "major", isAttribute = true)
    private int major;
    @JacksonXmlProperty(localName = "minor", isAttribute = true)
    private int minor;
    @JacksonXmlProperty(localName = "totalAmount", isAttribute = true)
    private double totalAmount;
}
