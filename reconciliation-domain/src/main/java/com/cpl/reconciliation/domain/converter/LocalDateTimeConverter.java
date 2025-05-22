package com.cpl.reconciliation.domain.converter;

import com.cpl.core.api.constant.Formatter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDateTime;

@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    @Override
    public String convertToDatabaseColumn(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.format(Formatter.YYYYMMDD_HHMMSS_DASH);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDateTime.parse(dbData, Formatter.YYYYMMDD_HHMMSS_DASH);
    }
}
