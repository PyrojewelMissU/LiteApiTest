package com.sen.api.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 职责：JSON解析、序列化、JsonPath查询
 * 使用Jackson和Jayway JsonPath
 */
public class JsonUtil {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final ObjectMapper objectMapper;

    // JsonPath配置：总是返回列表，忽略路径缺失错误
    private static final Configuration jsonPathConf = Configuration.builder()
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
            .build();

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    private JsonUtil() {
        // 工具类不允许实例化
    }

    /**
     * 获取ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    // ==================== JsonPath 操作 ====================

    /**
     * 从JSON字符串中使用JsonPath读取值
     *
     * @param json     JSON字符串
     * @param jsonPath JsonPath表达式，如：$.data.id
     * @return 读取到的值，转换为字符串
     */
    public static String read(String json, String jsonPath) {
        try {
            Object result = JsonPath.using(jsonPathConf).parse(json).read(jsonPath);
            if (result == null) {
                logger.debug("JsonPath {} returned null for json", jsonPath);
                return null;
            }
            return result.toString();
        } catch (PathNotFoundException e) {
            logger.warn("JsonPath not found: {} in json", jsonPath);
            return null;
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
            return JsonPath.using(jsonPathConf).parse(json).read(jsonPath, clazz);
        } catch (PathNotFoundException e) {
            logger.warn("JsonPath not found: {} in json", jsonPath);
            return null;
        } catch (Exception e) {
            logger.error("Failed to read JsonPath {} from json with class {}", jsonPath, clazz, e);
            throw new RuntimeException("读取JsonPath失败: " + jsonPath, e);
        }
    }

    /**
     * 从JSON字符串中使用JsonPath读取列表
     */
    public static <T> List<T> readList(String json, String jsonPath, Class<T> clazz) {
        try {
            return JsonPath.using(jsonPathConf).parse(json).read(jsonPath, new com.jayway.jsonpath.TypeRef<List<T>>() {});
        } catch (Exception e) {
            logger.error("Failed to read JsonPath list {} from json", jsonPath, e);
            throw new RuntimeException("读取JsonPath列表失败: " + jsonPath, e);
        }
    }

    /**
     * 安全读取JsonPath，不抛出异常
     */
    public static String readSafe(String json, String jsonPath, String defaultValue) {
        try {
            String result = read(json, jsonPath);
            return result != null ? result : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // ==================== JSON 解析 ====================

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
     * 安全解析JSON，失败返回null
     */
    public static JsonNode parseJsonSafe(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            logger.warn("Failed to parse json safely: {}", e.getMessage());
            return null;
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
     * 从JSON字符串解析为指定类型的对象（泛型支持）
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            logger.error("Failed to parse json to typed object", e);
            throw new RuntimeException("解析JSON为泛型对象失败", e);
        }
    }

    /**
     * 从JSON字符串解析为List
     */
    public static <T> List<T> parseList(String json, Class<T> elementClass) {
        try {
            return objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (Exception e) {
            logger.error("Failed to parse json to list of class: {}", elementClass, e);
            throw new RuntimeException("解析JSON为List失败", e);
        }
    }

    /**
     * 从JSON字符串解析为Map
     */
    public static Map<String, Object> parseMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            logger.error("Failed to parse json to map", e);
            throw new RuntimeException("解析JSON为Map失败", e);
        }
    }

    // ==================== JSON 序列化 ====================

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
     * 将对象转换为字节数组
     */
    public static byte[] toJsonBytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to json bytes", e);
            throw new RuntimeException("转换为JSON字节数组失败", e);
        }
    }

    // ==================== JSON 验证 ====================

    /**
     * 验证字符串是否为有效的JSON
     */
    public static boolean isValidJson(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为JSON对象
     */
    public static boolean isJsonObject(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.isObject();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证字符串是否为JSON数组
     */
    public static boolean isJsonArray(String json) {
        if (StringUtil.isEmpty(json)) {
            return false;
        }
        try {
            JsonNode node = objectMapper.readTree(json);
            return node.isArray();
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== JSON 节点操作 ====================

    /**
     * 创建空的ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    /**
     * 创建空的ArrayNode
     */
    public static ArrayNode createArrayNode() {
        return objectMapper.createArrayNode();
    }

    /**
     * 从JsonNode获取字符串值
     */
    public static String getString(JsonNode node, String fieldName) {
        return getString(node, fieldName, null);
    }

    /**
     * 从JsonNode获取字符串值，带默认值
     */
    public static String getString(JsonNode node, String fieldName, String defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asText();
    }

    /**
     * 从JsonNode获取整数值
     */
    public static int getInt(JsonNode node, String fieldName, int defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asInt();
    }

    /**
     * 从JsonNode获取长整数值
     */
    public static long getLong(JsonNode node, String fieldName, long defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asLong();
    }

    /**
     * 从JsonNode获取布尔值
     */
    public static boolean getBoolean(JsonNode node, String fieldName, boolean defaultValue) {
        if (node == null || !node.has(fieldName)) {
            return defaultValue;
        }
        JsonNode fieldNode = node.get(fieldName);
        return fieldNode.isNull() ? defaultValue : fieldNode.asBoolean();
    }

    /**
     * 对象转换为另一个类型
     */
    public static <T> T convertValue(Object fromValue, Class<T> toValueType) {
        return objectMapper.convertValue(fromValue, toValueType);
    }

    /**
     * 深度合并两个JSON对象
     */
    public static ObjectNode merge(ObjectNode mainNode, ObjectNode updateNode) {
        updateNode.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode updateValue = entry.getValue();
            JsonNode mainValue = mainNode.get(fieldName);

            if (mainValue != null && mainValue.isObject() && updateValue.isObject()) {
                merge((ObjectNode) mainValue, (ObjectNode) updateValue);
            } else {
                mainNode.set(fieldName, updateValue);
            }
        });
        return mainNode;
    }
}
