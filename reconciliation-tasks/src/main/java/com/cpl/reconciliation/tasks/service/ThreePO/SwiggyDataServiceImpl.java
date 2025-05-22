//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.core.api.util.StringUtils;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.OrderEntity;
//import com.cpl.reconciliation.domain.entity.Swiggy;
//import com.cpl.reconciliation.domain.entity.SwiggyMappings;
//import com.cpl.reconciliation.domain.repository.*;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Data
//@Slf4j
//@Service
//public class SwiggyDataServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private final SwiggyRepository swiggyRepository;
//    private final OrderRepository orderRepository;
//    private final SwiggyMappingsRepository swiggyMappingsRepository;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//    private final Utility utility;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("csv");
//    private final Set<String> rajasthanCodes = new HashSet<>(Arrays.asList("249749", "253717", "253991", "253992", "254003", "254023", "254121", "491141", "718121"));
//
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.SWIGGY;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        String path = "C:\\Users\\91813\\Documents\\McD\\3PO\\swiggy";
////        String path = "C:\\Users\\91813\\Documents\\McD\\3PO\\test";
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            File directory = new File(path);
//            if (directory.exists() && directory.isDirectory()) {
//                File[] files = directory.listFiles(isXLSXFile);
//                log.info("Total file size:{}", files.length);
//                for (File file : files) readTransactionsFromCSV(new FileInputStream(file));
//                logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//            } else {
//                log.error("The specified directory does not exist.");
//            }
//        } else {
//            log.info("file already parsed for today");
//
//        }
//
//
//    }
//
//    @Override
//    public boolean uploadManually(LocalDate businessDate, LocalDate endDate, List<MultipartFile> files) throws IOException {
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), businessDate, endDate);
//        if (dataEntryLogList.isEmpty()) {
//            List<InputStream> inputStreams = files.stream().map(file -> {
//                try {
//                    return file.getInputStream();
//                } catch (IOException e) {
//                    throw new ApiException("Error reading files");
//                }
//            }).toList();
//
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
//        } else {
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                try (inputStream) {
////                    readTransactionsFromCSV(inputStream);
//                    readBYColumnName(inputStream);
//                }
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            e.printStackTrace();
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//
//
//    public void readBYColumnName(InputStream file) throws IOException {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.SWIGGY);
//        List<Swiggy> swiggyList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                Swiggy swiggy = new Swiggy();
//                Row headerRow = sheet.getRow(0);
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (actualField) {
//                            case "restaurant_id" ->
//                                    swiggy.setRestaurantId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf((long) cell.getNumericCellValue()));
//                            case "order_no" ->
//                                    swiggy.setOrderNo(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf((long) cell.getNumericCellValue()));
//                            case "order_date" -> swiggy.setOrderDate(cell.getLocalDateTimeCellValue());
//                            case "item_total" -> swiggy.setItemTotal(cell.getNumericCellValue());
//                            case "packing_and_service_charges" ->
//                                    swiggy.setPackingAndServiceCharges(cell.getNumericCellValue());
//                            case "merchant_discount" -> swiggy.setMerchantDiscount(cell.getNumericCellValue());
//                            case "net_bill_value_without_tax" ->
//                                    swiggy.setNetBillValueWithoutTax(cell.getNumericCellValue());
//                            case "gst_on_order_including_cess" ->
//                                    swiggy.setGstOnOrderIncludingCess(cell.getNumericCellValue());
//                            case "customer_payable" -> swiggy.setCustomerPayable(cell.getNumericCellValue());
//                            case "swiggy_platform_fee_chargeable_on" ->
//                                    swiggy.setSwiggyPlatformFeeChargeableOn(cell.getNumericCellValue());
//                            case "swiggy_platform_servicefee_percent" ->
//                                    swiggy.setSwiggyPlatformServicefeePercent(cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue()));
//                            case "swiggy_platform_service_fee" ->
//                                    swiggy.setSwiggyPlatformServiceFee(cell.getNumericCellValue());
//                            case "discount_on_swiggy_platform_service_fee" ->
//                                    swiggy.setDiscountOnSwiggyPlatformServiceFee(cell == null ? null : (cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : ((cell.getStringCellValue().isEmpty() || cell.getStringCellValue().equalsIgnoreCase("-")) ? 0.0 : Double.parseDouble(cell.getStringCellValue()))));
//                            case "collection_fee" -> swiggy.setCollectionFee(cell.getNumericCellValue());
//                            case "access_fee" -> swiggy.setAccessFee(cell.getNumericCellValue());
//                            case "merchant_cancellation_charges" ->
//                                    swiggy.setMerchantCancellationCharges(cell.getNumericCellValue());
//                            case "call_center_servicefees" ->
//                                    swiggy.setCallCenterServicefees(cell.getNumericCellValue());
//                            case "total_swiggy_fee" -> swiggy.setTotalSwiggyFee(cell.getNumericCellValue());
//                            case "delivery_fee" -> swiggy.setDeliveryFee(cell.getNumericCellValue());
//                            case "total_swiggy_service_fee" ->
//                                    swiggy.setTotalSwiggyServiceFee(cell.getNumericCellValue());
//                            case "cash_pre_payment_at_restaurant" ->
//                                    swiggy.setCashPrePaymentAtRestaurant(cell.getNumericCellValue());
//                            case "cancellation_attribution" ->
//                                    swiggy.setCancellationAttribution(String.valueOf(cell.getNumericCellValue()));
//                            case "refund_for_disputed_order" ->
//                                    swiggy.setRefundForDisputedOrder(cell.getNumericCellValue());
//                            case "disputed_order_remarks" -> swiggy.setDisputedOrderRemarks(cell.getStringCellValue());
//                            case "total_of_order_level_adjustments" ->
//                                    swiggy.setTotalOfOrderLevelAdjustments(cell.getNumericCellValue());
//                            case "net_payable_amount_before_tcs_and_tds" ->
//                                    swiggy.setNetPayableAmountBeforeTcsAndTds(cell.getNumericCellValue());
//                            case "tcs" -> swiggy.setTcs(cell.getNumericCellValue());
//                            case "tds" -> swiggy.setTds(cell.getNumericCellValue());
//                            case "net_payable_amount_after_tcs_and_tds" ->
//                                    swiggy.setNetPayableAmountAfterTcsAndTds(cell.getNumericCellValue());
//                            case "cancellation_policy_applied" ->
//                                    swiggy.setCancellationPolicyApplied(cell.getStringCellValue());
//                            case "coupon_code_applied" -> swiggy.setCouponCodeApplied(cell.getStringCellValue());
//                            case "discount_campaign_id" -> swiggy.setDiscountCampaignId(cell.getStringCellValue());
//                            case "is_replicated" -> swiggy.setIsReplicated(cell.getStringCellValue());
//                            case "base_order_id" ->
//                                    swiggy.setBaseOrderId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue()));
//                            case "mrp_items" -> swiggy.setMrpItems(cell.getStringCellValue());
//                            case "order_payment_type" -> swiggy.setOrderPaymentType(cell.getStringCellValue());
//                            case "cancellation_time" ->
//                                    swiggy.setCancellationTime(cell.getCellType() == CellType.NUMERIC ? String.valueOf(cell.getNumericCellValue()) : cell.getStringCellValue());
//                            case "pick_up_status" -> swiggy.setPickUpStatus(cell.getStringCellValue());
//                            case "order_status" -> swiggy.setOrderStatus(cell.getStringCellValue());
//                            case "order_category" -> swiggy.setOrderCategory(cell.getStringCellValue());
//                            case "cancelled_by" ->
//                                    swiggy.setCancelled_by(cell == null ? null : cell.getStringCellValue());
//                            case "current_utr" -> swiggy.setCurrentUtr(cell == null ? null : cell.getStringCellValue());
//                            case "nodal_utr" -> swiggy.setNodalUtr(cell == null ? null : cell.getStringCellValue());
//                        }
//                    }
//                }
//                swiggyList.add(swiggy);
//                if (swiggyList.size() >= 500) {
//                    for (Swiggy sw : swiggyList) {
//                        prePersist(sw);
//                    }
//                    swiggyRepository.saveAll(swiggyList);
//                    log.info("Swiggy Data imported {}", swiggyList.size());
//                    swiggyList.clear();
//                }
//            }
//            for (Swiggy swiggy : swiggyList) {
//                prePersist(swiggy);
//            }
//            swiggyRepository.saveAll(swiggyList);
//            log.info("{} swiggy records saved", swiggyList.size());
//
//
//        } catch (Exception e) {
//            log.error("Exception occurred while reading swiggy file: ", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (file != null) {
//                try {
//                    file.close();
//                } catch (IOException e) {
//                    log.error("Exception occurred while closing the fileinputstream: ", e);
//                    throw new RuntimeException(e);
//                }
//            }
//        }
//    }
//
//
//    public void readTransactionsFromCSV(InputStream file) throws IOException {
//        CsvToBean<Swiggy> csvToBean = new CsvToBeanBuilder<Swiggy>(createReaderFromMultipartFile(file))
//                .withType(Swiggy.class)
//                .withIgnoreLeadingWhiteSpace(true)
//                .withOrderedResults(true)
//                .build();
//        List<Swiggy> transactionList = csvToBean.parse();
//
//        int count = 1;
//        List<List<Swiggy>> chunks = utility.chunkList(transactionList, 500);
//        log.info("Total swiggy entries {}, chunk size: {}", transactionList.size(), 500);
//        for (List<Swiggy> chunk : chunks) {
//            chunk.forEach(this::prePersist);
//            swiggyRepository.saveAll(chunk);
//            log.info("{}/{} chunk saved", count++, chunks.size());
//        }
//    }
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) throws IOException {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//
//    String getToken() {
//        String apiKey = "shared-api-key";
//        String secretKey = "shared-secret-key";
//        long tokenCreationTime = System.currentTimeMillis() / 1000;
//        return Jwts.builder().setIssuer(apiKey).setIssuedAt(new Date(tokenCreationTime * 1000))
//                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
//    }
//
//    public void prePersist(Swiggy entity) {
//        List<OrderEntity> orders = orderRepository.findByThreePOSourceAndOrderId("swiggy", entity.getOrderNo());
//        if (StringUtils.isNotEmpty(entity.getRestaurantId())) {
//            SwiggyMappings mcdStoreCode = swiggyMappingsRepository.findBySwiggyStoreCode(entity.getRestaurantId());
//            if (mcdStoreCode != null) {
//                entity.setMcdStoreCode(mcdStoreCode.getStoreCode());
//                if (entity.getItemTotal() != 0) {
//                    entity.setActualPackagingCharge(mcdStoreCode.getPackagingCharge());
//                }
//            }
//        }
//        if (!CollectionUtils.isEmpty(orders)) {
//            entity.setFoundInSTLD(true);
//            entity.setPosTotalAmount(orders.get(0).getTotalAmount());
//            entity.setPosTotalTax(orders.get(0).getTotalTax());
//            entity.setInvoiceNumber(orders.get(0).getInvoiceNumber());
//            entity.setBusinessDate(orders.get(0).getBusinessDate());
//            entity.setReceiptNumber(orders.get(0).getReceiptNumber());
//            entity.setPosId(orders.get(0).getPosId());
//        }
//    }
//}
