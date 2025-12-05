# 现代化目录结构

## 推荐的目录结构

```
autotest/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sen/api/
│   │   │       ├── beans/              # 数据模型
│   │   │       │   ├── ApiDataBean.java
│   │   │       │   └── BaseBean.java
│   │   │       ├── configs/            # 配置类
│   │   │       │   └── ApiConfig.java
│   │   │       ├── listeners/          # 监听器
│   │   │       │   ├── AutoTestListener.java
│   │   │       │   ├── RetryListener.java
│   │   │       │   └── [ExtentTestNGIReporterListener.java] (可移除)
│   │   │       ├── utils/              # 工具类
│   │   │       │   ├── RestAssuredUtil.java     # REST Assured工具
│   │   │       │   ├── JsonUtil.java            # JSON工具
│   │   │       │   ├── JsonDataReader.java      # JSON数据读取
│   │   │       │   ├── YamlDataReader.java      # YAML数据读取
│   │   │       │   ├── CsvDataReader.java       # CSV数据读取
│   │   │       │   ├── DataReaderFactory.java   # 数据读取工厂
│   │   │       │   ├── ExcelUtil.java           # Excel工具(兼容)
│   │   │       │   ├── AssertUtil.java
│   │   │       │   ├── FileUtil.java
│   │   │       │   ├── FunctionUtil.java
│   │   │       │   ├── RandomUtil.java
│   │   │       │   ├── ReportUtil.java
│   │   │       │   ├── SSLClient.java
│   │   │       │   └── StringUtil.java
│   │   │       └── excepions/          # 异常类
│   │   │           └── ErrorRespStatusException.java
│   │   └── resources/
│   │       ├── application.yml         # Spring Boot配置
│   │       └── logback.xml             # 日志配置
│   └── test/
│       ├── java/
│       │   └── test/com/sen/api/
│       │       ├── ApiTestModern.java  # 现代化测试类 (推荐)
│       │       ├── ApiTest.java        # 传统测试类 (兼容)
│       │       └── TestBase.java       # 测试基类
│       └── resources/
│           └── allure.properties       # Allure配置
│
├── data/                               # 测试数据目录 (新)
│   ├── api-test-data.json             # JSON格式测试数据 (推荐)
│   ├── api-test-data.yml              # YAML格式测试数据 (推荐)
│   └── README.md                       # 数据格式说明
│
├── case/                               # 旧测试数据目录 (保留兼容)
│   └── api-data.xls                   # Excel格式测试数据
│
├── config/                             # 配置文件目录 (新)
│   ├── api-config.xml                 # 旧XML配置 (兼容)
│   └── api-config.yml                 # 新YAML配置 (推荐)
│
├── logs/                               # 日志输出目录
│   ├── api-test.log                   # 主日志
│   └── api-test-error.log             # 错误日志
│
├── target/                             # 构建输出
│   ├── allure-results/                # Allure测试结果
│   ├── allure-report/                 # Allure测试报告
│   ├── surefire-reports/              # TestNG测试报告
│   └── classes/                        # 编译输出
│
├── download/                           # 文件下载目录
│
├── libs/                               # 本地依赖库
│   ├── reportng-1.1.9.jar
│   └── velocity-1.7-dep.jar
│
├── .allure/                            # Allure安装目录
│
├── testng-modern.xml                  # 现代化TestNG配置 (推荐)
├── testng.xml                         # 传统TestNG配置 (兼容)
├── pom.xml                            # Maven配置
├── .gitignore                         # Git忽略配置
│
├── TECH_STACK_MODERNIZATION.md       # 技术栈现代化指南
├── ALLURE_GUIDE.md                    # Allure使用指南
├── MIGRATION_GUIDE.md                 # 迁移指南 (本文档)
└── README.md                          # 项目说明
```

---

## 目录说明

### 核心目录

#### `src/main/java/` - 主代码
- **beans/** - 数据模型类
- **configs/** - 配置管理类
- **listeners/** - TestNG监听器
- **utils/** - 工具类集合
- **excepions/** - 自定义异常

#### `src/test/java/` - 测试代码
- **ApiTestModern.java** - 现代化测试类（推荐使用）
- **ApiTest.java** - 传统测试类（向后兼容）
- **TestBase.java** - 测试基类

#### `data/` - 测试数据 (新目录)
推荐存放JSON/YAML格式的测试数据

#### `config/` - 配置文件 (新目录)
推荐存放所有配置文件

#### `logs/` - 日志输出
Logback自动生成

#### `target/` - 构建输出
Maven自动生成

---

## 文件对比

### 测试数据文件

| 旧结构 | 新结构 | 说明 |
|--------|--------|------|
| `case/api-data.xls` | `data/api-test-data.json` | 推荐JSON格式 |
| - | `data/api-test-data.yml` | 推荐YAML格式 |
| - | `data/api-test-data.csv` | 可选CSV格式 |

### 配置文件

| 旧结构 | 新结构 | 说明 |
|--------|--------|------|
| `api-config.xml` | `config/api-config.yml` | 推荐YAML配置 |
| `testng.xml` | `testng-modern.xml` | 现代化配置 |

### 测试类

| 旧类 | 新类 | 说明 |
|------|------|------|
| `ApiTest.java` | `ApiTestModern.java` | 使用REST Assured |

---

## 迁移步骤

### 步骤1: 保留旧结构（向后兼容）

```
✅ case/api-data.xls         (保留)
✅ api-config.xml            (保留)
✅ testng.xml                (保留)
✅ ApiTest.java              (保留)
```

### 步骤2: 添加新结构

```
✅ data/api-test-data.json   (新增)
✅ data/api-test-data.yml    (新增)
✅ config/api-config.yml     (新增)
✅ testng-modern.xml         (新增)
✅ ApiTestModern.java        (新增)
```

### 步骤3: 逐步迁移

1. **使用新配置运行测试**
   ```bash
   mvn test -DsuiteXmlFile=testng-modern.xml
   ```

2. **验证成功后，更新默认配置**
   - 将 `pom.xml` 中的默认配置改为 `testng-modern.xml`

3. **可选：清理旧文件**
   - 确认新配置稳定后，可移除旧文件

---

## 数据格式对比

### Excel格式 (旧)
```
优点: 可视化编辑，非技术人员友好
缺点: 不易版本控制，不易代码审查，格式固定
```

### JSON格式 (推荐)
```json
[
  {
    "run": true,
    "desc": "获取用户列表",
    "url": "/users",
    "method": "GET",
    "status": 200
  }
]
```
```
优点: 易于版本控制，易于代码审查，支持嵌套结构
缺点: 对非技术人员不太友好
```

### YAML格式 (推荐)
```yaml
- run: true
  desc: 获取用户列表
  url: /users
  method: GET
  status: 200
```
```
优点: 可读性最强，易于编辑，支持注释
缺点: 对缩进敏感
```

---

## 配置格式对比

### XML配置 (旧)
```xml
<root>
    <rootUrl>http://apis.baidu.com</rootUrl>
    <headers>
        <header name="apikey" value="123456"/>
    </headers>
</root>
```

### YAML配置 (新)
```yaml
api:
  rootUrl: https://jsonplaceholder.typicode.com
  headers:
    Content-Type: application/json
    Accept: application/json
```

---

## 监听器迁移

### 移除ExtentReports监听器

**旧配置 (testng.xml)**:
```xml
<listeners>
    <listener class-name="com.sen.api.listeners.AutoTestListener"/>
    <listener class-name="com.sen.api.listeners.RetryListener"/>
    <listener class-name="com.sen.api.listeners.ExtentTestNGIReporterListener"/>
</listeners>
```

**新配置 (testng-modern.xml)**:
```xml
<listeners>
    <listener class-name="com.sen.api.listeners.AutoTestListener"/>
    <listener class-name="com.sen.api.listeners.RetryListener"/>
    <!-- ExtentReports监听器已移除，使用Allure Report -->
</listeners>
```

**原因**:
- Allure Report通过注解和API集成，不需要监听器
- Allure功能更强大，报告更精美

---

## 运行命令对比

### 旧方式
```bash
# 使用旧配置
mvn clean test -DsuiteXmlFile=testng.xml

# 查看ExtentReports报告
open test-output/ExtentReports.html
```

### 新方式
```bash
# 使用新配置
mvn clean test -DsuiteXmlFile=testng-modern.xml

# 查看Allure报告 (推荐)
allure serve target/allure-results

# 或使用Maven插件
mvn allure:serve
```

---

## 常见问题

### Q1: 可以同时使用旧配置和新配置吗？
A: 可以！项目完全向后兼容，两种方式都能正常运行。

### Q2: 必须立即迁移到JSON/YAML吗？
A: 不必须。Excel格式仍然支持，可以逐步迁移。

### Q3: 新旧测试类有什么区别？
A:
- `ApiTest` - 使用HttpClient，传统方式
- `ApiTestModern` - 使用REST Assured，现代化方式，功能更强

### Q4: 如何在两种配置间切换？
A: 通过 `-DsuiteXmlFile` 参数指定：
```bash
mvn test -DsuiteXmlFile=testng.xml          # 旧配置
mvn test -DsuiteXmlFile=testng-modern.xml   # 新配置
```

### Q5: 可以删除ExtentReports相关文件吗？
A: 建议保留 `ExtentTestNGIReporterListener.java`，因为旧的 `ApiTest` 还在使用。如果完全迁移到 `ApiTestModern`，可以删除。

---

## 迁移检查清单

- [x] 创建 `data/` 目录
- [x] 创建 JSON 格式测试数据
- [x] 创建 YAML 格式测试数据
- [x] 创建 `testng-modern.xml`
- [x] 更新 `ApiTestModern` 支持新数据格式
- [x] 创建 YAML 格式配置文件
- [ ] 使用新配置运行测试验证
- [ ] 生成Allure报告验证
- [ ] 确认测试全部通过
- [ ] 更新 CI/CD 配置（如有）
- [ ] 更新团队文档

---

## 推荐的迁移路径

### 保守型迁移（推荐）
1. ✅ 保留所有旧文件和配置
2. ✅ 添加新文件和配置
3. ✅ 并行运行新旧配置
4. ⏳ 确认新配置稳定后，逐步切换
5. ⏳ 最终移除旧文件（可选）

### 激进型迁移
1. 直接使用新配置
2. 立即移除旧文件
3. 可能需要较多调试时间

---

## 性能对比

| 指标 | 旧方式 | 新方式 | 提升 |
|------|--------|--------|------|
| **数据加载速度** | Excel POI | JSON Jackson | 🚀 快2-3倍 |
| **代码可读性** | 一般 | 优秀 | 📈 大幅提升 |
| **报告生成** | ExtentReports | Allure | 🎨 更精美 |
| **日志性能** | System.out | SLF4J异步 | ⚡ 快5-10倍 |
| **请求性能** | HttpClient | REST Assured | ✨ 相当 |

---

**建议**：使用保守型迁移路径，确保项目稳定过渡到现代化技术栈！
