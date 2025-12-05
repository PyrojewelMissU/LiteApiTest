package com.sen.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON工具类
 * 使用Jackson和Jayway JsonPath替代FastJSON
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // JsonPath配置：总是返回列表，忽略路径缺失错误
    private static final Configuration conf = Configuration.builder()
        .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
        .build();

    /**
     * 从JSON字符串中使用JsonPath读取值
     * @param json JSON字符串
     * @param jsonPath JsonPath表达式，如：$.data.id
     * @return 读取到的值，转换为字符串
     */
    public static String read(String json, String jsonPath) {
        try {
            Object result = JsonPath.using(conf).parse(json).read(jsonPath);
            if (result == null) {
                logger.warn("JsonPath {} returned null for json: {}", jsonPath, json);
                return null;
            }
            return result.toString();
        } catch (Exception e) {
            logger.error("Failed to read JsonPath {} from json: {}", jsonPath, json, e);
            throw new RuntimeException("读取JsonPath失败: " + jsonPath, e);
        }
    }

    /**
     * 从JSON字符串中使用JsonPath读取值，返回指定类型
     */
    public static <T> T read(String json, String jsonPath, Class<T> clazz) {
        try {
            return JsonPath.using(conf).parse(json).read(jsonPath, clazz);
        } catch (Exception e) {
            logger.error("Failed to read JsonPath {} from json with class {}", jsonPath, clazz, e);
            throw new RuntimeException("读取JsonPath失败: " + jsonPath, e);
        }
    }

    /**
     * 解析JSON字符串为JsonNode
     */
    public static JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.error("Failed to parse json: {}", json, e);
            throw new RuntimeException("解析JSON失败", e);
        }
    }

    /**
     * 将对象转换为JSON字符串
     */
    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to convert object to json string", e);
            throw new RuntimeException("转换为JSON字符串失败", e);
        }
    }

    /**
     * 将对象转换为格式化的JSON字符串
     */
    public static String toPrettyJsonString(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Failed to convert object to pretty json string", e);
            throw new RuntimeException("转换为格式化JSON字符串失败", e);
        }
    }

    /**
     * 从JSON字符串解析为指定类型的对象
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Failed to parse json to object of class: {}", clazz, e);
            throw new RuntimeException("解析JSON为对象失败", e);
        }
    }

    /**
     * 验证字符串是否为有效的JSON
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
