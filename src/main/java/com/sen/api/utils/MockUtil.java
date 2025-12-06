package com.sen.api.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * WireMock工具类
 * 用于API Mock测试
 */
public class MockUtil {

    private static final Logger logger = LoggerFactory.getLogger(MockUtil.class);

    private static WireMockServer wireMockServer;
    private static boolean isRunning = false;

    private MockUtil() {
        // 工具类不允许实例化
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
}
