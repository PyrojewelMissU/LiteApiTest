package com.sen.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 数据读取器工厂
 * 统一的数据读取入口，自动识别文件格式并使用相应的读取器
 */
public class DataReaderFactory {

    private static final Logger logger = LoggerFactory.getLogger(DataReaderFactory.class);

    /**
     * 根据文件扩展名自动选择合适的读取器读取数据
     */
    public static <T> List<T> readData(Class<T> clz, String path) {
        logger.info("Auto-detecting file format for: {}", path);

        String lowerPath = path.toLowerCase();

        if (lowerPath.endsWith(".json")) {
            logger.info("Detected JSON format");
            return JsonDataReader.readJson(clz, path);
        } else if (lowerPath.endsWith(".yaml") || lowerPath.endsWith(".yml")) {
            logger.info("Detected YAML format");
            return YamlDataReader.readYaml(clz, path);
        } else if (lowerPath.endsWith(".csv")) {
            logger.info("Detected CSV format");
            return CsvDataReader.readCsv(clz, path);
        } else if (lowerPath.endsWith(".xls") || lowerPath.endsWith(".xlsx")) {
            logger.info("Detected Excel format");
            return ExcelUtil.readExcel(clz, path);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + path +
                ". 支持的格式: .json, .yaml, .yml, .csv, .xls, .xlsx");
        }
    }

    /**
     * 读取数据为Map列表（不需要指定类型）
     */
    public static List<Map<String, Object>> readDataAsMap(String path) {
        logger.info("Reading data as Map from: {}", path);

        String lowerPath = path.toLowerCase();

        if (lowerPath.endsWith(".json")) {
            return JsonDataReader.readJsonAsList(path);
        } else if (lowerPath.endsWith(".yaml") || lowerPath.endsWith(".yml")) {
            return YamlDataReader.readYamlAsList(path);
        } else if (lowerPath.endsWith(".csv")) {
            return convertToObjectMap(CsvDataReader.readCsvAsMap(path));
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + path +
                ". 支持的格式: .json, .yaml, .yml, .csv");
        }
    }

    /**
     * 读取Excel指定Sheet的数据
     */
    public static <T> List<T> readExcelSheet(Class<T> clz, String path, String sheetName) {
        logger.info("Reading Excel sheet '{}' from: {}", sheetName, path);
        return ExcelUtil.readExcel(clz, path, sheetName);
    }

    /**
     * 辅助方法：将String Map转换为Object Map
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> convertToObjectMap(List<Map<String, String>> stringMapList) {
        return stringMapList.stream()
            .collect(java.util.stream.Collectors.toList())
            .stream()
            .map(map -> (Map<String, Object>) (Map<?, ?>) map)
            .collect(java.util.stream.Collectors.toList());
    }
}
