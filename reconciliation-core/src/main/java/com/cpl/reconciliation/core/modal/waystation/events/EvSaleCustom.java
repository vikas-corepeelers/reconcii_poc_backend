package com.cpl.reconciliation.core.modal.waystation.events;

import com.cpl.reconciliation.core.modal.waystation.events.order.Info;
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
@JacksonXmlRootElement(localName = "Ev_SaleCustom")
public class EvSaleCustom {

    @JacksonXmlElementWrapper(localName = "Info", useWrapping = false)
    @JacksonXmlProperty(localName = "Info")
    private List<Info> infoList;
}
