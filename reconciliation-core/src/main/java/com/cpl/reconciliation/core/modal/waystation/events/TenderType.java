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
@JacksonXmlRootElement(localName = "TenderType")
public class TenderType {

    @JacksonXmlProperty(localName = "TenderId")
    private int tenderId;
    @JacksonXmlProperty(localName = "TenderFiscalIndex")
    private int tenderFiscalIndex;
    @JacksonXmlProperty(localName = "TenderName")
    private String tenderName;
    @JacksonXmlProperty(localName = "TenderCategory")
    private String tenderCategory;
    @JacksonXmlProperty(localName = "TaxOption")
    private String taxOption;
    @JacksonXmlProperty(localName = "DefaultSkimLimit")
    private int defaultSkimLimit;
    @JacksonXmlProperty(localName = "DefaultHaloLimit")
    private int defaultHaloLimit;
    @JacksonXmlProperty(localName = "SubtotalOption")
    private String subtotalOption;
    @JacksonXmlProperty(localName = "CurrencyDecimals")
    private int currencyDecimals;
    @JacksonXmlElementWrapper(localName = "TenderFlags")
    @JacksonXmlProperty(localName = "TenderFlag")
    private List<String> tenderFlags;
    @JacksonXmlProperty(localName = "TenderChange")
    private TenderChange tenderChange;
    @JacksonXmlProperty(localName = "ElectronicPayment")
    private OtherPayment electronicPayment;
    @JacksonXmlProperty(localName = "OtherPayment")
    private OtherPayment otherPayment;
    @JacksonXmlProperty(localName = "CreditSales")
    private OtherPayment creditSales;
    @JacksonXmlProperty(localName = "ForeignCurrency")
    private ForeignCurrency foreignCurrency;
    @JacksonXmlProperty(localName = "GiftCoupon")
    private GiftCoupon giftCoupon;

}

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TenderChange")
class TenderChange {
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private int id;
    @JacksonXmlProperty(localName = "maxAllowed", isAttribute = true)
    private double maxAllowed;
    @JacksonXmlProperty(localName = "roundToMinAmount", isAttribute = true)
    private String roundToMinAmount;
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;
}

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "ForeignCurrency")
class ForeignCurrency {
    @JacksonXmlProperty(localName = "ExchangeRate", isAttribute = true)
    private double exchangeRate;
    @JacksonXmlProperty(localName = "LegacyId", isAttribute = true)
    private String legacyId;
    @JacksonXmlProperty(localName = "Orientation", isAttribute = true)
    private String orientation;
    @JacksonXmlProperty(localName = "Precision", isAttribute = true)
    private int precision;
    @JacksonXmlProperty(localName = "Rounding", isAttribute = true)
    private String rounding;
}

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "OtherPayment")
class OtherPayment {
    @JacksonXmlProperty(localName = "LegacyId", isAttribute = true)
    private String legacyId;
}

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "GiftCoupon")
class GiftCoupon {

    @JacksonXmlProperty(localName = "Amount", isAttribute = true)
    private double amount;
    @JacksonXmlProperty(localName = "LegacyId", isAttribute = true)
    private String legacyId;
    @JacksonXmlProperty(localName = "OperatorDefined", isAttribute = true)
    private boolean operatorDefined;
}
