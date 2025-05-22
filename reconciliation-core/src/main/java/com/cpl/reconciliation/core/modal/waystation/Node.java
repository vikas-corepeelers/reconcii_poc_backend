package com.cpl.reconciliation.core.modal.waystation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "Node")
public class Node {

    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private String id;
    @JacksonXmlProperty(localName = "nodeStatus", isAttribute = true)
    private String nodeStatus;
    @JacksonXmlElementWrapper(localName = "Event", useWrapping = false)
    @JacksonXmlProperty(localName = "Event")
    private List<Event> events = new ArrayList<>();

    @Override
    public String toString() {
        return "Node [id=" + id + ", nodeStatus=" + nodeStatus + ", events=" + (CollectionUtils.isEmpty(events) ? 0 : events.size()) + "]";
    }
}