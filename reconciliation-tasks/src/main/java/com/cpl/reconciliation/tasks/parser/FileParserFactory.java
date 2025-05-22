//package com.cpl.reconciliation.tasks.parser;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Component
//public class FileParserFactory {
//
//    private Map<String, FileParser> parserMap = new HashMap<>();
//
//    @Autowired
//    public FileParserFactory(List<FileParser> fileParsers) {
//        fileParsers.forEach(fileParser -> {
//            parserMap.put(fileParser.getFormat(), fileParser);
//        });
//    }
//
//    public FileParser createFileParser(String fileFormat) {
//        log.debug("File format: {}", fileFormat);
//        FileParser fileParser = parserMap.get(fileFormat);
//        if (fileParser == null) {
//            throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
//        }
//        return fileParser;
//    }
//}