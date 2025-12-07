package com.sen.api.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.jsonpath.JsonPath;
import com.sen.api.beans.ApiDataBean;
import com.sen.api.configs.EnvCenter;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YAML DSL 测试引擎
 * 让 QA 不用写 Java 就能写用例
 *
 * 示例用例格式：
 * <pre>
 * - name: 获取用户信息
 *   api: /user/get
 *   method: GET
 *   params:
 *     id: 1
 *   validate:
 *     - status: 200
 *     - $.code: 0
 *     - $.data.name: notEmpty
 *   save:
 *     userName: $.data.name
 * </pre>
 *
 * @author sen
 */
public class YamlDslTestEngine {

    private static final Logger logger = LoggerFactory.getLogger(YamlDslTestEngine.class);

    // 变量替换正则
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("__([a-zA-Z]+)\\(([^)]*)\\)");

    /**
     * DSL测试用例
     */
    public static class DslTestCase {
        private String name;
        private String description;
        private String api;
        private String method = "GET";
        private Map<String, Object> headers = new HashMap<>();
        private Map<String, Object> params = new HashMap<>();
        private Object body;
        private List<Map<String, Object>> validate = new ArrayList<>();
        private Map<String, String> save = new HashMap<>();
        private Map<String, String> preParam = new HashMap<>();
        private int sleep = 0;
        private boolean skip = false;
        private List<String> tags = new ArrayList<>();
        private int retry = 0;
        private String dependsOn;
        private String account; // 使用的账号

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getApi() {
            return api;
        }

        public void setApi(String api) {
            this.api = api;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public Map<String, Object> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, Object> headers) {
            this.headers = headers;
        }

        public Map<String, Object> getParams() {
            return params;
        }

        public void setParams(Map<String, Object> params) {
            this.params = params;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }

        public List<Map<String, Object>> getValidate() {
            return validate;
        }

        public void setValidate(List<Map<String, Object>> validate) {
            this.validate = validate;
        }

        public Map<String, String> getSave() {
            return save;
        }

        public void setSave(Map<String, String> save) {
            this.save = save;
        }

        public Map<String, String> getPreParam() {
            return preParam;
        }

        public void setPreParam(Map<String, String> preParam) {
            this.preParam = preParam;
        }

        public int getSleep() {
            return sleep;
        }

        public void setSleep(int sleep) {
            this.sleep = sleep;
        }

        public boolean isSkip() {
            return skip;
        }

        public void setSkip(boolean skip) {
            this.skip = skip;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        public int getRetry() {
            return retry;
        }

        public void setRetry(int retry) {
            this.retry = retry;
        }

        public String getDependsOn() {
            return dependsOn;
        }

        public void setDependsOn(String dependsOn) {
            this.dependsOn = dependsOn;
        }

        public String getAccount() {
            return account;
        }

        public void setAccount(String account) {
            this.account = account;
        }
    }

    /**
     * 测试执行结果
     */
    public static class DslTestResult {
        private String name;
        private boolean passed;
        private int statusCode;
        private String responseBody;
        private List<String> passedValidations = new ArrayList<>();
        private List<String> failedValidations = new ArrayList<>();
        private Map<String, String> savedData = new HashMap<>();
        private String errorMessage;
        private long durationMs;
        private int retryCount;

        public DslTestResult(String name) {
            this.name = name;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
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

        public List<String> getPassedValidations() {
            return passedValidations;
        }

        public List<String> getFailedValidations() {
            return failedValidations;
        }

        public Map<String, String> getSavedData() {
            return savedData;
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

        public int getRetryCount() {
            return retryCount;
        }

        public void setRetryCount(int retryCount) {
            this.retryCount = retryCount;
        }
    }

    /**
     * 测试套件结果
     */
    public static class DslSuiteResult {
        private String suiteName;
        private List<DslTestResult> results = new ArrayList<>();
        private int passedCount = 0;
        private int failedCount = 0;
        private int skippedCount = 0;
        private long totalDurationMs;

        public DslSuiteResult(String suiteName) {
            this.suiteName = suiteName;
        }

        public void addResult(DslTestResult result) {
            results.add(result);
            if (result.isPassed()) {
                passedCount++;
            } else {
                failedCount++;
            }
        }

        public void addSkipped() {
            skippedCount++;
        }

        // Getters
        public String getSuiteName() {
            return suiteName;
        }

        public List<DslTestResult> getResults() {
            return results;
        }

        public int getPassedCount() {
            return passedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public long getTotalDurationMs() {
            return totalDurationMs;
        }

        public void setTotalDurationMs(long totalDurationMs) {
            this.totalDurationMs = totalDurationMs;
        }

        public boolean isAllPassed() {
            return failedCount == 0;
        }

        public double getPassRate() {
            int total = passedCount + failedCount;
            return total > 0 ? (double) passedCount / total * 100 : 0;
        }
    }

    // 单例实例
    private static volatile YamlDslTestEngine instance;

    // 共享数据池
    private final Map<String, String> sharedData = new ConcurrentHashMap<>();

    // Root URL
    private String rootUrl = "";

    // 默认请求头
    private final Map<String, String> defaultHeaders = new HashMap<>();

    // ObjectMapper
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    private YamlDslTestEngine() {
        defaultHeaders.put("Content-Type", "application/json");
        defaultHeaders.put("Accept", "application/json");
        logger.info("YamlDslTestEngine 初始化");
    }

    /**
     * 获取单例实例
     */
    public static YamlDslTestEngine getInstance() {
        if (instance == null) {
            synchronized (YamlDslTestEngine.class) {
                if (instance == null) {
                    instance = new YamlDslTestEngine();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化
     */
    public void init(String rootUrl) {
        this.rootUrl = rootUrl;
        logger.info("YamlDslTestEngine 初始化完成，Root URL: {}", rootUrl);
    }

    /**
     * 初始化（使用EnvCenter）
     */
    public void initFromEnvCenter() {
        EnvCenter envCenter = EnvCenter.getInstance();
        this.rootUrl = envCenter.getRootUrl();
        this.defaultHeaders.putAll(envCenter.getHeaders());
        logger.info("YamlDslTestEngine 从EnvCenter初始化完成");
    }

    /**
     * 设置Root URL
     */
    public void setRootUrl(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /**
     * 添加默认Header
     */
    public void addDefaultHeader(String key, String value) {
        defaultHeaders.put(key, value);
    }

    /**
     * 设置共享数据
     */
    public void setSharedData(String key, String value) {
        sharedData.put(key, value);
    }

    /**
     * 获取共享数据
     */
    public String getSharedData(String key) {
        return sharedData.get(key);
    }

    /**
     * 从YAML文件加载并执行测试用例
     */
    public DslSuiteResult runFromFile(String yamlPath) {
        logger.info("从文件加载测试用例: {}", yamlPath);

        try {
            List<DslTestCase> testCases = loadTestCases(yamlPath);
            return runTestCases(yamlPath, testCases);
        } catch (Exception e) {
            logger.error("加载测试用例失败: {}", e.getMessage());
            throw new RuntimeException("加载测试用例失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从YAML内容执行测试用例
     */
    public DslSuiteResult runFromYaml(String yamlContent) {
        logger.info("从YAML内容执行测试用例");

        try {
            List<DslTestCase> testCases = yamlMapper.readValue(yamlContent,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, DslTestCase.class));
            return runTestCases("inline", testCases);
        } catch (Exception e) {
            logger.error("解析YAML内容失败: {}", e.getMessage());
            throw new RuntimeException("解析YAML内容失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加载测试用例
     */
    @SuppressWarnings("unchecked")
    private List<DslTestCase> loadTestCases(String yamlPath) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(yamlPath);
        if (is == null) {
            File file = new File(yamlPath);
            if (!file.exists()) {
                throw new RuntimeException("测试用例文件不存在: " + yamlPath);
            }
            return yamlMapper.readValue(file,
                yamlMapper.getTypeFactory().constructCollectionType(List.class, DslTestCase.class));
        }
        List<DslTestCase> cases = yamlMapper.readValue(is,
            yamlMapper.getTypeFactory().constructCollectionType(List.class, DslTestCase.class));
        is.close();
        return cases;
    }

    /**
     * 执行测试用例集
     */
    private DslSuiteResult runTestCases(String suiteName, List<DslTestCase> testCases) {
        logger.info("========== 开始执行测试套件: {} ==========", suiteName);
        long startTime = System.currentTimeMillis();

        DslSuiteResult suiteResult = new DslSuiteResult(suiteName);

        for (int i = 0; i < testCases.size(); i++) {
            DslTestCase testCase = testCases.get(i);
            logger.info("---------- 用例 {}/{}: {} ----------", i + 1, testCases.size(), testCase.getName());

            if (testCase.isSkip()) {
                logger.info("用例被跳过: {}", testCase.getName());
                suiteResult.addSkipped();
                continue;
            }

            DslTestResult result = runSingleTest(testCase);
            suiteResult.addResult(result);

            if (result.isPassed()) {
                logger.info("用例通过: {}", testCase.getName());
            } else {
                logger.error("用例失败: {} - {}", testCase.getName(), result.getErrorMessage());
            }
        }

        suiteResult.setTotalDurationMs(System.currentTimeMillis() - startTime);

        logger.info("========== 测试套件执行完成 ==========");
        logger.info("总计: {}, 通过: {}, 失败: {}, 跳过: {}, 通过率: {:.2f}%",
            suiteResult.getPassedCount() + suiteResult.getFailedCount(),
            suiteResult.getPassedCount(),
            suiteResult.getFailedCount(),
            suiteResult.getSkippedCount(),
            suiteResult.getPassRate());

        return suiteResult;
    }

    /**
     * 执行单个测试用例
     */
    private DslTestResult runSingleTest(DslTestCase testCase) {
        DslTestResult result = new DslTestResult(testCase.getName());
        long startTime = System.currentTimeMillis();

        int attempts = 0;
        int maxAttempts = testCase.getRetry() + 1;

        while (attempts < maxAttempts) {
            attempts++;
            result.setRetryCount(attempts - 1);

            try {
                // 执行前等待
                if (testCase.getSleep() > 0) {
                    Thread.sleep(testCase.getSleep() * 1000L);
                }

                // 处理前置参数
                processPreParams(testCase);

                // 发送请求
                Response response = sendRequest(testCase);

                result.setStatusCode(response.getStatusCode());
                result.setResponseBody(response.getBody().asString());

                // 执行验证
                boolean allValidationsPassed = executeValidations(testCase, response, result);

                // 保存数据
                if (allValidationsPassed) {
                    saveResponseData(testCase, response, result);
                }

                result.setPassed(allValidationsPassed);
                if (allValidationsPassed) {
                    break; // 成功，跳出重试
                }

            } catch (Exception e) {
                result.setPassed(false);
                result.setErrorMessage(e.getMessage());
                logger.warn("用例执行异常 (尝试 {}/{}): {}", attempts, maxAttempts, e.getMessage());

                if (attempts < maxAttempts) {
                    try {
                        Thread.sleep(1000);
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
     * 处理前置参数
     */
    private void processPreParams(DslTestCase testCase) {
        if (testCase.getPreParam() == null || testCase.getPreParam().isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : testCase.getPreParam().entrySet()) {
            String value = processValue(entry.getValue());
            sharedData.put(entry.getKey(), value);
            logger.debug("前置参数: {} = {}", entry.getKey(), value);
        }
    }

    /**
     * 发送HTTP请求
     */
    private Response sendRequest(DslTestCase testCase) {
        String url = rootUrl + replaceVariables(testCase.getApi());

        RequestSpecification request = RestAssured.given();

        // 添加默认Header
        defaultHeaders.forEach(request::header);

        // 添加用例自定义Header
        if (testCase.getHeaders() != null) {
            for (Map.Entry<String, Object> entry : testCase.getHeaders().entrySet()) {
                request.header(entry.getKey(), replaceVariables(String.valueOf(entry.getValue())));
            }
        }

        // 处理账号Token
        if (testCase.getAccount() != null && !testCase.getAccount().isEmpty()) {
            try {
                String token = TokenManager.getInstance().getToken(testCase.getAccount());
                request.header("Authorization", "Bearer " + token);
            } catch (Exception e) {
                logger.warn("获取账号Token失败: {}", testCase.getAccount());
            }
        }

        // 处理请求参数/请求体
        String method = testCase.getMethod().toUpperCase();
        if ("GET".equals(method) || "DELETE".equals(method)) {
            if (testCase.getParams() != null) {
                for (Map.Entry<String, Object> entry : testCase.getParams().entrySet()) {
                    request.queryParam(entry.getKey(), replaceVariables(String.valueOf(entry.getValue())));
                }
            }
        } else {
            // POST/PUT/PATCH
            Object body = testCase.getBody() != null ? testCase.getBody() : testCase.getParams();
            if (body != null) {
                String bodyStr;
                if (body instanceof String) {
                    bodyStr = replaceVariables((String) body);
                } else {
                    try {
                        bodyStr = replaceVariables(jsonMapper.writeValueAsString(body));
                    } catch (Exception e) {
                        bodyStr = body.toString();
                    }
                }
                request.contentType(ContentType.JSON).body(bodyStr);
            }
        }

        logger.debug("请求: {} {}", method, url);

        // 发送请求
        Response response;
        switch (method) {
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

        logger.debug("响应: {} - {}", response.getStatusCode(), response.getBody().asString());
        return response;
    }

    /**
     * 执行验证
     */
    private boolean executeValidations(DslTestCase testCase, Response response, DslTestResult result) {
        if (testCase.getValidate() == null || testCase.getValidate().isEmpty()) {
            return true;
        }

        boolean allPassed = true;
        String responseBody = response.getBody().asString();

        for (Map<String, Object> validation : testCase.getValidate()) {
            for (Map.Entry<String, Object> entry : validation.entrySet()) {
                String key = entry.getKey();
                Object expected = entry.getValue();

                try {
                    boolean passed = validateSingle(key, expected, response.getStatusCode(), responseBody);
                    if (passed) {
                        result.getPassedValidations().add(key + "=" + expected);
                        logger.debug("验证通过: {} = {}", key, expected);
                    } else {
                        result.getFailedValidations().add(key + "=" + expected);
                        result.setErrorMessage("验证失败: " + key + " 期望=" + expected);
                        logger.warn("验证失败: {} 期望={}", key, expected);
                        allPassed = false;
                    }
                } catch (Exception e) {
                    result.getFailedValidations().add(key + ": " + e.getMessage());
                    result.setErrorMessage("验证异常: " + key + " - " + e.getMessage());
                    allPassed = false;
                }
            }
        }

        return allPassed;
    }

    /**
     * 单个验证
     */
    private boolean validateSingle(String key, Object expected, int statusCode, String responseBody) {
        String expectedStr = String.valueOf(expected);

        // 状态码验证
        if ("status".equals(key)) {
            return statusCode == Integer.parseInt(expectedStr);
        }

        // JsonPath验证
        if (key.startsWith("$.") || key.startsWith("$[")) {
            Object actual = JsonPath.read(responseBody, key);
            String actualStr = actual != null ? actual.toString() : "";

            // 特殊验证器
            switch (expectedStr.toLowerCase()) {
                case "notnull":
                    return actual != null;
                case "null":
                    return actual == null;
                case "notempty":
                    return actual != null && !actualStr.isEmpty();
                case "empty":
                    return actualStr.isEmpty();
                case "exist":
                    return actual != null;
                default:
                    // 支持比较操作符
                    if (expectedStr.startsWith(">")) {
                        double actualNum = Double.parseDouble(actualStr);
                        double expectedNum = Double.parseDouble(expectedStr.substring(1));
                        return actualNum > expectedNum;
                    } else if (expectedStr.startsWith("<")) {
                        double actualNum = Double.parseDouble(actualStr);
                        double expectedNum = Double.parseDouble(expectedStr.substring(1));
                        return actualNum < expectedNum;
                    } else if (expectedStr.startsWith("~=")) {
                        // 正则匹配
                        return actualStr.matches(expectedStr.substring(2));
                    } else if (expectedStr.startsWith("!=")) {
                        return !actualStr.equals(expectedStr.substring(2));
                    } else {
                        return actualStr.equals(expectedStr);
                    }
            }
        }

        // 包含验证
        if ("contains".equals(key)) {
            return responseBody.contains(expectedStr);
        }

        // 正则匹配
        if ("matches".equals(key)) {
            return responseBody.matches(expectedStr);
        }

        return false;
    }

    /**
     * 保存响应数据
     */
    private void saveResponseData(DslTestCase testCase, Response response, DslTestResult result) {
        if (testCase.getSave() == null || testCase.getSave().isEmpty()) {
            return;
        }

        String responseBody = response.getBody().asString();

        for (Map.Entry<String, String> entry : testCase.getSave().entrySet()) {
            String key = entry.getKey();
            String jsonPath = entry.getValue();

            try {
                Object value = JsonPath.read(responseBody, jsonPath);
                String strValue = value != null ? value.toString() : "";
                sharedData.put(key, strValue);
                result.getSavedData().put(key, strValue);
                logger.debug("保存数据: {} = {}", key, strValue);
            } catch (Exception e) {
                logger.warn("保存数据失败: {} -> {}", key, jsonPath);
            }
        }
    }

    /**
     * 替换变量
     */
    private String replaceVariables(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 替换函数调用 __funcName(args)
        Matcher funcMatcher = FUNCTION_PATTERN.matcher(result);
        StringBuffer funcBuffer = new StringBuffer();
        while (funcMatcher.find()) {
            String funcName = funcMatcher.group(1);
            String funcArgs = funcMatcher.group(2);
            String funcResult = executeFunction(funcName, funcArgs);
            funcMatcher.appendReplacement(funcBuffer, Matcher.quoteReplacement(funcResult));
        }
        funcMatcher.appendTail(funcBuffer);
        result = funcBuffer.toString();

        // 替换变量 ${key}
        Matcher varMatcher = VARIABLE_PATTERN.matcher(result);
        StringBuffer varBuffer = new StringBuffer();
        while (varMatcher.find()) {
            String varName = varMatcher.group(1);
            String varValue = sharedData.getOrDefault(varName, "${" + varName + "}");
            varMatcher.appendReplacement(varBuffer, Matcher.quoteReplacement(varValue));
        }
        varMatcher.appendTail(varBuffer);

        return varBuffer.toString();
    }

    /**
     * 处理值（支持函数调用）
     */
    private String processValue(String value) {
        return replaceVariables(value);
    }

    /**
     * 执行内置函数
     */
    private String executeFunction(String funcName, String args) {
        try {
            String[] argArray = args.isEmpty() ? new String[0] : args.split(",");
            return FunctionUtil.execute(funcName, argArray);
        } catch (Exception e) {
            logger.warn("执行函数失败: __{}({})", funcName, args);
            return "__" + funcName + "(" + args + ")";
        }
    }

    /**
     * 转换为ApiDataBean（兼容现有框架）
     */
    public ApiDataBean convertToApiDataBean(DslTestCase dslCase) {
        ApiDataBean bean = new ApiDataBean();
        bean.setRun(!dslCase.isSkip());
        bean.setDesc(dslCase.getName());
        bean.setUrl(dslCase.getApi());
        bean.setMethod(dslCase.getMethod());
        bean.setStatus(200);

        // 转换params为JSON
        if (dslCase.getBody() != null) {
            try {
                bean.setParam(jsonMapper.writeValueAsString(dslCase.getBody()));
            } catch (Exception e) {
                bean.setParam(dslCase.getBody().toString());
            }
        } else if (dslCase.getParams() != null && !dslCase.getParams().isEmpty()) {
            try {
                bean.setParam(jsonMapper.writeValueAsString(dslCase.getParams()));
            } catch (Exception e) {
                bean.setParam("{}");
            }
        }

        // 转换validate为verify格式
        if (dslCase.getValidate() != null && !dslCase.getValidate().isEmpty()) {
            StringBuilder verify = new StringBuilder();
            for (Map<String, Object> v : dslCase.getValidate()) {
                for (Map.Entry<String, Object> entry : v.entrySet()) {
                    if (verify.length() > 0) {
                        verify.append(";");
                    }
                    verify.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
            bean.setVerify(verify.toString());
        }

        // 转换save
        if (dslCase.getSave() != null && !dslCase.getSave().isEmpty()) {
            StringBuilder save = new StringBuilder();
            for (Map.Entry<String, String> entry : dslCase.getSave().entrySet()) {
                if (save.length() > 0) {
                    save.append(";");
                }
                save.append(entry.getKey()).append("=").append(entry.getValue());
            }
            bean.setSave(save.toString());
        }

        bean.setSleep(dslCase.getSleep());
        bean.setDependsOn(dslCase.getDependsOn());

        if (dslCase.getTags() != null && !dslCase.getTags().isEmpty()) {
            bean.setTags(String.join(",", dslCase.getTags()));
        }

        return bean;
    }

    /**
     * 清除共享数据
     */
    public void clearSharedData() {
        sharedData.clear();
    }

    /**
     * 打印共享数据
     */
    public void printSharedData() {
        logger.info("========== 共享数据 ==========");
        sharedData.forEach((k, v) -> logger.info("{} = {}", k, v));
        logger.info("==============================");
    }
}
