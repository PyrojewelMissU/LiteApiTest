# API 自动化测试平台 - 项目分析报告

## 一、项目概述

**项目名称**: API 自动化测试平台（API Automation Testing Platform）
**技术栈**: Spring Boot 3.2.1 + TestNG 7.8.0 + REST Assured 5.4.0 + Allure 2.25.0
**Java版本**: Java 17

---

## 二、项目实现的具体功能

### 2.1 核心功能

| 功能模块 | 具体能力 |
|---------|---------|
| **多格式数据驱动** | 支持 JSON、YAML、CSV、Excel 格式的测试用例数据 |
| **HTTP 请求执行** | 支持 GET/POST/PUT/DELETE 方法，支持 JSON/Form-data/文件上传 |
| **参数化机制** | `${paramName}` 变量引用，跨用例参数传递 |
| **动态函数** | `__random()` `__date()` `__md5()` 等内置函数动态生成数据 |
| **响应验证** | JsonPath 表达式验证、包含验证、状态码验证 |
| **数据提取** | 从响应中提取数据保存到公共池供后续用例使用 |
| **测试报告** | Allure 现代化报告 + ExtentReports 传统报告 |
| **失败重试** | 测试失败自动重试机制 |
| **数据库断言** | 支持测试前后数据库验证 |
| **多环境切换** | 支持 dev/test/prod 多环境配置 |

### 2.2 内置函数列表

| 函数 | 用途 | 示例 |
|-----|------|------|
| `__date(format)` | 日期格式化 | `__date(yyyy-MM-dd)` |
| `__random(length,num)` | 随机字符串 | `__random(10,false)` |
| `__md5(text)` | MD5 加密 | `__md5(password)` |
| `__aes(text,key)` | AES 加密 | `__aes(data,secret)` |
| `__rsa(text,key)` | RSA 加密 | `__rsa(data,publicKey)` |
| `__plus(a,b)` | 加法运算 | `__plus(10,20)` |
| `__sub(a,b)` | 减法运算 | `__sub(100,30)` |
| `__multi(a,b)` | 乘法运算 | `__multi(5,10)` |
| `__max(a,b)` | 最大值 | `__max(50,100)` |
| `__bodyfile(path)` | 文件引用 | `__bodyfile(path/to/file)` |

---

## 三、适用场景

| 场景 | 说明 |
|-----|------|
| **接口回归测试** | 每次发版前批量执行接口用例，确保功能正常 |
| **接口冒烟测试** | 快速验证核心接口是否可用 |
| **持续集成(CI/CD)** | 集成到 Jenkins/GitLab CI 中自动执行 |
| **接口联调测试** | 前后端分离项目中验证后端接口 |
| **Mock 测试** | 配合 WireMock 服务进行隔离测试 |
| **性能基线测试** | 作为性能测试的基础用例库 |
| **数据库集成测试** | 验证接口与数据库交互的正确性 |
| **新人培训** | 作为接口测试入门学习项目 |

---

## 四、技术栈

| 层级 | 技术 | 版本 | 用途 |
|-----|------|------|------|
| **框架** | Spring Boot | 3.2.1 | 应用框架 |
| **测试框架** | TestNG | 7.8.0 | 测试组织和执行 |
| **HTTP 客户端** | REST Assured | 5.4.0 | API 请求发送 |
| **JSON 处理** | Jackson | 2.16.1 | JSON 解析 |
| **YAML 处理** | SnakeYAML | 2.2 | YAML 解析 |
| **Excel 处理** | Apache POI | 5.2.5 | Excel 数据读取 |
| **测试报告** | Allure | 2.25.0 | 现代化报告 |
| **Mock 服务** | WireMock | 3.3.1 | 接口 Mock |
| **数据库** | MySQL + MyBatis | 8.2.0 / 3.0.3 | 数据库访问 |
| **日志** | Logback | - | 日志记录 |
| **代码检查** | Checkstyle + SpotBugs | - | 代码质量 |

---

## 五、架构模式

### 5.1 整体架构

```
┌──────────────────────────────────────────────────────────┐
│                    测试执行层                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐   │
│  │ ApiTestModern│  │   ApiTest   │  │    TestBase     │   │
│  │   (推荐)     │  │   (遗留)    │  │    (基类)       │   │
│  └──────┬──────┘  └──────┬──────┘  └────────┬────────┘   │
└─────────┼────────────────┼──────────────────┼────────────┘
          │                │                  │
┌─────────▼────────────────▼──────────────────▼────────────┐
│                    工具层                                 │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐            │
│  │RestAssured │ │FunctionUtil│ │ AssertUtil │            │
│  │   Util     │ │ (动态函数) │ │  (断言)    │            │
│  └────────────┘ └────────────┘ └────────────┘            │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐            │
│  │  DbAssert  │ │ CryptoUtil │ │ MockUtil   │            │
│  │ (数据库断言)│ │ (加密工具) │ │ (Mock工具) │            │
│  └────────────┘ └────────────┘ └────────────┘            │
└──────────────────────────────────────────────────────────┘
          │
┌─────────▼────────────────────────────────────────────────┐
│                    数据层                                 │
│  ┌────────────────────────────────────────────────────┐  │
│  │              DataReaderFactory                      │  │
│  │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────────┐   │  │
│  │  │  JSON  │ │  YAML  │ │  CSV   │ │   Excel    │   │  │
│  │  │ Reader │ │ Reader │ │ Reader │ │   Util     │   │  │
│  │  └────────┘ └────────┘ └────────┘ └────────────┘   │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
          │
┌─────────▼────────────────────────────────────────────────┐
│                    配置层                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────┐  │
│  │ ApiProperties│  │ DbProperties │  │ EnvProperties  │  │
│  │ (API配置)    │  │ (数据库配置)  │  │ (环境配置)     │  │
│  └──────────────┘  └──────────────┘  └────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

### 5.2 设计模式

| 模式 | 应用场景 |
|-----|---------|
| **数据驱动模式** | 测试逻辑与测试数据分离 |
| **工厂模式** | DataReaderFactory 统一数据读取 |
| **策略模式** | 不同数据格式使用不同读取策略 |
| **模板方法模式** | TestBase 定义测试流程骨架 |
| **依赖注入** | Spring @Configuration 管理配置 |

---

## 六、目录结构

```
autotest/
├── src/
│   ├── main/java/com/sen/api/
│   │   ├── ApiTestApplication.java      # Spring Boot 启动类
│   │   ├── beans/                       # 数据模型
│   │   │   ├── ApiDataBean.java         # API测试数据Bean
│   │   │   └── BaseBean.java            # 基础Bean
│   │   ├── configs/                     # 配置类
│   │   │   ├── ApiProperties.java       # API配置属性
│   │   │   ├── ApiConfiguration.java    # API配置类
│   │   │   └── EnvConfiguration.java    # 环境配置类
│   │   ├── exceptions/                  # 异常类
│   │   │   └── ErrorRespStatusException.java
│   │   ├── functions/                   # 动态函数
│   │   │   ├── Function.java            # 函数接口
│   │   │   ├── DateFunction.java        # 日期函数
│   │   │   ├── RandomFunction.java      # 随机函数
│   │   │   ├── Md5Function.java         # MD5函数
│   │   │   ├── AesFunction.java         # AES加密函数
│   │   │   └── RsaFunction.java         # RSA加密函数
│   │   ├── listeners/                   # 测试监听器
│   │   │   ├── AutoTestListener.java
│   │   │   └── RetryListener.java
│   │   └── utils/                       # 工具类
│   │       ├── RestAssuredUtil.java     # HTTP请求工具
│   │       ├── JsonUtil.java            # JSON工具
│   │       ├── StringUtil.java          # 字符串工具
│   │       ├── DbAssertUtil.java        # 数据库断言工具
│   │       ├── CryptoUtil.java          # 加密工具
│   │       └── MockUtil.java            # Mock工具
│   ├── main/resources/
│   │   ├── application.yml              # 主配置
│   │   ├── application-dev.yml          # 开发环境
│   │   ├── application-test.yml         # 测试环境
│   │   └── application-prod.yml         # 生产环境
│   └── test/java/test/com/sen/api/
│       ├── ApiTestModern.java           # 现代测试类
│       ├── TestBase.java                # 测试基类
│       └── unit/                        # 单元测试
├── data/                                # 测试数据
│   ├── api-test-data.json
│   └── api-test-data.yml
├── testng-modern.xml                    # TestNG配置(并行)
├── Dockerfile                           # Docker配置
├── checkstyle.xml                       # 代码规范配置
├── spotbugs-exclude.xml                 # SpotBugs排除配置
└── pom.xml                              # Maven配置
```

---

## 七、配置说明

### 7.1 多环境配置

```yaml
# application.yml
spring:
  profiles:
    active: ${ENV:dev}  # 默认开发环境

# 通过命令行切换环境
mvn test -Dspring.profiles.active=test
mvn test -Dspring.profiles.active=prod
```

### 7.2 数据库配置

```yaml
# application-dev.yml
api:
  db:
    enabled: true
    url: jdbc:mysql://localhost:3306/test_dev
    username: root
    password: 123456
```

### 7.3 TestNG 并行配置

```xml
<suite name="API Test Suite" parallel="methods" thread-count="5">
    <test name="API Tests">
        <classes>
            <class name="test.com.sen.api.ApiTestModern"/>
        </classes>
    </test>
</suite>
```

---

## 八、使用指南

### 8.1 基本命令

```bash
# 编译项目
mvn clean compile

# 运行测试（默认开发环境）
mvn clean test

# 指定环境运行
mvn clean test -Dspring.profiles.active=test

# 生成报告
allure serve target/allure-results

# Docker 运行
docker build -t api-test .
docker run api-test
```

### 8.2 测试数据示例

```json
{
  "run": true,
  "desc": "创建用户",
  "url": "/users",
  "method": "POST",
  "param": "{\"name\":\"${userName}\",\"email\":\"test@example.com\"}",
  "verify": "$.id!=null;$.name=${userName}",
  "save": "userId=$.id",
  "preSql": "DELETE FROM users WHERE email='test@example.com'",
  "postSql": "SELECT * FROM users WHERE id=${userId}",
  "dbVerify": "$.name=${userName}"
}
```

---

## 九、扩展开发

### 9.1 自定义函数

```java
@Component
public class CustomFunction implements Function {
    @Override
    public String execute(String[] args) {
        // 实现自定义逻辑
        return result;
    }

    @Override
    public String getReferenceKey() {
        return "custom";  // 使用 __custom(args) 调用
    }
}
```

### 9.2 自定义数据读取器

```java
@Component
public class CustomDataReader implements DataReader {
    @Override
    public List<ApiDataBean> read(String path) {
        // 实现读取逻辑
    }

    @Override
    public boolean supports(String path) {
        return path.endsWith(".custom");
    }
}
```

---

## 十、版本历史

| 版本 | 日期 | 更新内容 |
|-----|------|---------|
| v2.1 | 2025-12 | 移除FastJSON，统一Jackson；添加数据库断言；多环境支持 |
| v2.0 | 2025-xx | 现代化重构，支持JSON/YAML数据源 |
| v1.0 | 2025-xx | 初始版本，Excel数据驱动 |

---

## 十一、贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 十二、许可证

MIT License
