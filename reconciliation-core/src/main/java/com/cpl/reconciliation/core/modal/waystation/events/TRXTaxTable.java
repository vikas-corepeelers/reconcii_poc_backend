package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_TaxTable")
public class TRXTaxTable {

    @JacksonXmlElementWrapper(localName = "TaxType", useWrapping = false)
    @JacksonXmlProperty(localName = "TaxType")
    private List<TaxType> taxTypes;

    @Override
    public String toString() {
        return "TRX_TaxTable {" +
                "TaxType=" + (CollectionUtils.isEmpty(taxTypes) ? 0 : taxTypes.size()) +
                '}';
    }
}
