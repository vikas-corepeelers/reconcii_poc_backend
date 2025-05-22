//package com.cpl.reconciliation.tasks.service.ThreePO.utr;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.ZomatoUTRDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.Zomato;
//import com.cpl.reconciliation.domain.entity.ZomatoUTREntity;
//import com.cpl.reconciliation.domain.models.ZomatoUTRModel;
//import com.cpl.reconciliation.domain.repository.ZomatoRepository;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.cpl.reconciliation.tasks.utils.Utility;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.nio.charset.StandardCharsets;
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
//public class ZomatoUTRServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private final ZomatoUTRDao zomatoUTRDao;
//    private final Utility utility;
//    private final ZomatoRepository repository;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("csv");
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) throws IOException {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.ZOMATO_UTR;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        String path = "C:\\utr\\zomato";
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            File directory = new File(path);
//            if (directory.exists() && directory.isDirectory()) {
//                File[] files = directory.listFiles(isXLSXFile);
//                if (files != null) {
//                    log.info("Total file size:{}", files.length);
//                    for (File file : files) readTransactionsFromCSV(new FileInputStream(file));
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD), files.length);
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
//    public void upload(LocalDate businessDate, LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) throws IOException {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                readTransactionsFromCSV(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time, businessDate, endDate);
//        } catch (Exception e) {
//            sendFailureMail(time, businessDate, endDate);
//
//        }
//    }
//
//    public void readTransactionsFromCSV(InputStream file) throws IOException {
//        CsvToBean<ZomatoUTRModel> csvToBean = new CsvToBeanBuilder<ZomatoUTRModel>(createReaderFromMultipartFile(file))
//                .withType(ZomatoUTRModel.class)
//                .withIgnoreLeadingWhiteSpace(true)
//                .withOrderedResults(true)
//                .build();
//        List<ZomatoUTRModel> transactionList = csvToBean.parse();
//        List<List<ZomatoUTRModel>> chunks = utility.chunkList(transactionList, 500);
//        int count = 1;
//        log.info("{} Zomato UTR chunks of size 500 getting invoked", chunks.size());
//        for (List<ZomatoUTRModel> chunk : chunks) {
//            List<ZomatoUTREntity> zomatoUTREntities = new ArrayList<>();
//            List<Zomato> zomatoes = new ArrayList<>();
//            for (ZomatoUTRModel model : chunk) {
//                ZomatoUTREntity zomatoUTREntity = new ZomatoUTREntity();
//                zomatoUTREntity.setUtr_number(model.getUtr_number());
//                zomatoUTREntity.setFinal_amount(model.getFinal_amount());
//                zomatoUTREntity.setOrder_id(model.getOrder_id());
//                zomatoUTREntity.setPayout_date(model.getPayout_date());
//                zomatoUTREntity.setId(model.getOrder_id() + "-" + model.getAction() + "-" + model.getService_id());
//
//                Optional<Zomato> zomatoOptional = repository.findById(zomatoUTREntity.getId());
//                if (zomatoOptional.isPresent()) {
//                    Zomato zomato = zomatoOptional.get();
//                    zomato.setPayout_date(zomatoUTREntity.getPayout_date());
//                    zomato.setPayout_amount(zomatoUTREntity.getFinal_amount());
//                    zomato.setReference_number(zomatoUTREntity.getUtr_number());
//                    zomatoes.add(zomato);
//                }
//                zomatoUTREntities.add(zomatoUTREntity);
//
//            }
//            zomatoUTRDao.saveAll(zomatoUTREntities);
//            repository.saveAll(zomatoes);
//            log.info("Zomato UTR chunk {} of count {} write invoked", count++, chunk.size());
//        }
//    }
//}
