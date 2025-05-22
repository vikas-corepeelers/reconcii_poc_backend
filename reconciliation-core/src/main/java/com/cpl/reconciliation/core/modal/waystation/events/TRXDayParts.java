package com.cpl.reconciliation.core.modal.waystation.events;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "TRX_DayParts")
public class TRXDayParts {

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;
    //
    @JacksonXmlProperty(localName = "DayPartMonday")
    private DayPart dayPartMonday;
    @JacksonXmlProperty(localName = "DayPartTuesday")
    private DayPart dayPartTuesday;
    @JacksonXmlProperty(localName = "DayPartWednesday")
    private DayPart dayPartWednesday;
    @JacksonXmlProperty(localName = "DayPartThursday")
    private DayPart dayPartThursday;
    @JacksonXmlProperty(localName = "DayPartFriday")
    private DayPart dayPartFriday;
    @JacksonXmlProperty(localName = "DayPartSaturday")
    private DayPart dayPartSaturday;
    @JacksonXmlProperty(localName = "DayPartSunday")
    private DayPart dayPartSunday;

    @Getter
    @Setter
    @ToString
    public static class DayPart {

        @JacksonXmlProperty(localName = "start", isAttribute = true)
        private String start;
        @JacksonXmlProperty(localName = "end", isAttribute = true)
        private String end;
    }
}
