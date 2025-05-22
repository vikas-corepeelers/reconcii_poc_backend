//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.HDFCTidChargesDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.HDFCTidChargesEntity;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.github.pjfanning.xlsx.StreamingReader;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Sheet;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.FileFilter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@Data
//@Slf4j
//public class HDFCTidChargesServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//    private final SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    private final HDFCTidChargesDao hdfcTidChargesDao;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.HDFC_TID_CHARGES;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//
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
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                parseExcelFile(inputStream,businessDate,endDate);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//
//        }
//    }
//
//    public void parseExcelFile(InputStream file,LocalDate businessDate, LocalDate endDate) {
//        List<HDFCTidChargesEntity> hdfcTidChargesEntityList = new ArrayList<>();
//        try (Workbook workbook = StreamingReader.builder()
//                .setUseSstTempFile(true)
//                .rowCacheSize(100)
//                .bufferSize(4096)
//                .open(file)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            boolean firstRow = true;
//            for (Row row : sheet) {
//                if (!firstRow) {
//                    HDFCTidChargesEntity hdfcTidChargesEntity = new HDFCTidChargesEntity();
//                    hdfcTidChargesEntity.setMeCode(row.getCell(0).getStringCellValue());
//                    hdfcTidChargesEntity.setTid(row.getCell(1).getStringCellValue());
//                    hdfcTidChargesEntity.setLegalName(row.getCell(2).getStringCellValue());
//                    hdfcTidChargesEntity.setDbaName(row.getCell(3).getStringCellValue());
//                    hdfcTidChargesEntity.setAddress(row.getCell(4).getStringCellValue());
//                    hdfcTidChargesEntity.setCity(row.getCell(5).getStringCellValue());
//                    hdfcTidChargesEntity.setPin(row.getCell(6).getStringCellValue());
//                    hdfcTidChargesEntity.setTel(row.getCell(7).getStringCellValue());
//                    hdfcTidChargesEntity.setAccountNo2(row.getCell(8).getStringCellValue());
//                    hdfcTidChargesEntity.setCreditCard(row.getCell(9).getNumericCellValue());
//                    hdfcTidChargesEntity.setForeignComm(row.getCell(10).getNumericCellValue());
//                    hdfcTidChargesEntity.setDinersComm(row.getCell(11) == null ? null : row.getCell(11).getNumericCellValue());
//                    hdfcTidChargesEntity.setDinersCommOn(row.getCell(12).getNumericCellValue());
//                    hdfcTidChargesEntity.setRuPayFlag(row.getCell(13).getStringCellValue());
//                    hdfcTidChargesEntity.setDdCommSlb1Below2k(row.getCell(14).getNumericCellValue());
//                    hdfcTidChargesEntity.setDdCommSlb2Above2k(row.getCell(15).getNumericCellValue());
//                    hdfcTidChargesEntity.setCmrclOnusRate(row.getCell(16).getNumericCellValue());
//                    hdfcTidChargesEntity.setEffectiveFrom(businessDate);
//                    hdfcTidChargesEntity.setEffectiveTo(endDate);
//                    hdfcTidChargesEntityList.add(hdfcTidChargesEntity);
//                    if (hdfcTidChargesEntityList.size() >= 500) {
//                        hdfcTidChargesDao.saveAll(hdfcTidChargesEntityList);
//                        log.info("HDFC TID Charges {}", hdfcTidChargesEntityList.size());
//                        hdfcTidChargesEntityList.clear();
//                    }
//                } else {
//                    firstRow = false;
//                }
//            }
//            hdfcTidChargesDao.saveAll(hdfcTidChargesEntityList);
//            log.info("HDFC TID Charges imported {}", hdfcTidChargesEntityList.size());
//        } catch (Exception e) {
//            log.error("Exception occurred while reading HDFC TID Charges: ", e);
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
//
//    private LocalDate parseDate(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault()).toLocalDate()
//                        ;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private LocalDateTime parseDate1(String value) {
//        try {
//            if (value != null && !value.isEmpty()) {
//                return dateFormat1.parse(value).toInstant()
//                        .atZone(ZoneId.systemDefault())
//                        .toLocalDateTime();
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
