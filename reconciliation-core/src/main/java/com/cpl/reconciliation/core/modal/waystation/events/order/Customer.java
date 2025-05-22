package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Customer")
public class Customer {

    @JacksonXmlProperty(localName = "greeting")
    private String greeting;
    @JacksonXmlProperty(localName = "id")
    private String id;
    @JacksonXmlProperty(localName = "loyaltyCardId")
    private String loyaltyCardId;
    @JacksonXmlProperty(localName = "loyaltyCardType")
    private String loyaltyCardType;
    @JacksonXmlProperty(localName = "nickname")
    private String nickname;
}
