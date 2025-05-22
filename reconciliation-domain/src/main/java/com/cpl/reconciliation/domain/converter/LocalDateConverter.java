package com.cpl.reconciliation.domain.converter;

import com.cpl.core.api.constant.Formatter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.LocalDate;

@Converter
public class LocalDateConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return localDate.format(Formatter.YYYYMMDD_DASH);
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return LocalDate.parse(dbData, Formatter.YYYYMMDD_DASH);
    }
}
