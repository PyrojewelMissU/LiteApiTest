# API 自动化测试平台 - 技术栈现代化完成指南

## 概述
本项目已成功完成从旧技术栈到现代技术栈的全面升级，现在使用 **RestAssured + Jackson + Allure + SLF4J/Logback** 等现代化工具。

## 技术栈对比

### 请求库升级
- **旧**: Apache HttpClient 4.5.14 (手动管理连接、请求配置复杂)
- **新**: REST Assured 5.4.0 (流式API、自动管理、内置断言)

### JSON处理升级
- **旧**: FastJSON 1.2.83 (安全漏洞、维护缓慢)
- **新**: Jackson 2.16.1 + Jayway JsonPath 2.9.0 (行业标准、安全可靠)

### 测试报告升级
- **旧**: ExtentReports 5.1.1 (功能有限)
- **新**: Allure Report 2.25.0 (现代化、功能强大、集成度高)

### 数据驱动升级
- **旧**: 仅支持 Excel (POI 5.2.5)
- **新**: 支持 Excel / CSV / YAML / JSON 多种格式

### 日志系统升级
- **旧**: System.out.println + log4j (过时)
- **新**: SLF4J + Logback (行业标准、性能优秀)

---

## 新增的工具类

### 1. 数据读取工具类

#### `YamlDataReader.java`
用于读取YAML格式的测试数据
```java
// 读取YAML文件为对象列表
List<ApiDataBean> data = YamlDataReader.readYaml(ApiDataBean.class, "data/test-data.yaml");

// 读取为Map
Map<String, Object> config = YamlDataReader.readYamlAsMap("config.yaml");
```

#### `JsonDataReader.java`
用于读取JSON格式的测试数据
```java
// 读取JSON文件
List<ApiDataBean> data = JsonDataReader.readJson(ApiDataBean.class, "data/test-data.json");

// 写入JSON文件
JsonDataReader.writeJson(data, "output.json");
```

#### `CsvDataReader.java`
用于读取CSV格式的测试数据
```java
// 读取CSV文件
List<ApiDataBean> data = CsvDataReader.readCsv(ApiDataBean.class, "data/test-data.csv");

// 读取为Map列表
List<Map<String, String>> mapData = CsvDataReader.readCsvAsMap("data/test-data.csv");
```

#### `DataReaderFactory.java`
统一的数据读取入口，自动识别文件格式
```java
// 自动识别格式并读取
List<ApiDataBean> data = DataReaderFactory.readData(ApiDataBean.class, "data/test-data.json");

// 支持的格式: .json, .yaml, .yml, .csv, .xls, .xlsx
```

### 2. REST Assured 工具类

#### `RestAssuredUtil.java`
封装常见的HTTP请求操作
```java
// 创建请求规范
RequestSpecification spec = RestAssuredUtil.createBaseSpec(baseUrl, headers);

// GET 请求
Response response = RestAssuredUtil.get(spec, "/api/users");

// POST 请求 (JSON)
Response response = RestAssuredUtil.post(spec, "/api/users", userObject);

// POST 请求 (Form data)
Map<String, Object> formData = new HashMap<>();
Response response = RestAssuredUtil.postForm(spec, "/api/login", formData);

// 文件上传
Response response = RestAssuredUtil.postMultipart(spec, "/api/upload",
    formParams, "file", new File("test.jpg"));
```

### 3. JSON工具类

#### `JsonUtil.java`
使用Jackson和JsonPath替代FastJSON
```java
// 使用JsonPath读取值
String userId = JsonUtil.read(jsonString, "$.data.user.id");

// 解析JSON为对象
User user = JsonUtil.parseObject(jsonString, User.class);

// 对象转JSON字符串
String json = JsonUtil.toJsonString(user);

// 验证JSON有效性
boolean isValid = JsonUtil.isValidJson(jsonString);
```

### 4. 现代化测试类

#### `ApiTestModern.java`
使用RestAssured和Allure的现代化测试类
```java
@Listeners({ AutoTestListener.class, RetryListener.class })
public class ApiTestModern extends TestBase {
    // 使用 RestAssured 替代 HttpClient
    // 使用 Jackson 替代 FastJSON
    // 使用 Allure 装饰器增强报告
}
```

---

## 配置文件

### 1. `logback.xml` - 日志配置
位置: `src/main/resources/logback.xml`

特性:
- 控制台输出 + 文件输出
- 按日期和大小滚动日志
- 错误日志单独记录
- 异步日志提升性能

### 2. `allure.properties` - Allure配置
位置: `src/test/resources/allure.properties`

配置项:
```properties
allure.results.directory=target/allure-results
allure.link.issue.pattern=https://github.com/your-org/your-repo/issues/{}
allure.link.tms.pattern=https://github.com/your-org/your-repo/issues/{}
```

---

## 依赖变更

### 新增依赖 (pom.xml)

```xml
<!-- REST Assured -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
</dependency>

<!-- Jackson 全家桶 -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.16.1</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-yaml</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.dataformat</groupId>
    <artifactId>jackson-dataformat-csv</artifactId>
</dependency>

<!-- Jayway JsonPath -->
<dependency>
    <groupId>com.jayway.jsonpath</groupId>
    <artifactId>json-path</artifactId>
    <version>2.9.0</version>
</dependency>

<!-- Allure Report -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-testng</artifactId>
    <version>2.25.0</version>
</dependency>
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-rest-assured</artifactId>
    <version>2.25.0</version>
</dependency>

<!-- Apache Commons CSV -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.10.0</version>
</dependency>

<!-- SLF4J + Logback (Spring Boot自带) -->
<!-- 已由 spring-boot-starter 提供 -->
```

### 可以移除的旧依赖
- Apache HttpClient (已被REST Assured替代)
- Apache HttpMime (已被REST Assured替代)
- FastJSON (已被Jackson替代)
- Apache POI (如果不再使用Excel，可选移除)
- ExtentReports (已被Allure替代)
- ReportNG和Velocity本地JAR (已被Allure替代)

---

## 使用方式对比

### 发送HTTP请求

#### 旧方式 (HttpClient)
```java
HttpClient client = new SSLClient();
RequestConfig config = RequestConfig.custom()
    .setConnectTimeout(60000)
    .setSocketTimeout(60000)
    .build();
HttpPost post = new HttpPost(url);
post.setHeaders(headers);
post.setConfig(config);
post.setEntity(new StringEntity(param, "UTF-8"));
HttpResponse response = client.execute(post);
String responseData = EntityUtils.toString(response.getEntity(), "UTF-8");
```

#### 新方式 (REST Assured)
```java
RequestSpecification spec = RestAssuredUtil.createBaseSpec(baseUrl, headers);
Response response = RestAssuredUtil.post(spec, path, param);
String responseData = response.getBody().asString();
```

### JSON处理

#### 旧方式 (FastJSON)
```java
Map<String, String> map = JSON.parseObject(json, HashMap.class);
String value = JSONPath.read(json, "$.data.id").toString();
```

#### 新方式 (Jackson + JsonPath)
```java
Map<String, String> map = JsonUtil.parseObject(json, HashMap.class);
String value = JsonUtil.read(json, "$.data.id");
```

### 数据读取

#### 旧方式 (仅Excel)
```java
List<ApiDataBean> data = ExcelUtil.readExcel(ApiDataBean.class, "case/api-data.xls");
```

#### 新方式 (多格式支持)
```java
// Excel (保持兼容)
List<ApiDataBean> data = ExcelUtil.readExcel(ApiDataBean.class, "case/api-data.xls");

// JSON
List<ApiDataBean> data = JsonDataReader.readJson(ApiDataBean.class, "case/api-data.json");

// YAML
List<ApiDataBean> data = YamlDataReader.readYaml(ApiDataBean.class, "case/api-data.yaml");

// CSV
List<ApiDataBean> data = CsvDataReader.readCsv(ApiDataBean.class, "case/api-data.csv");

// 自动识别
List<ApiDataBean> data = DataReaderFactory.readData(ApiDataBean.class, "case/api-data.json");
```

### 日志记录

#### 旧方式
```java
System.out.println("Test started");
ReportUtil.log("Processing request");
```

#### 新方式 (SLF4J)
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

private static final Logger logger = LoggerFactory.getLogger(ApiTest.class);

logger.info("Test started");
logger.debug("Processing request with params: {}", params);
logger.error("Request failed", exception);
```

---

## 运行测试

### 运行测试用例
```bash
# 运行所有测试
mvn test

# 运行指定TestNG套件
mvn test -DxmlFileName=testng.xml
```

### 生成Allure报告
```bash
# 运行测试并生成Allure结果
mvn clean test

# 生成Allure HTML报告
mvn allure:report

# 打开Allure报告
mvn allure:serve
```

---

## 测试数据格式示例

### JSON格式 (推荐)
```json
[
  {
    "run": true,
    "desc": "用户登录测试",
    "url": "/api/login",
    "method": "POST",
    "param": "{\"username\":\"admin\",\"password\":\"123456\"}",
    "status": 200,
    "verify": "$.code=0;$.data.token=__isNotEmpty()",
    "save": "token=$.data.token"
  }
]
```

### YAML格式 (可读性强)
```yaml
- run: true
  desc: 用户登录测试
  url: /api/login
  method: POST
  param: |
    {
      "username": "admin",
      "password": "123456"
    }
  status: 200
  verify: "$.code=0;$.data.token=__isNotEmpty()"
  save: "token=$.data.token"
```

### CSV格式 (简单场景)
```csv
run,desc,url,method,param,status,verify,save
true,用户登录测试,/api/login,POST,"{""username"":""admin"",""password"":""123456""}",200,$.code=0,token=$.data.token
```

---

## 迁移步骤

### 1. 使用新的测试类
将测试从 `ApiTest.java` 迁移到 `ApiTestModern.java`:
```java
// 旧的测试类仍然可用，但推荐使用新的
// 修改 testng.xml 指向新的测试类
<class name="test.com.sen.api.ApiTestModern"/>
```

### 2. 准备新格式的测试数据
可以将Excel数据转换为JSON/YAML格式:
```bash
# 手动转换或使用工具转换
# Excel -> JSON/YAML
```

### 3. 更新配置文件
- 检查 `testng.xml` 配置
- 确认 `api-config.xml` 配置
- 调整 `logback.xml` 日志级别

### 4. 运行测试验证
```bash
mvn clean test
mvn allure:serve
```

---

## 优势总结

### 1. 更现代的技术栈
- REST Assured: 行业标准的API测试框架
- Jackson: Java生态最流行的JSON库
- Allure: 最佳的测试报告工具
- SLF4J/Logback: 标准的日志解决方案

### 2. 更好的可维护性
- 代码更简洁易读
- 类型安全的API
- 丰富的IDE支持
- 活跃的社区维护

### 3. 更强的功能
- 多格式数据源支持
- 自动化的请求/响应日志
- 精美的测试报告
- 异步日志提升性能

### 4. 更高的安全性
- 摆脱FastJSON的安全漏洞
- 使用经过充分测试的库
- 及时的安全更新

---

## 下一步优化建议

### 1. 完全移除旧依赖
从 `pom.xml` 中删除:
- Apache HttpClient
- FastJSON
- ExtentReports
- ReportNG/Velocity

### 2. 优化目录结构
```
src/
├── main/
│   ├── java/
│   │   └── com/sen/api/
│   │       ├── beans/      # 数据模型
│   │       ├── configs/    # 配置类
│   │       ├── utils/      # 工具类
│   │       └── listeners/  # 监听器
│   └── resources/
│       ├── application.yml
│       └── logback.xml
└── test/
    ├── java/
    │   └── test/com/sen/api/
    │       ├── ApiTestModern.java
    │       └── TestBase.java
    └── resources/
        ├── allure.properties
        ├── data/           # 测试数据
        │   ├── *.json
        │   ├── *.yaml
        │   └── *.csv
        └── testng.xml
```

### 3. 迁移到YAML配置
将 `api-config.xml` 转换为 `api-config.yml`:
```yaml
api:
  rootUrl: http://localhost:8080
  headers:
    Content-Type: application/json
    Authorization: Bearer ${token}
  params:
    env: test
    version: 1.0
```

### 4. 添加更多Allure装饰器
```java
@Epic("API自动化测试")
@Feature("用户管理")
@Story("用户登录")
@Severity(SeverityLevel.CRITICAL)
@Description("测试用户登录功能")
public void testLogin() {
    // ...
}
```

---

## 常见问题

### Q1: 旧的ApiTest还能用吗？
A: 可以，旧的测试类保持不变，可以继续使用。但推荐逐步迁移到 `ApiTestModern`。

### Q2: 必须使用新的数据格式吗？
A: 不必须，Excel格式仍然支持。但JSON/YAML格式更现代、更易维护。

### Q3: Allure报告如何查看？
A: 运行 `mvn allure:serve` 会自动打开浏览器显示报告。

### Q4: 如何调整日志级别？
A: 编辑 `src/main/resources/logback.xml`，修改对应package的level。

---

## 版本信息

- **现代化完成日期**: 2025-12-05
- **项目版本**: 1.0.0
- **Spring Boot**: 3.2.1
- **JDK**: 17
- **REST Assured**: 5.4.0
- **Jackson**: 2.16.1
- **Allure**: 2.25.0

---

**技术栈现代化完成！** 🎉
项目现已采用行业标准的现代化工具，代码更简洁，功能更强大，维护更容易！
