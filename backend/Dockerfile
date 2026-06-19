# ========================================
# 阶段 1：构建阶段 - 使用 Maven 3.9 + JDK 11
# ========================================
FROM maven:3.9-eclipse-temurin-11 AS builder

WORKDIR /app

# 仅复制 pom.xml 先行，利用 Docker 缓存加速
COPY pom.xml .

# 下载依赖（加速后续构建）
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src

# 构建 JAR 包（跳过测试）
RUN mvn clean package -DskipTests -B

# ========================================
# 阶段 2：运行阶段 - 使用 OpenJDK 11 JRE Slim
# ========================================
FROM openjdk:11-jre-slim

WORKDIR /app

# 安装 curl 用于健康检查（slim 镜像默认没有 curl）
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/rent-management-backend-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查 - 检查 /api/health
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# 启动命令 - 使用 JAVA_OPTS 环境变量
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
