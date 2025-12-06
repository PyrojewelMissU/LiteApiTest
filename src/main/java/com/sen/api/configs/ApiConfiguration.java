package com.sen.api.configs;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

import static io.restassured.RestAssured.given;

/**
 * API测试配置类
 * 使用Spring @Configuration管理配置，替代XML解析
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
public class ApiConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApiConfiguration.class);

    @Autowired
    private ApiProperties apiProperties;

    /**
     * 创建RestAssured请求规范
     */
    @Bean
    public RequestSpecification requestSpecification() {
        logger.info("Initializing REST Assured with root URL: {}", apiProperties.getRootUrl());

        // 配置RestAssured
        RestAssured.baseURI = apiProperties.getRootUrl();
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", apiProperties.getTest().getTimeoutSeconds() * 1000)
                        .setParam("http.socket.timeout", apiProperties.getTest().getTimeoutSeconds() * 1000));

        // 创建请求规范
        RequestSpecification spec = given()
                .relaxedHTTPSValidation();

        // 添加全局headers
        apiProperties.getHeaders().forEach((key, value) -> {
            logger.debug("Adding header: {} = {}", key, value);
            spec.header(key, value);
        });

        return spec;
    }

    /**
     * 获取API配置属性
     */
    @Bean
    public ApiProperties apiProperties() {
        return apiProperties;
    }

    /**
     * 初始化后日志
     */
    @Bean
    public String apiConfigInitializer() {
        logger.info("=== API Test Configuration ===");
        logger.info("Project Name: {}", apiProperties.getProjectName());
        logger.info("Root URL: {}", apiProperties.getRootUrl());
        logger.info("Headers: {}", apiProperties.getHeaders());
        logger.info("Params: {}", apiProperties.getParams());
        logger.info("Database Enabled: {}", apiProperties.getDatabase().isEnabled());
        logger.info("Mock Enabled: {}", apiProperties.getMock().isEnabled());
        logger.info("Parallel Threads: {}", apiProperties.getTest().getParallelThreads());
        logger.info("==============================");
        return "initialized";
    }
}
