package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_RegTransfer")
public class TRXRegTransfer {

    @JacksonXmlProperty(localName = "Amount", isAttribute = true)
    private double amount;
    @JacksonXmlProperty(localName = "FaceValue", isAttribute = true)
    private double faceValue;
    @JacksonXmlProperty(localName = "OperType", isAttribute = true)
    private String operType;
    @JacksonXmlProperty(localName = "TenderID", isAttribute = true)
    private int tenderID;
    @JacksonXmlProperty(localName = "TenderName", isAttribute = true)
    private String tenderName;
    @JacksonXmlProperty(localName = "TransferType", isAttribute = true)
    private String transferType;
}
