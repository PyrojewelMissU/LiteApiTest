package com.sen.api.utils;

import com.jayway.jsonpath.JsonPath;
import com.sen.api.beans.ApiDataBean;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * API调用链执行器
 * 支持流程自动化：login → getUser → createOrder → pay → queryOrder
 *
 * @author sen
 */
public class ApiFlowExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ApiFlowExecutor.class);

    /**
     * 流程步骤
     */
    public static class FlowStep {
        private String name;
        private String description;
        private ApiDataBean apiData;
        private Map<String, String> extractRules = new HashMap<>();
        private Map<String, String> assertions = new HashMap<>();
        private Consumer<StepResult> onSuccess;
        private Consumer<StepResult> onFailure;
        private boolean stopOnFailure = true;
        private int retryCount = 0;
        private long retryDelayMs = 1000;
        private long delayBeforeMs = 0;
        private Map<String, String> paramOverrides = new HashMap<>();

        public FlowStep(String name) {
            this.name = name;
        }

        public FlowStep description(String description) {
            this.description = description;
            return this;
        }

        public FlowStep api(ApiDataBean apiData) {
            this.apiData = apiData;
            return this;
        }

        public FlowStep extract(String key, String jsonPath) {
            this.extractRules.put(key, jsonPath);
            return this;
        }

        public FlowStep assertion(String jsonPath, String expected) {
            this.assertions.put(jsonPath, expected);
            return this;
        }

        public FlowStep onSuccess(Consumer<StepResult> callback) {
            this.onSuccess = callback;
            return this;
        }

        public FlowStep onFailure(Consumer<StepResult> callback) {
            this.onFailure = callback;
            return this;
        }

        public FlowStep stopOnFailure(boolean stop) {
            this.stopOnFailure = stop;
            return this;
        }

        public FlowStep retry(int count, long delayMs) {
            this.retryCount = count;
            this.retryDelayMs = delayMs;
            return this;
        }

        public FlowStep delay(long beforeMs) {
            this.delayBeforeMs = beforeMs;
            return this;
        }

        public FlowStep param(String key, String value) {
            this.paramOverrides.put(key, value);
            return this;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public ApiDataBean getApiData() {
            return apiData;
        }

        public Map<String, String> getExtractRules() {
            return extractRules;
        }

        public Map<String, String> getAssertions() {
            return assertions;
        }

        public Consumer<StepResult> getOnSuccess() {
            return onSuccess;
        }

        public Consumer<StepResult> getOnFailure() {
            return onFailure;
        }

        public boolean isStopOnFailure() {
            return stopOnFailure;
        }

        public int getRetryCount() {
            return retryCount;
        }

        public long getRetryDelayMs() {
            return retryDelayMs;
        }

        public long getDelayBeforeMs() {
            return delayBeforeMs;
        }

        public Map<String, String> getParamOverrides() {
            return paramOverrides;
        }
    }

    /**
     * 步骤执行结果
     */
    public static class StepResult {
        private String stepName;
        private boolean success;
        private int statusCode;
        private String responseBody;
        private Map<String, String> extractedData = new HashMap<>();
        private String errorMessage;
        private long durationMs;
        private int retryAttempts;

        public StepResult(String stepName) {
            this.stepName = stepName;
        }

        // Getters and Setters
        public String getStepName() {
            return stepName;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public Map<String, String> getExtractedData() {
            return extractedData;
        }

        public void setExtractedData(Map<String, String> extractedData) {
            this.extractedData = extractedData;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(long durationMs) {
            this.durationMs = durationMs;
        }

        public int getRetryAttempts() {
            return retryAttempts;
        }

        public void setRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
        }

        public String getExtractedValue(String key) {
            return extractedData.get(key);
        }
    }

    /**
     * 流程执行结果
     */
    public static class FlowResult {
        private String flowName;
        private boolean success;
        private List<StepResult> stepResults = new ArrayList<>();
        private Map<String, String> sharedData = new HashMap<>();
        private long totalDurationMs;
        private int successCount;
        private int failureCount;

        public FlowResult(String flowName) {
            this.flowName = flowName;
        }

        public void addStepResult(StepResult result) {
            stepResults.add(result);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
            // 合并提取的数据到共享数据池
            sharedData.putAll(result.getExtractedData());
        }

        // Getters
        public String getFlowName() {
            return flowName;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public List<StepResult> getStepResults() {
            return stepResults;
        }

        public Map<String, String> getSharedData() {
            return sharedData;
        }

        public long getTotalDurationMs() {
            return totalDurationMs;
        }

        public void setTotalDurationMs(long totalDurationMs) {
            this.totalDurationMs = totalDurationMs;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailureCount() {
            return failureCount;
        }

        public StepResult getStepResult(String stepName) {
            return stepResults.stream()
                .filter(r -> r.getStepName().equals(stepName))
                .findFirst()
                .orElse(null);
        }

        public String getValue(String key) {
            return sharedData.get(key);
        }
    }

    // 流程名称
    private String flowName;

    // 步骤列表
    private final List<FlowStep> steps = new ArrayList<>();

    // 共享数据池（跨步骤共享）
    private final Map<String, String> sharedData = new ConcurrentHashMap<>();

    // Root URL
    private String rootUrl = "";

    // 请求头
    private final Map<String, String> headers = new HashMap<>();

    // REST Assured 工具
    private RestAssuredUtil restAssuredUtil;

    // 流程执行前后回调
    private Consumer<FlowResult> beforeFlow;
    private Consumer<FlowResult> afterFlow;

    public ApiFlowExecutor(String flowName) {
        this.flowName = flowName;
        logger.info("创建API调用链: {}", flowName);
    }

    /**
     * 创建新的流程执行器
     */
    public static ApiFlowExecutor create(String flowName) {
        return new ApiFlowExecutor(flowName);
    }

    /**
     * 设置Root URL
     */
    public ApiFlowExecutor rootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
        return this;
    }

    /**
     * 添加请求头
     */
    public ApiFlowExecutor header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    /**
     * 添加多个请求头
     */
    public ApiFlowExecutor headers(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * 预设共享数据
     */
    public ApiFlowExecutor withData(String key, String value) {
        this.sharedData.put(key, value);
        return this;
    }

    /**
     * 预设共享数据
     */
    public ApiFlowExecutor withData(Map<String, String> data) {
        this.sharedData.putAll(data);
        return this;
    }

    /**
     * 添加步骤
     */
    public ApiFlowExecutor step(FlowStep step) {
        this.steps.add(step);
        return this;
    }

    /**
     * 添加步骤（简化版）
     */
    public ApiFlowExecutor step(String name, String method, String url, String param) {
        ApiDataBean apiData = new ApiDataBean();
        apiData.setRun(true);
        apiData.setDesc(name);
        apiData.setMethod(method);
        apiData.setUrl(url);
        apiData.setParam(param);
        apiData.setStatus(200);

        FlowStep step = new FlowStep(name).api(apiData);
        this.steps.add(step);
        return this;
    }

    /**
     * 添加步骤（带提取规则）
     */
    public ApiFlowExecutor step(String name, String method, String url, String param,
                                 Map<String, String> extractRules) {
        ApiDataBean apiData = new ApiDataBean();
        apiData.setRun(true);
        apiData.setDesc(name);
        apiData.setMethod(method);
        apiData.setUrl(url);
        apiData.setParam(param);
        apiData.setStatus(200);

        FlowStep step = new FlowStep(name).api(apiData);
        if (extractRules != null) {
            extractRules.forEach(step::extract);
        }
        this.steps.add(step);
        return this;
    }

    /**
     * 设置流程执行前回调
     */
    public ApiFlowExecutor beforeFlow(Consumer<FlowResult> callback) {
        this.beforeFlow = callback;
        return this;
    }

    /**
     * 设置流程执行后回调
     */
    public ApiFlowExecutor afterFlow(Consumer<FlowResult> callback) {
        this.afterFlow = callback;
        return this;
    }

    /**
     * 执行流程
     */
    public FlowResult execute() {
        logger.info("========== 开始执行API调用链: {} ==========", flowName);
        long startTime = System.currentTimeMillis();

        FlowResult flowResult = new FlowResult(flowName);
        flowResult.getSharedData().putAll(sharedData);

        // 执行前回调
        if (beforeFlow != null) {
            beforeFlow.accept(flowResult);
        }

        boolean flowSuccess = true;

        for (int i = 0; i < steps.size(); i++) {
            FlowStep step = steps.get(i);
            logger.info("---------- 步骤 {}/{}: {} ----------", i + 1, steps.size(), step.getName());

            // 执行步骤前延迟
            if (step.getDelayBeforeMs() > 0) {
                try {
                    Thread.sleep(step.getDelayBeforeMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            StepResult stepResult = executeStep(step, flowResult.getSharedData());
            flowResult.addStepResult(stepResult);

            if (!stepResult.isSuccess()) {
                flowSuccess = false;
                logger.error("步骤执行失败: {} - {}", step.getName(), stepResult.getErrorMessage());

                // 执行失败回调
                if (step.getOnFailure() != null) {
                    step.getOnFailure().accept(stepResult);
                }

                // 是否终止流程
                if (step.isStopOnFailure()) {
                    logger.warn("流程终止于步骤: {}", step.getName());
                    break;
                }
            } else {
                logger.info("步骤执行成功: {}", step.getName());

                // 执行成功回调
                if (step.getOnSuccess() != null) {
                    step.getOnSuccess().accept(stepResult);
                }
            }
        }

        flowResult.setSuccess(flowSuccess);
        flowResult.setTotalDurationMs(System.currentTimeMillis() - startTime);

        // 执行后回调
        if (afterFlow != null) {
            afterFlow.accept(flowResult);
        }

        logger.info("========== API调用链执行完成: {} ==========", flowName);
        logger.info("结果: {}, 成功: {}, 失败: {}, 耗时: {}ms",
            flowSuccess ? "成功" : "失败",
            flowResult.getSuccessCount(),
            flowResult.getFailureCount(),
            flowResult.getTotalDurationMs());

        return flowResult;
    }

    /**
     * 执行单个步骤
     */
    private StepResult executeStep(FlowStep step, Map<String, String> sharedData) {
        StepResult result = new StepResult(step.getName());
        long startTime = System.currentTimeMillis();

        int attempts = 0;
        int maxAttempts = step.getRetryCount() + 1;

        while (attempts < maxAttempts) {
            attempts++;
            result.setRetryAttempts(attempts - 1);

            try {
                // 替换参数中的变量
                ApiDataBean apiData = step.getApiData();
                String processedUrl = replaceVariables(apiData.getUrl(), sharedData);
                String processedParam = replaceVariables(apiData.getParam(), sharedData);

                // 应用参数覆盖
                for (Map.Entry<String, String> entry : step.getParamOverrides().entrySet()) {
                    String value = replaceVariables(entry.getValue(), sharedData);
                    sharedData.put(entry.getKey(), value);
                }

                // 构建完整URL
                String fullUrl = rootUrl + processedUrl;

                // 发送请求
                Response response = sendRequest(apiData.getMethod(), fullUrl, processedParam);

                result.setStatusCode(response.getStatusCode());
                result.setResponseBody(response.getBody().asString());

                // 检查状态码
                if (apiData.getStatus() > 0 && response.getStatusCode() != apiData.getStatus()) {
                    throw new RuntimeException("状态码不匹配: 期望=" + apiData.getStatus() +
                        ", 实际=" + response.getStatusCode());
                }

                // 执行断言
                for (Map.Entry<String, String> assertion : step.getAssertions().entrySet()) {
                    String actualValue = JsonPath.read(result.getResponseBody(), assertion.getKey()).toString();
                    if (!actualValue.equals(assertion.getValue())) {
                        throw new RuntimeException("断言失败: " + assertion.getKey() +
                            " 期望=" + assertion.getValue() + ", 实际=" + actualValue);
                    }
                }

                // 提取数据
                for (Map.Entry<String, String> extractRule : step.getExtractRules().entrySet()) {
                    try {
                        Object value = JsonPath.read(result.getResponseBody(), extractRule.getValue());
                        String strValue = value != null ? value.toString() : "";
                        result.getExtractedData().put(extractRule.getKey(), strValue);
                        sharedData.put(extractRule.getKey(), strValue);
                        logger.debug("提取数据: {} = {}", extractRule.getKey(), strValue);
                    } catch (Exception e) {
                        logger.warn("提取数据失败: {} -> {}", extractRule.getKey(), extractRule.getValue());
                    }
                }

                result.setSuccess(true);
                break; // 成功，跳出重试循环

            } catch (Exception e) {
                result.setSuccess(false);
                result.setErrorMessage(e.getMessage());
                logger.warn("步骤执行失败 (尝试 {}/{}): {} - {}",
                    attempts, maxAttempts, step.getName(), e.getMessage());

                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(step.getRetryDelayMs());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        result.setDurationMs(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 发送HTTP请求
     */
    private Response sendRequest(String method, String url, String body) {
        logger.debug("发送请求: {} {}", method, url);
        logger.debug("请求体: {}", body);

        io.restassured.specification.RequestSpecification request = io.restassured.RestAssured.given();

        // 添加请求头
        for (Map.Entry<String, String> header : headers.entrySet()) {
            request.header(header.getKey(), header.getValue());
        }

        // 设置请求体
        if (body != null && !body.isEmpty() && !body.equals("{}")) {
            request.contentType(io.restassured.http.ContentType.JSON);
            request.body(body);
        }

        Response response;
        switch (method.toUpperCase()) {
            case "GET":
                response = request.get(url);
                break;
            case "POST":
                response = request.post(url);
                break;
            case "PUT":
                response = request.put(url);
                break;
            case "DELETE":
                response = request.delete(url);
                break;
            case "PATCH":
                response = request.patch(url);
                break;
            default:
                throw new RuntimeException("不支持的HTTP方法: " + method);
        }

        logger.debug("响应状态码: {}", response.getStatusCode());
        logger.debug("响应体: {}", response.getBody().asString());

        return response;
    }

    /**
     * 替换变量 ${key}
     */
    private String replaceVariables(String text, Map<String, String> data) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, entry.getValue());
            }
        }
        return result;
    }

    /**
     * 获取共享数据
     */
    public Map<String, String> getSharedData() {
        return new HashMap<>(sharedData);
    }

    /**
     * 获取共享数据值
     */
    public String getValue(String key) {
        return sharedData.get(key);
    }

    /**
     * 清除共享数据
     */
    public void clearSharedData() {
        sharedData.clear();
    }

    /**
     * 构建器模式：创建流程步骤
     */
    public static FlowStep newStep(String name) {
        return new FlowStep(name);
    }

    // ==================== 便捷方法 ====================

    /**
     * 登录步骤（便捷方法）
     */
    public ApiFlowExecutor login(String loginUrl, String username, String password,
                                  String tokenPath, String tokenKey) {
        String param = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        FlowStep step = new FlowStep("login")
            .description("用户登录")
            .api(createApiDataBean("POST", loginUrl, param, 200))
            .extract(tokenKey, tokenPath);

        return step(step);
    }

    /**
     * GET请求步骤（便捷方法）
     */
    public ApiFlowExecutor get(String name, String url) {
        return step(name, "GET", url, "{}");
    }

    /**
     * GET请求步骤（带提取）
     */
    public ApiFlowExecutor get(String name, String url, String extractKey, String extractPath) {
        FlowStep step = new FlowStep(name)
            .api(createApiDataBean("GET", url, "{}", 200))
            .extract(extractKey, extractPath);
        return step(step);
    }

    /**
     * POST请求步骤（便捷方法）
     */
    public ApiFlowExecutor post(String name, String url, String body) {
        return step(name, "POST", url, body);
    }

    /**
     * POST请求步骤（带提取）
     */
    public ApiFlowExecutor post(String name, String url, String body,
                                 String extractKey, String extractPath) {
        FlowStep step = new FlowStep(name)
            .api(createApiDataBean("POST", url, body, 200))
            .extract(extractKey, extractPath);
        return step(step);
    }

    /**
     * 创建ApiDataBean
     */
    private ApiDataBean createApiDataBean(String method, String url, String param, int status) {
        ApiDataBean apiData = new ApiDataBean();
        apiData.setRun(true);
        apiData.setDesc(method + " " + url);
        apiData.setMethod(method);
        apiData.setUrl(url);
        apiData.setParam(param);
        apiData.setStatus(status);
        return apiData;
    }

    /**
     * 打印流程结构
     */
    public void printFlowStructure() {
        logger.info("========== 流程结构: {} ==========", flowName);
        for (int i = 0; i < steps.size(); i++) {
            FlowStep step = steps.get(i);
            logger.info("{}. {} - {}", i + 1, step.getName(),
                step.getDescription() != null ? step.getDescription() : "");
            if (step.getApiData() != null) {
                logger.info("   {} {}", step.getApiData().getMethod(), step.getApiData().getUrl());
            }
            if (!step.getExtractRules().isEmpty()) {
                logger.info("   提取: {}", step.getExtractRules());
            }
        }
        logger.info("==========================================");
    }
}
