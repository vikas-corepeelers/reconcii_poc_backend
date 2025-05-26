/*
 * package com.cpl.reconciliation.tasks.service.reco.parser;
 * 
 * import com.cpl.reconciliation.core.request.RecoLogicRequest; import
 * com.cpl.reconciliation.domain.entity.RecoLogicsEntity; import
 * com.cpl.reconciliation.domain.repository.RecoLogicRepository; import
 * com.cpl.reconciliation.domain.util.QueryConfig; import static
 * com.cpl.reconciliation.domain.util.QueryConfig.
 * TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP; import static
 * com.cpl.reconciliation.domain.util.QueryConfig.
 * TENDER_WISE_UNRECONCILED_REASONS_MAP; import
 * com.fasterxml.jackson.core.type.TypeReference; import
 * com.fasterxml.jackson.databind.ObjectMapper; import java.util.ArrayList;
 * import java.util.HashMap; import java.util.LinkedHashMap; import
 * java.util.List; import java.util.Map; import java.util.concurrent.TimeUnit;
 * import java.util.regex.Matcher; import java.util.regex.Pattern; import
 * lombok.extern.slf4j.Slf4j; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.scheduling.annotation.Scheduled; import
 * org.springframework.stereotype.Component;
 * 
 * 
 * @author Abhishek N
 * 
 * @Component
 * 
 * @Slf4j public class RecoLogicParser {
 * 
 * @Autowired RecoLogicRepository recoRepository;
 * 
 * @Autowired ObjectMapper objectMapper;
 * 
 * boolean executeAtAppStartup = true;
 * 
 * @Scheduled(fixedDelay = 60, initialDelay = 0, timeUnit = TimeUnit.SECONDS)
 * public void parse() throws Exception {
 * 
 * List<RecoLogicsEntity> logicsEntities = recoRepository.findAll();
 * 
 * for (RecoLogicsEntity entity : logicsEntities) {
 * 
 * if (executeAtAppStartup || !entity.getStatus().equalsIgnoreCase("PROCESSED"))
 * { Long recoId = entity.getId(); String effectiveType =
 * entity.getEffectiveType(); String effectiveFrom = entity.getEffectiveFrom();
 * String effectiveTo = entity.getEffectiveTo();
 * 
 * List<RecoLogicRequest> recoLogicList =
 * objectMapper.readValue(entity.getRecologic(), new
 * TypeReference<List<RecoLogicRequest>>() { });
 * 
 * Map<String, String> threePOAndPosWiseVariableToDBTableFieldMap = new
 * LinkedHashMap();
 * 
 * Map<String, Map<String, String>> customTableFieldSetValuesMap = new
 * LinkedHashMap(); customTableFieldSetValuesMap.put("Custom", new
 * LinkedHashMap());
 * 
 * Map<String, Map<String, String>> recoLogicFormulaMap = new LinkedHashMap();
 * recoLogicFormulaMap.put("Self", new LinkedHashMap());
 * recoLogicFormulaMap.put("WithPos", new LinkedHashMap());
 * 
 * Map<String, Map<String, String>> dbTableFieldVsFormulaeMap = new
 * LinkedHashMap(); dbTableFieldVsFormulaeMap.put("Self", new LinkedHashMap());
 * dbTableFieldVsFormulaeMap.put("WithPos", new LinkedHashMap());
 * 
 * for (RecoLogicRequest logic : recoLogicList) { Map<String, String> recoLogic
 * = logic.getRecoLogic(); for (Map.Entry<String, String> keyValue :
 * recoLogic.entrySet()) { String variable = keyValue.getKey(); String formulae
 * = keyValue.getValue(); Varible linked with which table's column :: Example,
 * <TOTAL_AMOUNT> = zomato.total_amount if (formulae.contains("=")) { String[]
 * variableStrArray = formulae.split("="); User defined variables are enclosed
 * via <TOTAL_AMOUNT> if (variableStrArray[0].contains("<") &&
 * variableStrArray[0].contains(">")) {verification case String
 * userDefinedVariable = variableStrArray[0].replace("<", "").replace(">",
 * "").trim();
 * threePOAndPosWiseVariableToDBTableFieldMap.put(userDefinedVariable,
 * variableStrArray[1].trim()); } else {db variable with custom hardcoded value
 * verfification check String customeFieldsWithValue =
 * variableStrArray[0].trim(); String prefix = ""; if
 * (customeFieldsWithValue.contains(",")) { String[] dbFields =
 * customeFieldsWithValue.split(","); for (String dbfield : dbFields) { dbfield
 * = dbfield.trim(); if (dbfield.contains(".")) { prefix = dbfield.substring(0,
 * 1).toLowerCase() + "."; dbfield = dbfield.substring(dbfield.indexOf(".") +
 * 1); } customTableFieldSetValuesMap.get("Custom").put(prefix + dbfield.trim(),
 * variableStrArray[1].trim()); } } else { if
 * (customeFieldsWithValue.contains(".")) { customeFieldsWithValue =
 * customeFieldsWithValue.trim(); prefix = customeFieldsWithValue.substring(0,
 * 1).toLowerCase() + "."; customeFieldsWithValue =
 * customeFieldsWithValue.substring(customeFieldsWithValue.indexOf(".") + 1); }
 * customTableFieldSetValuesMap.get("Custom").put(prefix +
 * customeFieldsWithValue, variableStrArray[1].trim()); } } } else { if
 * (formulae.contains("<") && formulae.contains(">")) { String regex =
 * "<(.*?)>"; Pattern pattern = Pattern.compile(regex); Matcher matcher =
 * pattern.matcher(formulae); while (matcher.find()) { String var =
 * matcher.group(1); String replaceingValue = null; if
 * (recoLogicFormulaMap.get("Self").containsKey(var)) { replaceingValue =
 * recoLogicFormulaMap.get("Self").get(var); } else if
 * (recoLogicFormulaMap.get("WithPos").containsKey(var)) { replaceingValue =
 * recoLogicFormulaMap.get("WithPos").get(var); } if (replaceingValue != null) {
 * formulae = formulae.replace("<" + var + ">", "(" + replaceingValue + ")"); }
 * } } if (!formulae.contains("orders.")) {
 * recoLogicFormulaMap.get("Self").put(variable, formulae); } else {
 * recoLogicFormulaMap.get("WithPos").put(variable, formulae); } } } } log.info(
 * "*************************************************************************************"
 * ); for (Map.Entry<String, Map<String, String>> tenderWiseLogicKeyValue :
 * recoLogicFormulaMap.entrySet()) { String type =
 * tenderWiseLogicKeyValue.getKey(); Map<String, String> logicKeyValueMap =
 * tenderWiseLogicKeyValue.getValue(); for (Map.Entry<String, String>
 * logicKeyValue : logicKeyValueMap.entrySet()) { if
 * (threePOAndPosWiseVariableToDBTableFieldMap.containsKey(logicKeyValue.getKey(
 * ))) { String mappedDBTableField =
 * threePOAndPosWiseVariableToDBTableFieldMap.get(logicKeyValue.getKey()); if
 * (mappedDBTableField != null) { String formulae = logicKeyValue.getValue(); if
 * (mappedDBTableField.contains("zomato.") || formulae.contains("zomato.")) {
 * mappedDBTableField = mappedDBTableField.replace("zomato.", "z.").trim();
 * formulae = formulae.replace("zomato.", "z."); } else if
 * (mappedDBTableField.contains("swiggy.") || formulae.contains("swiggy.")) {
 * mappedDBTableField = mappedDBTableField.replace("swiggy.", "s.").trim();
 * formulae = formulae.replace("swiggy.", "s."); } else if
 * (mappedDBTableField.contains("magicpin.") || formulae.contains("magicpin."))
 * { mappedDBTableField = mappedDBTableField.replace("magicpin.", "m.").trim();
 * formulae = formulae.replace("magicpin.", "m."); } if
 * (formulae.contains("orders.")) { formulae = formulae.replace("orders.",
 * "o."); } dbTableFieldVsFormulaeMap.get(type).put(mappedDBTableField, "ROUND("
 * + formulae + ",2)"); } } } } List<Map<String, Map<String, String>>>
 * formulaesList = new ArrayList();
 * formulaesList.add(customTableFieldSetValuesMap);
 * formulaesList.add(dbTableFieldVsFormulaeMap);
 * 
 * Map<String, Map<String, String>> customFieldsList = formulaesList.get(0);
 * Map<String, String> customKeyValueFields = customFieldsList.get("Custom");
 * 
 * Map<String, Map<String, String>> threePolist = formulaesList.get(1);
 * Map<String, String> selfKeyValueFields = threePolist.get("Self"); Map<String,
 * String> withPosKeyValueFields = threePolist.get("WithPos");
 * 
 * log.info("CustomKeyValueFields:: {} ", customKeyValueFields);
 * log.info("selfKeyValueFields:: {} ", selfKeyValueFields);
 * log.info("withPosKeyValueFields:: {} ", withPosKeyValueFields);
 * 
 * StringBuilder reconciledAmountQueryCasePart = new StringBuilder();
 * StringBuilder unReconciledAmountQueryCasePart = new StringBuilder();
 * 
 * StringBuilder threePOSalesPart = new StringBuilder(); StringBuilder
 * POSSalesPart = new StringBuilder();
 * 
 * StringBuilder threepoReceivablesPart = new StringBuilder(); StringBuilder
 * posReceivablesPart = new StringBuilder();
 * 
 * StringBuilder commissionAmountThreePOPart = new StringBuilder();
 * StringBuilder commissionAmountPosPart = new StringBuilder();
 * 
 * StringBuilder tdsAmountThreePOPart = new StringBuilder(); StringBuilder
 * tdsAmountPosPart = new StringBuilder();
 * 
 * StringBuilder chargesGSTThreePOPart = new StringBuilder(); StringBuilder
 * chargesGSTPosPart = new StringBuilder();
 * 
 * StringBuilder consumerGSTThreePoPart = new StringBuilder(); StringBuilder
 * consumerGSTPosPart = new StringBuilder();
 * 
 * StringBuilder pgChargeThreePOPart = new StringBuilder(); StringBuilder
 * pgChargePosPart = new StringBuilder();
 * 
 * if (entity.getTender().toLowerCase().contains("zomato")) { Threepo Sales =>
 * Actual vs Calculated if (selfKeyValueFields.get("z.total_amount") != null) {
 * threePOSalesPart.append("CASE\n" + "    WHEN z.action = 'sale' THEN " +
 * selfKeyValueFields.get("z.total_amount") + "\n" + "    ELSE 0\n" + "END"); //
 * threePOSalesPart.append("CALCULATE_THREE_PO_SALES_ZOMATO_NEW(").append(
 * selfKeyValueFields.get("z.total_amount")).append(",'zomato',z.action)"); }
 * else { threePOSalesPart.append("0"); } POS Sales => Actual vs Calculated if
 * (withPosKeyValueFields.get("z.total_amount") != null) { String
 * posSalesCalculation = withPosKeyValueFields.get("z.total_amount");
 * POSSalesPart.append(posSalesCalculation); } else { POSSalesPart.append("0");
 * } Threepo Receivables => Actual vs Calculated if
 * (selfKeyValueFields.get("z.final_amount") != null) {
 * threepoReceivablesPart.append(selfKeyValueFields.get("z.final_amount")); }
 * else { threepoReceivablesPart.append("0"); } POS Receivables => Actual vs
 * Calculated if (withPosKeyValueFields.get("z.final_amount") != null) { String
 * receceivablesAmountPos = withPosKeyValueFields.get("z.final_amount");
 * posReceivablesPart.append("CASE\n" + "    WHEN z.action = 'sale' THEN " +
 * receceivablesAmountPos + "\n" + "    ELSE 0\n" + "END"); //
 * posReceivablesPart.append("CALCULATE_POS_RECEIVABLES_FOR_THREEPO_ZOMATO_NEW("
 * ).append(receceivablesAmountPos).append(",'zomato',z.action,'','')"); } else
 * { posReceivablesPart.append("0"); } Threepo Commission => Actual vs
 * Calculated if (selfKeyValueFields.get("z.commission_value") != null) {
 * commissionAmountThreePOPart.append(selfKeyValueFields.get(
 * "z.commission_value")); } else { commissionAmountThreePOPart.append("0"); }
 * POS Commission => Actual vs Calculated if
 * (withPosKeyValueFields.get("z.commission_value") != null) { String
 * commisionAmountPos = withPosKeyValueFields.get("z.commission_value");
 * commissionAmountPosPart.append("CASE\n" + "    WHEN z.action = 'sale' THEN "
 * + commisionAmountPos + "\n" + "    ELSE 0\n" + "END"); //
 * commissionAmountPosPart.append("CALCULATE_COMMISSION_AMOUNT_POS_ZOMATO_NEW(")
 * .append(commisionAmountPos).append(",'zomato',z.action)"); } else {
 * commissionAmountPosPart.append("0"); } Threepo tds => Actual vs Calculated if
 * (selfKeyValueFields.get("z.tds_amount") != null) {
 * tdsAmountThreePOPart.append(selfKeyValueFields.get("z.tds_amount")); } else {
 * tdsAmountThreePOPart.append("0"); } POS tds => Actual vs Calculated if
 * (withPosKeyValueFields.get("z.tds_amount") != null) { String tdsAmountPos =
 * withPosKeyValueFields.get("z.tds_amount"); tdsAmountPosPart.append("CASE\n" +
 * "    WHEN z.action = 'sale' THEN " + tdsAmountPos + "\n" + "    ELSE 0\n" +
 * "END"); //
 * tdsAmountPosPart.append("CALCULATE_TDS_POS_ZOMATO_NEW(").append(tdsAmountPos)
 * .append(",'zomato',z.action)"); } else { tdsAmountPosPart.append("0"); }
 * Threepo Charges GST => Actual vs Calculated if
 * (selfKeyValueFields.get("z.taxes_zomato_fee") != null) {
 * chargesGSTThreePOPart.append(selfKeyValueFields.get("z.taxes_zomato_fee")); }
 * else { chargesGSTThreePOPart.append("0"); } POS Charges GST => Actual vs
 * Calculated if (withPosKeyValueFields.get("z.taxes_zomato_fee") != null) {
 * String taxesZomatoFeePos = withPosKeyValueFields.get("z.taxes_zomato_fee");
 * chargesGSTPosPart.append("CASE\n" + "    WHEN z.action = 'sale' THEN " +
 * taxesZomatoFeePos + "\n" + "    ELSE 0\n" + "END"); //
 * chargesGSTPosPart.append("CALCULATE_CHARGES_GST_POS_ZOMATO_NEW(").append(
 * taxesZomatoFeePos).append(",'zomato',z.action)"); } else {
 * chargesGSTPosPart.append("0"); } Threepo Consumer GST => Actual vs Calculated
 * if (selfKeyValueFields.get("z.gst_consumer_bill") != null) {
 * consumerGSTThreePoPart.append(selfKeyValueFields.get("z.gst_consumer_bill"));
 * } else { consumerGSTThreePoPart.append("0"); } POS Consumer GST => Actual vs
 * Calculated if (withPosKeyValueFields.get("z.gst_consumer_bill") != null) {
 * String consumerGstPartPos = withPosKeyValueFields.get("z.gst_consumer_bill");
 * consumerGSTPosPart.append(consumerGstPartPos);
 * //consumerGSTPosPart.append("CALCULATE_CONSUMER_GST_POS_ZOMATO_NEW(").append(
 * consumerGstPartPos).append(",'zomato')"); } else {
 * consumerGSTPosPart.append("0"); } Threepo PGCharge => Actual vs Calculated if
 * (selfKeyValueFields.get("z.pg_charge") != null) {
 * pgChargeThreePOPart.append(selfKeyValueFields.get("z.pg_charge")); } else {
 * pgChargeThreePOPart.append("0"); } POS PGCharge => Actual vs Calculated if
 * (withPosKeyValueFields.get("z.pg_charge") != null) { String pgchargePart =
 * withPosKeyValueFields.get("z.pg_charge"); pgChargePosPart.append("CASE\n" +
 * "    WHEN z.action = 'addition' THEN 0\n" + "    ELSE " + pgchargePart + "\n"
 * + "END"); //
 * pgChargePosPart.append("CALCULATE_PG_CHARGE_POS_NEW(").append(pgchargePart).
 * append(",'zomato',z.action)"); } else { pgChargePosPart.append("0"); }
 * 
 * reconciledAmountQueryCasePart.append("CASE\n" +
 * " WHEN z.action IN ('deduction', 'refund', 'cancel', 'addition') THEN 0\n" +
 * " ELSE \n" + " CASE\n" + " WHEN (");
 * unReconciledAmountQueryCasePart.append("CASE\n" +
 * " WHEN z.action IN ('deduction', 'refund', 'cancel', 'addition') THEN 0\n" +
 * " ELSE \n" + " CASE\n" + " WHEN ("); } else if
 * (entity.getTender().toLowerCase().contains("swiggy")) { Threepo Sales =>
 * Actual vs Calculated String threePoSalePart =
 * selfKeyValueFields.get("s.net_bill_value_without_tax"); if (threePoSalePart
 * != null) { threePOSalesPart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + threePoSalePart + "\n" +
 * "    ELSE 0\n" + "END"); //
 * threePOSalesPart.append("CALCULATE_THREE_PO_SALES_SWIGGY_NEW(").append(
 * threePoSalePart).append(",'swiggy',s.order_status)"); } else {
 * threePOSalesPart.append("0"); } POS Sales => Actual vs Calculated if
 * (withPosKeyValueFields.get("s.net_bill_value_without_tax") != null) { String
 * posCalculation = withPosKeyValueFields.get("s.net_bill_value_without_tax");
 * POSSalesPart.append(posCalculation); } else { POSSalesPart.append("0"); }
 * Threepo Receivables => Actual vs Calculated if
 * (selfKeyValueFields.get("s.net_payable_amount_after_tcs_and_tds") != null) {
 * threepoReceivablesPart.append(selfKeyValueFields.get(
 * "s.net_payable_amount_after_tcs_and_tds")); } else {
 * threepoReceivablesPart.append("0"); } POS Receivables => Actual vs Calculated
 * if (withPosKeyValueFields.get("s.net_payable_amount_after_tcs_and_tds") !=
 * null) { String posReceivablesAount =
 * withPosKeyValueFields.get("s.net_payable_amount_after_tcs_and_tds");
 * posReceivablesPart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + posReceivablesAount + "\n" +
 * "    ELSE 0\n" + "END"); //
 * posReceivablesPart.append("CALCULATE_POS_RECEIVABLES_FOR_THREEPO_SWIGGY_NEW("
 * ).append(posReceivablesAount).append(",'swiggy',s.order_status,'','')"); }
 * else { posReceivablesPart.append("0"); } Threepo Commission => Actual vs
 * Calculated if (selfKeyValueFields.get("s.commission_value") != null) {
 * commissionAmountThreePOPart.append(selfKeyValueFields.get(
 * "s.commission_value")); } else { commissionAmountThreePOPart.append("0"); }
 * POS Commission => Actual vs Calculated if
 * (withPosKeyValueFields.get("s.total_swiggy_service_fee") != null) { String
 * commisionAmountPos = withPosKeyValueFields.get("s.total_swiggy_service_fee");
 * commissionAmountPosPart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + commisionAmountPos + "\n" +
 * "    ELSE 0\n" + "END"); //
 * commissionAmountPosPart.append("CALCULATE_COMMISSION_AMOUNT_POS_SWIGGY_NEW(")
 * .append(commisionAmountPos).append(",'swiggy',s.order_status)"); } else {
 * commissionAmountPosPart.append("0"); } Threepo TDS => Actual vs Calculated if
 * (selfKeyValueFields.get("s.tds") != null) {
 * tdsAmountThreePOPart.append(selfKeyValueFields.get("s.tds")); } else {
 * tdsAmountThreePOPart.append("0"); } POS TDS => Actual vs Calculated if
 * (withPosKeyValueFields.get("s.tds") != null) { String tdsAmountPos =
 * withPosKeyValueFields.get("s.tds"); tdsAmountPosPart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + tdsAmountPos + "\n" +
 * "    ELSE 0\n" + "END"); //
 * tdsAmountPosPart.append("CALCULATE_TDS_POS_SWIGGY_NEW(").append(tdsAmountPos)
 * .append(",'swiggy',s.order_status)"); } else { tdsAmountPosPart.append("0");
 * } Threepo Charges GST => Actual vs Calculated if
 * (selfKeyValueFields.get("s.gst_on_order_including_cess") != null) {
 * chargesGSTThreePOPart.append(selfKeyValueFields.get(
 * "s.gst_on_order_including_cess")); } else {
 * chargesGSTThreePOPart.append("0"); } POS Charges GST => Actual vs Calculated
 * if (withPosKeyValueFields.get("s.gst_on_order_including_cess") != null) {
 * String gstCharges =
 * withPosKeyValueFields.get("s.gst_on_order_including_cess");
 * chargesGSTPosPart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + gstCharges + "\n" +
 * "    ELSE 0\n" + "END"); //
 * chargesGSTPosPart.append("CALCULATE_CHARGES_GST_POS_SWIGGY_NEW(").append(
 * gstCharges).append(",'swiggy',s.order_status)"); } else {
 * chargesGSTPosPart.append("0"); }
 * 
 * consumerGSTThreePoPart.append("0"); consumerGSTPosPart.append("0"); //
 * consumerGSTPosPart.append("CALCULATE_CONSUMER_GST_POS_SWIGGY_NEW(").append(
 * "0").append(",'swiggy')");
 * 
 * pgChargeThreePOPart.append("0"); pgChargePosPart.append("0"); //
 * pgChargePosPart.append("CALCULATE_PG_CHARGE_POS_NEW(").append("0").append(
 * ",'swiggy',s.order_status)");
 * 
 * reconciledAmountQueryCasePart.append("CASE\n" +
 * " WHEN s.order_status IN ('cancelled') THEN 0\n" + " ELSE \n" + " CASE\n" +
 * " WHEN ("); unReconciledAmountQueryCasePart.append("CASE\n" +
 * " WHEN s.order_status IN ('cancelled') THEN 0\n" + " ELSE \n" + " CASE\n" +
 * " WHEN ("); } else if (entity.getTender().toLowerCase().contains("magicpin"))
 * { // reconciledAmountQueryCasePart.append("CASE\n" // + " WHEN ("); }
 * QueryConfig queryConfig = new QueryConfig(); if
 * (entity.getTender().toLowerCase().contains("zomato")) {To be used in
 * ThreePOvsPOS report TENDER_WISE_UNRECONCILED_REASONS_MAP.put("ZOMATO_CUSTOM",
 * customKeyValueFields);
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("ZOMATO_SELF", selfKeyValueFields);
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("ZOMATO_POS",
 * withPosKeyValueFields); } else if
 * (entity.getTender().toLowerCase().contains("swiggy")) {
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("SWIGGY_CUSTOM",
 * customKeyValueFields);
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("SWIGGY_SELF", selfKeyValueFields);
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("SWIGGY_POS",
 * withPosKeyValueFields); } else if
 * (entity.getTender().toLowerCase().contains("magicpin")) {
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("MAGICPIN_CUSTOM",
 * customKeyValueFields);
 * TENDER_WISE_UNRECONCILED_REASONS_MAP.put("MAGICPIN_SELF",
 * selfKeyValueFields); TENDER_WISE_UNRECONCILED_REASONS_MAP.put("MAGICPIN_POS",
 * withPosKeyValueFields); } StringBuilder reconciledDiffCases = new
 * StringBuilder(); StringBuilder threePoKeyFieldsUseInRecoCases = new
 * StringBuilder();
 * 
 * StringBuilder exceptionalReportColumns = new StringBuilder(); StringBuilder
 * exceptionalReportWhereCondition = new StringBuilder(); for (Map.Entry<String,
 * String> keyValue : customKeyValueFields.entrySet()) {
 * exceptionalReportColumns.append(keyValue.getKey()).append(",");
 * exceptionalReportWhereCondition.append(keyValue.getKey()).append("<>").append
 * (keyValue.getValue()).append(" OR ");
 * threePoKeyFieldsUseInRecoCases.append("(").append(keyValue.getKey()).
 * append(") AS ").append(keyValue.getKey().substring(2).toUpperCase()).append(
 * "_THREEPO_CUSTOM,");
 * reconciledDiffCases.append("(").append(keyValue.getKey()).append("-").append(
 * keyValue.getValue()).append(") AS ")
 * .append(keyValue.getKey().substring(2).toUpperCase()).append(
 * "_THREEPO_CUSTOM_MISMATCH").append(",");
 * reconciledAmountQueryCasePart.append("ABS(").append(keyValue.getKey()).append
 * ("-").append(keyValue.getValue()).append(")>0 OR ");
 * unReconciledAmountQueryCasePart.append("ABS(").append(keyValue.getKey()).
 * append("-").append(keyValue.getValue()).append(")>0 OR "); } if
 * (!customKeyValueFields.isEmpty()) { exceptionalReportColumns = new
 * StringBuilder(exceptionalReportColumns.toString().substring(0,
 * exceptionalReportColumns.toString().lastIndexOf(",")));
 * exceptionalReportWhereCondition = new
 * StringBuilder(exceptionalReportWhereCondition.toString().substring(0,
 * exceptionalReportWhereCondition.toString().lastIndexOf(" OR "))); } for
 * (Map.Entry<String, String> keyValue : selfKeyValueFields.entrySet()) {
 * threePoKeyFieldsUseInRecoCases.append("(").append(keyValue.getKey()).
 * append(") AS ").append(keyValue.getKey().substring(2).toUpperCase()).append(
 * "_THREEPO_SELF,");
 * reconciledDiffCases.append("(").append(keyValue.getKey()).append("-").append(
 * keyValue.getValue()).append(") AS ")
 * .append(keyValue.getKey().substring(2).toUpperCase()).append(
 * "_THREEPO_SELF_MISMATCH").append(",");
 * reconciledAmountQueryCasePart.append("ABS(").append(keyValue.getKey()).append
 * ("-").append(keyValue.getValue()).append(")>0 OR ");
 * unReconciledAmountQueryCasePart.append("ABS(").append(keyValue.getKey()).
 * append("-").append(keyValue.getValue()).append(")>0 OR "); } for
 * (Map.Entry<String, String> keyValue : withPosKeyValueFields.entrySet()) {
 * threePoKeyFieldsUseInRecoCases.append("(").append(keyValue.getKey()).
 * append(") AS ").append(keyValue.getKey().substring(2).toUpperCase()).append(
 * "_THREEPO_POS,");
 * reconciledDiffCases.append("(").append(keyValue.getKey()).append("-").append(
 * keyValue.getValue()).append(") AS ")
 * .append(keyValue.getKey().substring(2).toUpperCase()).append(
 * "_THREEPO_POS_MISMATCH").append(",");
 * reconciledAmountQueryCasePart.append("ABS(").append(keyValue.getKey()).append
 * ("-").append(keyValue.getValue()).append(")>0 OR ");
 * unReconciledAmountQueryCasePart.append("ABS(").append(keyValue.getKey()).
 * append("-").append(keyValue.getValue()).append(")>0 OR "); }
 * threePoKeyFieldsUseInRecoCases = new
 * StringBuilder(threePoKeyFieldsUseInRecoCases.toString().substring(0,
 * threePoKeyFieldsUseInRecoCases.toString().lastIndexOf(",")));
 * reconciledDiffCases = new
 * StringBuilder(reconciledDiffCases.toString().substring(0,
 * reconciledDiffCases.toString().lastIndexOf(",")));
 * reconciledAmountQueryCasePart = new
 * StringBuilder(reconciledAmountQueryCasePart.toString().substring(0,
 * reconciledAmountQueryCasePart.toString().lastIndexOf("OR ")));
 * unReconciledAmountQueryCasePart = new
 * StringBuilder(unReconciledAmountQueryCasePart.toString().substring(0,
 * unReconciledAmountQueryCasePart.toString().lastIndexOf("OR ")));
 * 
 * if (entity.getTender().toLowerCase().contains("zomato")) { String
 * threePoSalePart = selfKeyValueFields.get("z.total_amount");
 * unReconciledAmountQueryCasePart.append(") THEN \n");
 * reconciledAmountQueryCasePart.append(") THEN 0\n" + " ELSE \n"); if
 * (threePoSalePart != null) { reconciledAmountQueryCasePart.append("CASE\n" +
 * "    WHEN z.action = 'sale' THEN " + threePoSalePart + "\n" + "    ELSE 0\n"
 * + "END"); //
 * reconciledAmountQueryCasePart.append("CALCULATE_THREE_PO_SALES_ZOMATO_NEW(").
 * append(threePoSalePart).append(",'zomato',z.action)");
 * unReconciledAmountQueryCasePart.append("CASE\n" +
 * "    WHEN z.action = 'sale' THEN " + threePoSalePart + "\n" + "    ELSE 0\n"
 * + "END"); //
 * unReconciledAmountQueryCasePart.append("CALCULATE_THREE_PO_SALES_ZOMATO_NEW("
 * ).append(threePoSalePart).append(",'zomato',z.action)"); } else {
 * reconciledAmountQueryCasePart.append("0");
 * unReconciledAmountQueryCasePart.append("0"); }
 * unReconciledAmountQueryCasePart.append("\n ELSE 0\n" + " END\n" + " END");
 * reconciledAmountQueryCasePart.append("\n " + " END\n" + " END"); } else if
 * (entity.getTender().toLowerCase().contains("swiggy")) { String
 * threePoSalePart = selfKeyValueFields.get("s.net_bill_value_without_tax");
 * unReconciledAmountQueryCasePart.append(") THEN \n");
 * reconciledAmountQueryCasePart.append(") THEN 0\n" + " ELSE \n"); if
 * (threePoSalePart != null) { reconciledAmountQueryCasePart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + threePoSalePart + "\n" +
 * "    ELSE 0\n" + "END"); //
 * reconciledAmountQueryCasePart.append("CALCULATE_THREE_PO_SALES_SWIGGY_NEW(").
 * append(threePoSalePart).append(",'swiggy',s.order_status)");
 * unReconciledAmountQueryCasePart.append("CASE\n" +
 * "    WHEN s.order_status = 'delivered' THEN " + threePoSalePart + "\n" +
 * "    ELSE 0\n" + "END"); //
 * unReconciledAmountQueryCasePart.append("CALCULATE_THREE_PO_SALES_SWIGGY_NEW("
 * ).append(threePoSalePart).append(",'swiggy',s.order_status)"); } else {
 * reconciledAmountQueryCasePart.append("0");
 * unReconciledAmountQueryCasePart.append("0"); }
 * unReconciledAmountQueryCasePart.append("\n ELSE 0\n" + " END\n" + " END");
 * reconciledAmountQueryCasePart.append("\n " + " END\n" + " END"); }
 * StringBuilder whereDateRangeConditionQueryPart = new StringBuilder();
 * StringBuilder whereEffectiveTypeConditionQueryPart = new StringBuilder();
 * StringBuilder effectiveTypeFieldInSummaryQueryPart = new StringBuilder(); if
 * (entity.getTender().toLowerCase().contains("zomato")) { if
 * (!effectiveType.isEmpty()) { String effectiveDt; if
 * (effectiveType.toLowerCase().contains("transaction_date")) { effectiveDt =
 * "Date(z.order_date)"; } else { effectiveDt = "Date(z." + effectiveType + ")";
 * } whereEffectiveTypeConditionQueryPart.append(effectiveDt);
 * effectiveTypeFieldInSummaryQueryPart.append(effectiveDt).
 * append(" as businessDate");
 * whereDateRangeConditionQueryPart.append(effectiveDt); if
 * (!effectiveFrom.isEmpty() && !effectiveTo.isEmpty()) {
 * whereDateRangeConditionQueryPart.append(">='").append(effectiveFrom).
 * append("' AND "); whereDateRangeConditionQueryPart.append(effectiveDt);
 * whereDateRangeConditionQueryPart.append("<='").append(effectiveTo).append("'"
 * ); } else if (!effectiveFrom.isEmpty()) {
 * whereDateRangeConditionQueryPart.append(">='").append(effectiveFrom).append(
 * "'"); } } else {
 * whereDateRangeConditionQueryPart.append("Date(z.order_date) <= :businessDate"
 * ); } } else if (entity.getTender().toLowerCase().contains("swiggy")) { if
 * (!effectiveType.isEmpty()) { String effectiveDt; if
 * (effectiveType.toLowerCase().contains("transaction_date")) { effectiveDt =
 * "Date(s.order_date)"; } else { effectiveDt = "Date(s." + effectiveType + ")";
 * } whereEffectiveTypeConditionQueryPart.append(effectiveDt);
 * effectiveTypeFieldInSummaryQueryPart.append(effectiveDt).
 * append(" as businessDate");
 * whereDateRangeConditionQueryPart.append(effectiveDt); if
 * (!effectiveFrom.isEmpty() && !effectiveTo.isEmpty()) {
 * whereDateRangeConditionQueryPart.append(">='").append(effectiveFrom).
 * append("' AND "); whereDateRangeConditionQueryPart.append(effectiveDt);
 * whereDateRangeConditionQueryPart.append("<='").append(effectiveTo).append("'"
 * ); } else if (!effectiveFrom.isEmpty()) {
 * whereDateRangeConditionQueryPart.append(">='").append(effectiveFrom).append(
 * "'"); } } else {
 * whereDateRangeConditionQueryPart.append("Date(s.order_date) <= :businessDate"
 * ); } }
 * 
 * Object[][] recoCases = { {"THREEPO_KEYFIELDS_USE_IN_THREEPOvsPOS",
 * threePoKeyFieldsUseInRecoCases}, {"RECONCILED_ACTUAL_CALCULATED_DIFF_CASES",
 * reconciledDiffCases}, {"RECONCILED_AMOUNT_QUERY_PART",
 * reconciledAmountQueryCasePart}, {"UNRECONCILED_AMOUNT_QUERY_PART",
 * unReconciledAmountQueryCasePart}, {"CALCULATE_THREE_PO_SALES_QUERY_PART",
 * threePOSalesPart}, {"CALCULATE_POS_SALES_QUERY_PART", POSSalesPart},
 * {"CALCULATE_THREEPO_RECEIVABLES_QUERY_PART", threepoReceivablesPart},
 * {"CALCULATE_POS_RECEIVABLES_FOR_THREEPO_QUERY_PART", posReceivablesPart},
 * {"CALCULATE_THREEPO_COMMISSION_AMOUNT_QUERY_PART",
 * commissionAmountThreePOPart}, {"CALCULATE_POS_COMMISSION_AMOUNT_QUERY_PART",
 * commissionAmountPosPart}, {"CALCULATE_THREEPO_TDS_AMOUNT_QUERY_PART",
 * tdsAmountThreePOPart}, {"CALCULATE_POS_TDS_AMOUNT_QUERY_PART",
 * tdsAmountPosPart}, {"CALCULATE_THREEPO_CHARGES_GST_QUERY_PART",
 * chargesGSTThreePOPart}, {"CALCULATE_POS_CHARGES_GST_QUERY_PART",
 * chargesGSTPosPart}, {"CALCULATE_THREEPO_CONSUMER_GST_QUERY_PART",
 * consumerGSTThreePoPart}, {"CALCULATE_POS_CONSUMER_GST_QUERY_PART",
 * consumerGSTPosPart}, {"CALCULATE_THREEPO_PG_CHARGE_QUERY_PART",
 * pgChargeThreePOPart}, {"CALCULATE_POS_PG_CHARGE_QUERY_PART",
 * pgChargePosPart}, {"DATE_RANGE_SUMMARY_CLAUSE_CONDITION",
 * whereDateRangeConditionQueryPart}, {"EFFECTIVETYPE_SUMMARY_CLAUSE_CONDITION",
 * whereEffectiveTypeConditionQueryPart}, {"DATE_EFFECTIVETYPE_SUMMARY_SELECT",
 * effectiveTypeFieldInSummaryQueryPart}, {"EXCEPTIONAL_REPORT_COLUMNS",
 * exceptionalReportColumns}, {"EXCEPTIONAL_REPORT_WHERE_CONDITION",
 * exceptionalReportWhereCondition} };
 * 
 * Map<String, String> dynamicQueryMap = new LinkedHashMap(); if
 * (entity.getTender().toLowerCase().contains("zomato")) { Zomato Summary
 * Dashboard, ThreepoVsPos Zomato PosRecievable, posCharges, posCommision,
 * posReceivableVsReceipts, Reconciled Report Download dynamic Queries dynamic
 * queries for (Object[] cases : recoCases) { String key = cases[0].toString();
 * String value = cases[1].toString();
 * queryConfig.setZomatoThreePOSummaryDynamicQuery(queryConfig.
 * getZomatoThreePOSummaryDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_threePOReceivablesQuery(queryConfig.
 * getZomato_threePOReceivablesQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_threePOCommissionQuery(queryConfig.
 * getZomato_threePOCommissionQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_threePOChargesQuery(queryConfig.
 * getZomato_threePOChargesQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_threePOFreebieQuery(queryConfig.
 * getZomato_threePOFreebieQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_PosFreebieQuery(queryConfig.getZomato_PosFreebieQuery()
 * .replace("<" + key + ">", value));
 * queryConfig.setZomato_allThreePOChargesQuery(queryConfig.
 * getZomato_allThreePOChargesQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato3POvsPOSDynamicQuery(queryConfig.
 * getZomato3POvsPOSDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setZomatoExceptionalReportQuery(queryConfig.
 * getZomatoExceptionalReportQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_posRecievableDynamicQuery(queryConfig.
 * getZomato_posRecievableDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_posChargesDynamicQuery(queryConfig.
 * getZomato_posChargesDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_allPOSChargesQuery(queryConfig.
 * getZomato_allPOSChargesQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_posCommissionDynamicQuery(queryConfig.
 * getZomato_posCommissionDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setZomato_receivablesVsReceiptsDynamicQuery(queryConfig.
 * getZomato_receivablesVsReceiptsDynamicQuery().replace("<" + key + ">",
 * value)); queryConfig.setZomatoReconciledDynamicQuery(queryConfig.
 * getZomatoReconciledDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setZomatoPromoDynamicQuery(queryConfig.getZomatoPromoDynamicQuery
 * ().replace("<" + key + ">", value));
 * queryConfig.setZomatoSaltDynamicQuery(queryConfig.getZomatoSaltDynamicQuery()
 * .replace("<" + key + ">", value));
 * queryConfig.setOrdersNotFoundInZomatoQuery(queryConfig.
 * getOrdersNotFoundInZomatoQuery().replace("<" + key + ">", value));
 * queryConfig.setPosSalesZomatoQuery(queryConfig.getPosSalesZomatoQuery().
 * replace("<" + key + ">", value));
 * queryConfig.setPosSalesZomatoNextDateQuery(queryConfig.
 * getPosSalesZomatoNextDateQuery().replace("<" + key + ">", value)); }
 * dynamicQueryMap.put("THREEPO_SUMMARY_QUERY",
 * queryConfig.getZomatoThreePOSummaryDynamicQuery());
 * dynamicQueryMap.put("THREEPO_SALES_QUERY",
 * queryConfig.getZomato_threePOSaleQuery());
 * dynamicQueryMap.put("THREEPO_RECIEVABLES_QUERY",
 * queryConfig.getZomato_threePOReceivablesQuery());
 * dynamicQueryMap.put("THREEPO_COMMISSION_QUERY",
 * queryConfig.getZomato_threePOCommissionQuery());
 * dynamicQueryMap.put("THREEPO_CHARGES_QUERY",
 * queryConfig.getZomato_threePOChargesQuery());
 * dynamicQueryMap.put("THREEPO_ALL_CHARGES_QUERY",
 * queryConfig.getZomato_allThreePOChargesQuery());
 * dynamicQueryMap.put("THREEPO_FREEBIE_QUERY",
 * queryConfig.getZomato_threePOFreebieQuery());
 * dynamicQueryMap.put("POS_FREEBIE_QUERY",
 * queryConfig.getZomato_PosFreebieQuery());
 * dynamicQueryMap.put("THREEPOvsPOS_QUERY",
 * queryConfig.getZomato3POvsPOSDynamicQuery());
 * dynamicQueryMap.put("ORDER_NOT_FOUND_QUERY",
 * queryConfig.getOrdersNotFoundInZomatoQuery());
 * dynamicQueryMap.put("EXCEPTIONAL_REPORT_QUERY",
 * queryConfig.getZomatoExceptionalReportQuery());
 * dynamicQueryMap.put("POS_SALES_QUERY", queryConfig.getPosSalesZomatoQuery());
 * dynamicQueryMap.put("POS_SALES_NextDate_QUERY",
 * queryConfig.getPosSalesZomatoNextDateQuery());
 * dynamicQueryMap.put("POS_RECIEVABLES_QUERY",
 * queryConfig.getZomato_posRecievableDynamicQuery());
 * dynamicQueryMap.put("POS_CHARGES_QUERY",
 * queryConfig.getZomato_posChargesDynamicQuery());
 * dynamicQueryMap.put("POS_ALL_CHARGES_QUERY",
 * queryConfig.getZomato_allPOSChargesQuery());
 * dynamicQueryMap.put("POS_COMMISSION_QUERY",
 * queryConfig.getZomato_posCommissionDynamicQuery());
 * dynamicQueryMap.put("POS_RECIEVABLE_VS_RECEIPT_QUERY",
 * queryConfig.getZomato_receivablesVsReceiptsDynamicQuery());
 * dynamicQueryMap.put("THREEPO_RECONCILED_REPORT_QUERY",
 * queryConfig.getZomatoReconciledDynamicQuery());
 * dynamicQueryMap.put("POS_PROMO_REPORT_QUERY",
 * queryConfig.getZomatoPromoDynamicQuery());
 * dynamicQueryMap.put("POS_SALT_REPORT_QUERY",
 * queryConfig.getZomatoSaltDynamicQuery());
 * 
 * Map<Long, Map<String, Map<String, String>>> idBasedRecoLogicsMap; Map<String,
 * Map<String, String>> dateRangeBasedRecoLogicsMap; if
 * (TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.containsKey("zomato")) {
 * idBasedRecoLogicsMap = TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.get("zomato");
 * } else { idBasedRecoLogicsMap = new HashMap<>(); }
 * dateRangeBasedRecoLogicsMap = new HashMap<>();
 * dateRangeBasedRecoLogicsMap.put(effectiveFrom + ',' + effectiveTo + "," +
 * effectiveType, dynamicQueryMap); idBasedRecoLogicsMap.put(recoId,
 * dateRangeBasedRecoLogicsMap);
 * TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.put("zomato", idBasedRecoLogicsMap);
 * log.info("queryConfig.getZomatoExceptionalReportQuery {}",
 * queryConfig.getZomatoExceptionalReportQuery()); } else if
 * (entity.getTender().toLowerCase().contains("swiggy")) { for (Object[] cases :
 * recoCases) { String key = cases[0].toString(); String value =
 * cases[1].toString();
 * queryConfig.setSwiggyThreePOSymmaryDynamicQuery(queryConfig.
 * getSwiggyThreePOSymmaryDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_threePOReceivablesQuery(queryConfig.
 * getSwiggy_threePOReceivablesQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_threePOCommissionQuery(queryConfig.
 * getSwiggy_threePOCommissionQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_threePOChargesQuery(queryConfig.
 * getSwiggy_threePOChargesQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_threePOFreebieQuery(queryConfig.
 * getSwiggy_threePOFreebieQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_PosFreebieQuery(queryConfig.getSwiggy_PosFreebieQuery()
 * .replace("<" + key + ">", value));
 * queryConfig.setSwiggy_allThreePOChargesQuery(queryConfig.
 * getSwiggy_allThreePOChargesQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy3POvsPOSDynamicQuery(queryConfig.
 * getSwiggy3POvsPOSDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggyExceptionalReportQuery(queryConfig.
 * getSwiggyExceptionalReportQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_posRecievableDynamicQuery(queryConfig.
 * getSwiggy_posRecievableDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_posChargesDynamicQuery(queryConfig.
 * getSwiggy_posChargesDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_allPOSChargesDymanicQuery(queryConfig.
 * getSwiggy_allPOSChargesDymanicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_posCommissionDynamicQuery(queryConfig.
 * getSwiggy_posCommissionDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggy_receivablesVsReceiptsQuery(queryConfig.
 * getSwiggy_receivablesVsReceiptsQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggyReconciledDynamicQuery(queryConfig.
 * getSwiggyReconciledDynamicQuery().replace("<" + key + ">", value));
 * queryConfig.setSwiggyPromoDynamicQuery(queryConfig.getSwiggyPromoDynamicQuery
 * ().replace("<" + key + ">", value));
 * queryConfig.setOrdersNotFoundInSwiggyQuery(queryConfig.
 * getOrdersNotFoundInSwiggyQuery().replace("<" + key + ">", value));
 * queryConfig.setPosSalesSwiggyQuery(queryConfig.getPosSalesSwiggyQuery().
 * replace("<" + key + ">", value));
 * queryConfig.setPosSalesSwiggyNextDateQuery(queryConfig.
 * getPosSalesSwiggyNextDateQuery().replace("<" + key + ">", value)); }
 * dynamicQueryMap.put("THREEPO_SUMMARY_QUERY",
 * queryConfig.getSwiggyThreePOSymmaryDynamicQuery());
 * dynamicQueryMap.put("THREEPO_SALES_QUERY",
 * queryConfig.getSwiggy_threePOSaleQuery());
 * dynamicQueryMap.put("THREEPO_RECIEVABLES_QUERY",
 * queryConfig.getSwiggy_threePOReceivablesQuery());
 * dynamicQueryMap.put("THREEPO_COMMISSION_QUERY",
 * queryConfig.getSwiggy_threePOCommissionQuery());
 * dynamicQueryMap.put("THREEPO_CHARGES_QUERY",
 * queryConfig.getSwiggy_threePOChargesQuery());
 * dynamicQueryMap.put("THREEPO_ALL_CHARGES_QUERY",
 * queryConfig.getSwiggy_allThreePOChargesQuery());
 * dynamicQueryMap.put("THREEPO_FREEBIE_QUERY",
 * queryConfig.getSwiggy_threePOFreebieQuery());
 * dynamicQueryMap.put("POS_FREEBIE_QUERY",
 * queryConfig.getSwiggy_PosFreebieQuery());
 * dynamicQueryMap.put("THREEPOvsPOS_QUERY",
 * queryConfig.getSwiggy3POvsPOSDynamicQuery());
 * dynamicQueryMap.put("ORDER_NOT_FOUND_QUERY",
 * queryConfig.getOrdersNotFoundInSwiggyQuery());
 * dynamicQueryMap.put("EXCEPTIONAL_REPORT_QUERY",
 * queryConfig.getSwiggyExceptionalReportQuery());
 * dynamicQueryMap.put("POS_SALES_QUERY",
 * queryConfig.getPosSalesSwiggyNextDateQuery());
 * dynamicQueryMap.put("POS_SALES_NextDate_QUERY",
 * queryConfig.getPosSalesSwiggyNextDateQuery());
 * dynamicQueryMap.put("POS_RECIEVABLES_QUERY",
 * queryConfig.getSwiggy_posRecievableDynamicQuery());
 * dynamicQueryMap.put("POS_CHARGES_QUERY",
 * queryConfig.getSwiggy_posChargesDynamicQuery());
 * dynamicQueryMap.put("POS_ALL_CHARGES_QUERY",
 * queryConfig.getSwiggy_allPOSChargesDymanicQuery());
 * dynamicQueryMap.put("POS_COMMISSION_QUERY",
 * queryConfig.getSwiggy_posCommissionDynamicQuery());
 * dynamicQueryMap.put("POS_RECIEVABLE_VS_RECEIPT_QUERY",
 * queryConfig.getSwiggy_receivablesVsReceiptsQuery());
 * dynamicQueryMap.put("THREEPO_RECONCILED_REPORT_QUERY",
 * queryConfig.getSwiggyReconciledDynamicQuery());
 * dynamicQueryMap.put("POS_PROMO_REPORT_QUERY",
 * queryConfig.getSwiggyPromoDynamicQuery());
 * 
 * Map<Long, Map<String, Map<String, String>>> idBasedRecoLogicsMap; Map<String,
 * Map<String, String>> dateRangeBasedRecoLogicsMap; if
 * (TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.containsKey("swiggy")) {
 * idBasedRecoLogicsMap = TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.get("swiggy");
 * } else { idBasedRecoLogicsMap = new HashMap<>(); }
 * dateRangeBasedRecoLogicsMap = new HashMap<>();
 * dateRangeBasedRecoLogicsMap.put(effectiveFrom + ',' + effectiveTo + "," +
 * effectiveType, dynamicQueryMap); idBasedRecoLogicsMap.put(recoId,
 * dateRangeBasedRecoLogicsMap);
 * TENDER_DATERANGE_WISE_DYNAMIC_QUERY_MAP.put("swiggy", idBasedRecoLogicsMap);
 * } entity.setStatus("PROCESSED"); recoRepository.save(entity); } } if
 * (executeAtAppStartup) { executeAtAppStartup = false; } } }
 */