package com.sen.api.utils;

import com.sen.api.beans.ApiDataBean;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Swagger/OpenAPI导入工具类
 * 从OpenAPI文档自动生成测试用例
 */
public class SwaggerImportUtil {

    private static final Logger logger = LoggerFactory.getLogger(SwaggerImportUtil.class);

    private SwaggerImportUtil() {
        // 工具类不允许实例化
    }

    /**
     * 从OpenAPI文档URL解析并生成测试用例
     *
     * @param swaggerUrl OpenAPI文档URL（如: http://localhost:8080/v3/api-docs）
     * @return 测试用例列表
     */
    public static List<ApiDataBean> importFromUrl(String swaggerUrl) {
        logger.info("Importing from Swagger URL: {}", swaggerUrl);

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(swaggerUrl, null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            logger.error("Failed to parse OpenAPI from URL: {}", swaggerUrl);
            if (result.getMessages() != null) {
                result.getMessages().forEach(msg -> logger.error("Parse error: {}", msg));
            }
            throw new RuntimeException("无法解析OpenAPI文档: " + swaggerUrl);
        }

        return parseOpenAPI(openAPI);
    }

    /**
     * 从OpenAPI文档文件解析并生成测试用例
     *
     * @param filePath OpenAPI文档文件路径（支持yaml/json）
     * @return 测试用例列表
     */
    public static List<ApiDataBean> importFromFile(String filePath) {
        logger.info("Importing from Swagger file: {}", filePath);

        SwaggerParseResult result = new OpenAPIV3Parser().readLocation(filePath, null, null);
        OpenAPI openAPI = result.getOpenAPI();

        if (openAPI == null) {
            logger.error("Failed to parse OpenAPI from file: {}", filePath);
            throw new RuntimeException("无法解析OpenAPI文档: " + filePath);
        }

        return parseOpenAPI(openAPI);
    }

    /**
     * 解析OpenAPI对象生成测试用例
     */
    private static List<ApiDataBean> parseOpenAPI(OpenAPI openAPI) {
        List<ApiDataBean> testCases = new ArrayList<>();

        if (openAPI.getPaths() == null) {
            logger.warn("No paths found in OpenAPI document");
            return testCases;
        }

        int caseIndex = 1;
        for (Map.Entry<String, PathItem> pathEntry : openAPI.getPaths().entrySet()) {
            String path = pathEntry.getKey();
            PathItem pathItem = pathEntry.getValue();

            // 处理各种HTTP方法
            if (pathItem.getGet() != null) {
                testCases.add(createTestCase(path, "GET", pathItem.getGet(), caseIndex++));
            }
            if (pathItem.getPost() != null) {
                testCases.add(createTestCase(path, "POST", pathItem.getPost(), caseIndex++));
            }
            if (pathItem.getPut() != null) {
                testCases.add(createTestCase(path, "PUT", pathItem.getPut(), caseIndex++));
            }
            if (pathItem.getDelete() != null) {
                testCases.add(createTestCase(path, "DELETE", pathItem.getDelete(), caseIndex++));
            }
            if (pathItem.getPatch() != null) {
                testCases.add(createTestCase(path, "PATCH", pathItem.getPatch(), caseIndex++));
            }
        }

        logger.info("Generated {} test cases from OpenAPI document", testCases.size());
        return testCases;
    }

    /**
     * 创建单个测试用例
     */
    private static ApiDataBean createTestCase(String path, String method, Operation operation, int index) {
        ApiDataBean testCase = new ApiDataBean();

        testCase.setRun(true);
        testCase.setCaseId("SWAGGER_" + String.format("%03d", index));
        testCase.setUrl(path);
        testCase.setMethod(method);

        // 设置描述
        String desc = operation.getSummary();
        if (StringUtil.isEmpty(desc)) {
            desc = operation.getOperationId();
        }
        if (StringUtil.isEmpty(desc)) {
            desc = method + " " + path;
        }
        testCase.setDesc(desc);

        // 处理参数
        String param = buildParameters(operation);
        testCase.setParam(param);

        // 设置默认状态码验证
        testCase.setStatus(200);

        // 设置默认验证
        testCase.setVerify("");

        logger.debug("Created test case: {} {} - {}", method, path, desc);
        return testCase;
    }

    /**
     * 构建请求参数
     */
    private static String buildParameters(Operation operation) {
        Map<String, Object> params = new LinkedHashMap<>();

        // 处理Query/Path/Header参数
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                String paramName = param.getName();
                Object defaultValue = getDefaultValue(param.getSchema());
                params.put(paramName, defaultValue);
            }
        }

        // 处理RequestBody
        RequestBody requestBody = operation.getRequestBody();
        if (requestBody != null && requestBody.getContent() != null) {
            Content content = requestBody.getContent();

            // 优先处理application/json
            MediaType jsonMedia = content.get("application/json");
            if (jsonMedia != null && jsonMedia.getSchema() != null) {
                Map<String, Object> bodyParams = buildSchemaParams(jsonMedia.getSchema());
                params.putAll(bodyParams);
            }

            // 处理form-data
            MediaType formMedia = content.get("application/x-www-form-urlencoded");
            if (formMedia != null && formMedia.getSchema() != null) {
                Map<String, Object> formParams = buildSchemaParams(formMedia.getSchema());
                params.putAll(formParams);
            }
        }

        return JsonUtil.toJsonString(params);
    }

    /**
     * 从Schema构建参数
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> buildSchemaParams(Schema<?> schema) {
        Map<String, Object> params = new LinkedHashMap<>();

        if (schema == null) {
            return params;
        }

        // 处理properties
        Map<String, Schema> properties = schema.getProperties();
        if (properties != null) {
            for (Map.Entry<String, Schema> entry : properties.entrySet()) {
                String propName = entry.getKey();
                Schema propSchema = entry.getValue();
                Object defaultValue = getDefaultValue(propSchema);
                params.put(propName, defaultValue);
            }
        }

        return params;
    }

    /**
     * 获取Schema的默认值
     */
    private static Object getDefaultValue(Schema<?> schema) {
        if (schema == null) {
            return "";
        }

        // 如果有example，使用example
        if (schema.getExample() != null) {
            return schema.getExample();
        }

        // 如果有default，使用default
        if (schema.getDefault() != null) {
            return schema.getDefault();
        }

        // 根据类型生成默认值
        String type = schema.getType();
        if (type == null) {
            return "";
        }

        switch (type) {
            case "string":
                String format = schema.getFormat();
                if ("date".equals(format)) {
                    return "${__date(yyyy-MM-dd)}";
                } else if ("date-time".equals(format)) {
                    return "${__date(yyyy-MM-dd HH:mm:ss)}";
                } else if ("email".equals(format)) {
                    return "test@example.com";
                } else if ("uuid".equals(format)) {
                    return "${__random(32,false)}";
                }
                return "test_string";
            case "integer":
            case "number":
                return 1;
            case "boolean":
                return true;
            case "array":
                return new ArrayList<>();
            case "object":
                return new LinkedHashMap<>();
            default:
                return "";
        }
    }

    /**
     * 将测试用例导出为JSON文件
     *
     * @param testCases  测试用例列表
     * @param outputPath 输出文件路径
     */
    public static void exportToJson(List<ApiDataBean> testCases, String outputPath) {
        try (FileWriter writer = new FileWriter(outputPath)) {
            String json = JsonUtil.toPrettyJsonString(testCases);
            writer.write(json);
            logger.info("Exported {} test cases to {}", testCases.size(), outputPath);
        } catch (IOException e) {
            logger.error("Failed to export test cases to {}", outputPath, e);
            throw new RuntimeException("导出测试用例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从Swagger URL导入并直接导出为JSON文件
     *
     * @param swaggerUrl Swagger文档URL
     * @param outputPath 输出文件路径
     */
    public static void importAndExport(String swaggerUrl, String outputPath) {
        List<ApiDataBean> testCases = importFromUrl(swaggerUrl);
        exportToJson(testCases, outputPath);
    }
}
