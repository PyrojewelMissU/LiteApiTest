# API 自动化测试平台 - 项目重构说明

## 概述
本项目已成功从 9 年前的 Eclipse + JDK 8 项目重构为现代化的 IDEA + Spring Boot 3.2.1 + JDK 17 项目。

## 项目信息
- **原项目**: Eclipse Maven 项目 (JDK 8)
- **新项目**: IntelliJ IDEA + Spring Boot 3.2.1 + JDK 17
- **构建工具**: Maven
- **测试框架**: TestNG 7.8.0
- **报告框架**: ExtentReports 5.1.1

## 主要变更

### 1. Spring Boot 升级
- **Spring Boot**: 2.7.18 → 3.2.1
- **Java 版本**: JDK 8 → JDK 17
- **Spring Framework**: 5.x → 6.x

### 2. 依赖库更新

#### 核心依赖
- **MyBatis Spring Boot Starter**: 2.2.2 → 3.0.3
- **MySQL Connector**: mysql-connector-java:8.0.33 → mysql-connector-j:8.2.0
- **TestNG**: 6.8 → 7.8.0
- **Apache HttpClient**: 4.2 → 4.5.14
- **FastJSON**: 1.2.13 → 1.2.83
- **POI**: 3.16 → 5.2.5
- **ExtentReports**: 3.0.3 → 5.1.1
- **Google Guice**: 3.0 → 5.1.0
- **XStream**: 1.4.2 → 1.4.20
- **DOM4J**: 1.6.1 → 2.1.4

### 3. 代码适配修改

#### API 更新
1. **HttpClient API**
   - `CoreConnectionPNames` (已废弃) → `RequestConfig`
   - `MultipartEntity` → `MultipartEntityBuilder`
   - `StringBody` 构造器更新

2. **TestNG API**
   - `ITestAnnotation.getRetryAnalyzer()` → `getRetryAnalyzerClass()`

3. **Apache POI API**
   - `Cell.getCellTypeEnum()` → `getCellType()`

4. **ExtentReports API**
   - `ExtentHtmlReporter` → `ExtentSparkReporter`
   - `test.debug()` → `test.info()`
   - Category API 简化处理

### 4. 新增文件

#### Spring Boot 配置
- `src/main/java/com/sen/api/ApiTestApplication.java` - Spring Boot 主启动类
- `src/main/resources/application.yml` - Spring Boot 配置文件

#### IDEA 项目配置
- `.idea/misc.xml` - 项目 JDK 配置 (JDK 17)
- `.idea/compiler.xml` - 编译器配置
- `.idea/codeStyles/codeStyleConfig.xml` - 代码风格配置
- `autotest.iml` - IntelliJ IDEA 模块文件

### 5. 配置文件说明

#### application.yml
```yaml
server:
  port: 8080
  context-path: /api-test

spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/test
    username: root
    password: root

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.sen.api.beans
```

## 如何使用

### 1. 在 IDEA 中打开项目
```bash
File → Open → 选择项目目录
```

### 2. 配置 JDK
- 确保安装了 JDK 17
- File → Project Structure → Project → SDK 选择 JDK 17

### 3. 导入 Maven 依赖
```bash
# Maven 会自动下载依赖，或手动刷新
mvn clean install
```

### 4. 启动 Spring Boot 应用
```bash
# 方式1: Maven 命令
mvn spring-boot:run

# 方式2: 直接运行主类
运行 com.sen.api.ApiTestApplication
```

### 5. 运行测试用例
```bash
# 运行所有测试
mvn test

# 运行指定的 TestNG 套件
mvn test -DxmlFileName=testng.xml
```

## 项目结构
```
autotest/
├── .idea/                          # IntelliJ IDEA 配置目录
│   ├── codeStyles/                 # 代码风格配置
│   ├── compiler.xml                # 编译器配置
│   └── misc.xml                    # 项目 JDK 配置
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sen/api/
│   │   │       ├── beans/          # 数据模型
│   │   │       ├── configs/        # 配置类
│   │   │       ├── exceptions/     # 自定义异常
│   │   │       ├── functions/      # 函数工具
│   │   │       ├── listeners/      # TestNG 监听器
│   │   │       ├── utils/          # 工具类
│   │   │       └── ApiTestApplication.java  # Spring Boot 主类
│   │   └── resources/
│   │       └── application.yml     # Spring Boot 配置
│   └── test/
│       └── java/
│           └── test/com/sen/api/
│               ├── ApiTest.java    # API 测试主类
│               └── TestBase.java   # 测试基类
├── case/                           # 测试用例数据
├── libs/                           # 本地 JAR 包
├── .gitignore                      # Git 忽略文件配置
├── api-config.xml                  # API 配置文件
├── testng.xml                      # TestNG 套件配置
├── pom.xml                         # Maven 配置
├── README.md                       # 原项目说明
└── REFACTOR_GUIDE.md              # 重构指南（本文件）
```

## 已清理的文件
以下 Eclipse 相关的冗余文件已被删除：
- `.classpath` - Eclipse 类路径配置
- `.project` - Eclipse 项目配置
- `.settings/` - Eclipse 设置目录
- `autotest.iml` - IDEA 旧模块文件（已由 Maven 自动管理）
- `pom.xml.backup` - POM 备份文件

## 注意事项

### 1. 数据库配置
请根据实际情况修改 `application.yml` 中的数据库连接信息。

### 2. 测试数据
测试用例数据存放在 `case/api-data.xls` 中，请确保该文件存在。

### 3. 报告生成
测试报告生成在 `test-output/` 目录下，使用 ExtentReports 5.x 格式。

### 4. SSL 支持
项目中保留了 `SSLClient` 类用于 HTTPS 请求，使用了过时的 API，如需更新请参考新版 HttpClient 文档。

### 5. 兼容性
- 需要 JDK 17 或更高版本
- Maven 3.6+ 推荐
- IntelliJ IDEA 2021.3+ 推荐

## 已知问题
1. `SSLClient` 使用了部分过时的 API，建议后续重构
2. `reportng` 和 `velocity` 使用本地 JAR 包，建议迁移到 Maven 中央仓库版本

## 后续优化建议
1. 将本地 JAR 包依赖迁移到 Maven 中央仓库
2. 更新 `SSLClient` 以使用最新的 HttpClient API
3. 添加 Swagger/OpenAPI 文档支持
4. 集成 Spring Boot DevTools 提升开发效率
5. 添加单元测试覆盖率统计
6. 考虑使用 Spring Boot Actuator 进行健康检查

## 版本信息
- **重构日期**: 2025-12-05
- **项目版本**: 1.0.0
- **Spring Boot**: 3.2.1
- **JDK**: 17

---
**重构完成！** 🎉 项目现已可以在 IntelliJ IDEA 中正常启动和运行。
