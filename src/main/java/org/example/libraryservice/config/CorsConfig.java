package org.example.libraryservice.config; // Or your config package

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. Set allowed origins (YOUR FRONTEND URL)
        // You CANNOT use "*" when allowCredentials is true
        config.setAllowedOrigins(List.of(
                "http://localhost:3000", // React default
                "http://localhost:5173", // Vite default
                "http://localhost:4200"  // Angular default
        ));

        // 2. Set allowed methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 3. Set allowed headers
        config.setAllowedHeaders(List.of("*"));

        // 4. Allow credentials (cookies, auth tokens)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply this config to all routes

        return source;
    }
}