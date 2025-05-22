package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Offers")
public class Offers {

    @JacksonXmlProperty(localName = "applied")
    private boolean applied;
    @JacksonXmlProperty(localName = "customerId")
    private String customerId;
    @JacksonXmlProperty(localName = "customerType")
    private String customerType;
    @JacksonXmlProperty(localName = "offerBarcodeType")
    private String offerBarcodeType;
    @JacksonXmlProperty(localName = "offerId")
    private String offerId;
    @JacksonXmlProperty(localName = "offerName")
    private String offerName;
    @JacksonXmlProperty(localName = "offerType")
    private String offerType;
    @JacksonXmlProperty(localName = "override")
    private boolean override;
    @JacksonXmlProperty(localName = "promotionId")
    private String promotionId;
}
