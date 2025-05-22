package com.cpl.reconciliation.core.enums;

public enum CardCategory {
    DC, DD, FC, FD, NA;

    public static CardCategory getHDFCCardCategory(String cardCategory) {
        if (DC.name().equalsIgnoreCase(cardCategory)) {
            return DC;
        } else if (DD.name().equalsIgnoreCase(cardCategory)) {
            return DD;
        } else if (FC.name().equalsIgnoreCase(cardCategory)) {
            return FC;
        } else if (FD.name().equalsIgnoreCase(cardCategory)) {
            return FD;
        }
        return CardCategory.NA;
    }

    public static CardCategory getSBICardCategory(String cardRegion, String cardType) {
        if (SbiCardRegion.DOMESTIC.name().equalsIgnoreCase(cardRegion)) {
            return SbiCardType.Debit.name().equalsIgnoreCase(cardType) ? DD : DC;
        } else if (SbiCardRegion.INTERNATIONAL.name().equalsIgnoreCase(cardRegion)) {
            return SbiCardType.Debit.name().equalsIgnoreCase(cardType) ? FD : FC;
        }
        return NA;
    }
}
