# Allure 报告使用指南

## 概述
项目已集成Allure Report 2.25.0，提供精美的测试报告功能。

## 方式一：使用Allure命令行工具（推荐）

### 1. 安装Allure命令行工具

#### Windows (使用Scoop)
```bash
# 安装 Scoop (如果还没有)
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
irm get.scoop.sh | iex

# 安装 Allure
scoop install allure
```

#### Windows (手动安装)
1. 下载 Allure: https://github.com/allure-framework/allure2/releases
2. 解压到某个目录，如 `C:\allure`
3. 添加到PATH: `C:\allure\bin`

#### 验证安装
```bash
allure --version
```

### 2. 生成和查看报告

```bash
# 运行测试（会生成 target/allure-results）
mvn clean test

# 生成并打开报告
allure serve target/allure-results

# 或者先生成报告，再打开
allure generate target/allure-results -o target/allure-report --clean
allure open target/allure-report
```

---

## 方式二：使用Maven插件（已修复）

```bash
# 运行测试
mvn clean test

# 生成并查看报告（如果插件工作正常）
mvn allure:serve

# 仅生成报告
mvn allure:report

# 打开已生成的报告
mvn allure:open
```

**注意**：如果Maven插件仍有问题，请使用方式一（命令行工具）。

---

## 方式三：在测试后手动查看

```bash
# 1. 运行测试
mvn clean test

# 2. 检查结果目录
dir target\allure-results

# 3. 使用Allure命令行查看
allure serve target/allure-results
```

---

## Allure报告特性

### 在测试代码中使用Allure注解

`ApiTestModern` 已经集成了Allure注解：

```java
@Test(dataProvider = "apiDatas")
@Description("API 自动化测试")
public void apiTest(ApiDataBean apiDataBean) throws Exception {
    // Feature分类
    Allure.feature(apiDataBean.getSheetName());

    // Story子分类
    Allure.story(apiDataBean.getDesc());

    // 测试描述
    Allure.description("Test Case: " + apiDataBean.getDesc());

    // 步骤标记
    Allure.step("Execute " + apiDataBean.getMethod() + " request");

    // 附件
    Allure.addAttachment("Request Body", "application/json", requestBody, ".json");
    Allure.addAttachment("Response Body", "application/json", responseBody, ".json");
}
```

### 更多Allure装饰器

```java
import io.qameta.allure.*;

@Epic("API自动化测试平台")
@Feature("用户管理")
@Story("用户登录")
@Severity(SeverityLevel.CRITICAL)
@Description("测试用户登录功能的各种场景")
@Link(name = "需求文档", url = "https://...")
@Issue("BUG-123")
@TmsLink("TEST-456")
public void testUserLogin() {
    Allure.step("步骤1：准备测试数据", () -> {
        // 准备数据
    });

    Allure.step("步骤2：发送登录请求", () -> {
        // 发送请求
    });

    Allure.step("步骤3：验证响应", () -> {
        // 验证结果
    });
}
```

---

## 报告目录结构

```
target/
├── allure-results/          # 测试结果（JSON格式）
│   ├── *-result.json
│   ├── *-container.json
│   └── *-attachment.*
└── allure-report/           # 生成的HTML报告
    ├── index.html
    ├── data/
    └── ...
```

---

## 故障排除

### 问题1: Maven插件错误
**症状**: `java.lang.ExceptionInInitializerError`

**解决方案**:
1. 使用Allure命令行工具代替Maven插件
2. 或者更新Maven插件版本到2.13.0+

### 问题2: 找不到allure-results目录
**原因**: 测试失败或没有运行

**解决方案**:
```bash
# 确保至少运行一次测试
mvn clean test -DskipTests=false

# 检查目录
dir target\allure-results
```

### 问题3: 报告为空
**原因**: 测试没有使用Allure注解

**解决方案**:
- 使用 `ApiTestModern` 测试类（已集成Allure）
- 或在现有测试中添加Allure注解

---

## 集成CI/CD

### Jenkins
```groovy
pipeline {
    stages {
        stage('Test') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage('Report') {
            steps {
                allure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }
    }
}
```

### GitHub Actions
```yaml
- name: Run tests
  run: mvn clean test

- name: Allure Report
  uses: simple-elf/allure-report-action@master
  if: always()
  with:
    allure_results: target/allure-results
    allure_history: allure-history
```

---

## 对比：ExtentReports vs Allure

| 特性 | ExtentReports | Allure |
|------|---------------|--------|
| **视觉效果** | 🟢 好 | 🟢 优秀 |
| **功能丰富度** | 🟡 中等 | 🟢 丰富 |
| **集成难度** | 🟢 简单 | 🟢 简单 |
| **CI/CD支持** | 🟡 一般 | 🟢 优秀 |
| **报告类型** | 单一HTML | 多种格式 |
| **历史趋势** | ❌ 无 | ✅ 有 |
| **失败重试** | 🟡 基础 | 🟢 详细 |
| **截图附件** | ✅ 支持 | ✅ 支持 |

---

## 快速命令参考

```bash
# 安装Allure (Windows + Scoop)
scoop install allure

# 运行测试
mvn clean test

# 查看报告（推荐）
allure serve target/allure-results

# 生成报告
allure generate target/allure-results -o target/allure-report --clean

# 打开报告
allure open target/allure-report

# 清理旧报告
rmdir /s /q target\allure-results
rmdir /s /q target\allure-report
```

---

## 示例报告截图说明

Allure报告包含以下页面：

1. **Overview（概览）** - 测试执行总览
   - 成功/失败/跳过统计
   - 执行时间
   - 环境信息

2. **Categories（分类）** - 失败原因分类
   - 产品缺陷
   - 测试缺陷
   - 环境问题

3. **Suites（套件）** - 按测试套件查看
   - 完整的测试层级
   - 每个测试的详细信息

4. **Graphs（图表）** - 可视化统计
   - 状态分布
   - 严重程度分布
   - 持续时间图表

5. **Timeline（时间线）** - 测试执行时间线
   - 并行执行可视化
   - 执行顺序

6. **Behaviors（行为）** - BDD风格视图
   - Feature/Story分组
   - 业务场景组织

---

## 最佳实践

1. **使用有意义的描述**
   ```java
   @Description("验证用户使用正确的用户名和密码可以成功登录系统")
   ```

2. **合理使用步骤**
   ```java
   Allure.step("准备测试数据");
   Allure.step("发送API请求");
   Allure.step("验证响应结果");
   ```

3. **添加关键附件**
   ```java
   Allure.addAttachment("请求参数", "application/json", requestJson, ".json");
   Allure.addAttachment("响应数据", "application/json", responseJson, ".json");
   ```

4. **设置合适的严重级别**
   ```java
   @Severity(SeverityLevel.BLOCKER)  // 阻塞性
   @Severity(SeverityLevel.CRITICAL) // 严重
   @Severity(SeverityLevel.NORMAL)   // 一般
   @Severity(SeverityLevel.MINOR)    // 次要
   @Severity(SeverityLevel.TRIVIAL)  // 轻微
   ```

---

**推荐使用Allure命令行工具以获得最佳体验！**
