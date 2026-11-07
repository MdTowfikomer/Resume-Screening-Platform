package com.resumeai.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ═══════════════════════════════════════════════════════════════
//  SWAGGER / OPENAPI CONFIGURATION
// ═══════════════════════════════════════════════════════════════
@Configuration
class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Resume Screening & Job Matching Platform")
                        .description("""
                                Enterprise-grade AI-powered platform for:
                                - Resume upload and PDF parsing
                                - NLP-based skill extraction
                                - Elasticsearch-powered job matching with scoring
                                - Kafka async processing pipeline
                                - Redis caching
                                - Recruiter dashboard and analytics
                                """)
                        .version("v1.0.0")
                        .contact(new Contact().name("Resume AI Team").email("dev@resumeai.com"))
                        .license(new License().name("Apache 2.0")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide JWT token obtained from /api/v1/auth/login")));
    }
}
