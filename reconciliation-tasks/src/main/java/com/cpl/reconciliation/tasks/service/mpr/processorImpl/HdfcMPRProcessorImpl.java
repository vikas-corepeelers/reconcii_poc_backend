//package com.cpl.reconciliation.tasks.service.mpr.processorImpl;
//
//import com.cpl.core.common.utility.ZIPUtils;
//import com.cpl.reconciliation.core.enums.Bank;
//import com.cpl.reconciliation.core.enums.CardCategory;
//import com.cpl.reconciliation.core.enums.PaymentType;
//import com.cpl.reconciliation.domain.dao.MPRDao;
//import com.cpl.reconciliation.domain.dao.StoreTIDMappingDao;
//import com.cpl.reconciliation.domain.entity.MPREntity;
//import com.cpl.reconciliation.domain.entity.StoreTIDMapping;
//import com.cpl.reconciliation.tasks.service.mpr.CardChargesUtilImpl;
//import com.cpl.reconciliation.tasks.service.mpr.MessageProcessor;
//import com.cpl.reconciliation.tasks.utils.BankSettlementDateUtil;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import net.lingala.zip4j.ZipFile;
//import net.lingala.zip4j.model.FileHeader;
//import org.springframework.stereotype.Service;
//import org.springframework.util.FileCopyUtils;
//import org.springframework.util.StringUtils;
//
//import javax.annotation.PostConstruct;
//import javax.mail.*;
//import javax.mail.internet.MimeBodyPart;
//import java.io.*;
//import java.nio.file.Path;
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
//public class HdfcMPRProcessorImpl implements MessageProcessor {
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
//
//    private static boolean isExcelFile(Path file) {
//        String fileName = file.getFileName().toString().toLowerCase();
//        return fileName.endsWith(".xls") || fileName.endsWith(".xlsx");
//    }
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
//    public void processZipFile(InputStream inputStream) throws Exception {
//        String zipSource = System.getProperty("java.io.tmpdir") + "hdfc_mpr.zip";
//        String mprSource = System.getProperty("java.io.tmpdir") + "mpr" + File.separator + "hdfc" + File.separator + UUID.randomUUID();
//        File tempFile = new File(zipSource);
//        FileCopyUtils.copy(inputStream, new FileOutputStream(tempFile));
//        try (ZipFile zipFile = new ZipFile(tempFile)) {
//            zipFile.extractAll(mprSource);
//        } catch (Exception e) {
//            log.error("Error while extracting HDFC MPR zip file", e);
//            throw e;
//        } finally {
//            log.info("Delete: {} - {}", tempFile.getName(), tempFile.delete());
//        }
//        File hdfcMPRRootFolder = new File(mprSource);
//        if (hdfcMPRRootFolder != null && hdfcMPRRootFolder.exists() && hdfcMPRRootFolder.isDirectory()) {
//            try {
//                File[] files = hdfcMPRRootFolder.listFiles((f) -> f.isDirectory());
//                if (files != null) {
//                    if (files.length == 0){
//                        throw new RuntimeException("Incorrect format of HDFC MPR");
//                    }
//                    for (File directory : files) {
//                        try {
//                            log.info("Extracted directory Name: {}, Path: {}", directory.getName(), directory.getAbsoluteFile());
//                            for (File file : directory.listFiles((f) -> f.isFile() && f.getName().endsWith("zip"))) {
//                                unzipMprFiles(mprSource, file);
//                            }
//                        } catch (Exception e) {
//                            log.error("Exception occurred while parsing HDFC MPR: {}", directory.getName(), e);
//                            throw e;
//                        } finally {
//                            log.info("Delete: {} - {}", directory.getName(), directory.delete());
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Exception occurred while parsing HDFC MPR: {}", hdfcMPRRootFolder.getName(), e);
//                throw e;
//            } finally {
//                log.info("Delete: {} - {}", hdfcMPRRootFolder.getName(), hdfcMPRRootFolder.delete());
//            }
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
//                        if (wordsArray[0].equalsIgnoreCase("MERCHANT CODE")){
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
//                        if (cardTxn){
//                            buf.readLine();
//                            lineJustFetched = buf.readLine();
//                            if (!lineJustFetched.isBlank()){
//                                wordsArray = lineJustFetched.split("\t");
//                                removeQuotes(wordsArray);
//                                if (!wordsArray[0].equalsIgnoreCase("EXTERNAL MID")) {
//                                    cardTxn = false;
//                                    upiTxn = false;
//                                    end = true;
//                                    log.info("HDFC MPR no UPI transactions found");
//                                    break;
//                                }
//                                else {
//                                    upiTxn = true;
//                                    cardTxn = false;
//                                    log.info("HDFC MPR importing UPI transactions");
//                                }
//                            }
//                        }
//                        else {
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
