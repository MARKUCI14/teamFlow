package edu.portikmark.teamflow.workmanagement.config;

import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(KafkaProducerSettings.class)
public class KafkaProducerConfig {

    @Bean
    public ObjectMapper eventObjectMapper() {
        return JsonMapper.builder().addModule(new JavaTimeModule()).build();
    }

    @Bean
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public ProducerFactory<String, String> producerFactory(KafkaProducerSettings settings) {
        String jaasConfig = "org.apache.kafka.common.security.plain.PlainLoginModule required "
                + "username=\"" + settings.username() + "\" password=\"" + settings.password() + "\";";
        Map<String, Object> properties = Map.ofEntries(
                Map.entry(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, settings.bootstrapServers()),
                Map.entry(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class),
                Map.entry(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class),
                Map.entry("security.protocol", "SASL_SSL"),
                Map.entry(SaslConfigs.SASL_MECHANISM, "PLAIN"),
                Map.entry(SaslConfigs.SASL_JAAS_CONFIG, jaasConfig),
                Map.entry(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, settings.sslTruststoreLocation()),
                Map.entry(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, settings.tlsPassword()),
                Map.entry(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS"));
        return new DefaultKafkaProducerFactory<>(properties);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
