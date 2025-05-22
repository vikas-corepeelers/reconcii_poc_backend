//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.dao.GlobalDataDao;
//import com.cpl.reconciliation.domain.entity.DataEntryLog;
//import com.cpl.reconciliation.domain.entity.GlobalEntity;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
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
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Service
//@Slf4j
//@Data
//public class GlobalDataServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private final GlobalDataDao globalDataDao;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("csv");
//
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.GLOBAL;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        String path = "C:\\global";
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            File directory = new File(path);
//            if (directory.exists() && directory.isDirectory()) {
//                File[] files = directory.listFiles(isXLSXFile);
//                if (files != null) {
//                    log.info("Total file size:{}", files.length);
//                    for (File file : files) readTransactionsFromCSV(new FileInputStream(file));
//                    logInDB(stringToLocalDate(businessDate, Formatter.YYYYMMDD_DASH), files.length);
//                }
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
//
//    public void upload(LocalDate businessDate,LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                readTransactionsFromCSV(inputStream);
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time,businessDate,endDate);
//        } catch (Exception e) {
//            sendFailureMail(time,businessDate,endDate);
//
//        }
//    }
//
//    public void readTransactionsFromCSV(InputStream file) throws IOException {
//        CsvToBean<GlobalEntity> csvToBean = new CsvToBeanBuilder<GlobalEntity>(createReaderFromMultipartFile(file))
//                .withType(GlobalEntity.class)
//                .withIgnoreLeadingWhiteSpace(true)
//                .withOrderedResults(true)
//                .build();
//        List<GlobalEntity> transactionList = csvToBean.parse();
//        List<List<GlobalEntity>> chunks = chunkList(transactionList, 500);
//        int count = 1;
//        log.info("{} Global chunks of size 500 getting invoked", chunks.size());
//        for (List<GlobalEntity> chunk : chunks) {
//            globalDataDao.saveAll(chunk);
//            log.info("Global 3PO chunk {} of count {} write invoked", count++, chunk.size());
//        }
//    }
//
//    public static BufferedReader createReaderFromMultipartFile(InputStream inputStream) throws IOException {
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        return new BufferedReader(inputStreamReader);
//    }
//
//    public List<List<GlobalEntity>> chunkList(List<GlobalEntity> list, int chunkSize) {
//        if (chunkSize <= 0) {
//            throw new IllegalArgumentException("Chunk size must be greater than zero");
//        }
//        int listSize = list.size();
//        int numberOfChunks = (int) Math.ceil((double) listSize / chunkSize);
//        return IntStream.range(0, numberOfChunks)
//                .mapToObj(i -> list.subList(i * chunkSize, Math.min((i + 1) * chunkSize, listSize)))
//                .collect(Collectors.toList());
//    }
//
//}
