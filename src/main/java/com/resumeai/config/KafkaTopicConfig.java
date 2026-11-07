package com.resumeai.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// ═══════════════════════════════════════════════════════════════
//  KAFKA TOPICS CONFIGURATION
// ═══════════════════════════════════════════════════════════════
@Configuration
class KafkaTopicConfig {

    @Bean
    public NewTopic resumeUploadedTopic() {
        return TopicBuilder.name("resume-uploaded-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic resumeProcessedTopic() {
        return TopicBuilder.name("resume-processed-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic jobMatchedTopic() {
        return TopicBuilder.name("job-matched-events").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("notification-events").partitions(3).replicas(1).build();
    }
}
