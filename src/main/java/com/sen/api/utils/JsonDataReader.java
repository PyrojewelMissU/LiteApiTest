package com.sen.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * JSON数据读取工具类
 * 用于读取JSON格式的测试数据
 */
public class JsonDataReader {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataReader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * 从文件路径读取JSON数据并转换为指定类型的对象列表
     */
    public static <T> List<T> readJson(Class<T> clz, String path) {
        try {
            logger.info("Reading JSON file from path: {}", path);
            File file = new File(path);
            return objectMapper.readValue(file,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clz));
        } catch (IOException e) {
            logger.error("Failed to read JSON file: {}", path, e);
            throw new RuntimeException("转换JSON文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从InputStream读取JSON数据
     */
    public static <T> List<T> readJson(Class<T> clz, InputStream inputStream) {
        try {
            logger.info("Reading JSON from input stream");
            return objectMapper.readValue(inputStream,
                objectMapper.getTypeFactory().constructCollectionType(List.class, clz));
        } catch (IOException e) {
            logger.error("Failed to read JSON from input stream", e);
            throw new RuntimeException("转换JSON数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取JSON文件为Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> readJsonAsMap(String path) {
        try {
            logger.info("Reading JSON file as Map from path: {}", path);
            File file = new File(path);
            return objectMapper.readValue(file, Map.class);
        } catch (IOException e) {
            logger.error("Failed to read JSON file as Map: {}", path, e);
            throw new RuntimeException("转换JSON文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取JSON文件为通用列表
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readJsonAsList(String path) {
        try {
            logger.info("Reading JSON file as List from path: {}", path);
            File file = new File(path);
            return objectMapper.readValue(file, List.class);
        } catch (IOException e) {
            logger.error("Failed to read JSON file as List: {}", path, e);
            throw new RuntimeException("转换JSON文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJsonString(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (IOException e) {
            logger.error("Failed to convert object to JSON string", e);
            throw new RuntimeException("转换为JSON字符串失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将对象写入JSON文件
     */
    public static void writeJson(Object data, String path) {
        try {
            logger.info("Writing JSON to file: {}", path);
            File file = new File(path);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            logger.error("Failed to write JSON file: {}", path, e);
            throw new RuntimeException("写入JSON文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从JSON字符串解析为对象
     */
    public static <T> T parseJson(String jsonString, Class<T> clz) {
        try {
            return objectMapper.readValue(jsonString, clz);
        } catch (IOException e) {
            logger.error("Failed to parse JSON string", e);
            throw new RuntimeException("解析JSON字符串失败：" + e.getMessage(), e);
        }
    }
}
