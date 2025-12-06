
  改进完成总结

  已完成的 16 项任务：

  | #   | 任务                   | 状态  | 说明                                       |
  |-----|----------------------|-----|------------------------------------------|
  | 1   | 保存项目分析文档             | ✅   | 创建 PROJECT_ANALYSIS.md                   |
  | 2   | 移除 FastJSON          | ✅   | 从 ExcelUtil.java 和 pom.xml 中移除           |
  | 3   | 修复包名拼写               | ✅   | excepions → exceptions                   |
  | 4   | 重构工具类                | ✅   | 增强 StringUtil 和 JsonUtil                 |
  | 5   | Spring Configuration | ✅   | 创建 ApiProperties 和 ApiConfiguration      |
  | 6   | 数据库断言                | ✅   | 创建 DbAssertUtil                          |
  | 7   | TestNG 并行执行          | ✅   | 修改 testng-modern.xml                     |
  | 8   | 多环境配置                | ✅   | 创建 application-dev/test/prod.yml         |
  | 9   | 前置/后置 SQL            | ✅   | 在 ApiDataBean 中添加字段                      |
  | 10  | 接口依赖链                | ✅   | 添加 dependsOn 和 caseId 字段                 |
  | 11  | 加密函数                 | ✅   | 创建 CryptoUtil + AES/RSA/SHA 函数类          |
  | 12  | WireMock 集成          | ✅   | 创建 MockUtil                              |
  | 13  | 单元测试                 | ✅   | 创建 StringUtilTest                        |
  | 14  | 代码检查                 | ✅   | 创建 checkstyle.xml 和 spotbugs-exclude.xml |
  | 15  | Docker 支持            | ✅   | 创建 Dockerfile 和 docker-compose.yml       |
  | 16  | Swagger 导入           | ✅   | 创建 SwaggerImportUtil                     |

  新增/修改的文件：

  新增文件：
  - PROJECT_ANALYSIS.md - 项目分析文档
  - src/main/java/com/sen/api/exceptions/ErrorRespStatusException.java
  - src/main/java/com/sen/api/configs/ApiProperties.java
  - src/main/java/com/sen/api/configs/ApiConfiguration.java
  - src/main/java/com/sen/api/utils/DbAssertUtil.java
  - src/main/java/com/sen/api/utils/CryptoUtil.java
  - src/main/java/com/sen/api/utils/MockUtil.java
  - src/main/java/com/sen/api/utils/SwaggerImportUtil.java
  - src/main/java/com/sen/api/functions/AesFunction.java
  - src/main/java/com/sen/api/functions/RsaFunction.java
  - src/main/java/com/sen/api/functions/Base64Function.java
  - src/main/java/com/sen/api/functions/ShaFunction.java
  - src/main/resources/application-dev.yml
  - src/main/resources/application-test.yml
  - src/main/resources/application-prod.yml
  - src/test/java/com/sen/api/utils/StringUtilTest.java
  - Dockerfile
  - docker-compose.yml
  - checkstyle.xml
  - spotbugs-exclude.xml

  修改文件：
  - pom.xml - 更新版本到 2.0.0，移除 FastJSON，添加新依赖
  - src/main/java/com/sen/api/utils/ExcelUtil.java - 使用 Jackson 替代 FastJSON
  - src/main/java/com/sen/api/utils/StringUtil.java - 扩展功能
  - src/main/java/com/sen/api/utils/JsonUtil.java - 扩展功能
  - src/main/java/com/sen/api/beans/ApiDataBean.java - 添加数据库和依赖链字段
  - src/main/java/com/sen/api/listeners/TestngRetry.java - 更新 import
  - src/test/java/test/com/sen/api/ApiTest.java - 更新 import
  - src/main/resources/application.yml - 更新配置结构
  - testng-modern.xml - 添加并行执行配置


使用命令

编译项目
- mvn clean compile

运行测试（默认开发环境）
- mvn clean test

指定环境运行测试
- mvn clean test -Dspring.profiles.active=test
- mvn clean test -Dspring.profiles.active=prod

代码检查
- mvn checkstyle:check
- mvn spotbugs:check

生成 Allure 报告
- allure serve target/allure-results

Docker 运行
- docker-compose up -d

新增功能函数

| 函数                      | 用法示例     |
  |-------------------------|----------|
| __aes(text,key)         | AES加密    |
| __aes(text,key,decrypt) | AES解密    |
| __rsa(text,publicKey)   | RSA加密    |
| __sha(text,256)         | SHA哈希    |
| __base64(text)          | Base64编码 |
