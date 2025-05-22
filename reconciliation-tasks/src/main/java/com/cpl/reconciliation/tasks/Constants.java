package com.cpl.reconciliation.tasks;

public class Constants {
    public static String BASE_RESOURCE_LOCATION = "/tmp";
    public static String HDFC_FILE_ATTACHMENT_DOWNLOAD_PATH = BASE_RESOURCE_LOCATION + "/hdfc/download/";
    public static String HDFC_OUTPUT_PATH = BASE_RESOURCE_LOCATION + "/hdfc/output";
    public static String HDFC_PROCESSED_FLAG = "processed5";

    public static String SBI_FILE_ATTACHMENT_DOWNLOAD_PATH = BASE_RESOURCE_LOCATION + "/sbi/download/";
    public static String SBI_OUTPUT_PATH = BASE_RESOURCE_LOCATION + "/sbi/output";
    public static String SBI_PROCESSED_FLAG = "test6";

    public static String ICICI_PROCESSED_FLAG = "test3";
    public static String ICICI_FILE_ATTACHMENT_DOWNLOAD_PATH = BASE_RESOURCE_LOCATION + "/icici/mis/download/";
    public static String ICICI_OUTPUT_PATH = BASE_RESOURCE_LOCATION + "/icici/mis/output";
    public static String ICICI_REFUND_FILE_ATTACHMENT_DOWNLOAD_PATH = BASE_RESOURCE_LOCATION + "/icici/refund/download/";
    public static String ICICI_REFUND_OUTPUT_PATH = BASE_RESOURCE_LOCATION + "/icici/refund/output";

    public static String REGEX_SINGLE_QUOTES = "^'+|'+$";


}
