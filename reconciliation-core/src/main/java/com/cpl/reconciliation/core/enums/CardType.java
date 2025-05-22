package com.cpl.reconciliation.core.enums;

public enum CardType {

    Credit,Debit,Unknown;

    public static CardType getCardType(String cardType) {
        if(cardType.toUpperCase().contains("CREDIT")){
            return Credit;
        } else if (cardType.toUpperCase().contains("DEBIT")) {
            return Debit;
        }
        return Unknown;
    }
}
