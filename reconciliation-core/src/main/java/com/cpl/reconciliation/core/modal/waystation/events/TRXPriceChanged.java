package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_Price_Changed")
public class TRXPriceChanged {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMddHHmmss")
    @JacksonXmlProperty(localName = "Time")
    private LocalDateTime time;
}
