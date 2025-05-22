//package com.cpl.reconciliation.tasks.parser.impl;
//
//
//import com.cpl.reconciliation.core.annotations.CSVColumn;
//import com.cpl.reconciliation.core.annotations.CSVColumnWrapper;
//import com.cpl.reconciliation.tasks.parser.FileParser;
//import com.cpl.reconciliation.tasks.parser.ParserResult;
//import com.opencsv.CSVReader;
//import com.opencsv.bean.CsvToBean;
//import com.opencsv.bean.CsvToBeanBuilder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.beans.IntrospectionException;
//import java.beans.PropertyDescriptor;
//import java.io.File;
//import java.io.FileReader;
//import java.lang.reflect.*;
//import java.util.List;
//import java.util.Map;
//
//@Slf4j
//@Service
//public class CSVFileParser implements FileParser {
//
//    public static <K> K getNewInstance(Class<K> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
//        Constructor<?> constructor = clazz.getConstructor();
//        Object instance = constructor.newInstance();
//        K myObject = (K) instance;
//        return myObject;
//    }
//
//    private static String getCSVWrapperFieldName(Class<?> clazz) {
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            if (field.isAnnotationPresent(CSVColumnWrapper.class)) {
//                CSVColumnWrapper csvColumnAnnotation = field.getAnnotation(CSVColumnWrapper.class);
//                if (List.class.isAssignableFrom(field.getType())) {
//                    return field.getName();
//                } else {
//                    throw new RuntimeException("@CSVColumnWrapper annotation is valid for List type");
//                }
//            }
//        }
//        throw new RuntimeException("No field annotated with @CSVColumnWrapper");
//    }
//
//    private static Class<?> getCSVWrapperFieldType(Class<?> clazz) {
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            if (field.isAnnotationPresent(CSVColumnWrapper.class)) {
//                CSVColumnWrapper csvColumnAnnotation = field.getAnnotation(CSVColumnWrapper.class);
//                if (List.class.isAssignableFrom(field.getType())) {
//                    return field.getType();
//                } else {
//                    throw new RuntimeException("@CSVColumnWrapper annotation is valid for List type");
//                }
//            }
//        }
//        throw new RuntimeException("No field annotated with @CSVColumnWrapper");
//    }
//
//    private static Class<?> getCSVWrapperFieldGenericType(Class<?> clazz) throws ClassNotFoundException {
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            if (field.isAnnotationPresent(CSVColumnWrapper.class)) {
//                CSVColumnWrapper csvColumnAnnotation = field.getAnnotation(CSVColumnWrapper.class);
//                if (List.class.isAssignableFrom(field.getType())) {
//                    System.out.println("Field Name: " + field.getName());
//                    System.out.println("Field type: " + field.getType());
//                    Type genericType = field.getGenericType();
//                    if (genericType instanceof ParameterizedType) {
//                        ParameterizedType parameterizedType = (ParameterizedType) genericType;
//                        Type[] typeArguments = parameterizedType.getActualTypeArguments();
//                        if (typeArguments.length > 0) {
//                            Type genericFieldType = typeArguments[0];
//                            System.out.println("Generic Type: " + genericFieldType.getTypeName());
//                            return Class.forName(genericFieldType.getTypeName());
//                        } else {
//                            throw new RuntimeException("@CSVColumnWrapper annotated field generic type is not given");
//                        }
//                    }
//                } else {
//                    throw new RuntimeException("@CSVColumnWrapper annotation is valid for List type");
//                }
//            }
//        }
//        throw new RuntimeException("No field annotated with @CSVColumnWrapper");
//    }
//
//    public static <T> void mapCSVColumnToObject(String[] row, Map<String, Integer> headerMap, T obj) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
//        Field[] fields = obj.getClass().getDeclaredFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            if (field.isAnnotationPresent(CSVColumn.class)) {
//                CSVColumn csvColumnAnnotation = field.getAnnotation(CSVColumn.class);
//                final String columnName = csvColumnAnnotation.columnName();
//                final String fieldName = field.getName();
//                final String value = row[headerMap.get(columnName)];
//                field.set(obj, value);
//                //PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), obj.getClass());
//                //Method setterMethod = propertyDescriptor.getWriteMethod();
//                //setterMethod.invoke(obj, value);
//            }
//        }
//    }
//
//    @Override
//    public String getFormat() {
//        return "csv";
//    }
//
//    @Override
//    public <T> ParserResult<T> parseFile(File path, Class<T> clazz) {
//        try (CSVReader reader = new CSVReader(new FileReader(path))) {
//            T csvWrapperType = getNewInstance(clazz);
//            log.info("Start CSV to Object conversion");
//            CsvToBean<?> csvToBean = new CsvToBeanBuilder<>(reader)
//                    .withType(getCSVWrapperFieldGenericType(clazz))
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .build();
//            List<?> data = csvToBean.parse();
//            log.info("End CSV to Object conversion");
//            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(getCSVWrapperFieldName(clazz), clazz);
//            Method setterMethod = propertyDescriptor.getWriteMethod();
//            setterMethod.invoke(csvWrapperType, data);
//            return new ParserResult<>("csv", csvWrapperType);
//        } catch (Exception e) {
//            log.error("Exception in CSV Parsing", e);
//            throw new RuntimeException("Exception in CSV Parsing");
//        }
//    }
//
//}
