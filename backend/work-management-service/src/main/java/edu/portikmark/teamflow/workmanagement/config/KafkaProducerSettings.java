package edu.portikmark.teamflow.workmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "teamflow.kafka")
public record KafkaProducerSettings(
        String bootstrapServers,
        String username,
        String password,
        String sslTruststoreLocation,
        String tlsPassword) { }
