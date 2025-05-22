package com.cpl.reconciliation.core.modal.waystation.events.order;

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
@JacksonXmlRootElement(localName = "Item")
public class Item {

    @JacksonXmlProperty(isAttribute = true)
    private String BDPrice;
    @JacksonXmlProperty(isAttribute = true)
    private String BDTax;
    @JacksonXmlProperty(isAttribute = true)
    private String BPPrice;
    @JacksonXmlProperty(isAttribute = true)
    private String BPTax;
    @JacksonXmlProperty(isAttribute = true)
    private String action;
    @JacksonXmlProperty(isAttribute = true)
    private String category;
    @JacksonXmlProperty(isAttribute = true)
    private String chgAfterTotal;
    @JacksonXmlProperty(isAttribute = true)
    private String code;
    @JacksonXmlProperty(isAttribute = true)
    private String dayPart;
    @JacksonXmlProperty(isAttribute = true)
    private String department;
    @JacksonXmlProperty(isAttribute = true)
    private String departmentClass;
    @JacksonXmlProperty(isAttribute = true)
    private String departmentSubClass;
    @JacksonXmlProperty(isAttribute = true)
    private String description;
    @JacksonXmlProperty(isAttribute = true)
    private String displayOrder;
    @JacksonXmlProperty(isAttribute = true)
    private String familyGroup;
    @JacksonXmlProperty(isAttribute = true)
    private String grillModifier;
    @JacksonXmlProperty(isAttribute = true)
    private String grillQty;
    @JacksonXmlProperty(isAttribute = true)
    private String id;
    @JacksonXmlProperty(isAttribute = true)
    private boolean isUpcharge;
    @JacksonXmlProperty(isAttribute = true)
    private String level;
    @JacksonXmlProperty(isAttribute = true)
    private String qty;
    @JacksonXmlProperty(isAttribute = true)
    private String qtyPromo;
    @JacksonXmlProperty(isAttribute = true)
    private String qtyVoided;
    @JacksonXmlProperty(isAttribute = true)
    private String solvedChoice;
    @JacksonXmlProperty(isAttribute = true)
    private String totalPrice;
    @JacksonXmlProperty(isAttribute = true)
    private String totalTax;
    @JacksonXmlProperty(isAttribute = true)
    private String type;
    @JacksonXmlProperty(isAttribute = true)
    private String unitPrice;
    @JacksonXmlProperty(isAttribute = true)
    private String unitTax;
    @JacksonXmlElementWrapper(localName = "Item", useWrapping = false)
    @JacksonXmlProperty(localName = "Item")
    private List<Item> items;
    @JacksonXmlProperty(localName = "Promo")
    private Promo promo;
    @JacksonXmlProperty(localName = "PromotionApplied")
    private PromotionApplied promotionApplied;
    @JacksonXmlProperty(localName = "Offers")
    private Offers offers;
    @JacksonXmlElementWrapper(localName = "TaxChain", useWrapping = false)
    @JacksonXmlProperty(localName = "TaxChain")
    private List<TaxChain> taxChains;
    @JacksonXmlElementWrapper(localName = "discounts")
    @JacksonXmlProperty(localName = "discount")
    private List<Discount> discounts;

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "Promo")
    public static class Promo {

        @JacksonXmlProperty(localName = "id", isAttribute = true)
        private String id;
        @JacksonXmlProperty(localName = "name", isAttribute = true)
        private String name;
        @JacksonXmlProperty(localName = "qty", isAttribute = true)
        private String qty;
    }

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "PromotionApplied")
    public static class PromotionApplied {

        @JacksonXmlProperty(localName = "discountAmount", isAttribute = true)
        private String discountAmount;
        @JacksonXmlProperty(localName = "discountType", isAttribute = true)
        private String discountType;
        @JacksonXmlProperty(localName = "eligible", isAttribute = true)
        private String eligible;
        @JacksonXmlProperty(localName = "offerId", isAttribute = true)
        private String offerId;
        @JacksonXmlProperty(localName = "originalItemPromoQty", isAttribute = true)
        private String originalItemPromoQty;
        @JacksonXmlProperty(localName = "originalPrice", isAttribute = true)
        private String originalPrice;
        @JacksonXmlProperty(localName = "originalProductCode", isAttribute = true)
        private String originalProductCode;
        @JacksonXmlProperty(localName = "promotionCounter", isAttribute = true)
        private String promotionCounter;
        @JacksonXmlProperty(localName = "promotionId", isAttribute = true)
        private String promotionId;
    }

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "Offers")
    public static class Offers {

        @JacksonXmlProperty(localName = "beforeOfferPrice", isAttribute = true)
        private String beforeOfferPrice;
        @JacksonXmlProperty(localName = "discountAmount", isAttribute = true)
        private String discountAmount;
        @JacksonXmlProperty(localName = "discountType", isAttribute = true)
        private String discountType;
        @JacksonXmlProperty(localName = "offerId", isAttribute = true)
        private String offerId;
    }

    @Getter
    @Setter
    @ToString
    @JacksonXmlRootElement(localName = "discount")
    public static class Discount {

        @JacksonXmlProperty(localName = "amount", isAttribute = true)
        private double amount;
        @JacksonXmlProperty(localName = "description", isAttribute = true)
        private String description;
        @JacksonXmlProperty(localName = "id", isAttribute = true)
        private int id;
        @JacksonXmlProperty(localName = "reduction", isAttribute = true)
        private double reduction;
        @JacksonXmlProperty(localName = "type", isAttribute = true)
        private String type;
        @JacksonXmlProperty(localName = "value", isAttribute = true)
        private double value;
    }
}


