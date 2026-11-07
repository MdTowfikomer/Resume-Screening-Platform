package com.resumeai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

// ═══════════════════════════════════════════════════════════════
//  MONGODB AUDITING — who created / updated the record
// ═══════════════════════════════════════════════════════════════
@Configuration
class MongoAuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of(auth.getName());
            }
            return Optional.of("system");
        };
    }
}
