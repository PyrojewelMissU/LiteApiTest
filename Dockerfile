# API自动化测试平台 Docker镜像
# 多阶段构建

# 阶段1: 构建
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src
COPY data ./data
COPY case ./case
COPY api-config.xml .
COPY testng-modern.xml .
COPY testng.xml .

# 编译项目
RUN mvn clean compile -DskipTests

# 阶段2: 运行
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="API Test Platform"
LABEL description="API自动化测试平台"

WORKDIR /app

# 安装必要工具
RUN apk add --no-cache curl bash

# 从构建阶段复制编译结果
COPY --from=builder /app/target /app/target
COPY --from=builder /app/pom.xml /app/
COPY --from=builder /app/data /app/data
COPY --from=builder /app/case /app/case
COPY --from=builder /app/api-config.xml /app/
COPY --from=builder /app/testng-modern.xml /app/
COPY --from=builder /app/testng.xml /app/

# 复制Maven（用于运行测试）
COPY --from=builder /root/.m2 /root/.m2

# 安装Maven
RUN apk add --no-cache maven

# 创建日志目录
RUN mkdir -p /app/logs /app/target/allure-results

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV ENV=test
ENV TZ=Asia/Shanghai

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/api-test/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# 运行测试的入口点
ENTRYPOINT ["sh", "-c"]
CMD ["mvn clean test -Dspring.profiles.active=${ENV} && mvn allure:serve"]
