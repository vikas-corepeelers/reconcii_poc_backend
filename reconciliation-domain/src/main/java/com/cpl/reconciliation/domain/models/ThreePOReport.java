package com.cpl.reconciliation.domain.models;

import com.cpl.reconciliation.core.enums.ThreePO;
import com.cpl.reconciliation.domain.entity.AdditionalData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.cpl.reconciliation.domain.util.Constants.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
public class ThreePOReport extends AdditionalData {

    private ThreePO threePO;
    private String threePoOrderId;

    private double posTotalAmount;
    private double threePoTotalAmount;

    private double posReceivable;
    private double threePoReceivable;

    private double posPackagingCharge;
    private double threePoPackagingCharge;

    private double threePoTds;
    private double posTds;

    private double posCommission;
    private double threePoCommission;

    private double posPgCharge;
    private double threePoPgCharge;

    private double posChargesGst;
    private double threePoChargesGst;

    private double posConsumerGst;
    private double threePoConsumerGst;

    private double deltaAmount;
    private double deltaReceivables;
    private double deltaPackagingCharge;
    private double deltaTds;
    private double deltaCommission;
    private double deltaPgCharges;
    private double deltaChargesGST;
    private double deltaConsumerGST;
    private double unreconciledAmount;

    private LocalDateTime orderDate;
    private String storeCode;

    private String orderStatus;
    private String pickupStatus;
    private String cancellationRemark;
    private double refundForDisputedOrder;
    private double billSubtotal;
    private double salt;
    private String reason;
    private String deltaThreePODifference;
    private double cashPrepayment;
    private double merchantCancellationCharges;

    //newly added
    private Map<String, Double> threepoFieldsForUnreconciled = new LinkedHashMap();
    private Map<String, Double> unreconciledMismatchesCases = new LinkedHashMap();

    public double getDeltaAmount() {
        return posTotalAmount - threePoTotalAmount;
    }

    public double getDeltaReceivables() {
        return posReceivable - threePoReceivable;
    }

    public double getDeltaPackagingCharge() {
        return posPackagingCharge - threePoPackagingCharge;
    }

    public double getDeltaTds() {
        return posTds - threePoTds;
    }

    public double getDeltaCommission() {
        return posCommission - threePoCommission;
    }

    public double getDeltaPgCharges() {
        return posPgCharge - threePoPgCharge;
    }

    public double getDeltaChargesGST() {
        return posChargesGst - threePoChargesGst;
    }

    public double getDeltaConsumerGST() {
        return posConsumerGst - threePoConsumerGst;
    }

    public double getDeltaThreePODifference() {
        return getThreePOReasonDeltaDifference();
    }

    public String getReason() {
        //   double threshold = 0;
        // double threshold = 0.10;
//        if (posPackagingCharge == 37.14) {
//            threshold = 0.2;
//        }
        if (getPosTotalAmount() == 0) {
            return ORDER_NOT_FOUND_IN_POS;
        } else {
            return getThreePOReason();
        }
//        if (threePO.equals(ThreePO.MAGICPIN)) {
//            return getMagicpinReason(threshold);
//        }
//        if ("cancel".equalsIgnoreCase(orderStatus) || "refund".equalsIgnoreCase(orderStatus) || "deduction".equalsIgnoreCase(orderStatus)
//                || "addition".equalsIgnoreCase(orderStatus)) {
//            return "";
//        }

//        if (Math.abs(getDeltaPackagingCharge()) > 0) {
//            return PACK_CHARGE_MISMATCH;
//        }
//
//        if (Math.abs(getDeltaAmount()) >= threshold) {
//            double posAmount = getPosTotalAmount();
//            double threePOAmount = getThreePoTotalAmount();
//            if (posAmount > threePOAmount) {
//                return POS_AMOUNT_GREATER;
//            }
//            return THREE_PO_AMOUNT_GREATER;
//        }
//
//        if (Math.abs(getDeltaCommission()) >= threshold && Math.abs(getDeltaPgCharges()) < threshold) {
//            return COMMISSION_MISMATCH;
//        }
//        if (Math.abs(getDeltaPgCharges()) >= threshold && Math.abs(getDeltaCommission()) < threshold) {
//            return PG_MISMATCH;
//        }
//
//        if (Math.abs(getDeltaPgCharges()) >= threshold && Math.abs(getDeltaCommission()) >= threshold) {
//            return COMMISSION_PG_BOTH_MISMATCH;
//        }
//
//        if (Math.abs(getDeltaTds()) >= threshold) {
//            return TDS_MISMATCH;
//        }
//
//        if (Math.abs(getDeltaChargesGST()) >= threshold) {
//            return GST_CHARGES_MISMATCH;
//        }
//        if (Math.abs(getDeltaConsumerGST()) >= threshold) {
//            return CONSUMER_GST_MISMATCH;
//        }
//
//        if (Math.abs(getDeltaReceivables()) >= threshold) {
//            if (getPosReceivable() > getThreePoReceivable()) {
//                if (getDeltaReceivables() <= 0.1) {
//                    return RECEIVABLE_MISMATCH_POS_GREATER + ROUNDING_OFF;
//                }
//                return RECEIVABLE_MISMATCH_POS_GREATER;
//            }
//            return RECEIVABLE_MISMATCH_THREE_PO_GREATER;
//        }
        // return "";
    }

    public String getMagicpinReason(double threshold) {
        if (getPosTotalAmount() == 0) {
            return ORDER_NOT_FOUND_IN_POS;
        }
        if (Math.abs(getDeltaPackagingCharge()) > 0) {
            return PACK_CHARGE_MISMATCH;
        }

        if (Math.abs(getDeltaConsumerGST()) >= threshold) {
            return CONSUMER_GST_MISMATCH;
        }

        if (Math.abs(getDeltaAmount()) >= threshold) {
            double posAmount = getPosTotalAmount();
            double threePOAmount = getThreePoTotalAmount();
            if (posAmount > threePOAmount) {
                return POS_AMOUNT_GREATER;
            }
            return THREE_PO_AMOUNT_GREATER;
        }

        if (Math.abs(getDeltaCommission()) >= threshold && Math.abs(getDeltaPgCharges()) < threshold) {
            return COMMISSION_MISMATCH;
        }
        if (Math.abs(getDeltaPgCharges()) >= threshold && Math.abs(getDeltaCommission()) < threshold) {
            return PG_MISMATCH;
        }

        if (Math.abs(getDeltaPgCharges()) >= threshold && Math.abs(getDeltaCommission()) >= threshold) {
            return COMMISSION_PG_BOTH_MISMATCH;
        }

        if (Math.abs(getDeltaTds()) >= threshold) {
            return TDS_MISMATCH;
        }

        if (Math.abs(getDeltaChargesGST()) >= threshold) {
            return GST_CHARGES_MISMATCH;
        }

        if (Math.abs(getDeltaReceivables()) >= threshold) {
            if (getPosReceivable() > getThreePoReceivable()) {
                if (getDeltaReceivables() <= 0.1) {
                    return RECEIVABLE_MISMATCH_POS_GREATER + ROUNDING_OFF;
                }
                return RECEIVABLE_MISMATCH_POS_GREATER;
            }
            return RECEIVABLE_MISMATCH_THREE_PO_GREATER;
        }
        return "";
    }

    private String getThreePOReason() {
        Optional<String> key = unreconciledMismatchesCases.entrySet().stream()
                .filter(entry -> entry.getValue() == null || entry.getValue() != 0) // Filter out zero values
                .map(Map.Entry::getKey) // Extract the key from the Map.Entry
                .findFirst(); // Return the first matching key
        if (key.isEmpty()) {
            return "";
        } else {
            return key.get();
        }
    }

    private double getThreePOReasonDeltaDifference() {
        Optional<Double> key = unreconciledMismatchesCases.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() != 0)
                .map(Map.Entry::getValue)
                .findFirst();
        if (key.isEmpty()) {
            return 0;
        } else {
            return key.get();
        }
    }

    private String getSwiggyReason(double threshold) {

        if ("MERCHANT".equalsIgnoreCase(getCancellationRemark())) {
            return MERCHANT_CANCELLED;
        }
        if ("CANCELLED".equalsIgnoreCase(orderStatus)) {
            if (posTotalAmount != 0) {
                if (Math.abs(getDeltaAmount()) < threshold && Math.abs(getDeltaReceivables()) < threshold) {
                    if (Math.round(posReceivable * 100 / posTotalAmount) == 60) {
                        return "Okay @ 60%";
                    } else if (Math.round(posReceivable * 100 / posTotalAmount) == 78) {
                        return "Okay @ 78.35%";
                    }
                }
                if (Math.abs(getDeltaAmount()) >= threshold) {
                    double posAmount = getPosTotalAmount();
                    double threePOAmount = getThreePoTotalAmount();
                    if (posAmount > threePOAmount) {
                        return POS_AMOUNT_GREATER;
                    }
                    return THREE_PO_AMOUNT_GREATER;
                }
                if (Math.abs(getDeltaReceivables()) >= threshold) {
                    if (getPosReceivable() > getThreePoReceivable()) {
                        return swiggyShortPaymentLabel(threshold);
                    }
                    return RECEIVABLE_MISMATCH_THREE_PO_GREATER;
                }
            } else if (getPosTotalAmount() == 0) {
                if ("swiggy".equalsIgnoreCase(cancellationRemark)) {
                    return "Cancelled by Swiggy,  " + ORDER_NOT_FOUND_IN_POS;
                } else if ("customer".equalsIgnoreCase(cancellationRemark)) {
                    return "Cancelled by Customer, " + ORDER_NOT_FOUND_IN_POS;
                }
            }
        }
        if (getPosTotalAmount() == 0) {
            return "Delivered " + ORDER_NOT_FOUND_IN_POS;
        }
        if (Math.abs(getDeltaPackagingCharge()) > 0) {
            return PACK_CHARGE_MISMATCH;
        }

        if (Math.abs(getDeltaConsumerGST()) >= threshold) {
            return CONSUMER_GST_MISMATCH;
        }

        if (Math.abs(getDeltaAmount()) >= threshold) {
            double posAmount = getPosTotalAmount();
            double threePOAmount = getThreePoTotalAmount();
            if (posAmount > threePOAmount) {
                return POS_AMOUNT_GREATER;
            }
            return THREE_PO_AMOUNT_GREATER;
        }

        if (Math.abs(getDeltaCommission()) >= threshold && Math.abs(getDeltaPgCharges()) < threshold) {
            return COMMISSION_MISMATCH;
        }
        if (Math.abs(getDeltaPgCharges()) >= threshold && Math.abs(getDeltaCommission()) < threshold) {
            return PG_MISMATCH;
        }

        if (Math.abs(getDeltaPgCharges()) >= threshold && Math.abs(getDeltaCommission()) >= threshold) {
            return COMMISSION_PG_BOTH_MISMATCH;
        }

        if (Math.abs(getDeltaTds()) >= threshold) {
            return TDS_MISMATCH;
        }

        if (Math.abs(getDeltaChargesGST()) >= threshold) {
            return GST_CHARGES_MISMATCH;
        }

        if (Math.abs(getDeltaReceivables()) >= threshold) {
            if (getPosReceivable() > getThreePoReceivable()) {
                return swiggyShortPaymentLabel(threshold);
            }
            return RECEIVABLE_MISMATCH_THREE_PO_GREATER;
        }
        return "";
    }

    private String swiggyShortPaymentLabel(double threshold) {
        String result = RECEIVABLE_MISMATCH_POS_GREATER;
        if (threePoReceivable < 0) {
            result += " , Negative Settlement";
        } else if (cashPrepayment != 0 && Math.abs(deltaReceivables - cashPrepayment) < threshold) {
            result += " due to Cash Prepayment";
        } else if (orderStatus.equalsIgnoreCase("delivered")) {
            result += " on delivered orders";
        } else if (orderStatus.equalsIgnoreCase("cancelled")) {
            result += " on cancelled orders";
        }
        if (getDeltaReceivables() <= 0.1) {
            result += ROUNDING_OFF;
        }
        return result;

    }

    public boolean isFreebieOrder() {
        return salt != 0;
    }

}
