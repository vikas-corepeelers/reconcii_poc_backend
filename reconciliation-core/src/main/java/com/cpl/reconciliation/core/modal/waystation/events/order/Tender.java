package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
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
@JacksonXmlRootElement(localName = "Tender")
public class Tender {
    @JacksonXmlProperty(localName = "TenderId")
    private String tenderId;
    @JacksonXmlProperty(localName = "TenderKind")
    private String tenderKind;
    @JacksonXmlProperty(localName = "TenderName")
    private String tenderName;
    @JacksonXmlProperty(localName = "TenderQuantity")
    private String tenderQuantity;
    @JacksonXmlProperty(localName = "FaceValue")
    private String faceValue;
    @JacksonXmlProperty(localName = "TenderAmount")
    private double tenderAmount;
    @JacksonXmlProperty(localName = "BaseAction")
    private String baseAction;
    @JacksonXmlProperty(localName = "Persisted")
    private String persisted;
    @JacksonXmlProperty(localName = "CardProviderID")
    private String cardProviderID;
    @JacksonXmlCData
    @JacksonXmlProperty(localName = "CashlessData")
    private String cashlessData;
    @JacksonXmlProperty(localName = "TaxOption")
    private String taxOption;
    @JacksonXmlProperty(localName = "SubtotalOption")
    private String subtotalOption;
    @JacksonXmlProperty(localName = "ForeignCurrencyIndicator")
    private String foreignCurrencyIndicator;
    @JacksonXmlProperty(localName = "DiscountDescription")
    private String discountDescription;
    @JacksonXmlProperty(localName = "CashlessTransactionID")
    private String cashlessTransactionID;
    @JacksonXmlProperty(localName = "PaymentChannel")
    private String paymentChannel;
    @JacksonXmlElementWrapper(localName = "SourceTenders")
    @JacksonXmlProperty(localName = "SourceTender")
    private List<SourceTender> sourceTenders;

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "SourceTender")
    public static class SourceTender {

        @JacksonXmlProperty(localName = "sourceCode", isAttribute = true)
        private int sourceCode;
        @JacksonXmlProperty(localName = "sourceValue", isAttribute = true)
        private double sourceValue;
    }
}



