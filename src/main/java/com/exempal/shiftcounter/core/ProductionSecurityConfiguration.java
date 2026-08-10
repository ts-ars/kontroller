package com.exempal.shiftcounter.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("prod")
public class ProductionSecurityConfiguration {

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    UserDetailsService productionUsers(
            PasswordEncoder encoder,
            @Value("${security.operator.username}") String operatorUsername,
            @Value("${security.operator.password}") String operatorPassword,
            @Value("${security.admin.username}") String adminUsername,
            @Value("${security.admin.password}") String adminPassword) {
        requireSecret(operatorUsername, "OPERATOR_USERNAME");
        requireSecret(operatorPassword, "OPERATOR_PASSWORD");
        requireSecret(adminUsername, "ADMIN_USERNAME");
        requireSecret(adminPassword, "ADMIN_PASSWORD");
        if (operatorUsername.equals(adminUsername)) {
            throw new IllegalStateException("Operator and admin usernames must differ");
        }
        return new InMemoryUserDetailsManager(
                User.withUsername(operatorUsername).password(encoder.encode(operatorPassword)).roles("OPERATOR").build(),
                User.withUsername(adminUsername).password(encoder.encode(adminPassword)).roles("OPERATOR", "ADMIN").build());
    }

    @Bean
    SecurityFilterChain productionSecurity(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/settings/**", "/api/settings/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("OPERATOR", "ADMIN"))
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .build();
    }

    private static void requireSecret(String value, String name) {
        if (value == null || value.isBlank() || "replace_me".equals(value)) {
            throw new IllegalStateException(name + " must be supplied from secret storage");
        }
    }
}
