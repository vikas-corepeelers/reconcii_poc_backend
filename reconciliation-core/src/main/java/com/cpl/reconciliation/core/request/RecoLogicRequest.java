package com.cpl.reconciliation.core.request;

import lombok.Data;
/**
 * @author Abhishek N
 */
import java.util.List;
import java.util.Map;

@Data
public class RecoLogicRequest {

    private int id;
  //  private String logicName;
  //  private List<Field> fields;
  //  private String formulaText;
 //   private String logicNameKey;
  //  private boolean multipleColumn;
  //  private String tender;
    private String effectiveFrom;
    private String effectiveTo;
    private Map<String, String> recoLogic;
    private String dbFields;
    private String createdBy;
    
    
    
    @Data
    private static class Field {
    private String type;
    private String dataset_type;
    private String selectedDataSetValue;
    private String selectedFieldValue;
    private String customFieldValue;
    private List<String> startBrackets;
    private List<String> endBrackets;
}
}
