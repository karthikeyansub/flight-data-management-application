package com.flight.data.management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    @Profile("!prod")
    public SecurityFilterChain securityFilterChainForTest(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests
                    .requestMatchers(antMatcher(HttpMethod.GET, "/api/flights")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/flights")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.PUT, "/api/flights/**")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/flights/**")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.POST, "/api/flights/search")).permitAll()
                    .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                    .requestMatchers(antMatcher("/swagger-ui/**")).permitAll()
                    .requestMatchers(antMatcher("/v3/api-docs/**")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.GET, "/actuator/**")).permitAll()
                    .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                    .anyRequest().denyAll())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    @Bean
    @Profile("prod")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(requests -> requests
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/flights")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/flights")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/api/flights/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/flights/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/flights/search")).permitAll()
                        .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/actuator/**")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.OPTIONS, "/**")).permitAll()
                        .anyRequest().denyAll())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
