//package com.cpl.reconciliation.tasks.service.mpr;
//
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.CardCategory;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.repository.HDFCTidChargesRepository;
//import com.cpl.reconciliation.domain.repository.SBITidChargesRepository;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//
//import static com.cpl.reconciliation.core.enums.CardCategory.*;
//
//@Data
//@Slf4j
//@Service
//public class CardChargesUtilImpl {
//    @Autowired
//    private HDFCTidChargesRepository hdfcTidChargesRepository;
//    @Autowired
//    private SBITidChargesRepository sbiTidChargesRepository;
//
//    public void setAndUpdateMDR(MPREntity mpr) {
//        if (Bank.SBI.equals(mpr.getBank())) setAndUpdateMDRForSBI(mpr);
//        else if (Bank.HDFC.equals(mpr.getBank())) setAndUpdateMDRForHDFC(mpr);
//        else if (Bank.AMEX.equals(mpr.getBank())) setAndUpdateMDRForAmex(mpr);
//        else if (Bank.ICICI.equals(mpr.getBank())) setAndUpdateMDRForICICI(mpr);
//    }
//
//    public void setAndUpdateMDRForSBI(MPREntity mpr) {
//        CardCategory cardCategory = mpr.getCardCategory();
//        double rate = 0;
//        try {
//            if (FC.equals(cardCategory) || FD.equals(cardCategory)) {
//                if (mpr.getTransactionDate() != null && mpr.getTid() != null) {
//                    rate = sbiTidChargesRepository.findIntRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                }
//            } else if ("OFFUS".equalsIgnoreCase(mpr.getCustomField1())) {
//                if (DD.equals(cardCategory)) {
//                    if (mpr.getMprAmount() > 2000) {
//                        rate = sbiTidChargesRepository.findOffUsDbtDomRateByDtTIdAmtGt2k(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                    } else {
//                        rate = sbiTidChargesRepository.findOffUsDbtDomRateByDtTIdAmtLt2k(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                    }
//
//                } else if (DC.equals(cardCategory)) {
//                    rate = sbiTidChargesRepository.findOffUsCrdRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                }
//
//            } else if ("ONUS".equalsIgnoreCase(mpr.getCustomField1())) {
//                if (DD.equals(cardCategory)) {
//                    if (mpr.getMprAmount() > 2000) {
//                        rate = sbiTidChargesRepository.findOnUsDbtDomRateByDtTIdAmtGt2k(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                    } else {
//                        rate = sbiTidChargesRepository.findOnUsDbtDomRateByDtTIdAmtLt2k(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                    }
//
//                } else if (DC.equals(cardCategory)) {
//                    rate = sbiTidChargesRepository.findOnUsCrdRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                }
//            }
//
//        } catch (NullPointerException e) {
//            rate = 0;
//        }
//        mpr.setExpectedMDR(rate);
//        double amount = mpr.getMprAmount() * rate/100;
//        amount =new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
//        mpr.setExpectedCardCharges(amount);
//
//    }
//
//    public void setAndUpdateMDRForHDFC(MPREntity mpr) {
//        CardCategory cardCategory = mpr.getCardCategory();
//        String cardType = mpr.getCardType();
//        double rate = 0;
//        try {
//            if (FC.equals(cardCategory) || DC.equals(cardCategory)) {
//                rate = hdfcTidChargesRepository.findCdtRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//            } else if (cardType.contains("BUSINESS") || cardType.contains("EMV")) {
//                rate = hdfcTidChargesRepository.findCommercialRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//            } else if (cardType.contains("DINERS")) {
//                rate = hdfcTidChargesRepository.findDinersRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//            } else if (DC.equals(cardCategory)) {
//                rate = hdfcTidChargesRepository.findDomCdtRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//            } else if (FC.equals(cardCategory) || FD.equals(cardCategory)) {
//                rate = hdfcTidChargesRepository.findForeignCardRateByDtTId(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//            } else if (DD.equals(cardCategory)) {
//                if (mpr.getMprAmount() > 2000) {
//                    rate = hdfcTidChargesRepository.findDomDbtRateByDtTIdAbove2k(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                } else {
//                    rate = hdfcTidChargesRepository.findDomDbtRateByDtTIdBelow2k(mpr.getTransactionDate().toLocalDate(), mpr.getTid());
//                }
//            }
//        } catch (Exception e) {
//            rate = 0;
//        }
//        mpr.setExpectedMDR(rate);
//        double amount = mpr.getMprAmount() * rate/100;
//        amount =new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
//        mpr.setExpectedCardCharges(amount);
//    }
//
//    public void setAndUpdateMDRForAmex(MPREntity mpr) {
//        double amount = 0;
//        double gstRate = 18;
//        double rate = 0;
//        if (mpr.getMprAmount() <= 2000) {
//            rate = 1.8;
//            amount = mpr.getMprAmount() * rate/100;
//        } else {
//            rate = 1.8;
//            amount = mpr.getMprAmount() * rate/100 + gstRate * mpr.getMprAmount()/100;
//        }
//        mpr.setExpectedMDR(rate);
//        amount =new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
//        mpr.setExpectedCardCharges(amount);
//    }
//
//    public void setAndUpdateMDRForICICI(MPREntity mpr) {
//        CardCategory cardCategory = mpr.getCardCategory();
//        String cardType = mpr.getCardType();
//        double rate = 0;
//        double amount = 0;
//        double gstRate = 18;
//        try {
//            if (FC.equals(cardCategory) || FD.equals(cardCategory)) {
//                //Foreign rate
//                rate = 2.76;
//                amount = mpr.getMprAmount()*rate/100+gstRate*mpr.getMprAmount()/100;
//            } else if (cardType.toUpperCase().contains("BUS") || cardType.contains("COMM")) {
//                // Commercial rate
//                rate = 2.76;
//                amount = mpr.getMprAmount()*rate/100+gstRate*mpr.getMprAmount()/100;
//            } else if (DC.equals(cardCategory)) {
//                //Domestic credit card rate
//                rate = 0.70;
//                amount = mpr.getMprAmount()*rate/100+gstRate*mpr.getMprAmount()/100;
//            } else if (DD.equals(cardCategory)){
//                //Domestic debit card
//                if (mpr.getMprAmount() > 2000) {
//                    rate = 0.70;
//                    amount = mpr.getMprAmount()*rate/100+gstRate*mpr.getMprAmount()/100;
//                } else {
//                    rate = 0.20;
//                    amount = mpr.getMprAmount()*rate/100+gstRate*mpr.getMprAmount()/100;
//                }
//            }
//        } catch (Exception e) {
//            rate = 0;
//        }
//        mpr.setExpectedMDR(rate);
//        amount =new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
//        mpr.setExpectedCardCharges(amount);
//    }
//
//}