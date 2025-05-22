//package com.cpl.reconciliation.tasks.utils;
//
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.poiji.config.Casting;
//import com.poiji.config.DefaultCasting;
//import com.poiji.option.PoijiOptions;
//import lombok.extern.slf4j.Slf4j;
//
//import java.lang.reflect.Field;
//import java.text.SimpleDateFormat;
//import java.time.ZoneId;
//
//@Slf4j
//public class PaytmMPRCustomCasting implements Casting {
//    private final DefaultCasting casting = new DefaultCasting();
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy");
//
//
//    @Override
//    public Object castValue(Field field, String s, int i, int i1, PoijiOptions poijiOptions) {
//        s = s.replaceAll("^'+|'+$", "");
//
//        if (field.getName().equalsIgnoreCase("transactionDate")) {
//            try {
//                return dateFormat.parse(s).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            } catch (Exception e) {
//                log.error("Error parsing Paytm mpr transaction date: " + s + " : " + e.getMessage());
//            }
//            return null;
//        } else if (field.getName().equalsIgnoreCase("settledDate")) {
//            try {
//                return dateFormat.parse(s).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            } catch (Exception e) {
//                log.error("Error parsing Paytm mpr transaction date: " + s + " : " + e.getMessage());
//            }
//            return null;
//        } else if (field.getName().equalsIgnoreCase("paymentType")) {
//
//            if (s.toUpperCase().contains("UPI")) {
//                return PaymentType.UPI;
//            } else if (s.toUpperCase().contains("CARD")) {
//                return PaymentType.CARD;
//            } else if (s.toUpperCase().contains("PAYTM_DIGITAL_CREDIT")) {
//                return PaymentType.PAYTM_DIGITAL_CREDIT;
//            } else if (s.toUpperCase().contains("NET_BANKING")) {
//                return PaymentType.NET_BANKING;
//            } else if (s.toUpperCase().contains("PPI")) {
//                return PaymentType.PPI;
//            }
//            return PaymentType.UNKNOWN;
//        }
//
//
//        return casting.castValue(field, s, i, i1, poijiOptions);
//    }
//}