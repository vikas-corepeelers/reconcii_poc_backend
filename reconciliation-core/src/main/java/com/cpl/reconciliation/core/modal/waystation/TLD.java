package com.cpl.reconciliation.core.modal.waystation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JacksonXmlRootElement(localName = "TLD")
public class TLD {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    @JacksonXmlProperty(localName = "businessDate", isAttribute = true)
    public String businessDate;
    @JacksonXmlProperty(localName = "checkPoint", isAttribute = true)
    private int checkPoint;
    @JacksonXmlProperty(localName = "dataComplete", isAttribute = true)
    private boolean dataComplete;
    @JacksonXmlProperty(localName = "end", isAttribute = true)
    private boolean end;
    @JacksonXmlProperty(localName = "hasMoreContent", isAttribute = true)
    private boolean hasMoreContent;
    @JacksonXmlProperty(localName = "logVersion", isAttribute = true)
    private String logVersion;
    @JacksonXmlProperty(localName = "productionStatus", isAttribute = true)
    private String productionStatus;
    @JacksonXmlProperty(localName = "storeId", isAttribute = true)
    private String storeId;
    @JacksonXmlProperty(localName = "swVersion", isAttribute = true)
    private String swVersion;
    @JacksonXmlElementWrapper(localName = "Node", useWrapping = false)
    @JacksonXmlProperty(localName = "Node")
    private List<Node> nodes = new ArrayList<>();

    public int getTotalPos() {
        return CollectionUtils.isEmpty(nodes) ? 0 : nodes.size();
    }

    public int getTotalEvents() {
        return CollectionUtils.isEmpty(nodes) ? 0 : nodes.stream().mapToInt(node -> {
            List<Event> events = node.getEvents();
            return CollectionUtils.isEmpty(events) ? 0 : events.size();
        }).sum();
    }

    @Override
    public String toString() {
        return "TLD{" +
                "storeId='" + storeId + '\'' +
                ", businessDate='" + businessDate + '\'' +
                ", checkPoint=" + checkPoint +
                ", dataComplete=" + dataComplete +
                ", end=" + end +
                ", hasMoreContent=" + hasMoreContent +
                ", logVersion='" + logVersion + '\'' +
                ", productionStatus='" + productionStatus + '\'' +
                ", swVersion='" + swVersion + '\'' +
                ", nodes=" + (CollectionUtils.isEmpty(nodes) ? 0 : nodes.size()) +
                '}';
    }
}
