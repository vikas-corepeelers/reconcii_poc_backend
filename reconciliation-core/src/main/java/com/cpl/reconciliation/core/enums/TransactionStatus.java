package com.cpl.reconciliation.core.enums;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public enum TransactionStatus {

    // PineLabs UPI - Failed, Session Expired, Pending Get status, Success
    // PineLabs Card - Settled
    // Paytm - Failure, Pending, Success
    UNSETTLED,
    SETTLED,
    SUCCESS,
    VOID,
    REFUND,
    FAILED,
    PENDING,
    SESSION_EXPIRED,
    CANCELLED,
    UNKNOWN;

    public static final Map<String, TransactionStatus> PINELABS_UPI_STATUS = new HashMap<>();
    public static final Map<String, TransactionStatus> PINELABS_CARD_STATUS = new HashMap<>();

    static {
        PINELABS_UPI_STATUS.put("SETTLED", SUCCESS);
        PINELABS_UPI_STATUS.put("UNSETTLED", SUCCESS);
        PINELABS_UPI_STATUS.put("VOID", VOID);
        PINELABS_UPI_STATUS.put("REFUND", REFUND);
        PINELABS_UPI_STATUS.put("FAILED", FAILED);
        PINELABS_UPI_STATUS.put("PENDING GET STATUS", PENDING);
        PINELABS_UPI_STATUS.put("TIMED OUT - REVERSAL SUCCESS", SESSION_EXPIRED);
        PINELABS_UPI_STATUS.put("TIMED OUT - REVERSAL FAILED", SESSION_EXPIRED);
        PINELABS_UPI_STATUS.put("OTHER", UNKNOWN);
    }

    static {
        // 1 > UNSETTLED -> SUCCESS
        PINELABS_CARD_STATUS.put("1", SUCCESS);
        PINELABS_CARD_STATUS.put("2", VOID);
        PINELABS_CARD_STATUS.put("3", REFUND);
        // 4 > SETTLED -> SUCCESS
        PINELABS_CARD_STATUS.put("4", SUCCESS);
        PINELABS_CARD_STATUS.put("5", FAILED);
        PINELABS_CARD_STATUS.put("6", FAILED);
        PINELABS_CARD_STATUS.put("7", PENDING);
        PINELABS_CARD_STATUS.put("8", FAILED);
        PINELABS_CARD_STATUS.put("9", FAILED);
        PINELABS_CARD_STATUS.put("10", FAILED);
        PINELABS_CARD_STATUS.put("11", VOID);
        PINELABS_CARD_STATUS.put("12", FAILED);
        PINELABS_CARD_STATUS.put("13", FAILED);
        PINELABS_CARD_STATUS.put("14", FAILED);
        PINELABS_CARD_STATUS.put("99", FAILED);
    }

    public static TransactionStatus getTransactionStatus(String txnStatus) {
        if(txnStatus.toUpperCase().contains("SUCCESS")){
            return SUCCESS;
        } else if (txnStatus.toUpperCase().contains("FAILED")) {
            return FAILED;
        }else if (txnStatus.toUpperCase().contains("REFUND")) {
            return REFUND;
        }else if (txnStatus.toUpperCase().contains("VOID")) {
            return VOID;
        }else if (txnStatus.toUpperCase().contains("PENDING")) {
            return PENDING;
        }else if (txnStatus.toUpperCase().contains("UNSETTLED")) {
            return UNSETTLED;
        }else if (txnStatus.toUpperCase().contains("SETTLED")) {
            return SETTLED;
        }else if (txnStatus.toUpperCase().contains("SESSION_EXPIRED") || txnStatus.toUpperCase().contains("SESSION EXPIRED")) {
            return SESSION_EXPIRED;
        }else if (txnStatus.toUpperCase().contains("CANCELLED")) {
            return CANCELLED;
        }
        return UNKNOWN;
    }
}
