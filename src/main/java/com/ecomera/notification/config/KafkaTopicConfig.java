package com.ecomera.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/*
 * Kafka topics need to exist before producers can send to them.
 * Without this bean, you'd have to create the topic manually via CLI:
 *   kafka-topics.sh --create --topic ecomera.notifications --bootstrap-server localhost:9092
 *
 * Spring Kafka can auto-create topics for you when you define a NewTopic bean.
 * This only works if your Kafka broker allows auto-creation (it does by default).
 *
 * partitions(1) — one partition is fine for demo/learning
 * replicas(1)   — one replica is fine for single-broker setup
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name("ecomera.notifications")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
