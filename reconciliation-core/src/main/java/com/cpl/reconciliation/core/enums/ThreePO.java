package com.cpl.reconciliation.core.enums;

public enum ThreePO {
    SWIGGY("Swiggy"),
    ZOMATO("Zomato"),
    MAGICPIN("MagicPin"),
    OTHER("Others");

    public String displayName;

    ThreePO(String displayName) {
        this.displayName = displayName;
    }

    public static ThreePO getEnum(String val) {
        if (val.equalsIgnoreCase("zomato")) return ZOMATO;
        else if (val.equalsIgnoreCase("swiggy")) return SWIGGY;
        else if (val.equalsIgnoreCase("magicpin")) return MAGICPIN;
        return OTHER;
    }

    public static ThreePO getEnumByShortValCode(String shortVal) {
        if (shortVal.equalsIgnoreCase("zo")) return ZOMATO;
        else if (shortVal.equalsIgnoreCase("sw")) return SWIGGY;
        else if (shortVal.equalsIgnoreCase("ma")) return MAGICPIN;
        return OTHER;
    }

}
