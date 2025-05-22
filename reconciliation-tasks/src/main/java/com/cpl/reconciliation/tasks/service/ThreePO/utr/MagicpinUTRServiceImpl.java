//package com.cpl.reconciliation.tasks.service.ThreePO.utr;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.Magicpin;
//import com.cpl.reconciliation.domain.entity.MagicpinUTREntity;
//import com.cpl.reconciliation.domain.models.MagicpinUTRModel;
//import com.cpl.reconciliation.domain.repository.MagicpinRepository;
//import com.cpl.reconciliation.domain.repository.MagicpinUTRRepository;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.poiji.bind.Poiji;
//import com.poiji.exception.PoijiExcelType;
//import com.poiji.option.PoijiOptions;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Data
//@Slf4j
//@Service
//public class MagicpinUTRServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("csv");
//    private final MagicpinUTRRepository magicpinUTRRepository;
//    private final Utility utility;
//    private final MagicpinRepository magicpinRepository;
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.MAGICPIN_UTR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        String path = "C:\\utr\\magicpin";
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            File directory = new File(path);
//            if (directory.exists() && directory.isDirectory()) {
//                File[] files = directory.listFiles(isXLSXFile);
//                if (files != null) {
//                    log.info("Total file size:{}", files.length);
//                    for (File file : files) readTransactionsFromExcel(new FileInputStream(file));
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD),files.length);
//                }
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
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
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
//                readTransactionsFromExcel(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//
//        }
//    }
//
//    public void readTransactionsFromExcel(InputStream inputStream) throws IOException {
//        log.info("Going to read Magicpin UTR data file:");
//
//        List<MagicpinUTRModel> transactionList;
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .sheetIndex(0)
//                .dateFormatter(Formatter.DDMMYYYY_SLASH)
//                .build();
//        try {
//            transactionList = Poiji.fromExcel(inputStream, PoijiExcelType.XLSX, MagicpinUTRModel.class, options);
//        } catch (Exception e) {
//            log.error("Error while reading magicpin UTR sheet", e);
//            throw new RuntimeException(e);
//        }
//
//        List<List<MagicpinUTRModel>> chunks = utility.chunkList(transactionList, 500);
//        int count = 1;
//        for (List<MagicpinUTRModel> chunk : chunks) {
//            List<MagicpinUTREntity> magicpinUTREntities = new ArrayList<>();
//            List<Magicpin> magicpins = new ArrayList<>();
//            for (MagicpinUTRModel model : chunk) {
//                MagicpinUTREntity magicpinUTREntity = new MagicpinUTREntity();
//                magicpinUTREntity.setUtr_number(model.getReferenceNumber());
//                magicpinUTREntity.setFinal_amount(model.getFinalAmount());
//                magicpinUTREntity.setOrder_id(model.getOrderId());
//                magicpinUTREntity.setPayout_date(model.getPayoutDate());
//
//                Optional<Magicpin> magicpinOptional = magicpinRepository.findById(magicpinUTREntity.getOrder_id());
//                if (magicpinOptional.isPresent()) {
//                    Magicpin magicpin = magicpinOptional.get();
//                    magicpin.setPayout_date(magicpinUTREntity.getPayout_date());
//                    magicpin.setPayout_amount(magicpinUTREntity.getFinal_amount());
//                    magicpin.setReference_number(magicpinUTREntity.getUtr_number());
//                    magicpins.add(magicpin);
//                }
//                magicpinUTREntities.add(magicpinUTREntity);
//
//            }
//            magicpinUTRRepository.saveAll(magicpinUTREntities);
//            magicpinRepository.saveAll(magicpins);
//            log.info("Magicpin UTR chunk {} of count {} write invoked", count++, chunk.size());
//        }
//    }
//
//}
