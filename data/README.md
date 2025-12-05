# 测试数据目录

此目录包含API自动化测试的测试数据文件。

## 支持的数据格式

### 1. JSON格式 (推荐) ⭐

**文件**: `api-test-data.json`

**优点**:
- ✅ 易于版本控制
- ✅ 易于代码审查
- ✅ 支持嵌套结构
- ✅ 工具支持好

**示例**:
```json
[
  {
    "run": true,
    "desc": "获取用户列表",
    "url": "/users",
    "method": "GET",
    "param": "{}",
    "status": 200,
    "verify": "$[0].id=1",
    "save": "user_id=$[0].id"
  }
]
```

### 2. YAML格式 (推荐) ⭐

**文件**: `api-test-data.yml`

**优点**:
- ✅ 可读性最强
- ✅ 支持注释
- ✅ 易于编辑
- ✅ 结构清晰

**示例**:
```yaml
# 用户管理测试
- run: true
  desc: 获取用户列表
  url: /users
  method: GET
  param: "{}"
  status: 200
  verify: "$[0].id=1"
  save: "user_id=$[0].id"
```

### 3. CSV格式 (简单场景)

**优点**:
- ✅ Excel兼容
- ✅ 简单直观
- ⚠️ 适合简单数据

### 4. Excel格式 (兼容)

**文件**: `../case/api-data.xls`

**说明**: 保留用于向后兼容

---

## 字段说明

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| `run` | boolean | 是 | 是否执行此用例 | true / false |
| `desc` | string | 是 | 测试用例描述 | "获取用户列表" |
| `url` | string | 是 | 请求路径 | "/users" 或 "/users/${user_id}" |
| `method` | string | 是 | 请求方法 | GET / POST / PUT / DELETE |
| `param` | string | 是 | 请求参数(JSON字符串) | "{}" 或 "{"name":"test"}" |
| `status` | int | 是 | 期望的HTTP状态码 | 200 / 201 / 404 |
| `verify` | string | 否 | 响应验证规则(分号分隔) | "$.id=1;$.name=test" |
| `save` | string | 否 | 保存响应数据(分号分隔) | "user_id=$.id" |
| `preParam` | string | 否 | 前置参数处理 | "token=__getToken()" |
| `sleep` | int | 否 | 等待秒数 | 0 / 1 / 2 |
| `sheetName` | string | 否 | 测试分组(用于报告) | "用户管理" |
| `contains` | boolean | 否 | 验证方式(包含/精确) | true / false |

---

## 特殊语法

### URL参数替换
使用 `${变量名}` 引用之前保存的值：
```json
{
  "url": "/users/${user_id}"
}
```

### JsonPath验证
使用JsonPath表达式验证响应：
```json
{
  "verify": "$.data.id=123;$.data.name=test"
}
```

### 保存响应数据
提取响应数据供后续使用：
```json
{
  "save": "user_id=$.id;user_name=$.name"
}
```

### 函数调用
使用内置函数生成动态值：
```json
{
  "preParam": "timestamp=__timestamp();random=__random(8)"
}
```

---

## 测试API说明

当前使用 **JSONPlaceholder** 作为测试API：
- 网址: https://jsonplaceholder.typicode.com
- 说明: 免费的在线REST API，用于测试和原型开发
- 稳定性: ⭐⭐⭐⭐⭐

### 可用的端点

| 资源 | 端点 | 数量 |
|------|------|------|
| Posts | /posts | 100条 |
| Comments | /comments | 500条 |
| Albums | /albums | 100条 |
| Photos | /photos | 5000条 |
| Todos | /todos | 200条 |
| Users | /users | 10条 |

### 支持的操作

- ✅ GET - 获取资源
- ✅ POST - 创建资源（模拟）
- ✅ PUT - 更新资源（模拟）
- ✅ PATCH - 部分更新（模拟）
- ✅ DELETE - 删除资源（模拟）

**注意**: POST/PUT/DELETE操作是模拟的，不会真正修改服务器数据。

---

## 使用示例

### 在testng-modern.xml中配置

```xml
<test name="API测试 - JSON数据源">
    <parameter name="dataPath" value="data/api-test-data.json"/>
    <parameter name="dataFormat" value="json"/>
    <classes>
        <class name="test.com.sen.api.ApiTestModern"/>
    </classes>
</test>
```

### 运行测试

```bash
# 使用JSON数据
mvn test -DsuiteXmlFile=testng-modern.xml

# 查看Allure报告
allure serve target/allure-results
```

---

## 数据文件管理建议

### 按功能模块拆分
```
data/
├── user-management.json     # 用户管理
├── post-management.json     # 文章管理
├── comment-management.json  # 评论管理
└── ...
```

### 按环境拆分
```
data/
├── dev/
│   └── api-test-data.json
├── test/
│   └── api-test-data.json
└── prod/
    └── api-test-data.json
```

### 版本控制
- ✅ 提交到Git仓库
- ✅ 记录每次修改
- ✅ 代码审查测试数据

---

## 从Excel迁移到JSON/YAML

### 工具推荐
- Excel → JSON: https://www.convertcsv.com/excel-to-json.htm
- Excel → YAML: 手动转换或使用脚本

### 迁移步骤
1. 导出Excel为CSV
2. 转换CSV为JSON/YAML
3. 调整字段格式
4. 验证数据正确性
5. 运行测试确认

---

## 常见问题

### Q: 如何选择数据格式？
A:
- 推荐JSON（易于工具处理）
- 或YAML（可读性最强）
- 避免Excel（不易版本控制）

### Q: 可以混用多种格式吗？
A: 可以，在testng.xml中配置不同的test使用不同的数据文件。

### Q: 数据文件太大怎么办？
A: 建议拆分为多个文件，按功能模块或业务场景组织。

---

**推荐使用JSON或YAML格式，获得最佳的开发体验！**
