package com.sen.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * YAML数据读取工具类
 * 用于读取YAML格式的测试数据
 */
public class YamlDataReader {

    private static final Logger logger = LoggerFactory.getLogger(YamlDataReader.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * 从文件路径读取YAML数据并转换为指定类型的对象列表
     */
    public static <T> List<T> readYaml(Class<T> clz, String path) {
        try {
            logger.info("Reading YAML file from path: {}", path);
            File file = new File(path);
            return yamlMapper.readValue(file,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, clz));
        } catch (IOException e) {
            logger.error("Failed to read YAML file: {}", path, e);
            throw new RuntimeException("转换YAML文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 从InputStream读取YAML数据
     */
    public static <T> List<T> readYaml(Class<T> clz, InputStream inputStream) {
        try {
            logger.info("Reading YAML from input stream");
            return yamlMapper.readValue(inputStream,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, clz));
        } catch (IOException e) {
            logger.error("Failed to read YAML from input stream", e);
            throw new RuntimeException("转换YAML数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取YAML文件为Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> readYamlAsMap(String path) {
        try {
            logger.info("Reading YAML file as Map from path: {}", path);
            File file = new File(path);
            return yamlMapper.readValue(file, Map.class);
        } catch (IOException e) {
            logger.error("Failed to read YAML file as Map: {}", path, e);
            throw new RuntimeException("转换YAML文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 读取YAML文件为通用列表
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readYamlAsList(String path) {
        try {
            logger.info("Reading YAML file as List from path: {}", path);
            File file = new File(path);
            return yamlMapper.readValue(file, List.class);
        } catch (IOException e) {
            logger.error("Failed to read YAML file as List: {}", path, e);
            throw new RuntimeException("转换YAML文件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 将对象写入YAML文件
     */
    public static void writeYaml(Object data, String path) {
        try {
            logger.info("Writing YAML to file: {}", path);
            File file = new File(path);
            yamlMapper.writeValue(file, data);
        } catch (IOException e) {
            logger.error("Failed to write YAML file: {}", path, e);
            throw new RuntimeException("写入YAML文件失败：" + e.getMessage(), e);
        }
    }
}
