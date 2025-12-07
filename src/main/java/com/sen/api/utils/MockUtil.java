package com.sen.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * WireMock工具类（增强版）
 * 用于API Mock测试
 *
 * 支持功能：
 * - mock 接口未开发
 * - mock 异常场景（500/Timeout）
 * - mock AB 实验
 * - 动态Mock配置
 * - 场景录制回放
 *
 * @author sen
 */
public class MockUtil {

    private static final Logger logger = LoggerFactory.getLogger(MockUtil.class);

    private static WireMockServer wireMockServer;
    private static boolean isRunning = false;

    // Mock场景存储
    private static final Map<String, List<StubMapping>> scenarioMappings = new ConcurrentHashMap<>();

    // AB实验配置
    private static final Map<String, ABExperiment> abExperiments = new ConcurrentHashMap<>();

    // 请求计数器（用于AB实验轮询）
    private static final Map<String, Integer> requestCounters = new ConcurrentHashMap<>();

    private MockUtil() {
        // 工具类不允许实例化
    }

    /**
     * AB实验配置
     */
    public static class ABExperiment {
        private String name;
        private String url;
        private List<ABVariant> variants = new ArrayList<>();
        private String strategy = "random"; // random, round_robin, weighted

        public ABExperiment(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public ABExperiment addVariant(String name, String response, int statusCode, int weight) {
            variants.add(new ABVariant(name, response, statusCode, weight));
            return this;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public List<ABVariant> getVariants() {
            return variants;
        }

        public String getStrategy() {
            return strategy;
        }

        public void setStrategy(String strategy) {
            this.strategy = strategy;
        }
    }

    /**
     * AB实验变体
     */
    public static class ABVariant {
        private String name;
        private String response;
        private int statusCode;
        private int weight;

        public ABVariant(String name, String response, int statusCode, int weight) {
            this.name = name;
            this.response = response;
            this.statusCode = statusCode;
            this.weight = weight;
        }

        // Getters
        public String getName() {
            return name;
        }

        public String getResponse() {
            return response;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public int getWeight() {
            return weight;
        }
    }

    /**
     * 启动Mock服务器
     *
     * @param port 端口号
     */
    public static void startMockServer(int port) {
        if (isRunning) {
            logger.warn("Mock server is already running");
            return;
        }

        try {
            wireMockServer = new WireMockServer(WireMockConfiguration.options()
                    .port(port)
                    .usingFilesUnderDirectory("src/test/resources/wiremock"));

            wireMockServer.start();
            WireMock.configureFor("localhost", port);
            isRunning = true;

            logger.info("Mock server started on port {}", port);
        } catch (Exception e) {
            logger.error("Failed to start mock server", e);
            throw new RuntimeException("启动Mock服务器失败: " + e.getMessage(), e);
        }
    }

    /**
     * 停止Mock服务器
     */
    public static void stopMockServer() {
        if (wireMockServer != null && isRunning) {
            wireMockServer.stop();
            isRunning = false;
            logger.info("Mock server stopped");
        }
    }

    /**
     * 重置所有Mock配置
     */
    public static void reset() {
        if (wireMockServer != null && isRunning) {
            wireMockServer.resetAll();
            logger.debug("Mock server reset");
        }
    }

    /**
     * 检查Mock服务器是否运行中
     */
    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * 添加GET请求Mock
     *
     * @param url          请求路径
     * @param responseBody 响应体
     * @param statusCode   状态码
     */
    public static void stubGet(String url, String responseBody, int statusCode) {
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        logger.debug("Stubbed GET {} -> {} ({})", url, statusCode, responseBody);
    }

    /**
     * 添加GET请求Mock（带路径参数匹配）
     */
    public static void stubGetMatching(String urlPattern, String responseBody, int statusCode) {
        stubFor(get(urlMatching(urlPattern))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        logger.debug("Stubbed GET pattern {} -> {}", urlPattern, statusCode);
    }

    /**
     * 添加POST请求Mock
     *
     * @param url          请求路径
     * @param responseBody 响应体
     * @param statusCode   状态码
     */
    public static void stubPost(String url, String responseBody, int statusCode) {
        stubFor(post(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        logger.debug("Stubbed POST {} -> {} ({})", url, statusCode, responseBody);
    }

    /**
     * 添加POST请求Mock（带请求体匹配）
     */
    public static void stubPostWithBody(String url, String requestBodyPattern, String responseBody, int statusCode) {
        stubFor(post(urlEqualTo(url))
                .withRequestBody(matching(requestBodyPattern))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        logger.debug("Stubbed POST {} with body pattern -> {}", url, statusCode);
    }

    /**
     * 添加PUT请求Mock
     */
    public static void stubPut(String url, String responseBody, int statusCode) {
        stubFor(put(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));
        logger.debug("Stubbed PUT {} -> {}", url, statusCode);
    }

    /**
     * 添加DELETE请求Mock
     */
    public static void stubDelete(String url, int statusCode) {
        stubFor(delete(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(statusCode)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"success\": true}")));
        logger.debug("Stubbed DELETE {} -> {}", url, statusCode);
    }

    /**
     * 添加延迟响应的Mock
     */
    public static void stubWithDelay(String method, String url, String responseBody, int statusCode, int delayMs) {
        switch (method.toUpperCase()) {
            case "GET":
                stubFor(get(urlEqualTo(url))
                        .willReturn(aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)
                                .withFixedDelay(delayMs)));
                break;
            case "POST":
                stubFor(post(urlEqualTo(url))
                        .willReturn(aResponse()
                                .withStatus(statusCode)
                                .withHeader("Content-Type", "application/json")
                                .withBody(responseBody)
                                .withFixedDelay(delayMs)));
                break;
            default:
                logger.warn("Unsupported method for delay stub: {}", method);
        }
        logger.debug("Stubbed {} {} with {}ms delay -> {}", method, url, delayMs, statusCode);
    }

    /**
     * 验证请求是否被调用
     */
    public static void verifyGetCalled(String url, int times) {
        verify(times, getRequestedFor(urlEqualTo(url)));
        logger.debug("Verified GET {} called {} times", url, times);
    }

    /**
     * 验证POST请求是否被调用
     */
    public static void verifyPostCalled(String url, int times) {
        verify(times, postRequestedFor(urlEqualTo(url)));
        logger.debug("Verified POST {} called {} times", url, times);
    }

    /**
     * 获取Mock服务器URL
     */
    public static String getMockServerUrl() {
        if (wireMockServer != null && isRunning) {
            return "http://localhost:" + wireMockServer.port();
        }
        return null;
    }

    /**
     * 获取Mock服务器端口
     */
    public static int getMockServerPort() {
        if (wireMockServer != null && isRunning) {
            return wireMockServer.port();
        }
        return -1;
    }

    // ==================== 增强功能：异常场景Mock ====================

    /**
     * Mock 500 内部服务器错误
     */
    public static void stubInternalServerError(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"code\":500,\"message\":\"Internal Server Error\"}")));
        logger.debug("Stubbed 500 error for {}", url);
    }

    /**
     * Mock 502 网关错误
     */
    public static void stubBadGateway(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(502)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"code\":502,\"message\":\"Bad Gateway\"}")));
        logger.debug("Stubbed 502 error for {}", url);
    }

    /**
     * Mock 503 服务不可用
     */
    public static void stubServiceUnavailable(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(503)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"code\":503,\"message\":\"Service Unavailable\"}")));
        logger.debug("Stubbed 503 error for {}", url);
    }

    /**
     * Mock 504 网关超时
     */
    public static void stubGatewayTimeout(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(504)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"code\":504,\"message\":\"Gateway Timeout\"}")));
        logger.debug("Stubbed 504 error for {}", url);
    }

    /**
     * Mock 连接重置
     */
    public static void stubConnectionReset(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        logger.debug("Stubbed connection reset for {}", url);
    }

    /**
     * Mock 空响应
     */
    public static void stubEmptyResponse(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        logger.debug("Stubbed empty response for {}", url);
    }

    /**
     * Mock 随机数据损坏
     */
    public static void stubRandomDataCorruption(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        logger.debug("Stubbed random data corruption for {}", url);
    }

    /**
     * Mock 超时场景
     */
    public static void stubTimeout(String url, int timeoutMs) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(200)
                .withFixedDelay(timeoutMs)));
        logger.debug("Stubbed timeout {}ms for {}", timeoutMs, url);
    }

    /**
     * Mock 慢响应（随机延迟）
     */
    public static void stubSlowResponse(String url, String responseBody, int statusCode, int minDelayMs, int maxDelayMs) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)
                .withUniformRandomDelay(minDelayMs, maxDelayMs)));
        logger.debug("Stubbed slow response {}~{}ms for {}", minDelayMs, maxDelayMs, url);
    }

    // ==================== 增强功能：接口未开发Mock ====================

    /**
     * Mock 未开发的接口（返回开发中状态）
     */
    public static void stubNotImplemented(String url) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(501)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"code\":501,\"message\":\"Not Implemented\",\"dev_status\":\"in_progress\"}")));
        logger.debug("Stubbed not implemented for {}", url);
    }

    /**
     * Mock 未开发的接口（返回模拟数据）
     */
    public static void stubMockData(String url, String mockData, int statusCode) {
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withHeader("X-Mock-Data", "true")
                .withBody(mockData)));
        logger.debug("Stubbed mock data for {}", url);
    }

    /**
     * Mock 接口占位符（带文档说明）
     */
    public static void stubPlaceholder(String url, String apiDoc) {
        String body = String.format("{\"code\":200,\"message\":\"API Placeholder\",\"doc\":\"%s\",\"data\":{}}",
            apiDoc.replace("\"", "\\\""));
        stubFor(any(urlEqualTo(url))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("X-API-Status", "placeholder")
                .withBody(body)));
        logger.debug("Stubbed placeholder for {}: {}", url, apiDoc);
    }

    // ==================== 增强功能：AB实验Mock ====================

    /**
     * 创建AB实验
     */
    public static ABExperiment createABExperiment(String name, String url) {
        ABExperiment experiment = new ABExperiment(name, url);
        abExperiments.put(name, experiment);
        return experiment;
    }

    /**
     * 应用AB实验
     */
    public static void applyABExperiment(String experimentName) {
        ABExperiment experiment = abExperiments.get(experimentName);
        if (experiment == null) {
            logger.warn("AB experiment not found: {}", experimentName);
            return;
        }

        // 根据策略选择变体
        ABVariant selectedVariant = selectVariant(experiment);
        if (selectedVariant != null) {
            stubFor(any(urlEqualTo(experiment.getUrl()))
                .willReturn(aResponse()
                    .withStatus(selectedVariant.getStatusCode())
                    .withHeader("Content-Type", "application/json")
                    .withHeader("X-AB-Experiment", experiment.getName())
                    .withHeader("X-AB-Variant", selectedVariant.getName())
                    .withBody(selectedVariant.getResponse())));
            logger.info("Applied AB experiment: {} -> variant: {}", experimentName, selectedVariant.getName());
        }
    }

    /**
     * 根据策略选择变体
     */
    private static ABVariant selectVariant(ABExperiment experiment) {
        List<ABVariant> variants = experiment.getVariants();
        if (variants.isEmpty()) {
            return null;
        }

        switch (experiment.getStrategy()) {
            case "round_robin":
                int counter = requestCounters.getOrDefault(experiment.getName(), 0);
                ABVariant variant = variants.get(counter % variants.size());
                requestCounters.put(experiment.getName(), counter + 1);
                return variant;

            case "weighted":
                int totalWeight = variants.stream().mapToInt(ABVariant::getWeight).sum();
                int random = new Random().nextInt(totalWeight);
                int cumulative = 0;
                for (ABVariant v : variants) {
                    cumulative += v.getWeight();
                    if (random < cumulative) {
                        return v;
                    }
                }
                return variants.get(0);

            case "random":
            default:
                return variants.get(new Random().nextInt(variants.size()));
        }
    }

    /**
     * 固定AB实验到特定变体
     */
    public static void fixABVariant(String experimentName, String variantName) {
        ABExperiment experiment = abExperiments.get(experimentName);
        if (experiment == null) {
            logger.warn("AB experiment not found: {}", experimentName);
            return;
        }

        for (ABVariant variant : experiment.getVariants()) {
            if (variant.getName().equals(variantName)) {
                stubFor(any(urlEqualTo(experiment.getUrl()))
                    .willReturn(aResponse()
                        .withStatus(variant.getStatusCode())
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-AB-Experiment", experiment.getName())
                        .withHeader("X-AB-Variant", variant.getName())
                        .withBody(variant.getResponse())));
                logger.info("Fixed AB experiment {} to variant: {}", experimentName, variantName);
                return;
            }
        }
        logger.warn("Variant not found: {} in experiment: {}", variantName, experimentName);
    }

    // ==================== 增强功能：场景管理 ====================

    /**
     * 保存当前Mock配置到场景
     */
    public static void saveScenario(String scenarioName) {
        if (wireMockServer != null && isRunning) {
            List<StubMapping> mappings = wireMockServer.getStubMappings();
            scenarioMappings.put(scenarioName, new ArrayList<>(mappings));
            logger.info("Saved scenario: {} ({} mappings)", scenarioName, mappings.size());
        }
    }

    /**
     * 加载场景
     */
    public static void loadScenario(String scenarioName) {
        List<StubMapping> mappings = scenarioMappings.get(scenarioName);
        if (mappings != null && wireMockServer != null && isRunning) {
            wireMockServer.resetAll();
            for (StubMapping mapping : mappings) {
                wireMockServer.addStubMapping(mapping);
            }
            logger.info("Loaded scenario: {} ({} mappings)", scenarioName, mappings.size());
        } else {
            logger.warn("Scenario not found or server not running: {}", scenarioName);
        }
    }

    /**
     * 从YAML文件加载Mock配置
     */
    @SuppressWarnings("unchecked")
    public static void loadMockFromYaml(String yamlPath) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

            InputStream is = MockUtil.class.getClassLoader().getResourceAsStream(yamlPath);
            Map<String, Object> config;
            if (is != null) {
                config = mapper.readValue(is, Map.class);
                is.close();
            } else {
                File file = new File(yamlPath);
                if (!file.exists()) {
                    logger.warn("Mock config file not found: {}", yamlPath);
                    return;
                }
                config = mapper.readValue(file, Map.class);
            }

            // 解析mocks配置
            List<Map<String, Object>> mocks = (List<Map<String, Object>>) config.get("mocks");
            if (mocks != null) {
                for (Map<String, Object> mock : mocks) {
                    String url = (String) mock.get("url");
                    String method = (String) mock.getOrDefault("method", "GET");
                    String response = (String) mock.get("response");
                    int status = (int) mock.getOrDefault("status", 200);
                    int delay = (int) mock.getOrDefault("delay", 0);

                    if (delay > 0) {
                        stubWithDelay(method, url, response, status, delay);
                    } else {
                        switch (method.toUpperCase()) {
                            case "GET":
                                stubGet(url, response, status);
                                break;
                            case "POST":
                                stubPost(url, response, status);
                                break;
                            case "PUT":
                                stubPut(url, response, status);
                                break;
                            case "DELETE":
                                stubDelete(url, status);
                                break;
                        }
                    }
                }
            }

            logger.info("Loaded {} mocks from {}", mocks != null ? mocks.size() : 0, yamlPath);

        } catch (Exception e) {
            logger.error("Failed to load mock config: {}", e.getMessage());
        }
    }

    // ==================== 增强功能：验证增强 ====================

    /**
     * 验证请求Header
     */
    public static void verifyRequestHeader(String url, String headerName, String headerValue) {
        verify(getRequestedFor(urlEqualTo(url))
            .withHeader(headerName, equalTo(headerValue)));
        logger.debug("Verified request header: {}={} for {}", headerName, headerValue, url);
    }

    /**
     * 验证请求体包含
     */
    public static void verifyRequestBodyContains(String url, String bodyContent) {
        verify(postRequestedFor(urlEqualTo(url))
            .withRequestBody(containing(bodyContent)));
        logger.debug("Verified request body contains: {} for {}", bodyContent, url);
    }

    /**
     * 验证请求体JSON路径
     */
    public static void verifyRequestBodyJsonPath(String url, String jsonPath, String expectedValue) {
        verify(postRequestedFor(urlEqualTo(url))
            .withRequestBody(matchingJsonPath(jsonPath, equalTo(expectedValue))));
        logger.debug("Verified JSON path {}={} for {}", jsonPath, expectedValue, url);
    }

    /**
     * 获取请求日志
     */
    public static List<String> getRequestLog() {
        List<String> logs = new ArrayList<>();
        if (wireMockServer != null && isRunning) {
            wireMockServer.getAllServeEvents().forEach(event -> {
                logs.add(String.format("%s %s -> %d",
                    event.getRequest().getMethod(),
                    event.getRequest().getUrl(),
                    event.getResponse().getStatus()));
            });
        }
        return logs;
    }

    /**
     * 打印请求日志
     */
    public static void printRequestLog() {
        logger.info("========== Mock Request Log ==========");
        getRequestLog().forEach(logger::info);
        logger.info("=======================================");
    }

    /**
     * 清除AB实验
     */
    public static void clearABExperiments() {
        abExperiments.clear();
        requestCounters.clear();
        logger.debug("Cleared all AB experiments");
    }

    /**
     * 清除场景
     */
    public static void clearScenarios() {
        scenarioMappings.clear();
        logger.debug("Cleared all scenarios");
    }
}
