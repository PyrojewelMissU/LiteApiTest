# API自动化测试平台 - 项目分析报告

> 更新日期: 2025-12-07
> 版本: 3.1.0 (功能增强版)

---

## 目录

1. [项目概述](#1-项目概述)
2. [V3.1.0 新增功能](#2-v310-新增功能)
3. [重构更新日志](#3-重构更新日志)
4. [功能清单与使用场景](#4-功能清单与使用场景)
5. [技术栈](#5-技术栈)
6. [项目架构](#6-项目架构)
7. [核心类说明](#7-核心类说明)
8. [配置说明](#8-配置说明)
9. [断言语法](#9-断言语法)
10. [使用指南](#10-使用指南)

---

## 1. 项目概述

本项目是一个基于Spring Boot的API自动化测试平台，采用数据驱动的测试方法。核心功能是从JSON/YAML/CSV/Excel文件中读取测试用例，自动执行HTTP请求并进行响应验证。

### 核心特点

- **统一架构**: 基于Spring Boot，移除遗留依赖
- **数据驱动**: 测试用例与代码分离，支持多种数据格式
- **增强断言**: 支持 =, !=, >, >=, <, <=, ~= 等多种运算符
- **多环境管理**: 支持 dev/test/stage/prod 环境切换
- **Token自动化**: Token自动获取与刷新机制
- **API调用链**: 支持复杂业务流程自动化
- **YAML DSL**: QA无需写Java即可编写用例
- **Mock增强**: 支持异常场景、AB实验等高级Mock
- **现代报告**: 统一使用Allure报告

---

## 2. V3.1.0 新增功能

### 2.1 多环境管理（ENV Center）

**文件位置**: `src/main/java/com/sen/api/configs/EnvCenter.java`

**功能特性**:
- 支持 dev / test / stage / prod 四种环境
- 通过命令参数切换：`-Denv=test`
- 不同环境的 URL / token / headers 自动切换

**使用方式**:
```bash
# 运行测试时切换环境
mvn clean test -Denv=test
mvn clean test -Denv=prod
```

```java
// 代码中使用
EnvCenter envCenter = EnvCenter.getInstance();
envCenter.init("env-config.yml");
envCenter.switchEnvironment("test");
String rootUrl = envCenter.getRootUrl();
```

**配置文件**: `src/main/resources/env-config.yml`

---

### 2.2 Token 自动获取与刷新机制

**文件位置**: `src/main/java/com/sen/api/utils/TokenManager.java`

**功能特性**:
- 自动登录获取 Token
- Token 缓存池管理
- Token 过期自动刷新（提前60秒刷新）
- 多账号支持（admin/user/guest…）

**使用方式**:
```java
TokenManager tokenManager = TokenManager.getInstance();
tokenManager.init("https://api.example.com");

// 获取Token（自动登录/刷新）
String token = tokenManager.getToken("admin");

// 切换账号
tokenManager.switchAccount("user");
```

---

### 2.3 API 调用链执行器

**文件位置**: `src/main/java/com/sen/api/utils/ApiFlowExecutor.java`

**功能特性**:
- 支持链式调用：login → getUser → createOrder → pay
- 步骤间数据共享
- 支持步骤重试和延迟
- 成功/失败回调

**使用方式**:
```java
ApiFlowExecutor flow = ApiFlowExecutor.create("订单流程")
    .rootUrl("https://api.example.com")
    .login("/api/login", "admin", "password", "$.token", "token")
    .post("创建订单", "/api/order", "{\"productId\":1}", "orderId", "$.orderId")
    .get("查询订单", "/api/order/${orderId}");

ApiFlowExecutor.FlowResult result = flow.execute();
```

---

### 2.4 YAML DSL 测试引擎

**文件位置**: `src/main/java/com/sen/api/utils/YamlDslTestEngine.java`

**功能特性**:
- QA 无需编写 Java 代码
- 纯 YAML 格式编写测试用例
- 支持变量引用和函数调用

**用例格式** (`data/dsl-test-cases.yml`):
```yaml
- name: 获取用户信息
  api: /user/get
  method: GET
  params:
    id: 1
  validate:
    - status: 200
    - $.code: 0
    - $.data.name: notEmpty
  save:
    userName: $.data.name
```

**使用方式**:
```java
YamlDslTestEngine engine = YamlDslTestEngine.getInstance();
engine.initFromEnvCenter();
YamlDslTestEngine.DslSuiteResult result = engine.runFromFile("data/dsl-test-cases.yml");
```

---

### 2.5 Mock 能力增强

**文件位置**: `src/main/java/com/sen/api/utils/MockUtil.java`

**新增功能**:
- Mock 接口未开发
- Mock 异常场景（500/502/503/504/Timeout）
- Mock AB 实验
- 场景管理（保存/加载）

**使用方式**:
```java
// Mock 500错误
MockUtil.stubInternalServerError("/api/error");

// Mock 超时
MockUtil.stubTimeout("/api/slow", 30000);

// AB实验
MockUtil.ABExperiment experiment = MockUtil.createABExperiment("test", "/api/feature")
    .addVariant("A", "{\"version\":\"A\"}", 200, 70)
    .addVariant("B", "{\"version\":\"B\"}", 200, 30);
MockUtil.applyABExperiment("test");

// 从YAML加载Mock
MockUtil.loadMockFromYaml("mock-config.yml");
```

---

### 2.6 日志体系优化

**文件位置**:
- `src/main/java/com/sen/api/utils/LogEnhancer.java`
- `src/main/resources/logback.xml`

**功能特性**:
- 请求/响应日志
- traceId 追踪
- 失败用例日志特殊标记
- 分离日志文件

**日志文件**:
| 文件 | 说明 |
|------|------|
| logs/api-test.log | 所有日志 |
| logs/api-test-error.log | 错误日志 |
| logs/api-test-failed.log | 失败用例日志 |
| logs/api-test-http.log | HTTP请求响应日志 |

**使用方式**:
```java
LogEnhancer.startTestCase("用例名称");
LogEnhancer.logRequest("POST", "/api/users", headers, body);
LogEnhancer.logResponse(200, responseHeaders, responseBody, durationMs);
LogEnhancer.endTestCase("用例名称", true);
```

---

### 2.7 新增文件清单

| 文件 | 说明 |
|------|------|
| `src/main/java/com/sen/api/configs/EnvCenter.java` | 多环境管理中心 |
| `src/main/java/com/sen/api/utils/TokenManager.java` | Token自动管理 |
| `src/main/java/com/sen/api/utils/ApiFlowExecutor.java` | API调用链执行器 |
| `src/main/java/com/sen/api/utils/YamlDslTestEngine.java` | YAML DSL引擎 |
| `src/main/java/com/sen/api/utils/LogEnhancer.java` | 日志增强工具 |
| `src/main/resources/env-config.yml` | 环境配置文件 |
| `src/main/resources/mock-config.yml` | Mock配置文件 |
| `data/dsl-test-cases.yml` | DSL测试用例示例 |

---

## 3. 重构更新日志

### V3.1.0 (2025-12-07) - 功能增强版

#### 新增功能

| 功能 | 说明 |
|------|------|
| 多环境管理（ENV Center） | 支持 dev/test/stage/prod 环境切换 |
| Token自动管理 | 自动获取、缓存、刷新Token |
| API调用链执行器 | 支持复杂业务流程自动化 |
| YAML DSL引擎 | QA无需写Java即可编写用例 |
| Mock能力增强 | 异常场景、AB实验、场景管理 |
| 日志体系优化 | traceId追踪、失败标记、分离日志 |

### V3.0.0 (2025-12-06) - 架构重构版

#### 移除的内容

| 项目 | 说明 |
|------|------|
| `ApiTest.java` | 移除遗留Excel测试类 |
| `ExtentTestNGIReporterListener.java` | 移除ExtentReports监听器 |
| `api-config.xml` | 移除XML配置，统一使用YAML |
| `testng.xml` | 移除遗留TestNG配置 |
| `RandomStrArrFucntion.java` | 修复为正确拼写 |
| Guice依赖 | 移除，统一使用Spring |
| ExtentReports依赖 | 移除，统一使用Allure |

#### 新增的内容

| 项目 | 说明 |
|------|------|
| `DataReader.java` | 统一的数据读取器接口 |
| `RandomStrArrFunction.java` | 修正拼写的随机数组函数 |
| `ApiTestException.java` | API测试异常基类 |
| `ConfigurationException.java` | 配置异常类 |
| `DataReadException.java` | 数据读取异常类 |
| `VerificationException.java` | 验证异常类 |

#### 增强的功能

| 功能 | 说明 |
|------|------|
| **断言增强** | 支持 !=, >, >=, <, <=, ~=, :exist, :null, :in 等操作符 |
| **超时控制** | 可配置connect/read/write超时 |
| **标签管理** | ApiDataBean新增tags, group, priority字段 |
| **配置统一** | 全部使用YAML配置 |

---

## 3. 功能清单与使用场景

### 3.1 HTTP请求功能

| 功能 | 描述 | 使用场景 |
|------|------|----------|
| GET请求 | 支持查询参数 | 查询接口测试、资源获取 |
| POST请求 | 支持JSON/Form-data | 创建资源、表单提交 |
| PUT请求 | JSON请求体 | 更新资源 |
| DELETE请求 | URL参数 | 删除资源 |
| 文件上传 | Multipart | 文件上传接口测试 |
| HTTPS支持 | SSL/TLS | 安全接口测试 |

### 3.2 数据源支持

| 格式 | 文件位置 | 推荐程度 |
|------|----------|----------|
| JSON | `data/*.json` | ★★★★★ 推荐 |
| YAML | `data/*.yml` | ★★★★☆ |
| CSV | `data/*.csv` | ★★★☆☆ |
| Excel | `case/*.xls` | ★★☆☆☆ |

### 3.3 动态函数 (14个)

| 函数 | 语法 | 用途 |
|------|------|------|
| `__random` | `__random(8,true)` | 随机字符串 |
| `__date` | `__date(yyyy-MM-dd)` | 日期格式化 |
| `__md5` | `__md5(text)` | MD5哈希 |
| `__base64` | `__base64(text)` | Base64编码 |
| `__aes` | `__aes(data,key)` | AES加密 |
| `__rsa` | `__rsa(data,pubkey)` | RSA加密 |
| `__sha` | `__sha(text,SHA256)` | SHA哈希 |
| `__plus` | `__plus(1,2,3)` | 加法 |
| `__sub` | `__sub(10,3)` | 减法 |
| `__multi` | `__multi(100,0.8)` | 乘法 |
| `__max` | `__max(1,5,3)` | 最大值 |
| `__randomText` | `__randomText(10)` | 中文随机文本 |
| `__randomStrArr` | `__randomStrArr(3,8,true)` | 随机字符串数组 |

---

## 4. 技术栈

### 4.1 核心框架

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 编程语言 |
| Spring Boot | 3.2.1 | 应用框架 |
| TestNG | 7.8.0 | 测试框架 |
| REST Assured | 5.4.0 | HTTP客户端 |
| Maven | 3.x | 构建工具 |

### 4.2 数据处理

| 组件 | 版本 | 用途 |
|------|------|------|
| Jackson | 2.16.1 | JSON/YAML处理 |
| Apache POI | 5.2.5 | Excel读写 |
| Jayway JsonPath | 2.9.0 | JSON查询 |

### 4.3 测试与报告

| 组件 | 版本 | 用途 |
|------|------|------|
| Allure | 2.25.0 | 测试报告 |
| WireMock | 3.3.1 | API Mock |
| JUnit 5 | 5.10.1 | 单元测试 |

### 4.4 已移除的依赖

| 组件 | 原因 |
|------|------|
| ExtentReports | 统一使用Allure |
| Google Guice | 统一使用Spring |
| DOM4J | 不再需要解析XML配置 |

---

## 5. 项目架构

### 5.1 目录结构

```
autotest/
├── src/main/java/com/sen/api/
│   ├── ApiTestApplication.java      # Spring Boot入口
│   ├── beans/
│   │   ├── ApiDataBean.java         # 测试数据Bean (含标签、分组、优先级)
│   │   └── BaseBean.java
│   ├── configs/
│   │   ├── ApiConfig.java           # YAML配置加载
│   │   ├── ApiConfiguration.java    # Spring配置
│   │   └── ApiProperties.java       # 配置属性 (含超时配置)
│   ├── exceptions/                  # 异常类
│   │   ├── ApiTestException.java    # 基础异常
│   │   ├── ConfigurationException.java
│   │   ├── DataReadException.java
│   │   ├── ErrorRespStatusException.java
│   │   └── VerificationException.java
│   ├── functions/                   # 动态函数 (14个)
│   │   ├── Function.java            # 函数接口
│   │   ├── RandomStrArrFunction.java # 修正拼写
│   │   └── ...
│   ├── listeners/                   # TestNG监听器
│   │   ├── AutoTestListener.java
│   │   ├── RetryListener.java
│   │   └── TestngRetry.java
│   └── utils/
│       ├── DataReader.java          # 数据读取器接口
│       ├── DataReaderFactory.java   # 数据源工厂
│       ├── AssertUtil.java          # 增强版断言工具
│       ├── RestAssuredUtil.java     # HTTP请求
│       └── ...
├── src/test/java/test/com/sen/api/
│   ├── ApiTestModern.java          # 测试入口
│   └── TestBase.java               # 测试基类
├── data/                           # JSON/YAML测试数据
│   ├── api-test-data.json
│   └── api-test-data.yml
├── api-config.yml                  # API配置 (YAML)
├── testng-modern.xml               # TestNG配置
└── pom.xml                         # Maven配置
```

### 5.2 执行流程

```
┌─────────────────┐
│  加载YAML配置    │  api-config.yml
└────────┬────────┘
         ↓
┌─────────────────┐
│  读取测试数据    │  JSON/YAML/CSV/Excel
└────────┬────────┘
         ↓
┌─────────────────┐
│  预处理参数      │  执行preParam函数
└────────┬────────┘
         ↓
┌─────────────────┐
│  参数替换        │  ${param} → 实际值
└────────┬────────┘
         ↓
┌─────────────────┐
│  发送HTTP请求   │  REST Assured
└────────┬────────┘
         ↓
┌─────────────────┐
│  验证响应       │  状态码 + 增强断言
└────────┬────────┘
         ↓
┌─────────────────┐
│  提取数据保存   │  save字段 → 参数池
└────────┬────────┘
         ↓
┌─────────────────┐
│  生成Allure报告 │
└─────────────────┘
```

---

## 6. 核心类说明

### 6.1 测试执行类

| 类 | 功能 |
|----|------|
| `ApiTestModern` | 测试入口，使用JSON/YAML数据 |
| `TestBase` | 参数替换、验证逻辑、数据提取 |

### 6.2 数据模型

**ApiDataBean 字段说明：**

| 字段 | 类型 | 说明 |
|------|------|------|
| run | boolean | 是否执行 |
| desc | String | 测试描述 |
| url | String | 请求URL |
| method | String | HTTP方法 |
| param | String | 请求参数(JSON) |
| status | int | 预期状态码 |
| verify | String | 验证表达式 |
| save | String | 数据提取 |
| preParam | String | 前置参数 |
| sleep | int | 等待秒数 |
| preSql | String | 前置SQL |
| postSql | String | 后置SQL |
| dbVerify | String | 数据库验证 |
| dependsOn | String | 依赖用例 |
| **tags** | String | 标签(逗号分隔) |
| **group** | String | 分组 |
| **priority** | int | 优先级(1-5) |

### 6.3 异常类

| 类 | 说明 |
|----|------|
| `ApiTestException` | API测试异常基类 |
| `ConfigurationException` | 配置文件异常 |
| `DataReadException` | 数据读取异常 |
| `VerificationException` | 验证失败异常 |
| `ErrorRespStatusException` | HTTP状态码异常 |

---

## 7. 配置说明

### 7.1 api-config.yml

```yaml
api:
  rootUrl: https://api.example.com
  headers:
    Content-Type: application/json
    Accept: application/json
  params:
    test_env: production

project:
  name: API自动化测试平台
  version: 3.0
```

### 7.2 application.yml (超时配置)

```yaml
api:
  test:
    timeout:
      connect: 5000      # 连接超时 5秒
      read: 30000        # 读取超时 30秒
      write: 30000       # 写入超时 30秒
      connection-request: 5000  # 连接池超时 5秒
```

---

## 8. 断言语法

### 8.1 支持的操作符

| 操作符 | 说明 | 示例 |
|--------|------|------|
| `=` / `==` | 相等 | `$.code=0` |
| `!=` / `<>` | 不相等 | `$.code!=1` |
| `>` | 大于 | `$.count>0` |
| `>=` | 大于等于 | `$.count>=1` |
| `<` | 小于 | `$.count<100` |
| `<=` | 小于等于 | `$.count<=50` |
| `~=` | 正则匹配 | `$.name~=^test.*` |
| `:exist` | 字段存在 | `$.id:exist` |
| `:null` | 字段为null | `$.deleted:null` |
| `:notNull` | 字段不为null | `$.id:notNull` |
| `:empty` | 字段为空 | `$.name:empty` |
| `:notEmpty` | 字段不为空 | `$.name:notEmpty` |
| `:in` | 在列表中 | `$.type:in[A,B,C]` |
| `:notIn` | 不在列表中 | `$.type:notIn[X,Y]` |

### 8.2 使用示例

```json
{
  "verify": "$.code=0;$.data.id>0;$.data.name~=^test.*;$.data.type:in[A,B,C]"
}
```

---

## 9. 使用指南

### 9.1 基本命令

```bash
# 编译项目
mvn clean compile

# 运行所有测试
mvn clean test

# 运行单个测试类
mvn clean test -Dtest=ApiTestModern

# 指定环境运行
mvn clean test -Dspring.profiles.active=prod

# 生成Allure报告
mvn allure:report
allure serve target/allure-results
```

### 9.2 测试数据示例 (JSON)

```json
[
  {
    "run": true,
    "desc": "获取用户列表",
    "url": "/users",
    "method": "GET",
    "status": 200,
    "verify": "$[0].id>0;$[0].name:notEmpty",
    "save": "firstUserId=$[0].id",
    "tags": "smoke,user,P0",
    "group": "user-api",
    "priority": 1
  },
  {
    "run": true,
    "desc": "创建新用户",
    "url": "/users",
    "method": "POST",
    "preParam": "randomEmail=__random(8)@test.com",
    "param": "{\"name\":\"Test\",\"email\":\"${randomEmail}\"}",
    "status": 201,
    "verify": "$.name=Test;$.id>0",
    "save": "newUserId=$.id",
    "tags": "smoke,user,P0",
    "group": "user-api",
    "priority": 1
  }
]
```

### 9.3 测试数据示例 (YAML)

```yaml
- run: true
  desc: 获取用户列表
  url: /users
  method: GET
  status: 200
  verify: "$[0].id>0;$[0].name:notEmpty"
  save: "firstUserId=$[0].id"
  tags: "smoke,user,P0"
  group: "user-api"
  priority: 1

- run: true
  desc: 创建新用户
  url: /users
  method: POST
  preParam: "randomEmail=__random(8)@test.com"
  param: '{"name":"Test","email":"${randomEmail}"}'
  status: 201
  verify: "$.name=Test;$.id>0"
  save: "newUserId=$.id"
  tags: "smoke,user,P0"
  group: "user-api"
  priority: 1
```

---

## 总结

### 版本演进

| 版本 | 日期 | 主要更新 |
|------|------|----------|
| V3.1.0 | 2025-12-07 | 多环境管理、Token自动化、API调用链、YAML DSL、Mock增强、日志优化 |
| V3.0.0 | 2025-12-06 | 架构统一、YAML配置、Allure报告、断言增强 |
| V2.0.0 | - | 现代化技术栈重构 |
| V1.0.0 | - | 初始版本 |

### V3.1.0 版本改进总结

1. **多环境管理**: 支持 dev/test/stage/prod 环境一键切换
2. **Token自动化**: 自动登录、缓存、刷新，支持多账号
3. **API调用链**: 支持复杂业务流程自动化测试
4. **YAML DSL**: QA无需写Java代码即可编写测试用例
5. **Mock增强**: 支持异常场景、AB实验、场景管理
6. **日志优化**: traceId追踪、失败标记、分离日志文件

---

> 报告更新时间: 2025-12-07
> 版本: 3.1.0
