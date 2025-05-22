package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "TRX_TenderTable")
public class TRXTenderTable {

    @JacksonXmlElementWrapper(localName = "TenderType", useWrapping = false)
    @JacksonXmlProperty(localName = "TenderType")
    private List<TenderType> tenderTypes;

    @Override
    public String toString() {
        return "TRX_TenderTable {" +
                "TenderType=" + (CollectionUtils.isEmpty(tenderTypes) ? 0 : tenderTypes.size()) +
                '}';
    }
}
