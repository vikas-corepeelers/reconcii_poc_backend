//package com.cpl.reconciliation.tasks.parser.impl;
//
//import com.cpl.reconciliation.tasks.parser.FileParser;
//import com.cpl.reconciliation.tasks.parser.ParserResult;
//import com.fasterxml.jackson.annotation.JsonInclude;
//import com.fasterxml.jackson.databind.Module;
//import com.fasterxml.jackson.dataformat.xml.XmlMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
//import org.springframework.stereotype.Service;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//@Slf4j
//@Service
//public class XMLFileParser implements FileParser {
//
//    private final XmlMapper xmlMapper;
//
//    public XMLFileParser(){
//        List<Module> modules = new ArrayList<>();
//        JavaTimeModule javaTimeModule = new JavaTimeModule();
//        modules.add(javaTimeModule);
//        this.xmlMapper =  Jackson2ObjectMapperBuilder.xml()
//                .defaultUseWrapper(true)
//                .failOnUnknownProperties(true)
//                .serializationInclusion(JsonInclude.Include.NON_NULL)
//                .modules(modules)
//                .build();
//    }
//
//    @Override
//    public String getFormat() {
//        return "xml";
//    }
//
//    @Override
//    public <T> ParserResult<T> parseFile(File file, Class<T> tClass) {
//        try {
//            T value = xmlMapper.readValue(file, tClass);
//            //log.debug(xmlMapper.writeValueAsString(value));
//            return new ParserResult<>("xml", value);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}
