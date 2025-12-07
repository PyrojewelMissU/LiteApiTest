package com.sen.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志增强工具类
 * 支持：
 * - 请求日志
 * - 响应日志
 * - traceId 捕获
 * - 失败用例日志特殊标记
 *
 * @author sen
 */
public class LogEnhancer {

    private static final Logger logger = LoggerFactory.getLogger(LogEnhancer.class);

    // TraceId Key
    public static final String TRACE_ID_KEY = "traceId";
    public static final String TEST_CASE_KEY = "testCase";
    public static final String TEST_STATUS_KEY = "testStatus";

    // 日志存储
    private static final Map<String, List<LogEntry>> testLogs = new ConcurrentHashMap<>();

    // 失败用例日志
    private static final Map<String, FailedTestLog> failedTestLogs = new ConcurrentHashMap<>();

    // 日期格式
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private LogEnhancer() {
    }

    /**
     * 日志条目
     */
    public static class LogEntry {
        private String timestamp;
        private String level;
        private String message;
        private String traceId;
        private String testCase;
        private Map<String, String> extras = new HashMap<>();

        public LogEntry(String level, String message) {
            this.timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
            this.level = level;
            this.message = message;
            this.traceId = MDC.get(TRACE_ID_KEY);
            this.testCase = MDC.get(TEST_CASE_KEY);
        }

        public void addExtra(String key, String value) {
            extras.put(key, value);
        }

        // Getters
        public String getTimestamp() {
            return timestamp;
        }

        public String getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getTestCase() {
            return testCase;
        }

        public Map<String, String> getExtras() {
            return extras;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(timestamp).append("] ");
            sb.append("[").append(level).append("] ");
            if (traceId != null) {
                sb.append("[").append(traceId).append("] ");
            }
            sb.append(message);
            return sb.toString();
        }
    }

    /**
     * 失败测试日志
     */
    public static class FailedTestLog {
        private String testName;
        private String traceId;
        private String errorMessage;
        private String requestUrl;
        private String requestMethod;
        private String requestBody;
        private int responseStatus;
        private String responseBody;
        private List<String> validationErrors = new ArrayList<>();
        private String stackTrace;
        private LocalDateTime timestamp;

        public FailedTestLog(String testName) {
            this.testName = testName;
            this.traceId = MDC.get(TRACE_ID_KEY);
            this.timestamp = LocalDateTime.now();
        }

        // Getters and Setters
        public String getTestName() {
            return testName;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public void setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
        }

        public String getRequestMethod() {
            return requestMethod;
        }

        public void setRequestMethod(String requestMethod) {
            this.requestMethod = requestMethod;
        }

        public String getRequestBody() {
            return requestBody;
        }

        public void setRequestBody(String requestBody) {
            this.requestBody = requestBody;
        }

        public int getResponseStatus() {
            return responseStatus;
        }

        public void setResponseStatus(int responseStatus) {
            this.responseStatus = responseStatus;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public List<String> getValidationErrors() {
            return validationErrors;
        }

        public void addValidationError(String error) {
            this.validationErrors.add(error);
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }

    // ==================== TraceId 管理 ====================

    /**
     * 生成并设置TraceId
     */
    public static String generateTraceId() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(TRACE_ID_KEY, traceId);
        return traceId;
    }

    /**
     * 设置TraceId
     */
    public static void setTraceId(String traceId) {
        MDC.put(TRACE_ID_KEY, traceId);
    }

    /**
     * 获取当前TraceId
     */
    public static String getTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    /**
     * 清除TraceId
     */
    public static void clearTraceId() {
        MDC.remove(TRACE_ID_KEY);
    }

    /**
     * 从响应Header中捕获TraceId
     */
    public static void captureTraceIdFromHeader(Map<String, String> headers) {
        String[] possibleKeys = {"X-Trace-Id", "X-Request-Id", "X-Correlation-Id", "trace-id", "request-id"};
        for (String key : possibleKeys) {
            if (headers.containsKey(key)) {
                String traceId = headers.get(key);
                MDC.put(TRACE_ID_KEY, traceId);
                logger.debug("Captured traceId from header {}: {}", key, traceId);
                return;
            }
        }
    }

    // ==================== 测试用例上下文 ====================

    /**
     * 开始测试用例
     */
    public static void startTestCase(String testName) {
        MDC.put(TEST_CASE_KEY, testName);
        MDC.put(TEST_STATUS_KEY, "RUNNING");
        generateTraceId();
        testLogs.put(testName, new ArrayList<>());
        logger.info("========== [START] {} ==========", testName);
    }

    /**
     * 结束测试用例
     */
    public static void endTestCase(String testName, boolean passed) {
        String status = passed ? "PASSED" : "FAILED";
        MDC.put(TEST_STATUS_KEY, status);

        if (passed) {
            logger.info("========== [PASSED] {} ==========", testName);
        } else {
            logger.error("========== [FAILED] {} ==========", testName);
        }

        MDC.remove(TEST_CASE_KEY);
        MDC.remove(TEST_STATUS_KEY);
        MDC.remove(TRACE_ID_KEY);
    }

    // ==================== 请求/响应日志 ====================

    /**
     * 记录请求日志
     */
    public static void logRequest(String method, String url, Map<String, String> headers, String body) {
        String traceId = getTraceId();

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("[REQUEST] ").append(method).append(" ").append(url).append("\n");
        if (traceId != null) {
            sb.append("[TraceId] ").append(traceId).append("\n");
        }
        sb.append("-".repeat(60)).append("\n");
        sb.append("[Headers]\n");
        if (headers != null) {
            headers.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        }
        if (body != null && !body.isEmpty() && !body.equals("{}")) {
            sb.append("[Body]\n  ").append(formatJson(body)).append("\n");
        }
        sb.append("=".repeat(60));

        logger.info(sb.toString());
        addLogEntry("INFO", "REQUEST: " + method + " " + url);
    }

    /**
     * 记录响应日志
     */
    public static void logResponse(int statusCode, Map<String, String> headers, String body, long durationMs) {
        String traceId = getTraceId();

        // 捕获响应中的traceId
        if (headers != null) {
            captureTraceIdFromHeader(headers);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("=".repeat(60)).append("\n");
        sb.append("[RESPONSE] Status: ").append(statusCode).append(" (").append(durationMs).append("ms)\n");
        if (traceId != null) {
            sb.append("[TraceId] ").append(traceId).append("\n");
        }
        sb.append("-".repeat(60)).append("\n");
        if (headers != null && !headers.isEmpty()) {
            sb.append("[Headers]\n");
            headers.forEach((k, v) -> sb.append("  ").append(k).append(": ").append(v).append("\n"));
        }
        if (body != null && !body.isEmpty()) {
            sb.append("[Body]\n  ").append(formatJson(body)).append("\n");
        }
        sb.append("=".repeat(60));

        if (statusCode >= 400) {
            logger.warn(sb.toString());
        } else {
            logger.info(sb.toString());
        }

        addLogEntry(statusCode >= 400 ? "WARN" : "INFO", "RESPONSE: " + statusCode);
    }

    /**
     * 记录验证结果
     */
    public static void logValidation(String validation, boolean passed, String actual, String expected) {
        if (passed) {
            logger.info("[VALIDATION PASSED] {} | actual={}", validation, actual);
            addLogEntry("INFO", "VALIDATION PASSED: " + validation);
        } else {
            logger.error("[VALIDATION FAILED] {} | expected={}, actual={}", validation, expected, actual);
            addLogEntry("ERROR", "VALIDATION FAILED: " + validation + " expected=" + expected + ", actual=" + actual);
        }
    }

    // ==================== 失败日志记录 ====================

    /**
     * 记录失败的测试
     */
    public static FailedTestLog recordFailedTest(String testName) {
        FailedTestLog failedLog = new FailedTestLog(testName);
        failedTestLogs.put(testName, failedLog);
        return failedLog;
    }

    /**
     * 获取失败测试日志
     */
    public static FailedTestLog getFailedTestLog(String testName) {
        return failedTestLogs.get(testName);
    }

    /**
     * 获取所有失败测试日志
     */
    public static Map<String, FailedTestLog> getAllFailedTestLogs() {
        return new HashMap<>(failedTestLogs);
    }

    /**
     * 打印失败测试摘要
     */
    public static void printFailedTestSummary() {
        if (failedTestLogs.isEmpty()) {
            logger.info("No failed tests!");
            return;
        }

        logger.error("\n" + "=".repeat(80));
        logger.error("[FAILED TEST SUMMARY] Total: {} failed tests", failedTestLogs.size());
        logger.error("=".repeat(80));

        int index = 1;
        for (Map.Entry<String, FailedTestLog> entry : failedTestLogs.entrySet()) {
            FailedTestLog log = entry.getValue();
            logger.error("\n[{}] {}", index++, log.getTestName());
            logger.error("    TraceId: {}", log.getTraceId());
            logger.error("    Time: {}", log.getTimestamp().format(DATETIME_FORMATTER));
            if (log.getRequestUrl() != null) {
                logger.error("    Request: {} {}", log.getRequestMethod(), log.getRequestUrl());
            }
            if (log.getResponseStatus() > 0) {
                logger.error("    Response Status: {}", log.getResponseStatus());
            }
            logger.error("    Error: {}", log.getErrorMessage());
            if (!log.getValidationErrors().isEmpty()) {
                logger.error("    Validation Errors:");
                log.getValidationErrors().forEach(e -> logger.error("      - {}", e));
            }
        }
        logger.error("\n" + "=".repeat(80));
    }

    // ==================== 日志存储 ====================

    /**
     * 添加日志条目
     */
    private static void addLogEntry(String level, String message) {
        String testCase = MDC.get(TEST_CASE_KEY);
        if (testCase != null) {
            List<LogEntry> logs = testLogs.computeIfAbsent(testCase, k -> new ArrayList<>());
            logs.add(new LogEntry(level, message));
        }
    }

    /**
     * 获取测试用例的日志
     */
    public static List<LogEntry> getTestLogs(String testName) {
        return testLogs.getOrDefault(testName, new ArrayList<>());
    }

    /**
     * 清除所有日志
     */
    public static void clearAllLogs() {
        testLogs.clear();
        failedTestLogs.clear();
    }

    // ==================== 辅助方法 ====================

    /**
     * 格式化JSON（简单缩进）
     */
    private static String formatJson(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        // 简单的JSON格式化（单行显示，超长截断）
        String formatted = json.trim();
        if (formatted.length() > 500) {
            formatted = formatted.substring(0, 500) + "... (truncated)";
        }
        return formatted;
    }

    /**
     * 格式化时间
     */
    public static String formatDuration(long ms) {
        if (ms < 1000) {
            return ms + "ms";
        } else if (ms < 60000) {
            return String.format("%.2fs", ms / 1000.0);
        } else {
            long minutes = ms / 60000;
            long seconds = (ms % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    /**
     * 生成测试报告
     */
    public static String generateTestReport(List<String> passedTests, List<String> failedTests, long totalDuration) {
        StringBuilder report = new StringBuilder();
        report.append("\n");
        report.append("╔══════════════════════════════════════════════════════════════════╗\n");
        report.append("║                      TEST EXECUTION REPORT                       ║\n");
        report.append("╠══════════════════════════════════════════════════════════════════╣\n");
        report.append(String.format("║  Total: %-10d  Passed: %-10d  Failed: %-10d     ║\n",
            passedTests.size() + failedTests.size(), passedTests.size(), failedTests.size()));
        report.append(String.format("║  Duration: %-20s  Pass Rate: %.2f%%                ║\n",
            formatDuration(totalDuration),
            passedTests.size() * 100.0 / (passedTests.size() + failedTests.size())));
        report.append("╠══════════════════════════════════════════════════════════════════╣\n");

        if (!failedTests.isEmpty()) {
            report.append("║  FAILED TESTS:                                                   ║\n");
            for (String test : failedTests) {
                report.append(String.format("║    ✗ %-60s║\n", truncate(test, 60)));
            }
            report.append("╠══════════════════════════════════════════════════════════════════╣\n");
        }

        report.append("║  PASSED TESTS:                                                   ║\n");
        for (String test : passedTests) {
            report.append(String.format("║    ✓ %-60s║\n", truncate(test, 60)));
        }

        report.append("╚══════════════════════════════════════════════════════════════════╝\n");

        return report.toString();
    }

    private static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
