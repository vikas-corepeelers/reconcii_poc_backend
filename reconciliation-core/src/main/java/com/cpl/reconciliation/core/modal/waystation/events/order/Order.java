package com.cpl.reconciliation.core.modal.waystation.events.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonIgnoreProperties({"EventsDetail"})
@JacksonXmlRootElement(localName = "Order")
public class Order {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmmss")
    @JacksonXmlProperty(localName = "Timestamp", isAttribute = true)
    private String timestamp;
    //Unique Identifier
    @JacksonXmlProperty(isAttribute = true)
    private String key;
    @JacksonXmlProperty(isAttribute = true)
    private String uniqueId;
    //Receipt number
    @JacksonXmlProperty(isAttribute = true)
    private String fpFiscalReceiptNumber;
    @JacksonXmlProperty(isAttribute = true)
    private String fpReceiptNumber;
    @JacksonXmlProperty(isAttribute = true)
    private String receiptNumber;
    //Sale timestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    @JacksonXmlProperty(isAttribute = true)
    private LocalDate endSaleDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    @JacksonXmlProperty(isAttribute = true)
    private LocalDate startSaleDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HHmmss")
    @JacksonXmlProperty(isAttribute = true)
    private LocalTime endSaleTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HHmmss")
    @JacksonXmlProperty(isAttribute = true)
    private LocalTime startSaleTime;
    //Order diffrent Amount
    @JacksonXmlProperty(isAttribute = true)
    private double totalAmount;
    @JacksonXmlProperty(isAttribute = true)
    private double totalTaxableAmount;
    @JacksonXmlProperty(isAttribute = true)
    private double totalTax;
    @JacksonXmlProperty(isAttribute = true)
    private double totalChargedTaxAmount;
    @JacksonXmlProperty(isAttribute = true)
    private double nonProductAmount;
    @JacksonXmlProperty(isAttribute = true)
    private double nonProductTax;
    //
    @JacksonXmlProperty(localName = "booth", isAttribute = true)
    private int booth;
    @JacksonXmlProperty(isAttribute = true)
    private String kind;
    @JacksonXmlProperty(isAttribute = true)
    private int orderSrc;
    @JacksonXmlProperty(isAttribute = true)
    private String saleType;
    @JacksonXmlProperty(isAttribute = true)
    private int major;
    @JacksonXmlProperty(isAttribute = true)
    private int minor;
    @JacksonXmlProperty(isAttribute = true)
    private String side;
    @JacksonXmlProperty(isAttribute = true)
    private String paymentType;
    @JacksonXmlProperty(isAttribute = true)
    private String foreignOrderId;
    @JacksonXmlProperty(isAttribute = true)
    private String deliveryChannel;
    @JacksonXmlElementWrapper(localName = "taxItem", useWrapping = false)
    @JacksonXmlProperty(localName = "taxItem")
    private List<TaxItem> taxItems;
    @JacksonXmlElementWrapper(localName = "Item", useWrapping = false)
    @JacksonXmlProperty(localName = "Item")
    private List<Item> items;
    @JacksonXmlElementWrapper(localName = "Promotions")
    @JacksonXmlProperty(localName = "Promotions")
    private List<Promotion> promotions;
    @JacksonXmlProperty(localName = "Offers")
    private Offers offers;
    @JacksonXmlProperty(localName = "Customer")
    private Customer customer;
    @JacksonXmlElementWrapper(localName = "CustomInfo")
    @JacksonXmlProperty(localName = "CustomInfo")
    private List<Info> customInfoList;
    @JacksonXmlElementWrapper(localName = "Tenders")
    @JacksonXmlProperty(localName = "Tenders")
    private List<Tender> tenders;
    @JacksonXmlProperty(localName = "POSTimings")
    private POSTimings posTimings;
    @JacksonXmlProperty(localName = "Reduction")
    private Reduction reduction;
    @JacksonXmlElementWrapper(localName = "TaxChain", useWrapping = false)
    @JacksonXmlProperty(localName = "TaxChain")
    private List<TaxChain> taxChains;
    @JacksonXmlProperty(localName = "Ev_SaleCustomInfo")
    private EvSaleCustomInfo evSaleCustomInfo;
    @JacksonXmlProperty(localName = "COD")
    private Cod cod;
    @JacksonXmlProperty(localName = "Fiscal_Information")
    private FiscalInformation fiscalInformation;

    @JsonIgnore
    public String getTenderName(){
        if(!CollectionUtils.isEmpty(this.tenders)){
            return tenders.stream().map(Tender::getTenderName).distinct().collect(Collectors.joining("|"));
        }
        return null;
    }

    @JsonIgnore
    public double getTenderAmount(){
        if(!CollectionUtils.isEmpty(this.tenders)){
            return tenders.stream().mapToDouble(Tender::getTenderAmount).sum();
        }
        return 0.0d;
    }

    @JsonIgnore
    public String getInvoiceNumber(){
        if(!CollectionUtils.isEmpty(this.customInfoList)){
            Optional<Info> info = this.customInfoList.stream().filter(i -> "India_0040".equals(i.getName())).findFirst();
            if(info.isPresent()){
                Info i = info.get();
                String invoice = i.getValue();
                if(StringUtils.hasText(invoice)){
                    String [] t = invoice.split("\\|");
                    if(t.length>2){
                        return t[1].trim();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Order{" +
                "timestamp='" + timestamp + '\'' +
                ", key='" + key + '\'' +
                ", uniqueId='" + uniqueId + '\'' +
                ", fpFiscalReceiptNumber=" + fpFiscalReceiptNumber +
                ", fpReceiptNumber=" + fpReceiptNumber +
                ", receiptNumber=" + receiptNumber +
                ", endSaleDate=" + endSaleDate +
                ", startSaleDate=" + startSaleDate +
                ", endSaleTime=" + endSaleTime +
                ", startSaleTime=" + startSaleTime +
                ", totalAmount=" + totalAmount +
                ", totalTaxableAmount=" + totalTaxableAmount +
                ", totalTax=" + totalTax +
                ", totalChargedTaxAmount=" + totalChargedTaxAmount +
                ", nonProductAmount=" + nonProductAmount +
                ", nonProductTax=" + nonProductTax +
                ", booth=" + booth +
                ", kind='" + kind + '\'' +
                ", orderSrc=" + orderSrc +
                ", saleType='" + saleType + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                ", side='" + side + '\'' +
                '}';
    }
}
