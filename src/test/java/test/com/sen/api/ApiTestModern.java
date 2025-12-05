package test.com.sen.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sen.api.beans.ApiDataBean;
import com.sen.api.configs.ApiConfig;
import com.sen.api.listeners.AutoTestListener;
import com.sen.api.listeners.RetryListener;
import com.sen.api.utils.*;
import io.qameta.allure.*;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

/**
 * 现代化的API测试类
 * 使用 RestAssured + Jackson + Allure + SLF4J
 */
@Listeners({ AutoTestListener.class, RetryListener.class })
public class ApiTestModern extends TestBase {

    private static final Logger logger = LoggerFactory.getLogger(ApiTestModern.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * API请求根路径
     */
    private static String rootUrl;

    /**
     * 根路径是否以'/'结尾
     */
    private static boolean rootUrlEndWithSlash = false;

    /**
     * 所有公共header
     */
    private static Map<String, String> publicHeaders = new HashMap<>();

    /**
     * 是否使用form-data传参
     */
    private static boolean requestByFormData = false;

    /**
     * 配置
     */
    private static ApiConfig apiConfig;

    /**
     * RequestSpecification for RestAssured
     */
    private static RequestSpecification requestSpec;

    /**
     * 所有API测试用例数据
     */
    protected List<ApiDataBean> dataList = new ArrayList<>();

    /**
     * 初始化测试数据
     */
    @Parameters("envName")
    @BeforeSuite
    public void init(@org.testng.annotations.Optional("api-config.xml") String envName) throws Exception {
        String configFilePath = Paths.get(System.getProperty("user.dir"), envName).toString();
        logger.info("API config path: {}", configFilePath);

        apiConfig = new ApiConfig(configFilePath);

        // 获取基础数据
        rootUrl = apiConfig.getRootUrl();
        rootUrlEndWithSlash = rootUrl.endsWith("/");
        logger.info("Root URL: {}", rootUrl);

        // 读取 param，并将值保存到公共数据map
        Map<String, String> params = apiConfig.getParams();
        setSaveDates(params);

        // 处理headers
        apiConfig.getHeaders().forEach((key, value) -> {
            publicHeaders.put(key, value);
            if (!requestByFormData && key.equalsIgnoreCase("content-type")
                    && value.toLowerCase().contains("form-data")) {
                requestByFormData = true;
            }
        });

        // 创建 RestAssured RequestSpecification
        requestSpec = RestAssuredUtil.createBaseSpec(rootUrl, publicHeaders);

        logger.info("API test initialization completed");
    }

    @Parameters({ "dataPath", "dataFormat" })
    @BeforeTest
    public void readData(
            @org.testng.annotations.Optional("data/api-test-data.json") String dataPath,
            @org.testng.annotations.Optional("json") String dataFormat) throws DocumentException {

        logger.info("Loading test data from: {} (format: {})", dataPath, dataFormat);

        String fullPath = Paths.get(System.getProperty("user.dir"), dataPath).toString();

        // 根据格式读取数据
        if ("json".equalsIgnoreCase(dataFormat)) {
            dataList = JsonDataReader.readJson(ApiDataBean.class, fullPath);
        } else if ("yaml".equalsIgnoreCase(dataFormat) || "yml".equalsIgnoreCase(dataFormat)) {
            dataList = YamlDataReader.readYaml(ApiDataBean.class, fullPath);
        } else if ("excel".equalsIgnoreCase(dataFormat) || "xls".equalsIgnoreCase(dataFormat)) {
            // 向后兼容Excel格式
            String[] paths = dataPath.split(";");
            String sheetName = "Sheet1"; // 默认sheet名
            dataList = readExcelData(ApiDataBean.class, paths, new String[]{sheetName});
        } else {
            // 使用DataReaderFactory自动识别
            dataList = DataReaderFactory.readData(ApiDataBean.class, fullPath);
        }

        logger.info("Loaded {} test cases from {}", dataList.size(), dataPath);
    }

    /**
     * 过滤数据，run标记为Y的执行
     */
    @DataProvider(name = "apiDatas")
    public Iterator<Object[]> getApiData(ITestContext context) {
        List<Object[]> dataProvider = new ArrayList<>();
        for (ApiDataBean data : dataList) {
            if (data.isRun()) {
                dataProvider.add(new Object[] { data });
            }
        }
        logger.info("Filtered {} executable test cases", dataProvider.size());
        return dataProvider.iterator();
    }

    @Test(dataProvider = "apiDatas")
    @Description("API 自动化测试")
    public void apiTest(ApiDataBean apiDataBean) throws Exception {
        // Allure 报告装饰
        Allure.feature(apiDataBean.getSheetName());
        Allure.story(apiDataBean.getDesc());
        Allure.description("Test Case: " + apiDataBean.getDesc());

        logger.info("=== Test Start: {} ===", apiDataBean.getDesc());

        if (apiDataBean.getSleep() > 0) {
            logger.info("Sleeping for {} seconds", apiDataBean.getSleep());
            Allure.step("Sleep " + apiDataBean.getSleep() + " seconds");
            Thread.sleep(apiDataBean.getSleep() * 1000);
        }

        // 构建请求参数
        String apiParam = buildRequestParam(apiDataBean);

        // 执行请求
        Response response = executeRequest(apiDataBean, apiParam);

        // 处理响应
        String responseData = processResponse(response, apiDataBean);

        // 记录响应数据
        logger.info("Response: {}", responseData);
        Allure.step("Response received", () -> {
            Allure.addAttachment("Response Body", "application/json", responseData, ".json");
        });

        // 验证预期信息
        verifyResult(responseData, apiDataBean.getVerify(), apiDataBean.isContains());

        // 保存返回结果
        saveResult(responseData, apiDataBean.getSave());

        logger.info("=== Test End ===");
    }

    /**
     * 构建请求参数
     */
    private String buildRequestParam(ApiDataBean apiDataBean) {
        // 处理预参数（函数生成的参数）
        String preParam = buildParam(apiDataBean.getPreParam());
        savePreParam(preParam);

        // 处理参数
        String apiParam = buildParam(apiDataBean.getParam());
        return apiParam;
    }

    /**
     * 执行HTTP请求
     */
    @Step("Execute {apiDataBean.method} request to {apiDataBean.url}")
    private Response executeRequest(ApiDataBean apiDataBean, String param) throws Exception {
        String url = parseUrl(apiDataBean.getUrl());
        String method = apiDataBean.getMethod().toLowerCase();

        logger.info("Method: {}", method);
        logger.info("URL: {}", url);
        logger.info("Param: {}", param.replace("\r\n", "").replace("\n", ""));

        // Allure 报告附件
        Allure.addAttachment("Request Method", method);
        Allure.addAttachment("Request URL", url);
        Allure.addAttachment("Request Param", "application/json", param, ".json");

        Response response;

        switch (method) {
            case "get":
                response = executeGetRequest(url, param);
                break;
            case "post":
            case "upload":
                response = executePostRequest(url, param, "upload".equalsIgnoreCase(method));
                break;
            case "put":
                response = executePutRequest(url, param);
                break;
            case "delete":
                response = executeDeleteRequest(url, param);
                break;
            default:
                throw new IllegalArgumentException("不支持的请求方法: " + method);
        }

        return response;
    }

    /**
     * 执行GET请求
     */
    private Response executeGetRequest(String url, String param) throws Exception {
        if (param != null && !param.isEmpty() && !param.equals("{}")) {
            Map<String, Object> queryParams = objectMapper.readValue(param,
                new TypeReference<Map<String, Object>>() {});
            return RestAssuredUtil.get(requestSpec, url, queryParams);
        }
        return RestAssuredUtil.get(requestSpec, url);
    }

    /**
     * 执行POST请求
     */
    private Response executePostRequest(String url, String param, boolean isUpload) throws Exception {
        if (requestByFormData || isUpload) {
            // Form-data 或文件上传
            Map<String, Object> formParams = parseFormData(param);

            // 检查是否有文件上传
            File fileToUpload = null;
            String fileParamName = null;
            Map<String, Object> nonFileParams = new HashMap<>();

            for (Map.Entry<String, Object> entry : formParams.entrySet()) {
                String value = String.valueOf(entry.getValue());
                Matcher m = funPattern.matcher(value);
                if (m.matches() && "bodyfile".equals(m.group(1))) {
                    fileParamName = entry.getKey();
                    fileToUpload = new File(m.group(2));
                } else {
                    nonFileParams.put(entry.getKey(), entry.getValue());
                }
            }

            if (fileToUpload != null) {
                return RestAssuredUtil.postMultipart(requestSpec, url, nonFileParams,
                    fileParamName, fileToUpload);
            } else {
                return RestAssuredUtil.postForm(requestSpec, url, formParams);
            }
        } else {
            // JSON body
            return RestAssuredUtil.post(requestSpec, url, param);
        }
    }

    /**
     * 执行PUT请求
     */
    private Response executePutRequest(String url, String param) throws Exception {
        if (requestByFormData) {
            Map<String, Object> formParams = parseFormData(param);
            return RestAssuredUtil.putForm(requestSpec, url, formParams);
        } else {
            return RestAssuredUtil.put(requestSpec, url, param);
        }
    }

    /**
     * 执行DELETE请求
     */
    private Response executeDeleteRequest(String url, String param) throws Exception {
        if (param != null && !param.isEmpty() && !param.equals("{}")) {
            Map<String, Object> queryParams = objectMapper.readValue(param,
                new TypeReference<Map<String, Object>>() {});
            return RestAssuredUtil.delete(requestSpec, url, queryParams);
        }
        return RestAssuredUtil.delete(requestSpec, url);
    }

    /**
     * 处理响应数据
     */
    @Step("Process response")
    private String processResponse(Response response, ApiDataBean apiDataBean) throws Exception {
        int statusCode = response.getStatusCode();
        logger.info("Response Status Code: {}", statusCode);

        // 验证状态码
        if (apiDataBean.getStatus() != 0) {
            Assert.assertEquals(statusCode, apiDataBean.getStatus(), "返回状态码与预期不符合!");
        }

        // 检查是否为文件下载
        String contentType = response.getContentType();
        if (contentType != null && (contentType.contains("download") || contentType.contains("octet-stream"))) {
            return handleFileDownload(response);
        }

        return response.getBody().asString();
    }

    /**
     * 处理文件下载
     */
    private String handleFileDownload(Response response) throws Exception {
        String contentDisposition = response.getHeader("Content-disposition");
        if (contentDisposition == null) {
            throw new RuntimeException("文件下载响应缺少 Content-disposition header");
        }

        String fileType = contentDisposition.substring(
            contentDisposition.lastIndexOf("."),
            contentDisposition.length());
        String filePath = "download/" + RandomUtil.getRandom(8, false) + fileType;

        InputStream is = response.getBody().asInputStream();
        Assert.assertTrue(FileUtil.writeFile(is, filePath), "下载文件失败");

        logger.info("File downloaded to: {}", filePath);
        return "{\"filePath\":\"" + filePath + "\"}";
    }

    /**
     * 格式化URL
     */
    private String parseUrl(String shortUrl) {
        shortUrl = getCommonParam(shortUrl);

        if (shortUrl.startsWith("http")) {
            return shortUrl;
        }

        if (rootUrlEndWithSlash == shortUrl.startsWith("/")) {
            if (rootUrlEndWithSlash) {
                shortUrl = shortUrl.replaceFirst("/", "");
            } else {
                shortUrl = "/" + shortUrl;
            }
        }

        return rootUrl + shortUrl;
    }

    /**
     * 解析form-data参数
     */
    private Map<String, Object> parseFormData(String param) throws Exception {
        return objectMapper.readValue(param, new TypeReference<Map<String, Object>>() {});
    }
}
