package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_DayClose")
public class TRXDayClose {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    @JacksonXmlProperty(localName = "BusinessDate", isAttribute = true)
    private LocalDate businessDate;

}