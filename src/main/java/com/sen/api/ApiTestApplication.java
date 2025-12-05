package com.sen.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * API自动化测试平台主启动类
 *
 * @author sen
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.sen.api")
public class ApiTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiTestApplication.class, args);
        System.out.println("====================================");
        System.out.println("API自动化测试平台启动成功！");
        System.out.println("====================================");
    }
}
