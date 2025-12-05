# 在 IDEA 中启动和验证 Spring Boot 项目指南

## 方法一：在 IDEA 中直接运行主类（最简单）

### 步骤

1. **在 IDEA 中打开项目**
   - File → Open → 选择 `f:\JavaProject\autotest` 目录
   - IDEA 会自动识别为 Maven 项目

2. **等待 Maven 依赖下载完成**
   - 在 IDEA 右下角可以看到 "Indexing..." 或 "Building..."
   - 等待完成后继续

3. **找到并运行主启动类**
   - 打开文件：`src/main/java/com/sen/api/ApiTestApplication.java`
   - 在类名或 `main` 方法左侧，找到绿色的运行按钮 ▶️
   - 点击运行按钮，选择 "Run 'ApiTestApplication.main()'"

4. **查看控制台输出 - 启动成功标志**

   如果看到以下输出，说明启动成功：
   ```
   ====================================
   API自动化测试平台启动成功！
   ====================================

   Started ApiTestApplication in X.XXX seconds
   ```

5. **验证应用运行**
   - 打开浏览器访问：`http://localhost:8080/api-test`
   - 或使用 curl：`curl http://localhost:8080/api-test/actuator/health`

---

## 方法二：使用 Maven 命令启动

### 在 IDEA 内置终端中执行

1. **打开终端**
   - 点击 IDEA 底部的 "Terminal" 标签（或按 `Alt+F12`）

2. **运行命令**
   ```bash
   mvn clean spring-boot:run
   ```

3. **查看输出**
   - 等待 Maven 下载依赖并编译
   - 看到启动成功的标志即可

---

## 方法三：打包后运行 JAR

### 步骤

1. **构建 JAR 包**
   ```bash
   mvn clean package -DskipTests
   ```

2. **运行 JAR**
   ```bash
   java -jar target/api-1.0.0.jar
   ```

3. **查看启动日志**

---

## 启动成功的标志

当看到以下任一输出时，表示启动成功：

### 关键日志
```
2025-12-05 21:xx:xx.xxx  INFO --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http)
2025-12-05 21:xx:xx.xxx  INFO --- [  restartedMain] com.sen.api.ApiTestApplication           : Started ApiTestApplication in X.XXX seconds

====================================
API自动化测试平台启动成功！
====================================
```

### 端口监听
```bash
# Windows 验证端口占用
netstat -ano | findstr :8080

# 应该能看到类似输出：
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    12345
```

---

## 验证方法

### 1. 浏览器验证
打开浏览器访问：
- **基础路径**: http://localhost:8080/api-test
- **健康检查**（如果配置了 Actuator）: http://localhost:8080/api-test/actuator/health

### 2. curl 验证
```bash
# 测试基础路径
curl http://localhost:8080/api-test

# 预期响应：404（因为没有配置根路径的 Controller）
# 这是正常的，说明 Tomcat 已经启动
```

### 3. 查看 IDEA 控制台
- 确认没有红色 ERROR 日志
- 确认看到 "Started ApiTestApplication" 消息
- 确认看到 "Tomcat started on port(s): 8080"

---

## 常见问题及解决方案

### 问题 1: 端口 8080 被占用
**错误信息**:
```
The Tomcat connector configured to listen on port 8080 failed to start
Port 8080 was already in use
```

**解决方法**:
1. 修改 `src/main/resources/application.yml` 中的端口：
   ```yaml
   server:
     port: 8081  # 改为其他端口
   ```

2. 或者停止占用 8080 端口的程序：
   ```bash
   # Windows 查找并停止
   netstat -ano | findstr :8080
   taskkill /PID <进程ID> /F
   ```

### 问题 2: 数据库连接失败
**错误信息**:
```
Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago
```

**解决方法**:
1. 确保 MySQL 服务已启动
2. 检查 `application.yml` 中的数据库配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/test?...
       username: root
       password: 123456  # 确认密码正确
   ```
3. 如果不需要数据库，可以在主类上添加注解：
   ```java
   @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
   ```

### 问题 3: 依赖下载失败
**解决方法**:
1. 检查网络连接
2. 清理 Maven 缓存：
   ```bash
   mvn clean
   rm -rf ~/.m2/repository/*  # 或删除 Windows 下的 C:\Users\你的用户名\.m2\repository
   mvn install
   ```

### 问题 4: JDK 版本不匹配
**错误信息**:
```
Error: LinkageError occurred while loading main class
```

**解决方法**:
1. 确认使用的是 JDK 17：
   ```bash
   java -version  # 应显示 "17.x.x"
   ```
2. 在 IDEA 中配置：
   - File → Project Structure → Project → SDK 选择 JDK 17

---

## 测试接口

由于这是一个测试框架项目，主要功能是运行 TestNG 测试用例，而不是提供 REST API。

### 运行测试用例
```bash
# 运行所有测试
mvn test

# 运行指定的 TestNG 套件
mvn test -DxmlFileName=testng.xml

# 查看测试报告
# 报告位置：test-output/index.html
```

---

## 停止应用

### IDEA 中停止
- 点击控制台上方的红色停止按钮 ⬛

### 命令行中停止
- 按 `Ctrl+C`

---

## 配置说明

### 应用配置文件位置
- **Spring Boot 配置**: `src/main/resources/application.yml`
- **API 测试配置**: `api-config.xml`（项目根目录）
- **TestNG 配置**: `testng.xml`（项目根目录）

### 日志文件位置
- 控制台输出
- 文件日志：`logs/api-test.log`（如果配置了文件日志）

---

## 下一步

1. ✅ 确认 Spring Boot 应用成功启动
2. ✅ 验证端口 8080 可访问
3. 📋 运行 TestNG 测试用例：`mvn test`
4. 📊 查看测试报告：`test-output/index.html`
5. 🔧 根据需要修改配置文件

---

## 快速启动命令

```bash
# 1. 进入项目目录
cd f:\JavaProject\autotest

# 2. 编译项目
mvn clean compile

# 3. 启动应用
mvn spring-boot:run

# 或者直接在 IDEA 中运行 ApiTestApplication.main()
```

祝使用愉快！
