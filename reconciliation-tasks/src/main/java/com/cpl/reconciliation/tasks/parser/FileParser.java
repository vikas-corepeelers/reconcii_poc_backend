//package com.cpl.reconciliation.tasks.parser;
//
//import java.io.File;
//import java.nio.file.Path;
//
//public interface FileParser {
//
//    String getFormat();
//
//    default <T> ParserResult<T> parseFile(String filePath, Class<T> tClass) {
//        return parseFile(Path.of(filePath), tClass);
//    }
//
//    default <T> ParserResult<T> parseFile(Path path, Class<T> tClass) {
//        return parseFile(path.toFile(), tClass);
//    }
//
//    <T> ParserResult<T> parseFile(File path, Class<T> tClass);
//
//    static String getFileExtension(Path filePath) throws Exception {
//        String fileName = filePath.getFileName().toString();
//        int dotIndex = fileName.lastIndexOf('.');
//        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
//            return fileName.substring(dotIndex + 1);
//        }
//        throw new Exception("Invalid file name");
//    }
//}