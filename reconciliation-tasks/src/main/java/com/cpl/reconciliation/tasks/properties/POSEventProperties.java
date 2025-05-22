package com.cpl.reconciliation.tasks.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "pos.persistence.enabled")
public class POSEventProperties {

    private List<String> events;

    public boolean isPersistenceEnabled(String event){
        return events.contains(event);
    }
}
