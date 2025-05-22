package com.cpl.reconciliation.core.request;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Data;

/**
 *
 * @author Abhishek N
 */
@Data
public class SaveRecoLogicRequest {
    private List<String> tenders;
    private String createdBy;
    private String effectiveFrom;
    private String effectiveTo;
    private String effectiveType;
    private JsonNode recoData;
}
