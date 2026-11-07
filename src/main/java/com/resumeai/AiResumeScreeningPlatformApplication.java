package com.resumeai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI-Powered Resume Screening & Job Matching Platform
 *
 * <p>Enterprise Spring Boot application providing:
 * <ul>
 *   <li>Resume upload and PDF parsing</li>
 *   <li>AI/NLP-driven skill extraction</li>
 *   <li>Elasticsearch-powered job matching with scoring</li>
 *   <li>Kafka-based async processing pipeline</li>
 *   <li>Redis caching for performance</li>
 *   <li>JWT-secured recruiter dashboard APIs</li>
 * </ul>
 *
 * @author Resume AI Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.resumeai.repository.mongo")
@EnableMongoAuditing
@EnableCaching
@EnableKafka
@EnableAsync
@EnableScheduling
@Slf4j
public class AiResumeScreeningPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiResumeScreeningPlatformApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("═══════════════════════════════════════════════════════");
        log.info("  AI Resume Screening Platform — STARTED SUCCESSFULLY  ");
        log.info("  Swagger UI  : http://localhost:8080/swagger-ui.html  ");
        log.info("  API Docs    : http://localhost:8080/api-docs          ");
        log.info("  Actuator    : http://localhost:8080/actuator/health   ");
        log.info("═══════════════════════════════════════════════════════");
    }
}
