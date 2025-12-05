package com.sen.api.utils;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * CSV数据读取工具类
 * 用于读取CSV格式的测试数据
 */
public class CsvDataReader {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataReader.class);
    private static final CsvMapper csvMapper = new CsvMapper();

    /**
     * 从文件路径读取CSV数据并转换为指定类型的对象列表
     * 默认使用第一行作为列头
     */
    public static <T> List<T> readCsv(Class<T> clz, String path) {
        try {
            logger.info("Reading CSV file from path: {}", path);
            File file = new File(path);
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<T> iterator = csvMapper.readerFor(clz)
                .with(schema)
                .readValues(file);
            return iterator.readAll();
        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", path, e);
            throw new RuntimeException("转换CSV文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从文件路径读取CSV数据并转换为Map列表
     */
    public static List<Map<String, String>> readCsvAsMap(String path) {
        try {
            logger.info("Reading CSV file as Map from path: {}", path);
            File file = new File(path);
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            MappingIterator<Map<String, String>> iterator = csvMapper.readerFor(Map.class)
                .with(schema)
                .readValues(file);
            return iterator.readAll();
        } catch (IOException e) {
            logger.error("Failed to read CSV file as Map: {}", path, e);
            throw new RuntimeException("转换CSV文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取CSV文件，指定分隔符
     */
    public static <T> List<T> readCsvWithSeparator(Class<T> clz, String path, char separator) {
        try {
            logger.info("Reading CSV file from path: {} with separator: {}", path, separator);
            File file = new File(path);
            CsvSchema schema = CsvSchema.emptySchema()
                .withHeader()
                .withColumnSeparator(separator);
            MappingIterator<T> iterator = csvMapper.readerFor(clz)
                .with(schema)
                .readValues(file);
            return iterator.readAll();
        } catch (IOException e) {
            logger.error("Failed to read CSV file: {}", path, e);
            throw new RuntimeException("转换CSV文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将对象列表写入CSV文件
     */
    public static <T> void writeCsv(List<T> data, Class<T> clz, String path) {
        try {
            logger.info("Writing CSV to file: {}", path);
            File file = new File(path);
            CsvSchema schema = csvMapper.schemaFor(clz).withHeader();
            csvMapper.writer(schema).writeValue(file, data);
        } catch (IOException e) {
            logger.error("Failed to write CSV file: {}", path, e);
            throw new RuntimeException("写入CSV文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将Map列表写入CSV文件
     */
    public static void writeCsvFromMap(List<Map<String, Object>> data, String path) {
        try {
            logger.info("Writing CSV from Map to file: {}", path);
            File file = new File(path);
            if (data.isEmpty()) {
                logger.warn("Data list is empty, nothing to write");
                return;
            }
            // 使用第一个Map的键作为列头
            CsvSchema.Builder builder = CsvSchema.builder();
            data.get(0).keySet().forEach(builder::addColumn);
            CsvSchema schema = builder.build().withHeader();

            csvMapper.writer(schema).writeValue(file, data);
        } catch (IOException e) {
            logger.error("Failed to write CSV file from Map: {}", path, e);
            throw new RuntimeException("写入CSV文件失败：" + e.getMessage(), e);
        }
    }
}
