package com.cpl.reconciliation.core.util;

import com.cpl.core.api.constant.Formatter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;

public class CompositeKeyUtil {
    public static String getComposite(String one, String two,String three){
        if(Strings.isBlank(three) || Strings.isBlank(one) || Strings.isBlank(two))return null;
        return StringUtils.deleteWhitespace(one +"_" + two +"_" + getLastFourDigits(three));
    }

    private static String getLastFourDigits(String number){
        if(number.length()<=4)return number;
        return number.substring(number.length() - 4);
    }
}
