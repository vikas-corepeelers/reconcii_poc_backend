//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.core.api.util.StringUtils;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.entity.*;
//import com.cpl.reconciliation.domain.repository.*;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
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
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Service
//@Data
//@Slf4j
//public class ZomatoDataServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//
//    private final ZomatoRepository zomatoRepository;
//    private final ZomatoMappingsRepository mappingsRepository;
//    private final ZomatoUTRRepository zomatoUTRRepository;
//    private final OrderRepository orderRepository;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//    private final Utility utility;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("csv");
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.ZOMATO;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        String path = "C:\\Users\\91813\\Documents\\McD\\3PO\\zomato";
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
//    }
//
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
//            log.info("File already parsed for {}", businessDate);
//            return false;
//        }
//        return true;
//    }
//
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
//    public void readTransactionsFromCSV(InputStream file) throws IOException {
//        CsvToBean<Zomato> csvToBean = new CsvToBeanBuilder<Zomato>(createReaderFromMultipartFile(file))
//                .withType(Zomato.class)
//                .withIgnoreLeadingWhiteSpace(true)
//                .withOrderedResults(true)
//                .build();
//        List<Zomato> transactionList = csvToBean.parse();
//        List<List<Zomato>> chunks = utility.chunkList(transactionList, 500);
//        log.info("Total zomato entries {}, chunk size: {}", transactionList.size(), 500);
//        int count = 1;
//        for (List<Zomato> chunk : chunks) {
//            for (Zomato zomato : chunk) {
//                zomato.setId(zomato.getOrderId() + "-" + zomato.getAction() + "-" + zomato.getServiceId());
//                prePersist(zomato);
//            }
//            zomatoRepository.saveAll(chunk);
//            log.info("{}/{} zomato chunk saved", count++, chunks.size());
//
//        }
//    }
//
//    public void readBYColumnName(InputStream file) throws IOException {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.ZOMATO);
//        List<Zomato> zomatoEntryList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                Zomato zomato = new Zomato();
//                Row headerRow = sheet.getRow(0);
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        try {
//                            switch (actualField) {
//                                case "res_id" ->
//                                        zomato.setResId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf((long) cell.getNumericCellValue()));
//                                case "res_name" -> zomato.setResName(cell.getStringCellValue().trim());
//                                case "business_id" -> zomato.setBusinessId(cell.getStringCellValue().trim());
//                                case "service_id" -> zomato.setServiceId(cell.getStringCellValue().trim());
//                                case "order_id" ->
//                                        zomato.setOrderId(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf( (long)cell.getNumericCellValue()));
//                                case "order_date" -> zomato.setOrderDate(cell.getLocalDateTimeCellValue());
//                                case "action" -> zomato.setAction(cell.getStringCellValue().trim());
//                                case "city_id" -> zomato.setCityId(String.valueOf(cell.getNumericCellValue()));
//                                case "city" -> zomato.setCity(cell.getStringCellValue());
//                                case "promo_code" -> zomato.setPromoCode(cell.getStringCellValue());
//                                case "bill_subtotal" ->
//                                        zomato.setBillSubtotal(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "gst_customer_bill" ->
//                                        zomato.setGstCustomerBill(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "source_tax" ->
//                                        zomato.setSourceTax(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "merchant_voucher_discount" ->
//                                        zomato.setMerchantVoucherDiscount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "zomato_voucher_discount" ->
//                                        zomato.setZomatoVoucherDiscount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "payment_method" -> zomato.setPaymentMethod(cell.getStringCellValue().trim());
//                                case "total_amount" ->
//                                        zomato.setTotalAmount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "net_amount" ->
//                                        zomato.setNetAmount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "final_amount" ->
//                                        zomato.setFinalAmount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "currency" -> zomato.setCurrency(cell.getStringCellValue());
//                                case "customer_compensation" ->
//                                        zomato.setCustomerCompensation(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "commission_rate" ->
//                                        zomato.setCommissionRate(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "commission_value" ->
//                                        zomato.setCommissionValue(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "pg_charge" ->
//                                        zomato.setPgCharge(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "pg_charge_base" ->
//                                        zomato.setPgChargeBase(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "tax_rate" ->
//                                        zomato.setTaxRate(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "taxes_zomato_fee" ->
//                                        zomato.setTaxesZomatoFee(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "tcs_amount" ->
//                                        zomato.setTcsAmount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "tcs_base" ->
//                                        zomato.setTcsBase(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "tds_amount" ->
//                                        zomato.setTdsAmount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "logistics_charge" ->
//                                        zomato.setLogisticsCharge(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "pro_discount_passthrough" ->
//                                        zomato.setProDiscountPassthrough(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "customer_discount" ->
//                                        zomato.setCustomerDiscount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "rejection_penalty_charge" ->
//                                        zomato.setRejectionPenaltyCharge(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "cancellation_refund" ->
//                                        zomato.setCancellationRefund(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "promo_recovery_adj" ->
//                                        zomato.setPromoRecoveryAdj(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "icecream_handling" ->
//                                        zomato.setIcecreamHandling(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "icecream_deductions" ->
//                                        zomato.setIcecreamDeductions(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "order_support_cost" ->
//                                        zomato.setOderSupportCost(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "credit_note_amount" ->
//                                        zomato.setCreditNoteAmount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "merchant_pack_charge" ->
//                                        zomato.setMerchantPackCharge(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "merchant_delivery_charge" ->
//                                        zomato.setMerchantDeliveryCharge(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "source_tax_base" ->
//                                        zomato.setSourceTaxBase(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                                case "status" -> zomato.setStatus(cell.getStringCellValue());
//                                case "utr_number" -> zomato.setUtrNumber(cell.getStringCellValue());
//                                case "utr_date" ->
//                                        zomato.setUtrDate(cell.getCellType() == CellType.NUMERIC ? cell.getLocalDateTimeCellValue() : null);
//                                case "user_credit_charge" ->
//                                        zomato.setUserCreditCharge(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : (cell.getStringCellValue().isEmpty() ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                            }
//                        } catch (Exception e) {
//                            log.error("error", e);
//                        }
//
//                    }
//                }
//                zomatoEntryList.add(zomato);
//                if (zomatoEntryList.size() >= 500) {
//                    for (Zomato zmt : zomatoEntryList) {
//                        zmt.setId(zmt.getOrderId() + "-" + zmt.getAction() + "-" + zmt.getServiceId());
//                        prePersist(zmt);
//                    }
//                    zomatoRepository.saveAll(zomatoEntryList);
//                    log.info("Zomato Data imported {}", zomatoEntryList.size());
//                    zomatoEntryList.clear();
//                }
//            }
//            for (Zomato zomato : zomatoEntryList) {
//                zomato.setId(zomato.getOrderId() + "-" + zomato.getAction() + "-" + zomato.getServiceId());
//                prePersist(zomato);
//            }
//            zomatoRepository.saveAll(zomatoEntryList);
//            log.info("{} zomato records saved", zomatoEntryList.size());
//
//
//        } catch (Exception e) {
//            log.error("Exception occurred while reading Zomato file: ", e);
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
//    public void prePersist(Zomato entity) {
//        List<OrderEntity> orders = orderRepository.findByThreePOSourceAndOrderId("zomato", entity.getOrderId());
//        Optional<ZomatoUTREntity> zomatoUTREntityOptional = zomatoUTRRepository.findById(entity.getId());
//        if (zomatoUTREntityOptional.isPresent()) {
//            ZomatoUTREntity zomatoUTREntity = zomatoUTREntityOptional.get();
//            entity.setPayout_date(zomatoUTREntity.getPayout_date());
//            entity.setPayout_amount(zomatoUTREntity.getFinal_amount());
//            entity.setReference_number(zomatoUTREntity.getUtr_number());
//        }
//        if (StringUtils.isNotEmpty(entity.getResId())) {
//            ZomatoMappings mappings = mappingsRepository.findByZomatoStoreCode(entity.getResId());
//            if (mappings != null) {
//                entity.setMcdStoreCode(mappings.getStoreCode());
//                if (entity.getBillSubtotal() != 0) {
//                    entity.setActualPackagingCharge(mappings.getPackagingCharge());
//                }
//                if ("addition".equalsIgnoreCase(entity.getAction())) {
//                    entity.setMerchantPackCharge(mappings.getPackagingCharge());
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
//
//    }
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//
//}
