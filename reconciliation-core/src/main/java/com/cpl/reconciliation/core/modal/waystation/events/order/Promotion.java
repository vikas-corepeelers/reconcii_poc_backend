package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "Promotion")
public class Promotion {

    @JacksonXmlProperty(localName = "countTowardsPromotionLimit")
    private boolean countTowardsPromotionLimit;
    @JacksonXmlProperty(localName = "discountAmount")
    private double discountAmount;
    @JacksonXmlProperty(localName = "discountType")
    private String discountType;
    @JacksonXmlProperty(localName = "exclusive")
    private boolean exclusive;
    @JacksonXmlProperty(localName = "offerId")
    private int offerId;
    @JacksonXmlProperty(localName = "promotionCounter")
    private int promotionCounter;
    @JacksonXmlProperty(localName = "promotionId")
    private int promotionId;
    @JacksonXmlProperty(localName = "promotionName")
    private String promotionName;
    @JacksonXmlProperty(localName = "promotionOnTender")
    private boolean promotionOnTender;
    @JacksonXmlProperty(localName = "returnedValue")
    private double returnedValue;
}

