package com.sen.api.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.SSLConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * REST Assured 请求工具类
 * 封装常见的HTTP请求操作
 */
public class RestAssuredUtil {

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredUtil.class);

    static {
        // 配置 RestAssured 全局设置
        RestAssured.config = RestAssured.config()
            .sslConfig(SSLConfig.sslConfig().relaxedHTTPSValidation());
    }

    /**
     * 创建基础请求规范
     */
    public static RequestSpecification createBaseSpec(String baseUrl, Map<String, String> headers) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
            .setBaseUri(baseUrl)
            .setRelaxedHTTPSValidation()
            .log(LogDetail.ALL)
            .addFilter(new AllureRestAssured()); // 添加 Allure 报告集成

        if (headers != null && !headers.isEmpty()) {
            builder.addHeaders(headers);
        }

        return builder.build();
    }

    /**
     * GET 请求
     */
    public static Response get(RequestSpecification spec, String path) {
        logger.info("Executing GET request to: {}", path);
        return RestAssured.given()
            .spec(spec)
            .when()
            .get(path)
            .then()
            .extract()
            .response();
    }

    /**
     * GET 请求（带查询参数）
     */
    public static Response get(RequestSpecification spec, String path, Map<String, Object> queryParams) {
        logger.info("Executing GET request to: {} with params: {}", path, queryParams);
        return RestAssured.given()
            .spec(spec)
            .queryParams(queryParams)
            .when()
            .get(path)
            .then()
            .extract()
            .response();
    }

    /**
     * POST 请求（JSON body）
     */
    public static Response post(RequestSpecification spec, String path, Object body) {
        logger.info("Executing POST request to: {}", path);
        return RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .post(path)
            .then()
            .extract()
            .response();
    }

    /**
     * POST 请求（Form data）
     */
    public static Response postForm(RequestSpecification spec, String path, Map<String, Object> formParams) {
        logger.info("Executing POST form request to: {} with params: {}", path, formParams);
        return RestAssured.given()
            .spec(spec)
            .contentType(ContentType.URLENC)
            .formParams(formParams)
            .when()
            .post(path)
            .then()
            .extract()
            .response();
    }

    /**
     * POST 请求（Multipart - 文件上传）
     */
    public static Response postMultipart(RequestSpecification spec, String path,
                                        Map<String, Object> formParams,
                                        String fileParamName,
                                        File file) {
        logger.info("Executing POST multipart request to: {} with file: {}", path, file.getName());

        var request = RestAssured.given()
            .spec(spec)
            .contentType(ContentType.MULTIPART);

        if (formParams != null && !formParams.isEmpty()) {
            formParams.forEach((key, value) -> {
                if (value != null) {
                    request.formParam(key, value);
                }
            });
        }

        if (file != null && fileParamName != null) {
            request.multiPart(fileParamName, file);
        }

        return request
            .when()
            .post(path)
            .then()
            .extract()
            .response();
    }

    /**
     * PUT 请求（JSON body）
     */
    public static Response put(RequestSpecification spec, String path, Object body) {
        logger.info("Executing PUT request to: {}", path);
        return RestAssured.given()
            .spec(spec)
            .contentType(ContentType.JSON)
            .body(body)
            .when()
            .put(path)
            .then()
            .extract()
            .response();
    }

    /**
     * PUT 请求（Form data）
     */
    public static Response putForm(RequestSpecification spec, String path, Map<String, Object> formParams) {
        logger.info("Executing PUT form request to: {}", path);
        return RestAssured.given()
            .spec(spec)
            .contentType(ContentType.URLENC)
            .formParams(formParams)
            .when()
            .put(path)
            .then()
            .extract()
            .response();
    }

    /**
     * DELETE 请求
     */
    public static Response delete(RequestSpecification spec, String path) {
        logger.info("Executing DELETE request to: {}", path);
        return RestAssured.given()
            .spec(spec)
            .when()
            .delete(path)
            .then()
            .extract()
            .response();
    }

    /**
     * DELETE 请求（带查询参数）
     */
    public static Response delete(RequestSpecification spec, String path, Map<String, Object> queryParams) {
        logger.info("Executing DELETE request to: {} with params: {}", path, queryParams);
        return RestAssured.given()
            .spec(spec)
            .queryParams(queryParams)
            .when()
            .delete(path)
            .then()
            .extract()
            .response();
    }

    /**
     * 记录响应信息到日志
     */
    public static void logResponse(Response response) {
        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Headers: {}", response.getHeaders());
        logger.info("Response Body: {}", response.getBody().asString());
    }
}
