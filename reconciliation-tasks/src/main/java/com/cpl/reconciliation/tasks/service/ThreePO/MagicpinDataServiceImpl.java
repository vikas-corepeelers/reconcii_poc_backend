//package com.cpl.reconciliation.tasks.service.ThreePO;
//
//import com.cpl.core.api.constant.Formatter;
//import com.cpl.core.api.exception.ApiException;
//import com.cpl.core.api.util.DateToString;
//import com.cpl.core.api.util.StringUtils;
//import com.cpl.reconciliation.core.enums.DataSource;
//import com.cpl.reconciliation.domain.entity.*;
//import com.cpl.reconciliation.domain.repository.*;
//import com.cpl.reconciliation.tasks.service.AbstractService;
//import com.cpl.reconciliation.tasks.service.DataService;
//import com.poiji.bind.Poiji;
//import com.poiji.exception.PoijiExcelType;
//import com.poiji.option.PoijiOptions;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.*;
//import java.text.SimpleDateFormat;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static com.cpl.core.api.util.DateTimeUtils.stringToLocalDate;
//
//@Service
//@Data
//@Slf4j
//public class MagicpinDataServiceImpl extends AbstractService implements DataService {
//    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
//    private final MagicpinRepository magicpinRepository;
//    private final OrderRepository orderRepository;
//    private final MagicpinMappingsRepository magicpinMappingsRepository;
//    private final MagicpinUTRRepository magicpinUTRRepository;
//    private FileFilter isXLSXFile = (f) -> f.isFile() && f.getName().contains("xlsx");
//
//    @Override
//    public DataSource getDataSource() {
//        return DataSource.MAGICPIN;
//    }
//
//    @Override
//    public void executeTask() throws Exception {
//        String businessDate = DateToString.backDateString(Formatter.YYYYMMDD, 1);
//        String path = "C:\\Users\\91813\\Documents\\McD\\3PO\\magicpin";
//        List<DataEntryLog> dataEntryLogList = dataEntryLogDao.findDataByDateAndDataSource(getDataSource().name(), LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
//        if (dataEntryLogList.isEmpty()) {
//            File directory = new File(path);
//            if (directory.exists() && directory.isDirectory()) {
//                File[] files = directory.listFiles(isXLSXFile);
//                if (files != null) {
//                    log.info("Total file size:{}", files.length);
//
//                    for (File file : files) {
//                        InputStream inputStream = new FileInputStream(file);
//                        readTransactionsFromExcel(inputStream);
//                    }
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
//            uploadAsync(businessDate, endDate,inputStreams,LocalDateTime.now());
//        } else {
//            log.info("file already parsed for today");
//            return false;
//        }
//        return true;
//    }
//
//
//    @Override
//    public void upload(LocalDate businessDate,LocalDate endDate, List<InputStream> inputStreams, LocalDateTime time) {
//        try {
//            for (InputStream inputStream : inputStreams) {
//                try(inputStream){
//                    readTransactionsFromExcel(inputStream);
//                }
//            }
//            logInDB(businessDate, endDate, inputStreams.size());
//            sendSuccessMail(time,businessDate,endDate);
//        } catch (Exception e) {
//            log.error("Error while parsing magicpin date: ",e);
//            sendFailureMail(time,businessDate,endDate);
//
//        }
//    }
//
//    public void readTransactionsFromExcel(InputStream inputStream) {
//        log.info("Going to read Magicpin data file:");
//
//        List<Magicpin> magicpins = new ArrayList<>();
//        PoijiOptions options = PoijiOptions.PoijiOptionsBuilder.settings()
//                .preferNullOverDefault(true)
//                .sheetIndex(0)
//                .dateFormatter(Formatter.MMMdyyyy)
//                .build();
//        try {
//            magicpins = Poiji.fromExcel(inputStream, PoijiExcelType.XLSX, Magicpin.class, options);
//        } catch (Exception e) {
//            log.error("Error while reading magicpin sheet", e);
//            throw new RuntimeException(e);
//        }
//        int count = 1;
//        List<List<Magicpin>> chunks = chunkList(magicpins, 500);
//        log.info("Total magicpin entries {}, chunk size: {}", magicpins.size(), 500);
//        for (List<Magicpin> list : chunks) {
//            magicpinRepository.saveAll(list);
//            log.info("{}/{} chunk saved", count++, chunks.size());
//        }
//
//    }
//
//    public List<List<Magicpin>> chunkList(List<Magicpin> list, int chunkSize) {
//        if (chunkSize <= 0) {
//            throw new IllegalArgumentException("Chunk size must be greater than zero");
//        }
//
//        int listSize = list.size();
//        int numberOfChunks = (int) Math.ceil((double) listSize / chunkSize);
//
//        return IntStream.range(0, numberOfChunks)
//                .mapToObj(i -> list.subList(i * chunkSize, Math.min((i + 1) * chunkSize, listSize)))
//                .collect(Collectors.toList());
//    }
//
//
//}
