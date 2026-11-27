package edu.portikmark.teamflow.workmanagement.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@ConditionalOnProperty(name = "teamflow.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerConfig {

    @Bean
    public ObjectMapper eventObjectMapper() {
        return JsonMapper.builder().addModule(new JavaTimeModule()).build();
    }

    @Bean
    public ProducerFactory<String, String> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${KAFKA_USERNAME:teamflow}") String username,
            @Value("${KAFKA_PASSWORD:change-me-locally}") String password,
            @Value("${KAFKA_SSL_TRUSTSTORE_LOCATION:.local/kafka-certs/kafka.truststore.jks}") String truststore,
            @Value("${KAFKA_TLS_PASSWORD:change-me-locally}") String truststorePassword) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put("security.protocol", "SASL_SSL");
        properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        properties.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required "
                + "username=\"" + username + "\" password=\"" + password + "\";");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststore);
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
        properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
