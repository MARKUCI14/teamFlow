package edu.portikmark.teamflow.workmanagement.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                            .requestMatchers("/actuator/health").permitAll()
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                    .build();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to configure work-management security", exception);
        }
    }
}
