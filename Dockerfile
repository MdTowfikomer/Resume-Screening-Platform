# ─────────────────────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────
# Stage 2: Runtime (distroless for minimal attack surface)
# ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="resume-ai-team@example.com"
LABEL description="AI Resume Screening & Job Matching Platform — Spring Boot"

# Non-root user for security
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
USER appuser

WORKDIR /app

# Upload directory
RUN mkdir -p /app/uploads/resumes /app/logs

COPY --from=builder /build/target/ai-resume-screening-platform-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
