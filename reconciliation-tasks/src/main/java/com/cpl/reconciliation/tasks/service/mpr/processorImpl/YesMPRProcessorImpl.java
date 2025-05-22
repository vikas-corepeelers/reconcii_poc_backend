//package com.cpl.reconciliation.tasks.service.mpr.processorImpl;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.common.utility.ZIPUtils;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.CardCategory;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.CustomisedFieldsMappingDao;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.domain.repository.CustomisedFieldsMappingRepository;
//import com.cpl.reconciliation.tasks.service.mpr.CardChargesUtilImpl;
//import com.cpl.reconciliation.tasks.service.mpr.MessageProcessor;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import net.lingala.zip4j.ZipFile;
//import net.lingala.zip4j.model.FileHeader;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.PostConstruct;
//import javax.mail.*;
//import javax.mail.internet.MimeBodyPart;
//import java.io.*;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import static com.cpl.reconciliation.core.util.CompositeKeyUtil.getComposite;
//import static com.cpl.reconciliation.tasks.Constants.HDFC_PROCESSED_FLAG;
//
//@Data
//@Slf4j
//@Service
//public class YesMPRProcessorImpl implements MessageProcessor {
//
//    private final static Function<String, String> removeSingleQuotes = (a) -> {
//        if (StringUtils.isEmpty(a)) {
//            return a;
//        } else {
//            return a.replaceAll("'", "");
//        }
//    };
//    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy");
//    private final static DateTimeFormatter UPI_Formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//    private final static DateTimeFormatter Card_Formatter = DateTimeFormatter.ofPattern("dd-MM-yy");
//    private static HashMap<String, String> map = new HashMap<>();
//    private static HashMap<String, String> monthMap = new HashMap<>();
//    private final MPRDao mprDao;
//    private final ZIPUtils zipUtils;
//    private final StoreTIDMappingDao storeTIDMappingDao;
//    private final BankSettlementDateUtil settlementDateUtil;
//    private final CardChargesUtilImpl cardChargesUtil;
//    private final CustomisedFieldsMappingRepository customisedFieldsMappingRepository;
//    private final CustomisedFieldsMappingDao customisedFieldsMappingDao;
//
//    private static LocalDateTime parseDate(String value, DateTimeFormatter dateTimeFormatter) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                String[] dateArr = value.split("-");
//                dateArr[1] = monthMap.get(dateArr[1].toLowerCase());
//                String newDateStr = Arrays.stream(dateArr).collect(Collectors.joining("-"));
//                LocalDateTime localDateTime = LocalDate.parse(newDateStr, dateTimeFormatter).atStartOfDay();
//                return localDateTime;
//            }
//        } catch (Exception e) {
//            log.error("Exception in date parsing: {}", value, e);
//        }
//        return null;
//    }
//
//    @PostConstruct
//    private void postConstruct() {
//        map.put("22924", "M46194");
//        map.put("23549", "M46229");
//        map.put("23550", "M49004");
//        map.put("23551", "M46390");
//        map.put("23552", "M49346");
//        map.put("23553", "M73963");
//        map.put("23554", "M46348");
//        map.put("23555", "M46243");
//        map.put("23556", "M46227");
//        map.put("23557", "M46197");
//        map.put("23559", "M52854");
//        map.put("23560", "M46194");
//        //
//        monthMap.put("jan", "01");
//        monthMap.put("feb", "02");
//        monthMap.put("mar", "03");
//        monthMap.put("apr", "04");
//        monthMap.put("may", "05");
//        monthMap.put("jun", "06");
//        monthMap.put("jul", "07");
//        monthMap.put("aug", "08");
//        monthMap.put("sep", "09");
//        monthMap.put("oct", "10");
//        monthMap.put("nov", "11");
//        monthMap.put("dec", "12");
//    }
//
//    @Override
//    public void processMessages(Message[] messages) throws Exception {
//        String fileType = "zip";
//        String mprSource = System.getProperty("java.io.tmpdir") + "mpr" + File.separator + "hdfc";
//        boolean fileExtracted = false;
//        for (Message message : messages) {
//            Multipart multipart = (Multipart) message.getContent();
//            for (int i = 0; i < multipart.getCount(); i++) {
//                BodyPart bodyPart = multipart.getBodyPart(i);
//                if (fileType == null || (bodyPart.getFileName() != null && bodyPart.getFileName().endsWith(fileType))) {
//                    MimeBodyPart part = (MimeBodyPart) bodyPart;
//                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
//                        String path = System.getProperty("java.io.tmpdir") + part.getFileName();
//                        part.saveFile(path);
//                        unzipMprFiles(mprSource, new File(path));
//                    }
//                }
//            }
//            Flags processedFlag = new Flags(HDFC_PROCESSED_FLAG);
//            message.setFlags(processedFlag, true);
//        }
//    }
//
//    private void unzipMprFiles(String hdfcMPRRootFolder, File file) throws Exception {
//        try {
//            log.info("Extracted file Name: {}, Path: {}", file.getName(), file.getAbsoluteFile());
//            try (ZipFile zipFile = new ZipFile(file, map.getOrDefault(file.getName().split("-")[0], "").toCharArray())) {
//                List<FileHeader> fileHeaders = zipFile.getFileHeaders();
//                int numberOfFiles = fileHeaders.size();
//                if (numberOfFiles == 2) {
//                    String path = hdfcMPRRootFolder + File.separator + file.getName().split("\\.")[0];
//                    zipFile.extractAll(path);
//                    File hdfcMPRFolder = new File(path);
//                    if (hdfcMPRFolder != null && hdfcMPRFolder.exists() && hdfcMPRFolder.isDirectory()) {
//                        try {
//                            File[] mprFiles = hdfcMPRFolder.listFiles();
//                            for (File mprFile : mprFiles) {
//                                try {
//                                    log.info("MPR file Name: {}, Path: {}", mprFile.getName(), mprFile.getAbsoluteFile());
//                                    if (mprFile.getName().endsWith("XLS")) {
//                                        saveXLSFiles(mprFile);
//                                    }
//                                } catch (Exception e) {
//                                    log.error("Exception occurred while parsing HDFC MPR {}", mprFile.getName(), e);
//                                    throw e;
//                                } finally {
//                                    log.info("Delete: {} - {}", mprFile.getName(), mprFile.delete());
//                                }
//                            }
//                        } catch (Exception e) {
//                            log.error("Exception occurred while parsing HDFC MPR: {}", hdfcMPRFolder.getName(), e);
//                            throw e;
//                        } finally {
//                            log.info("Delete: {} - {}", hdfcMPRFolder.getName(), hdfcMPRFolder.delete());
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Exception occurred while parsing HDFC MPR: {}", file.getName(), e);
//            throw e;
//        } finally {
//            log.info("Delete: {} - {}", file.getName(), file.delete());
//        }
//    }
//
//    public void saveXLSFiles(File file) throws IOException {
//        saveXLSFiles(new FileInputStream(file));
//    }
//
//    public void saveXLSFiles(InputStream inputStream) throws IOException {
//        List<MPREntity> hdfcMPRList = new ArrayList<>();
//        try (BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream))) {
//            String lineJustFetched = null;
//            String[] wordsArray;
//            boolean firstLine = true;
//            int row = 2;
//            boolean upiTxn = false;
//            boolean cardTxn = false;
//            boolean end = false;
//            do {
//                lineJustFetched = buf.readLine();
//                if (lineJustFetched == null) return;
//                if (firstLine) {
//                    firstLine = false;
//                    if (!lineJustFetched.isBlank()) {
//                        wordsArray = lineJustFetched.split("\t");
//                        removeQuotes(wordsArray);
//                        if (wordsArray[0].equalsIgnoreCase("MERCHANT CODE")) {
//                            cardTxn = true;
//                            upiTxn = false;
//                            log.info("HDFC MPR importing card transactions");
//                        } else if (wordsArray[0].equalsIgnoreCase("EXTERNAL MID")) {
//                            cardTxn = false;
//                            upiTxn = true;
//                            log.info("HDFC MPR importing UPI transactions");
//                        }
//                    }
//                } else {
//                    if (lineJustFetched.isBlank()) {
//                        if (cardTxn) {
//                            buf.readLine();
//                            lineJustFetched = buf.readLine();
//                            if (!lineJustFetched.isBlank()) {
//                                wordsArray = lineJustFetched.split("\t");
//                                removeQuotes(wordsArray);
//                                if (!wordsArray[0].equalsIgnoreCase("EXTERNAL MID")) {
//                                    cardTxn = false;
//                                    upiTxn = false;
//                                    end = true;
//                                    log.info("HDFC MPR no UPI transactions found");
//                                    break;
//                                } else {
//                                    upiTxn = true;
//                                    cardTxn = false;
//                                    log.info("HDFC MPR importing UPI transactions");
//                                }
//                            }
//                        } else {
//                            end = true;
//                            break;
//                        }
//                    }
//                    wordsArray = lineJustFetched.split("\t");
//                    removeQuotes(wordsArray);
//                    if (upiTxn) {
//                        if (wordsArray.length == 33) {
//                            if (wordsArray[21].equalsIgnoreCase("P2M")) {
//                                MPREntity record = new MPREntity();
//                                record.setPaymentType(PaymentType.UPI);
//                                record.setMid(wordsArray[0]);
//                                record.setTid(wordsArray[1]);
//                                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(record.getTid());
//                                if (storeTIDMapping.isPresent()) {
//                                    record.setStoreId(storeTIDMapping.get().getStoreCode());
//                                } else {
//                                    record.setStoreId(null);
//                                }
//                                record.setTransactionDate(parseDate(wordsArray[9], UPI_Formatter));
//                                record.setSettledDate(parseDate(wordsArray[10], UPI_Formatter));
//                                record.setPayerVA(wordsArray[5]);
//                                record.setTransactionId(wordsArray[7]);
//                                record.setRrn(wordsArray[8]);
//                                record.setMprAmount(parseDouble(wordsArray[12]));
//                                record.setCommission(parseDouble(wordsArray[13]));
//                                record.setCgst(parseDouble(wordsArray[14]));
//                                record.setSgst(parseDouble(wordsArray[15]));
//                                record.setIgst(parseDouble(wordsArray[16]));
//                                record.setUtgst(parseDouble(wordsArray[17]));
//                                record.setGst(record.getCgst() + record.getSgst() + record.getIgst() + record.getUtgst());
//                                record.setSettledAmount(parseDouble(wordsArray[18]));
//                                record.setBank(Bank.HDFC);
//                                record.setUid(record.getTransactionId());
//                                record.setId(record.getUid());
//                                record.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.HDFC, record.getSettledDate()));
//                                hdfcMPRList.add(record);
//                            }
//                        } else {
//                            mprDao.saveAll(hdfcMPRList);
//                            throw new RuntimeException("HDFC UPI Row not as per expected format: " + String.join(",", wordsArray));
//                        }
//                    } else {
//                        if (wordsArray.length == 33) {
//                            MPREntity record = new MPREntity();
//                            if (wordsArray[2] != null && wordsArray[2].trim().equalsIgnoreCase("BAT")) {
//                                record.setPaymentType(PaymentType.CARD);
//                                record.setMid(wordsArray[0]);
//                                record.setTid(wordsArray[1]);
//                                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(record.getTid());
//                                if (storeTIDMapping.isPresent()) {
//                                    record.setStoreId(storeTIDMapping.get().getStoreCode());
//                                } else {
//                                    record.setStoreId(null);
//                                }
//                                record.setCardType(wordsArray[4]);
//                                record.setCardNumber(wordsArray[5]);
//                                record.setTransactionDate(parseDate(wordsArray[6], Card_Formatter));
//                                record.setSettledDate(parseDate(wordsArray[7], Card_Formatter));
//                                record.setAuthCode(wordsArray[8]);
//                                record.setMprAmount(parseDouble(wordsArray[9]));
//                                if (record.getMprAmount() <= 0) record.setMprAmount(parseDouble(wordsArray[10]));
//                                record.setTransactionId(wordsArray[11]);
//                                record.setCommission(parseDouble(wordsArray[14]));
//                                record.setServiceTax(parseDouble(wordsArray[15]));
//                                record.setSbCess(parseDouble(wordsArray[16]));
//                                record.setKkCess(parseDouble(wordsArray[17]));
//                                record.setCgst(parseDouble(wordsArray[18]));
//                                record.setSgst(parseDouble(wordsArray[19]));
//                                record.setIgst(parseDouble(wordsArray[20]));
//                                record.setUtgst(parseDouble(wordsArray[21]));
//                                record.setSettledAmount(parseDouble(wordsArray[22]));
//                                String cardCategory = wordsArray[23].trim();
//                                record.setCardCategory(CardCategory.getHDFCCardCategory(removeSingleQuotes.apply(cardCategory)));
//                                record.setArn(wordsArray[24]);
//                                //record.setInvoiceNumber(wordsArray[25]);
//                                record.setGstnTransactionId(wordsArray[26]);
//                                record.setRrn(wordsArray[32]);
//                                record.setBank(Bank.HDFC);
//                                record.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.HDFC, record.getSettledDate()));
//                                record.setUid(getComposite(record.getRrn(), record.getAuthCode(), record.getCardNumber()));
//                                record.setId(record.getGstnTransactionId());
//                                cardChargesUtil.setAndUpdateMDR(record);
//                                hdfcMPRList.add(record);
//                            }
//                        } else {
//                            mprDao.saveAll(hdfcMPRList);
//                            throw new RuntimeException("HDFC CARD Row not as per expected format: " + String.join(",", wordsArray));
//                        }
//                    }
//                }
//                if (hdfcMPRList.size() >= 500) {
//                    mprDao.saveAll(hdfcMPRList);
//                    log.info("HDFC MPR imported {}", hdfcMPRList.size());
//                    hdfcMPRList.clear();
//                }
//            } while (lineJustFetched != null);
//        }
//        mprDao.saveAll(hdfcMPRList);
//        log.info("HDFC MPR imported {}", hdfcMPRList.size());
//    }
//
//    //Parse By Name
//    public void parseExcelFileByName(InputStream file) {
//        Map<String, String> customisedAndActualFieldsMap = customisedFieldsMappingDao.getActualAndCustomisedFieldsMapByDataSource(DataSource.YESBANK_MPR);
//        LocalDateTime minSettledDate = null;
//        LocalDateTime maxSettledDate = null;
//        List<MPREntity> sbiMPRList = new ArrayList<>();
//        try (Workbook workbook = new XSSFWorkbook(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            for (int i = sheet.getFirstRowNum() + 1; i < sheet.getLastRowNum() + 1; i++) {
//                Row row = sheet.getRow(i);
//                MPREntity transaction = new MPREntity();
//                Row headerRow = sheet.getRow(0);
//                String Mid = null;
//                String Tid = null;
//                String cardRegion = null;
//                String cardCategory = null;
//                for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
//                    Cell cell = row.getCell(j);
//                    Cell headerCell = headerRow.getCell(j);
//                    String headerCellValue = headerCell.getStringCellValue().trim();
//                    if (customisedAndActualFieldsMap.get(headerCellValue) != null) {
//                        String actualField = customisedAndActualFieldsMap.get(headerCellValue);
//                        switch (actualField) {
//                            case "mid" -> {
//                                Mid = removeSingleQuotes.apply(cell.getStringCellValue().trim());
//                                if (Mid.contains("SBI Service Tax Number")) break;
//                                transaction.setMid(Mid);
//                            }
//                            case "tid" -> {
//                                Tid = getTID(cell);
//                                transaction.setTid(Tid);
//                            }
//                            case "card_number" -> transaction.setCardNumber(cell.getStringCellValue().trim());
//                            case "transaction_date" -> transaction.setTransactionDate(cell.getLocalDateTimeCellValue());
//                            case "settled_date" -> transaction.setSettledDate(cell.getLocalDateTimeCellValue());
//                            case "rrn" ->
//                                    transaction.setRrn(removeSingleQuotes.apply(cell.getStringCellValue().trim()));
//                            case "auth_code" ->
//                                    transaction.setAuthCode(removeSingleQuotes.apply(cell.getStringCellValue().trim()));
//                            case "mpr_amount" -> transaction.setMprAmount(cell.getNumericCellValue());
//                            case "commisson" -> transaction.setCommission(cell.getNumericCellValue());
//                            case "service_tax" -> transaction.setServiceTax(cell.getNumericCellValue());
//                            case "settled_amount" -> transaction.setSettledAmount(cell.getNumericCellValue());
//                            case "custom_field1" ->
//                                    transaction.setCustomField1(removeSingleQuotes.apply(cell.getStringCellValue().trim()));
//                            case "sb_cess" -> transaction.setSbCess(cell.getNumericCellValue());
//                            case "kk_cess" -> transaction.setKkCess(cell.getNumericCellValue());
//                            case "gst" -> transaction.setGst(cell.getNumericCellValue());
//                            case "card_type" -> transaction.setCardType(cell.getStringCellValue().trim());
//                        }
//                    }
//                    if (headerCellValue.equalsIgnoreCase("Card Region")) {
//                        cardRegion = cell.getStringCellValue().trim();
//                    }
//                    if (headerCellValue.equalsIgnoreCase("Card Category")) {
//                        cardCategory = cell.getStringCellValue().trim();
//                    }
//                }
//                transaction.setCardCategory(CardCategory.getSBICardCategory(cardRegion, cardCategory));
//                Optional<StoreTIDMapping> storeTIDMapping = storeTIDMappingDao.findByTid(transaction.getTid());
//                if (storeTIDMapping.isPresent()) {
//                    transaction.setStoreId(storeTIDMapping.get().getStoreCode());
//                } else {
//                    transaction.setStoreId(null);
//                }
////                transaction.setBank(Bank.getBank());
//                transaction.setPaymentType(PaymentType.CARD);
//                transaction.setExpectedBankSettlementDate(settlementDateUtil.getExpectedSettlementDate(Bank.SBI, transaction.getSettledDate()));
//                transaction.setUid(getComposite(transaction.getRrn(), transaction.getAuthCode(), transaction.getCardNumber()));
//                transaction.setId(transaction.getUid());
//                LocalDateTime currentDate = transaction.getSettledDate();
//                if (minSettledDate == null || currentDate.isBefore(minSettledDate)) {
//                    minSettledDate = currentDate;
//                }
//                if (maxSettledDate == null || currentDate.isAfter(maxSettledDate)) {
//                    maxSettledDate = currentDate;
//                }
//                cardChargesUtil.setAndUpdateMDR(transaction);
//                sbiMPRList.add(transaction);
//                if (sbiMPRList.size() >= 500) {
//                    mprDao.saveAll(sbiMPRList);
//                    log.info("SBI MPR imported {}", sbiMPRList.size());
//                    sbiMPRList.clear();
//                }
//            }
//            mprDao.saveAll(sbiMPRList);
//            mprDao.getMprBankDifference(Bank.SBI.name(), PaymentType.CARD.name(), minSettledDate.format(com.cpl.core.api.constant.Formatter.YYYYMMDD_DASH), maxSettledDate.format(Formatter.YYYYMMDD_DASH));
//            log.info("SBI MPR imported {}", sbiMPRList.size());
//        } catch (Exception e) {
//            log.error("Exception occurred while reading SBI MPR file: ", e);
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
//
//    }
//
//    private String getTID(Cell cell) {
//        if (cell.getCellType().equals(CellType.NUMERIC)) {
//            DecimalFormat df = new DecimalFormat("0");
//            return df.format(cell.getNumericCellValue()).replace("'", "");
//        } else {
//            return cell.getStringCellValue().replace("'", "");
//        }
//    }
//
//
//    private void removeQuotes(String[] wordsArray) {
//        for (int i = 0; i < wordsArray.length; i++) {
//            if (wordsArray[i] != null) {
//                wordsArray[i] = wordsArray[i].replace("'", "");
//            }
//        }
//    }
//
//    private Double parseDouble(String value) {
//        if (value != null && !value.isEmpty()) {
//            return Double.parseDouble(value);
//        }
//        return 0.0;
//    }
//}
