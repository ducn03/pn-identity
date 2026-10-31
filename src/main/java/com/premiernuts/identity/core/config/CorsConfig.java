package com.premiernuts.identity.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGIN_PATTERNS = List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://localhost:9000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:9000"
    );

    private static final List<String> ALLOWED_METHODS = List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    );

    private static final List<String> ALLOWED_HEADERS = List.of(
            "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "Accept-Language"
    );

    private static final Long MAX_AGE_SECS = 3600L;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(ALLOWED_ORIGIN_PATTERNS);
        config.setAllowedHeaders(ALLOWED_HEADERS);
        config.setAllowedMethods(ALLOWED_METHODS);
        config.setMaxAge(MAX_AGE_SECS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
