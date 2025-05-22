//package com.cpl.reconciliation.tasks.service.order;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.constant.TenderConstant;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.ThreePO;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.entity.*;
//import com.cpl.reconciliation.domain.repository.*;
//import com.cpl.reconciliation.tasks.parser.FileParser;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.annotation.PostConstruct;
//import java.io.FileFilter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.function.Function;
//
//import static com.cpl.reconciliation.tasks.Constants.REGEX_SINGLE_QUOTES;
//
//@Data
//@Slf4j
//@Service
//public class SubwayPosOrderDataServiceImpl extends AbstractService implements DataService {
//
//    private final static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(60);
//    Function<String, String> removeSingleQuotes = (a) -> a.replaceAll(REGEX_SINGLE_QUOTES, "");
//    private static final FileFilter isDir = (f) -> f.isDirectory();
//    private static final FileFilter isFile = (f) -> f.isFile();
//    private static final FileFilter zipFile = (f) -> f.isFile() && f.getName().endsWith("zip");
//    private static final FileFilter xmlFile = (f) -> f.isFile() && f.getName().endsWith("xml");
//    private final ModelMapper modelMapper;
//    private final OrderRepository orderRepository;
//    private final MagicpinRepository magicpinRepository;
//    private final ZomatoRepository zomatoRepository;
//    private final SwiggyRepository swiggyRepository;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//
//    private FileParser fileParser;
//    private final StoreRepository storeRepository;
//    private List<String> rajasthanStoreCodes;
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("MM/dd/YYYY hh:mm:SS");
//
//    @PostConstruct
//    private void init() {
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.POS_ORDERS;
//    }
//
//    @Override
//    public void executeTask() {
//
//    }
//
//    private void updateThreePO(List<Swiggy> swiggies, List<Zomato> zomatoes, List<Magicpin> magicpins,OrderEntity orderEntity) {
//        if (orderEntity.getThreePOSource().equalsIgnoreCase("swiggy")) {
//            swiggies.addAll(swiggyRepository.findByOrderId(orderEntity.getThreePOOrderId()));
//            swiggies.forEach(k -> {
//                k.setFoundInSTLD(true);
//                if ("paid".equalsIgnoreCase(orderEntity.getOrderStatus())) {
//                    k.setPosTotalTax(orderEntity.getTotalTax());
//                    k.setPosTotalAmount(orderEntity.getTotalAmount());
//                    k.setInvoiceNumber(orderEntity.getInvoiceNumber());
//                    k.setBusinessDate(orderEntity.getBusinessDate());
//                    k.setReceiptNumber(orderEntity.getReceiptNumber());
//                    k.setPosId(orderEntity.getPosId());
//                }
//            });
//
//        } else if (orderEntity.getThreePOSource().equalsIgnoreCase("zomato")) {
//            zomatoes.addAll(zomatoRepository.findByOrderId(orderEntity.getThreePOOrderId()));
//            zomatoes.forEach(k -> {
//                k.setFoundInSTLD(true);
//                if ("paid".equalsIgnoreCase(orderEntity.getOrderStatus())) {
//                    k.setPosTotalTax(orderEntity.getTotalTax());
//                    k.setPosTotalAmount(orderEntity.getTotalAmount());
//                    k.setInvoiceNumber(orderEntity.getInvoiceNumber());
//                    k.setBusinessDate(orderEntity.getBusinessDate());
//                    k.setReceiptNumber(orderEntity.getReceiptNumber());
//                    k.setPosId(orderEntity.getPosId());
//                }
//            });
//        } else if (orderEntity.getThreePOSource().equalsIgnoreCase("magicpin")) {
//            magicpins.addAll(magicpinRepository.findByOrderId(orderEntity.getThreePOOrderId()));
//            magicpins.forEach(k -> {
//                k.setFoundInSTLD(true);
//                if ("paid".equalsIgnoreCase(orderEntity.getOrderStatus())) {
//                    k.setPosTotalTax(orderEntity.getTotalTax());
//                    k.setPosTotalAmount(orderEntity.getTotalAmount());
//                    k.setInvoiceNumber(orderEntity.getInvoiceNumber());
//                    k.setBusinessDate(orderEntity.getBusinessDate());
//                    k.setReceiptNumber(orderEntity.getReceiptNumber());
//                    k.setPosId(orderEntity.getPosId());
//                }
//            });
//        }
//        if (!CollectionUtils.isEmpty(magicpins)) {
//            magicpinRepository.saveAll(magicpins);
//        }
//        if (!CollectionUtils.isEmpty(swiggies)) {
//            swiggyRepository.saveAll(swiggies);
//        }
//        if (!CollectionUtils.isEmpty(zomatoes)) {
//            zomatoRepository.saveAll(zomatoes);
//        }
//
//    }
//
//    private void setRajasthanStores() {
//        rajasthanStoreCodes = storeRepository.findRajasthanStoreCodes();
//    }
//
//    public boolean uploadManually(LocalDate businessDate, LocalDate endDate, List<MultipartFile> files) throws IOException {
//        log.info("Going to parse STLD file manually for {}", businessDate);
//        if (files != null || !files.isEmpty()) {
//            List<InputStream> inputStreams = files.stream().map(file -> {
//                try {
//                    return file.getInputStream();
//                } catch (IOException e) {
//                    throw new ApiException("Error reading files");
//                }
//            }).toList();
//            uploadAsync(businessDate, endDate, inputStreams, LocalDateTime.now());
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public void readBYColumnName(InputStream file, Map<String, Object> startEndDateAndStoresMap) throws IOException {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.POS_ORDERS);
//        List<OrderEntity> orderEntityList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                OrderEntity orderEntity = new OrderEntity();
//                Row headerRow = sheet.getRow(0);
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (headerCellValue) {
//                            case "Bill Number" ->
//                                    orderEntity.setBillNumber(cell.getCellType() == CellType.STRING ? removeSingleQuotes.apply(cell.getStringCellValue()) : String.valueOf((long) cell.getNumericCellValue()));
//                            case "Channel" -> orderEntity.setChannel(cell.getStringCellValue());
//                            case "Source" -> orderEntity.setSource(cell.getStringCellValue());
//                            case "Settlement Mode" -> orderEntity.setSettlementMode(cell.getStringCellValue());
//                            case "Mode Name" -> orderEntity.setModeName(cell.getStringCellValue());
//                            case "Gross Amount" -> orderEntity.setGrossAmount(cell.getNumericCellValue());
//                        }
//
//                        switch (actualField) {
//                            case "business_date" ->
//                                    orderEntity.setBusinessDate(cell.getCellType() == CellType.STRING ? parseDate(cell.getStringCellValue()) : LocalDate.parse(dateFormat.format(cell.getDateCellValue()), Formatter.MMDDYY_SLASH));
//                            case "store_name" -> {
//                                orderEntity.setStoreName(cell.getStringCellValue().trim());
//                                orderEntity.setStoreId(null);
//                                orderEntity.setPosId(null);
//                            }
//                            case "order_date" -> {
//                                orderEntity.setOrderDate(cell.getCellType() == CellType.STRING ? parseDateTime(cell.getStringCellValue()) : LocalDateTime.parse(dateFormat1.format(cell.getDateCellValue()), Formatter.MMDDYYYY_HHMMSS_SLASH));
//                                if (!startEndDateAndStoresMap.containsKey("startDate")) {
//                                    startEndDateAndStoresMap.put("startDate", orderEntity.getOrderDate());
//                                } else if (i == sheet.getLastRowNum()) {
//                                    startEndDateAndStoresMap.put("endDate", orderEntity.getOrderDate());
//                                }
//                            }
//                            case "invoice_number" ->
//                                    orderEntity.setInvoiceNumber(cell.getCellType() == CellType.STRING ? removeSingleQuotes.apply(cell.getStringCellValue().trim()) : String.valueOf(cell.getNumericCellValue()));
//                            case "bill_user" ->
//                                    orderEntity.setBillUser(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "sale_type" ->
//                                    orderEntity.setSaleType(cell.getCellType() == CellType.STRING ? cell.getStringCellValue().trim() : String.valueOf(cell.getNumericCellValue()));
//                            case "threeposource" ->
//                                    orderEntity.setThreePOSource(String.valueOf(ThreePO.getEnum(cell.getStringCellValue().trim())));
//                            case "tender_name" -> {
//                                String tenderName = cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue());
//                                orderEntity.setTenderName(tenderName);
//                            }
//                            case "sub_total" ->
//                                    orderEntity.setSubTotal(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : ((cell.getStringCellValue().isEmpty() || cell.getStringCellValue().equalsIgnoreCase("-")) ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                            case "discount" ->
//                                    orderEntity.setDiscount(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : ((cell.getStringCellValue().isEmpty() || cell.getStringCellValue().equalsIgnoreCase("-")) ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                            case "net_sale" ->
//                                    orderEntity.setNetSale(cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : ((cell.getStringCellValue().isEmpty() || cell.getStringCellValue().equalsIgnoreCase("-")) ? 0.0 : Double.parseDouble(cell.getStringCellValue())));
//                            case "gst_ecom_pct" ->
//                                    orderEntity.setGstEcomPct(cell == null ? null : (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("-") ? null : cell.getNumericCellValue()));
//                            case "gst_pct" ->
//                                    orderEntity.setGstPct(cell == null ? null : (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("-") ? null : cell.getNumericCellValue()));
//                            case "packaging_charge_swiggy" ->
//                                    orderEntity.setPackagingChargeSwiggy(cell == null ? null : (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("-") ? null : cell.getNumericCellValue()));
//                            case "restaurant_packaging _charges" ->
//                                    orderEntity.setRestaurantPackagingCharges(cell == null ? null : (cell.getCellType() == CellType.STRING && cell.getStringCellValue().contains("-") ? null : cell.getNumericCellValue()));
//                            case "total_amount" ->
//                                    orderEntity.setTotalAmount(cell == null ? null : cell.getNumericCellValue());
//                            case "mode_name" ->
//                                    orderEntity.setModeName(cell == null ? null : cell.getStringCellValue());
//                            case "transaction_number" ->
//                                    orderEntity.setTransactionNumber(cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf((long) cell.getNumericCellValue()));
//                            case "threepoorder_id" -> {
//                                orderEntity.setThreePOOrderId(cell == null ? null : (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf((long) cell.getNumericCellValue())));
//                                orderEntity.setOrderId(cell == null ? null : (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf((long) cell.getNumericCellValue())));
//                            }
//                            case "bill_time" ->
//                                    orderEntity.setBillTime(cell == null ? null : (cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : String.valueOf(cell.getNumericCellValue())));
//                        }
//                    }
//                }
//                if (orderEntity.getTenderName().toUpperCase().equalsIgnoreCase(TenderConstant.Cash)) {
//                    orderEntity.setTenderName(TenderConstant.Cash);
//                } else if (orderEntity.getTenderName().toUpperCase().equalsIgnoreCase("CREDITCARD")
//                        || (orderEntity.getTenderName().toUpperCase().equalsIgnoreCase("Other")
//                        && orderEntity.getModeName().equalsIgnoreCase("PineLabsPlutus"))) {
//                    orderEntity.setTenderName(TenderConstant._CARD);
//                } else if ((orderEntity.getTenderName().toUpperCase().equalsIgnoreCase("OTHER")
//                        && (orderEntity.getModeName().equalsIgnoreCase("PhonePe -Offline")
//                        || orderEntity.getModeName().equalsIgnoreCase("PineLabsPlutusUPI")))) {
//                    orderEntity.setTenderName(TenderConstant._UPI);
//                } else if (orderEntity.getTenderName().toUpperCase().equalsIgnoreCase("ONLINE")
//                        || orderEntity.getModeName().equalsIgnoreCase("Zomato")
//                        || orderEntity.getModeName().equalsIgnoreCase("Swiggy")
//                        || orderEntity.getModeName().equalsIgnoreCase("Magicpin")) {
//                    orderEntity.setTenderName(TenderConstant._3PO);
//                } else if (orderEntity.getTenderName().toUpperCase().equalsIgnoreCase(TenderConstant._COD)) {
//                    orderEntity.setTenderName(TenderConstant._COD);
//                }
//                orderEntity.setTotalTax((orderEntity.getGstEcomPct() == null ? 0.0 : orderEntity.getGstEcomPct()) + (orderEntity.getGstPct() == null ? 0.0 : orderEntity.getGstPct()));
//                orderEntity.setStoreId(orderEntity.getStoreName().length() >= 9 ? orderEntity.getStoreName().substring(0, 9) : orderEntity.getStoreName().substring(0, orderEntity.getStoreName().length()));
//
//                if (!startEndDateAndStoresMap.containsKey("storeCodes")) {
//                    List<String> stores = new ArrayList();
//                    stores.add(orderEntity.getStoreId());
//                    startEndDateAndStoresMap.put("storeCodes", stores);
//                } else {
//                    List<String> stores = (List<String>) startEndDateAndStoresMap.get("storeCode");
//                    stores.add(orderEntity.getStoreId());
//                    startEndDateAndStoresMap.put("storeCodes", stores);
//                }
//
//                orderEntity.setOrderStatus("paid");
//                orderEntity.setId(orderEntity.getBusinessDate() + "_" + orderEntity.getBillNumber() + "_" + orderEntity.getGrossAmount() + "_" + orderEntity.getSettlementMode());
//                TenderEntity tenderEntity = new TenderEntity();
//                tenderEntity.setName(orderEntity.getTenderName());
//                tenderEntity.setAmount(orderEntity.getTotalAmount());
//                tenderEntity.setOrder(orderEntity);
//                orderEntity.addTender(tenderEntity);
//
//                List<Swiggy> swiggies = new ArrayList<>();
//                List<Zomato> zomatoes = new ArrayList<>();
//                List<Magicpin> magicpins = new ArrayList<>();
//                if (!orderEntity.getThreePOOrderId().equalsIgnoreCase("-")) {
//                    updateThreePO(swiggies, zomatoes, magicpins, orderEntity);
//                } else {
//                    orderEntity.setThreePOOrderId(null);
//                    orderEntity.setThreePOSource("OTHER");
//                }
//                orderEntityList.add(orderEntity);
//                if (orderEntityList.size() >= 500) {
//                    orderRepository.saveAll(orderEntityList);
//                    log.info("Orders Data imported {}", orderEntityList.size());
//                    orderEntityList.clear();
//                }
//            }
//
//            orderRepository.saveAll(orderEntityList);
//            log.info("{} Orders records saved", orderEntityList.size());
//
//        } catch (Exception e) {
//            log.error("Exception occurred while reading trm file: ", e);
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
//    private LocalDate parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDate();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private LocalDateTime parseDateTime(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @Override
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            Map<String, Object> startEndDateAndStoresMap = new ConcurrentHashMap<>();
//            for (InputStream inputStream : inputStreams) {
//                try (inputStream) {
//                    readBYColumnName(inputStream, startEndDateAndStoresMap);
//                }
//            }
//            String startDate = startEndDateAndStoresMap.get("startDate") == null ? null : startEndDateAndStoresMap.get("startDate").toString();
//            String lastDate = startEndDateAndStoresMap.get("startDate") == null ? null : startEndDateAndStoresMap.get("startDate").toString();
//            String storeCodes = startEndDateAndStoresMap.get("storeCodes") == null ? null : String.join(", ", (List<String>) startEndDateAndStoresMap.get("storeCodes"));
//            logInDB(businessDate, endDate, inputStreams.size(), startDate, lastDate, storeCodes);
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//        }
//    }
//}
