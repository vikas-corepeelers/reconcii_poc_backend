package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_BaseConfig")
public class TRXBaseConfig {

    @JacksonXmlProperty(localName = "POD", isAttribute = true)
    private String pod;
    @JacksonXmlProperty(localName = "POS", isAttribute = true)
    private String pos;
    @JacksonXmlProperty(localName = "Config")
    private Config config;
    @JacksonXmlProperty(localName = "POSConfig")
    private POSConfig posConfig;

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "Config")
    public static class Config {
        @JacksonXmlProperty(localName = "MenuPriceBasis")
        private int menuPriceBasis;
        @JacksonXmlProperty(localName = "WeekEndBreakfastStartTime")
        private int weekEndBreakfastStartTime;
        @JacksonXmlProperty(localName = "WeekEndBreakfastStopTime")
        private int weekEndBreakfastStopTime;
        @JacksonXmlProperty(localName = "WeekDayBreakfastStartTime")
        private int weekDayBreakfastStartTime;
        @JacksonXmlProperty(localName = "WeekDayBreakfastStopTime")
        private int weekDayBreakfastStopTime;
        @JacksonXmlProperty(localName = "DecimalPlaces")
        private int decimalPlaces;
        @JacksonXmlProperty(localName = "CheckRefund")
        private int checkRefund;
        @JacksonXmlProperty(localName = "GrandTotalFlag")
        private int grandTotalFlag;
        @JacksonXmlProperty(localName = "StoreId")
        private int storeId;
        @JacksonXmlProperty(localName = "StoreName")
        private String storeName;
        @JacksonXmlProperty(localName = "AcceptNegativeQty")
        private int acceptNegativeQty;
        @JacksonXmlProperty(localName = "AcceptZeroPricePMix")
        private int acceptZeroPricePMix;
        @JacksonXmlProperty(localName = "FloatPriceTenderId")
        private int floatPriceTenderId;
        @JacksonXmlProperty(localName = "MinCirculatingAmount")
        private double minCirculatingAmount;
    }

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "POSConfig")
    public static class POSConfig {
        @JacksonXmlProperty(localName = "CountTCsFullDiscEM")
        private int countTCsFullDiscEM;
        @JacksonXmlProperty(localName = "RefundBehaviour")
        private int refundBehaviour;
        @JacksonXmlProperty(localName = "OverringBehaviour")
        private int overringBehaviour;
    }
}
